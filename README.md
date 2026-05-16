# Solicitations API

API de solicitações de atendimento desenvolvida como parte de um processo seletivo para desenvolvedor back-end pleno.

O sistema permite que clientes abram solicitações em etapas, analistas revisem e decidam sobre elas, e admins gerenciem usuários e coberturas por UF.

---

## Stack

Java 21, Spring Boot 3.4.5, PostgreSQL, Flyway, Elasticsearch 8, Spring Security + JWT, Docker.

---

## Como rodar

Você precisa ter Docker instalado.

```bash
docker-compose up --build
```

Isso sobe o PostgreSQL, o Elasticsearch e a aplicação. A API fica disponível em `http://localhost:8080`.

Se quiser rodar a aplicação fora do Docker (apenas subindo os serviços de infraestrutura):

```bash
docker-compose up postgres elasticsearch
./mvnw spring-boot:run
```

---

## Migrations

As migrations são gerenciadas pelo Flyway e rodam automaticamente na inicialização. Os arquivos estão em `src/main/resources/db/migration/`.

- `V1` — tabela de usuários
- `V2` — cobertura de UFs por analista
- `V3` — solicitações (multi-step)
- `V4` — logs de auditoria

---

## Criando o primeiro usuário ADMIN

Não existe endpoint público para isso. Após o banco subir, conecte nele e insira diretamente:

```bash
docker exec -it solicitations-postgres psql -U solicitations -d solicitations
```

```sql
INSERT INTO users (name, email, password_hash, role, enabled, created_at)
VALUES ('Admin', 'admin@sistema.com', '$2a$10$SEU_HASH_AQUI', 'ADMIN', true, NOW());
```

Para gerar o hash da senha, use `BCryptPasswordEncoder` do Spring ou qualquer gerador BCrypt online.

---

## Fluxo principal

### 1. Login

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@sistema.com","password":"sua_senha"}'
```

Use o `token` retornado no header `Authorization: Bearer {token}` das próximas requisições.

### 2. Admin cria um analista

```bash
curl -X POST http://localhost:8080/admin/users \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"name":"João Analista","email":"joao@sistema.com","password":"senha123","role":"ANALYST"}'
```

### 3. Admin define quais UFs o analista cobre

```bash
curl -X PUT http://localhost:8080/admin/users/{id}/coverage \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"states":["SP","RJ"]}'
```

### 4. Cliente abre uma solicitação (multi-step)

```bash
# Cria o rascunho
curl -X POST http://localhost:8080/solicitations \
  -H "Authorization: Bearer {token}"

# Step 1 — tipo e descrição
curl -X PUT http://localhost:8080/solicitations/{id}/step/1 \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"serviceType":"INSTALLATION","title":"Instalação de equipamento","description":"Preciso instalar um equipamento industrial na linha de produção."}'

# Step 2 — endereço (CEP é consultado automaticamente na ViaCEP)
curl -X PUT http://localhost:8080/solicitations/{id}/step/2 \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"cep":"01310-100","number":"100","complement":"Galpão B"}'

# Step 3 — prioridade e valor estimado
curl -X PUT http://localhost:8080/solicitations/{id}/step/3 \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"priority":"HIGH","preferredDate":"2026-07-01","estimatedValue":500.00,"termsAccepted":true}'

# Submete
curl -X POST http://localhost:8080/solicitations/{id}/submit \
  -H "Authorization: Bearer {token}"
```

### 5. Analista revisa e decide

```bash
# Lista solicitações da sua cobertura
curl http://localhost:8080/analyst/solicitations \
  -H "Authorization: Bearer {token}"

# Inicia a revisão
curl -X POST http://localhost:8080/analyst/solicitations/{id}/start \
  -H "Authorization: Bearer {token}"

# Aprova ou rejeita
curl -X POST http://localhost:8080/analyst/solicitations/{id}/decide \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"decision":"APPROVE","comment":"Solicitação válida e dentro da cobertura."}'
```

### 6. Busca no Elasticsearch (analista)

```bash
curl "http://localhost:8080/analyst/solicitations/search?q=instalacao&priority=HIGH&state=SP" \
  -H "Authorization: Bearer {token}"
```

---

## Documentação (Swagger)

Com a aplicação rodando, acesse:

```
http://localhost:8080/swagger-ui/index.html
```

Os endpoints estão agrupados por **Solicitações**, **Analista** e **Admin**. Para testar autenticado, clique em **Authorize** e informe o token no formato `Bearer {token}`.

---

## Regras importantes

- Prioridade `HIGH` exige `estimatedValue` >= 100.
- A data preferida não pode ser no passado.
- O analista só enxerga solicitações dos estados que ele cobre.
- Ações críticas são registradas automaticamente em `audit_logs` via AOP.
