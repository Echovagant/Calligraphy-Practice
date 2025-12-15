package com.calligraphy.platform.dto;

public class WorkStatisticsDto {
    private long totalWorks;
    private long publishedWorks;
    private long draftWorks;
    private long hiddenWorks;
    private long pendingWorks;
    private long approvedWorks;
    private long rejectedWorks;

    public WorkStatisticsDto() {
    }

    public WorkStatisticsDto(long totalWorks, long publishedWorks, long draftWorks, long hiddenWorks, long pendingWorks, long approvedWorks, long rejectedWorks) {
        this.totalWorks = totalWorks;
        this.publishedWorks = publishedWorks;
        this.draftWorks = draftWorks;
        this.hiddenWorks = hiddenWorks;
        this.pendingWorks = pendingWorks;
        this.approvedWorks = approvedWorks;
        this.rejectedWorks = rejectedWorks;
    }

    // Getters and Setters
    public long getTotalWorks() {
        return totalWorks;
    }

    public void setTotalWorks(long totalWorks) {
        this.totalWorks = totalWorks;
    }

    public long getPublishedWorks() {
        return publishedWorks;
    }

    public void setPublishedWorks(long publishedWorks) {
        this.publishedWorks = publishedWorks;
    }

    public long getDraftWorks() {
        return draftWorks;
    }

    public void setDraftWorks(long draftWorks) {
        this.draftWorks = draftWorks;
    }

    public long getHiddenWorks() {
        return hiddenWorks;
    }

    public void setHiddenWorks(long hiddenWorks) {
        this.hiddenWorks = hiddenWorks;
    }

    public long getPendingWorks() {
        return pendingWorks;
    }

    public void setPendingWorks(long pendingWorks) {
        this.pendingWorks = pendingWorks;
    }

    public long getApprovedWorks() {
        return approvedWorks;
    }

    public void setApprovedWorks(long approvedWorks) {
        this.approvedWorks = approvedWorks;
    }

    public long getRejectedWorks() {
        return rejectedWorks;
    }

    public void setRejectedWorks(long rejectedWorks) {
        this.rejectedWorks = rejectedWorks;
    }

    // Builder class
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private long totalWorks;
        private long publishedWorks;
        private long draftWorks;
        private long hiddenWorks;
        private long pendingWorks;
        private long approvedWorks;
        private long rejectedWorks;

        private Builder() {}

        public Builder totalWorks(long totalWorks) { this.totalWorks = totalWorks; return this; }
        public Builder publishedWorks(long publishedWorks) { this.publishedWorks = publishedWorks; return this; }
        public Builder draftWorks(long draftWorks) { this.draftWorks = draftWorks; return this; }
        public Builder hiddenWorks(long hiddenWorks) { this.hiddenWorks = hiddenWorks; return this; }
        public Builder pendingWorks(long pendingWorks) { this.pendingWorks = pendingWorks; return this; }
        public Builder approvedWorks(long approvedWorks) { this.approvedWorks = approvedWorks; return this; }
        public Builder rejectedWorks(long rejectedWorks) { this.rejectedWorks = rejectedWorks; return this; }

        public WorkStatisticsDto build() {
            return new WorkStatisticsDto(totalWorks, publishedWorks, draftWorks, hiddenWorks, pendingWorks, approvedWorks, rejectedWorks);
        }
    }
}


