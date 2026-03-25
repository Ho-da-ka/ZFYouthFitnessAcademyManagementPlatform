package com.shuzi.managementplatform.web.dto.coach;

import com.shuzi.managementplatform.domain.enums.CoachStatus;
import com.shuzi.managementplatform.domain.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CoachUpdateRequest(
        @NotBlank(message = "name is required")
        @Size(max = 64, message = "name max length is 64")
        String name,

        @NotNull(message = "gender is required")
        Gender gender,

        @Pattern(regexp = "^$|^[0-9+\\-]{6,20}$", message = "phone format is invalid")
        String phone,

        @Size(max = 255, message = "specialty max length is 255")
        String specialty,

        @NotNull(message = "status is required")
        CoachStatus status,

        @Size(max = 255, message = "remarks max length is 255")
        String remarks
) {
}
