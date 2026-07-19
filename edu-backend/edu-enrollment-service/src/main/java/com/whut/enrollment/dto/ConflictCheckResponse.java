package com.whut.enrollment.dto;

import java.util.List;

public class ConflictCheckResponse {

    private boolean hasConflict;
    private List<String> conflicts;

    public boolean isHasConflict() {
        return hasConflict;
    }

    public void setHasConflict(boolean hasConflict) {
        this.hasConflict = hasConflict;
    }

    public List<String> getConflicts() {
        return conflicts;
    }

    public void setConflicts(List<String> conflicts) {
        this.conflicts = conflicts;
    }
}
