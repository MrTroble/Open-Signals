package com.troblecodings.signals.enums;

import javax.annotation.Nullable;

import com.troblecodings.signals.signalbox.SignalBoxPathway;

public enum PathwayRequestResult {

    NO_EQUAL_PATH_TYPE("no_equal_path_type"), NOT_IN_GRID("not_in_grid"),
    ALREADY_USED("already_used"), OVERSTEPPING("overstepping"), NO_PATH("no_path"),
    NO_INTERSIGNALBOX_SELECTED("no_intersignalbox_selected"), PASS("pass");

    private final String name;
    private SignalBoxPathway pathway;

    private PathwayRequestResult(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public PathwayRequestResult setPathway(final SignalBoxPathway pathway) {
        if (!isPass())
            return this;
        this.pathway = pathway;
        return this;
    }

    @Nullable
    public SignalBoxPathway getPathway() {
        return pathway;
    }

    public boolean canBeAddedToSaver() {
        return this == ALREADY_USED || this == NO_PATH;
    }

    public boolean isPass() {
        return this == PASS;
    }

}