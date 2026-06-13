package com.smartdental.enums;

import java.time.DayOfWeek;
import java.time.LocalDate;

public enum WorkShiftDayType {
    ALL("Tất cả các ngày"),
    WEEKDAY("Ngày thường (Thứ 2 - Thứ 6)"),
    WEEKEND("Cuối tuần (Thứ 7 - Chủ nhật)");

    private final String label;

    WorkShiftDayType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public boolean appliesTo(LocalDate date) {
        if (date == null || this == ALL) {
            return true;
        }
        DayOfWeek day = date.getDayOfWeek();
        boolean weekend = day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
        return this == WEEKEND ? weekend : !weekend;
    }
}
