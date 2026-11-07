package br.ifsp.demo.service;

import br.ifsp.demo.controller.dto.AffectedStudentResponse;
import br.ifsp.demo.controller.dto.ConflictingActivityResponse;
import br.ifsp.demo.domain.model.Schedule;
import br.ifsp.demo.persistence.sqlite.SqliteScheduleConflictQueries;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class ScheduleConflictQueryService {

    private final SqliteScheduleConflictQueries queries;

    public ScheduleConflictQueryService(SqliteScheduleConflictQueries queries) {
        this.queries = queries;
    }

    public List<AffectedStudentResponse> findAffectedStudents(
            UUID activityClassId,
            Set<DayOfWeek> newWeekdays,
            LocalTime newStartTime,
            LocalTime newEndTime
    ) {
        Schedule newSchedule = new Schedule(newWeekdays, newStartTime, newEndTime);
        Map<UUID, AffectedStudentAccumulator> affectedStudents = new LinkedHashMap<>();

        for (SqliteScheduleConflictQueries.ConflictCandidate candidate
                : queries.findActiveActivitiesOfEnrolledStudents(activityClassId)) {

            Schedule existingSchedule = new Schedule(
                    parseWeekdays(candidate.weekdays()),
                    candidate.startTime(),
                    candidate.endTime()
            );

            if (!newSchedule.conflictsWith(existingSchedule)) {
                continue;
            }

            AffectedStudentAccumulator student = affectedStudents.computeIfAbsent(
                    candidate.customerId(),
                    ignored -> new AffectedStudentAccumulator(
                            candidate.customerId(), candidate.customerName())
            );

            student.addActivity(new ConflictingActivityResponse(
                    candidate.activityClassId(),
                    candidate.sportName(),
                    parseWeekdays(candidate.weekdays()),
                    candidate.startTime(),
                    candidate.endTime()
            ));
        }

        return affectedStudents.values().stream()
                .map(AffectedStudentAccumulator::toResponse)
                .toList();
    }

    private Set<DayOfWeek> parseWeekdays(String value) {
        if (value == null || value.isBlank()) {
            return EnumSet.noneOf(DayOfWeek.class);
        }

        return Arrays.stream(value.split(","))
                .map(String::trim)
                .map(DayOfWeek::valueOf)
                .collect(() -> EnumSet.noneOf(DayOfWeek.class), Set::add, Set::addAll);
    }

    private static final class AffectedStudentAccumulator {
        private final UUID customerId;
        private final String customerName;
        private final Map<UUID, ConflictingActivityResponse> conflictingActivities = new LinkedHashMap<>();

        private AffectedStudentAccumulator(UUID customerId, String customerName) {
            this.customerId = customerId;
            this.customerName = customerName;
        }

        private void addActivity(ConflictingActivityResponse activity) {
            conflictingActivities.putIfAbsent(activity.activityClassId(), activity);
        }

        private AffectedStudentResponse toResponse() {
            return new AffectedStudentResponse(
                    customerId,
                    customerName,
                    List.copyOf(conflictingActivities.values())
            );
        }
    }
}
