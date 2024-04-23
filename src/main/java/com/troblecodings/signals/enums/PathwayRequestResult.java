package com.troblecodings.signals.enums;

import javax.annotation.Nullable;

import com.troblecodings.signals.signalbox.SignalBoxPathway;

public enum PathwayRequestResult {

    NO_PATH_TYPE("no_path_type"), NOT_IN_GRID("not_in_grid"), ALREDY_USED("alredy_used"),
    OVERSTEPPING("overstepping"), NO_PATH("no_path"),
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
        if (this != PASS)
            return this;
        this.pathway = pathway;
        return this;
    }

    @Nullable
    public SignalBoxPathway getPathway() {
        return pathway;
    }

    public boolean wouldPathwayBePossilbe() {
        return this == ALREDY_USED;
    }

}
