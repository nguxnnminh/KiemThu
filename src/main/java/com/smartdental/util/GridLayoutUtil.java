package com.smartdental.util;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Sap xep cac su kien theo gio bat dau/ket thuc thanh cac cot khong chong cheo
 * de hien thi tren luoi tuan (weekly grid).
 */
public final class GridLayoutUtil {

    private GridLayoutUtil() {
    }

    public interface TimedEvent {
        LocalTime getStartTime();

        LocalTime getEndTime();
    }

    public static class Positioned<T extends TimedEvent> {
        private final T event;
        private final int columnIndex;
        private final int columnCount;

        public Positioned(T event, int columnIndex, int columnCount) {
            this.event = event;
            this.columnIndex = columnIndex;
            this.columnCount = columnCount;
        }

        public T getEvent() {
            return event;
        }

        public int getColumnIndex() {
            return columnIndex;
        }

        public int getColumnCount() {
            return columnCount;
        }
    }

    /**
     * Gan moi su kien vao mot cot sao cho khong co 2 su kien chong gio trong cung 1 cot,
     * va tinh tong so cot can thiet cho tung nhom su kien chong cheo lien thong.
     */
    public static <T extends TimedEvent> List<Positioned<T>> layout(List<T> events) {
        List<T> sorted = new ArrayList<>(events);
        sorted.sort((a, b) -> {
            int cmp = a.getStartTime().compareTo(b.getStartTime());
            if (cmp != 0) {
                return cmp;
            }
            return a.getEndTime().compareTo(b.getEndTime());
        });

        List<Positioned<T>> result = new ArrayList<>();
        int i = 0;
        while (i < sorted.size()) {
            List<T> cluster = new ArrayList<>();
            cluster.add(sorted.get(i));
            LocalTime clusterEnd = sorted.get(i).getEndTime();
            int j = i + 1;
            while (j < sorted.size() && sorted.get(j).getStartTime().isBefore(clusterEnd)) {
                if (sorted.get(j).getEndTime().isAfter(clusterEnd)) {
                    clusterEnd = sorted.get(j).getEndTime();
                }
                cluster.add(sorted.get(j));
                j++;
            }

            List<LocalTime> columnEnds = new ArrayList<>();
            int[] columnIndexes = new int[cluster.size()];
            for (int k = 0; k < cluster.size(); k++) {
                T event = cluster.get(k);
                int assigned = -1;
                for (int c = 0; c < columnEnds.size(); c++) {
                    if (!event.getStartTime().isBefore(columnEnds.get(c))) {
                        assigned = c;
                        break;
                    }
                }
                if (assigned == -1) {
                    assigned = columnEnds.size();
                    columnEnds.add(event.getEndTime());
                } else {
                    columnEnds.set(assigned, event.getEndTime());
                }
                columnIndexes[k] = assigned;
            }

            int columnCount = columnEnds.size();
            for (int k = 0; k < cluster.size(); k++) {
                result.add(new Positioned<>(cluster.get(k), columnIndexes[k], columnCount));
            }

            i = j;
        }

        return result;
    }
}
