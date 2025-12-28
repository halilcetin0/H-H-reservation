package com.project.appointment.dto.request;

import com.project.appointment.entity.DayOfWeek;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchWorkScheduleRequest {
    
    @NotNull(message = "Schedules list is required")
    @Valid
    private List<ScheduleItem> schedules;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ScheduleItem {
        @NotNull(message = "Day of week is required")
        private DayOfWeek dayOfWeek;
        
        private LocalTime startTime; // Required if isAvailable is true
        
        private LocalTime endTime; // Required if isAvailable is true
        
        @NotNull(message = "isAvailable is required")
        private Boolean isAvailable;
    }
}

