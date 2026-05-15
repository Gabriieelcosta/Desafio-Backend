package com.solicitations.aop;

import com.solicitations.domain.entity.AuditLog;
import com.solicitations.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;

    @Around("@annotation(audit)")
    public Object around(ProceedingJoinPoint joinPoint, Audit audit) throws Throwable {
        long start = System.currentTimeMillis();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (auth != null && auth.getPrincipal() instanceof Long)
                ? (Long) auth.getPrincipal() : null;
        String role = (auth != null && !auth.getAuthorities().isEmpty())
                ? auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "")
                : null;

        Long entityId = extractEntityId(joinPoint);

        boolean success = true;
        String errorMessage = null;

        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            success = false;
            errorMessage = e.getMessage();
            throw e;
        } finally {
            AuditLog log = new AuditLog();
            log.setUserId(userId);
            log.setRole(role);
            log.setAction(audit.action());
            log.setEntityId(entityId);
            log.setDurationMs(System.currentTimeMillis() - start);
            log.setSuccess(success);
            log.setErrorMessage(errorMessage);
            auditLogRepository.save(log);
        }
    }

    private Long extractEntityId(ProceedingJoinPoint joinPoint) {
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof Long) {
                return (Long) arg;
            }
        }
        return null;
    }
}
