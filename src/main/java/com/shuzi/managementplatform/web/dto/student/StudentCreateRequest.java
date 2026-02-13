package com.shuzi.managementplatform.web.dto.student;

import com.shuzi.managementplatform.domain.enums.Gender;
import com.shuzi.managementplatform.domain.enums.StudentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record StudentCreateRequest(
        @NotBlank(message = "studentNo is required")
        @Size(max = 32, message = "studentNo max length is 32")
        String studentNo,

        @NotBlank(message = "name is required")
        @Size(max = 64, message = "name max length is 64")
        String name,

        @NotNull(message = "gender is required")
        Gender gender,

        @NotNull(message = "birthDate is required")
        LocalDate birthDate,

        @Size(max = 64, message = "guardianName max length is 64")
        String guardianName,

        @Pattern(regexp = "^$|^[0-9+\\-]{6,20}$", message = "guardianPhone format is invalid")
        String guardianPhone,

        StudentStatus status,

        @Size(max = 255, message = "remarks max length is 255")
        String remarks
) {
}
