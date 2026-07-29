package com.mangban.blindplate.domain.dto;

import jakarta.validation.constraints.NotBlank;

public record IsolationPointOccupyRequest(
        @NotBlank String occupyStatus) {
}