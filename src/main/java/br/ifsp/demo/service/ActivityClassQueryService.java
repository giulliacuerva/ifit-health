package br.ifsp.demo.service;

import br.ifsp.demo.controller.dto.ActivityClassAvailabilityResponse;
import br.ifsp.demo.persistence.sqlite.SqliteActivityClassQueries;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
public class ActivityClassQueryService {

    private final SqliteActivityClassQueries queries;

    public ActivityClassQueryService(SqliteActivityClassQueries queries) {
        this.queries = queries;
    }

    public List<ActivityClassAvailabilityResponse> findAll(Boolean active) {
        return queries.findAll(active).stream()
                .map(row -> {
                    int availableSpots = row.active()
                            ? Math.max(0, row.capacity() - row.enrolledCount())
                            : 0;

                    return new ActivityClassAvailabilityResponse(
                            row.id(),
                            row.sportId(),
                            row.roomId(),
                            row.trainerId(),
                            parseWeekdays(row.weekdays()),
                            row.startTime(),
                            row.endTime(),
                            row.capacity(),
                            row.monthlyFee(),
                            row.active(),
                            row.sportName(),
                            row.roomName(),
                            row.trainerName(),
                            row.enrolledCount(),
                            availableSpots
                    );
                })
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
}
