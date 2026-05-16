package com.solicitations.dto.admin;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AssignCoverageRequest {

    @NotEmpty
    private List<String> states;
}
