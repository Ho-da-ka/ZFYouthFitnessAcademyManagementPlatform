package com.shuzi.managementplatform.web.dto.checkin;

import jakarta.validation.constraints.NotBlank;

public record StudentScanCheckinRequest(
        @NotBlank(message = "Token is required")
        String token
) {
}
