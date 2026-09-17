package com.example.demo.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("정기 모임 회차 계산")
class GatheringRecurrenceTest {

    private Gathering weekly(DayOfWeek day, LocalDate start, LocalDate until) {
        return Gathering.builder()
                .title("한강 러닝")
                .recurrenceRule(RecurrenceRule.WEEKLY)
                .recurrenceDayOfWeek(day)
                .startDate(start)
                .recurrenceUntil(until)
                .build();
    }

    @Test
    @DisplayName("매주 같은 요일로 회차가 이어진다")
    void upcomingOccurrences_WeeklySameDayOfWeek() {
        // 2026-09-16 은 수요일
        Gathering g = weekly(DayOfWeek.TUESDAY, LocalDate.of(2026, 9, 1), null);

        List<LocalDate> dates = g.upcomingOccurrences(LocalDate.of(2026, 9, 16), 3);

        assertThat(dates).containsExactly(
                LocalDate.of(2026, 9, 22),
                LocalDate.of(2026, 9, 29),
                LocalDate.of(2026, 10, 6));
        assertThat(dates).allMatch(d -> d.getDayOfWeek() == DayOfWeek.TUESDAY);
    }

    @Test
    @DisplayName("기준일이 반복 요일이면 당일도 회차에 포함된다")
    void upcomingOccurrences_IncludesSameDay() {
        Gathering g = weekly(DayOfWeek.WEDNESDAY, LocalDate.of(2026, 9, 1), null);

        List<LocalDate> dates = g.upcomingOccurrences(LocalDate.of(2026, 9, 16), 1);

        assertThat(dates).containsExactly(LocalDate.of(2026, 9, 16));
    }

    @Test
    @DisplayName("시작일이 미래면 그 이후부터 회차가 잡힌다")
    void upcomingOccurrences_StartsFromFutureStartDate() {
        Gathering g = weekly(DayOfWeek.MONDAY, LocalDate.of(2026, 12, 1), null);

        List<LocalDate> dates = g.upcomingOccurrences(LocalDate.of(2026, 9, 16), 2);

        assertThat(dates.get(0)).isAfterOrEqualTo(LocalDate.of(2026, 12, 1));
        assertThat(dates).containsExactly(LocalDate.of(2026, 12, 7), LocalDate.of(2026, 12, 14));
    }

    @Test
    @DisplayName("반복 종료일을 넘는 회차는 만들지 않는다")
    void upcomingOccurrences_StopsAtRecurrenceUntil() {
        Gathering g = weekly(DayOfWeek.TUESDAY, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 10, 1));

        List<LocalDate> dates = g.upcomingOccurrences(LocalDate.of(2026, 9, 16), 10);

        assertThat(dates).containsExactly(
                LocalDate.of(2026, 9, 22),
                LocalDate.of(2026, 9, 29));
    }

    @Test
    @DisplayName("일회성 모임은 회차가 없다")
    void upcomingOccurrences_NonRecurring_ReturnsEmpty() {
        Gathering g = Gathering.builder().title("단발 모임").recurrenceRule(RecurrenceRule.NONE).build();

        assertThat(g.isRecurring()).isFalse();
        assertThat(g.upcomingOccurrences(LocalDate.of(2026, 9, 16), 3)).isEmpty();
        assertThat(g.nextOccurrence(LocalDate.of(2026, 9, 16))).isNull();
    }

    @Test
    @DisplayName("규칙이 WEEKLY 라도 요일이 없으면 정기 모임으로 보지 않는다")
    void isRecurring_WeeklyWithoutDay_False() {
        Gathering g = Gathering.builder().recurrenceRule(RecurrenceRule.WEEKLY).build();

        assertThat(g.isRecurring()).isFalse();
        assertThat(g.upcomingOccurrences(LocalDate.of(2026, 9, 16), 3)).isEmpty();
    }

    @Test
    @DisplayName("잘못된 인자는 빈 목록을 반환한다")
    void upcomingOccurrences_InvalidArgs_ReturnsEmpty() {
        Gathering g = weekly(DayOfWeek.TUESDAY, LocalDate.of(2026, 9, 1), null);

        assertThat(g.upcomingOccurrences(null, 3)).isEmpty();
        assertThat(g.upcomingOccurrences(LocalDate.of(2026, 9, 16), 0)).isEmpty();
    }

    @Test
    @DisplayName("nextOccurrence 는 가장 가까운 회차 하나를 준다")
    void nextOccurrence_ReturnsFirst() {
        Gathering g = weekly(DayOfWeek.FRIDAY, LocalDate.of(2026, 9, 1), null);

        assertThat(g.nextOccurrence(LocalDate.of(2026, 9, 16))).isEqualTo(LocalDate.of(2026, 9, 18));
    }

    @Test
    @DisplayName("종료일이 지났으면 다음 회차가 없다")
    void nextOccurrence_AfterUntil_ReturnsNull() {
        Gathering g = weekly(DayOfWeek.TUESDAY, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 10));

        assertThat(g.nextOccurrence(LocalDate.of(2026, 9, 16))).isNull();
    }
}
