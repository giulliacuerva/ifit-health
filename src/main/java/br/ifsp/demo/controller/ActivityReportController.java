package br.ifsp.demo.controller;

import br.ifsp.demo.controller.dto.ActivityReportResponse;
import br.ifsp.demo.exception.ApiException;
import br.ifsp.demo.service.ActivityReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/activity-reports")
@Tag(name = "Activity reports", description = "Administrative reports for activity classes.")
@SecurityRequirement(name = "bearerAuth")
public class ActivityReportController {
    private final ActivityReportService reportService;

    public ActivityReportController(ActivityReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    @Operation(
            summary = "Generate an activity report",
            description = "Generates the existing activity report for an inclusive date range. "
                    + "Sport and weekday filters are optional. The report calculation follows the current backend implementation."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Report generated.",
                    content = @Content(schema = @Schema(implementation = ActivityReportResponse.class))),
            @ApiResponse(responseCode = "400", description = "The end date is before the start date.",
                    content = @Content(schema = @Schema(implementation = ApiException.class))),
            @ApiResponse(responseCode = "401", description = "Authentication is required."),
            @ApiResponse(responseCode = "403", description = "Only administrators can access reports."),
            @ApiResponse(responseCode = "404", description = "The selected sport was not found.",
                    content = @Content(schema = @Schema(implementation = ApiException.class)))
    })
    public ActivityReportResponse report(
            @Parameter(description = "First day of the report period (yyyy-MM-dd).", required = true,
                    example = "2026-01-01")
            @RequestParam LocalDate startDate,
            @Parameter(description = "Last day of the report period (yyyy-MM-dd).", required = true,
                    example = "2026-01-31")
            @RequestParam LocalDate endDate,
            @Parameter(description = "Optional sport UUID filter.", example = "550e8400-e29b-41d4-a716-446655440000")
            @RequestParam(required = false) UUID sportId,
            @Parameter(description = "Optional weekday filter.", schema = @Schema(implementation = DayOfWeek.class),
                    example = "MONDAY")
            @RequestParam(required = false) DayOfWeek dayOfWeek
    ) {
        var report = reportService.generate(startDate, endDate, sportId, dayOfWeek);
        return ActivityReportResponse.from(report);
    }
}
