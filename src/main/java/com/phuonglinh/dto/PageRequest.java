package com.phuonglinh.dto;

import java.util.ArrayList;
import java.util.List;

public class PageRequest {
    private int page = 0; // 0-based
    private int size = 10;
    private List<Sort> sorts = new ArrayList<>();

    public PageRequest() {}

    public PageRequest(int page, int size) {
        this.page = page;
        this.size = size;
    }

    public PageRequest(int page, int size, List<Sort> sorts) {
        this.page = page;
        this.size = size;
        this.sorts = sorts;
    }

    // Getters and Setters
    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<Sort> getSorts() {
        return sorts;
    }

    public void setSorts(List<Sort> sorts) {
        this.sorts = sorts;
    }

    public void addSort(String field, SortDirection direction) {
        sorts.add(new Sort(field, direction));
    }

    public static class Sort {
        private String field;
        private SortDirection direction;

        public Sort() {}

        public Sort(String field, SortDirection direction) {
            this.field = field;
            this.direction = direction;
        }

        // Getters and Setters
        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public SortDirection getDirection() {
            return direction;
        }

        public void setDirection(SortDirection direction) {
            this.direction = direction;
        }
    }

    public enum SortDirection {
        ASC, DESC
    }
}
