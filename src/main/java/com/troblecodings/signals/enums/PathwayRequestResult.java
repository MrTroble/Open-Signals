package com.troblecodings.signals.enums;

import javax.annotation.Nullable;

import com.troblecodings.signals.signalbox.PathwayData;

public enum PathwayRequestResult {

    NO_EQUAL_PATH_TYPE("no_equal_path_type"), NOT_IN_GRID("not_in_grid"),
    ALREADY_USED("already_used"), OVERSTEPPING("overstepping"), NO_PATH("no_path"),
    NO_INTERSIGNALBOX_SELECTED("no_intersignalbox_selected"), PASS("pass");

    private final String name;
    private PathwayData data;

    private PathwayRequestResult(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public PathwayRequestResult setPathwayData(final PathwayData data) {
        if (!isPass())
            return this;
        this.data = data;
        return this;
    }

    @Nullable
    public PathwayData getPathwayData() {
        return data;
    }

    public boolean canBeAddedToSaver() {
        return this == ALREADY_USED || this == NO_PATH;
    }

    public boolean isPass() {
        return this == PASS;
    }

}