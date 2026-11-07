
package br.ifsp.demo.service;

import br.ifsp.demo.domain.model.Sport;
import br.ifsp.demo.domain.repository.ActivityClassRepository;
import br.ifsp.demo.domain.usecase.GenerateActivityReportUseCase;
import br.ifsp.demo.domain.usecase.dto.ActivityReport;
import br.ifsp.demo.exception.ResourceNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivityReportServiceTest {

    @Mock
    private ActivityClassRepository activityClassRepository;

    @Mock
    private GenerateActivityReportUseCase reportUseCase;

    @InjectMocks
    private ActivityReportService service;

    private LocalDate startDate;
    private LocalDate endDate;
    private UUID sportId;
    private DayOfWeek dayOfWeek;

    private Sport sport;
    private ActivityReport expectedReport;

    @BeforeEach
    void setUp() {
        startDate = LocalDate.of(2026, 1, 1);
        endDate = LocalDate.of(2026, 1, 31);
        sportId = UUID.randomUUID();
        dayOfWeek = DayOfWeek.MONDAY;

        sport = mock(Sport.class);
        expectedReport = mock(ActivityReport.class);
    }

    @Test
    void shouldGenerateReportWithoutFilters() {
        when(reportUseCase.generate(startDate, endDate))
                .thenReturn(expectedReport);

        ActivityReport result = service.generate(startDate, endDate, null, null);

        assertThat(result).isEqualTo(expectedReport);

        verify(reportUseCase).generate(startDate, endDate);
        verifyNoInteractions(activityClassRepository);
    }

    @Test
    void shouldGenerateReportFilteredByDayOfWeek() {
        when(reportUseCase.generate(startDate, endDate, dayOfWeek))
                .thenReturn(expectedReport);

        ActivityReport result = service.generate(startDate, endDate, null, dayOfWeek);

        assertThat(result).isEqualTo(expectedReport);

        verify(reportUseCase).generate(startDate, endDate, dayOfWeek);
        verifyNoInteractions(activityClassRepository);
    }

    @Test
    void shouldGenerateReportFilteredBySport() {
        when(activityClassRepository.findSportById(sportId))
                .thenReturn(sport);

        when(reportUseCase.generate(startDate, endDate, sport))
                .thenReturn(expectedReport);

        ActivityReport result = service.generate(startDate, endDate, sportId, null);

        assertThat(result).isEqualTo(expectedReport);

        verify(activityClassRepository).findSportById(sportId);
        verify(reportUseCase).generate(startDate, endDate, sport);
    }

    @Test
    void shouldGenerateReportFilteredBySportAndDayOfWeek() {
        when(activityClassRepository.findSportById(sportId))
                .thenReturn(sport);

        when(reportUseCase.generate(startDate, endDate, sport, dayOfWeek))
                .thenReturn(expectedReport);

        ActivityReport result = service.generate(startDate, endDate, sportId, dayOfWeek);

        assertThat(result).isEqualTo(expectedReport);

        verify(activityClassRepository).findSportById(sportId);
        verify(reportUseCase).generate(startDate, endDate, sport, dayOfWeek);
    }

    @Test
    void shouldThrowExceptionWhenSportIsNotFound() {
        when(activityClassRepository.findSportById(sportId))
                .thenReturn(null);

        assertThatThrownBy(() -> service.generate(startDate, endDate, sportId, dayOfWeek)).isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Sport not found");

        verify(activityClassRepository).findSportById(sportId);
        verifyNoInteractions(reportUseCase);
    }
}
