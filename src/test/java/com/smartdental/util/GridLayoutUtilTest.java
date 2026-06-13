package com.smartdental.util;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GridLayoutUtilTest {

    private static class FakeEvent implements GridLayoutUtil.TimedEvent {
        private final String name;
        private final LocalTime start;
        private final LocalTime end;

        FakeEvent(String name, LocalTime start, LocalTime end) {
            this.name = name;
            this.start = start;
            this.end = end;
        }

        @Override
        public LocalTime getStartTime() {
            return start;
        }

        @Override
        public LocalTime getEndTime() {
            return end;
        }

        String getName() {
            return name;
        }
    }

    @Test
    void nonOverlappingEventsEachGetSingleColumn() {
        FakeEvent a = new FakeEvent("A", LocalTime.of(8, 0), LocalTime.of(9, 0));
        FakeEvent b = new FakeEvent("B", LocalTime.of(9, 0), LocalTime.of(10, 0));

        List<GridLayoutUtil.Positioned<FakeEvent>> result = GridLayoutUtil.layout(List.of(a, b));

        assertEquals(2, result.size());
        for (GridLayoutUtil.Positioned<FakeEvent> pos : result) {
            assertEquals(0, pos.getColumnIndex());
            assertEquals(1, pos.getColumnCount());
        }
    }

    @Test
    void overlappingEventsAreSplitIntoColumns() {
        FakeEvent a = new FakeEvent("A", LocalTime.of(8, 0), LocalTime.of(9, 0));
        FakeEvent b = new FakeEvent("B", LocalTime.of(8, 30), LocalTime.of(9, 30));

        List<GridLayoutUtil.Positioned<FakeEvent>> result = GridLayoutUtil.layout(List.of(a, b));

        assertEquals(2, result.size());
        assertEquals(2, result.get(0).getColumnCount());
        assertEquals(2, result.get(1).getColumnCount());
        assertEquals(0, result.get(0).getColumnIndex());
        assertEquals(1, result.get(1).getColumnIndex());
    }

    @Test
    void eventStartingAfterPreviousEndsReusesColumn() {
        FakeEvent a = new FakeEvent("A", LocalTime.of(8, 0), LocalTime.of(9, 0));
        FakeEvent b = new FakeEvent("B", LocalTime.of(8, 30), LocalTime.of(9, 30));
        FakeEvent c = new FakeEvent("C", LocalTime.of(9, 0), LocalTime.of(10, 0));

        List<GridLayoutUtil.Positioned<FakeEvent>> result = GridLayoutUtil.layout(List.of(a, b, c));

        assertEquals(3, result.size());
        for (GridLayoutUtil.Positioned<FakeEvent> pos : result) {
            assertEquals(2, pos.getColumnCount());
        }
        // A occupies column 0 from 8:00-9:00; C starts at 9:00 (not before A's end) -> reuses column 0
        GridLayoutUtil.Positioned<FakeEvent> posA = result.stream().filter(p -> p.getEvent().getName().equals("A")).findFirst().orElseThrow();
        GridLayoutUtil.Positioned<FakeEvent> posC = result.stream().filter(p -> p.getEvent().getName().equals("C")).findFirst().orElseThrow();
        assertEquals(posA.getColumnIndex(), posC.getColumnIndex());
    }

    @Test
    void emptyListReturnsEmptyResult() {
        List<GridLayoutUtil.Positioned<FakeEvent>> result = GridLayoutUtil.layout(List.of());
        assertEquals(0, result.size());
    }
}
