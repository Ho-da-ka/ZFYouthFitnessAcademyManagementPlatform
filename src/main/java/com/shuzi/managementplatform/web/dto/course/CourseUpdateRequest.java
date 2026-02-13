package com.shuzi.managementplatform.web.dto.course;

import com.shuzi.managementplatform.domain.enums.CourseStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CourseUpdateRequest(
        @NotBlank(message = "name is required")
        @Size(max = 100, message = "name max length is 100")
        String name,

        @NotBlank(message = "courseType is required")
        @Size(max = 64, message = "courseType max length is 64")
        String courseType,

        @NotBlank(message = "coachName is required")
        @Size(max = 64, message = "coachName max length is 64")
        String coachName,

        @NotBlank(message = "venue is required")
        @Size(max = 128, message = "venue max length is 128")
        String venue,

        @NotNull(message = "startTime is required")
        LocalDateTime startTime,

        @NotNull(message = "durationMinutes is required")
        @Min(value = 1, message = "durationMinutes must be greater than 0")
        Integer durationMinutes,

        @NotNull(message = "status is required")
        CourseStatus status,

        @Size(max = 255, message = "description max length is 255")
        String description
) {
}
