package com.troblecodings.signals.enums;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.troblecodings.signals.config.ConfigHandler;
import com.troblecodings.signals.signalbox.PathwayData;

public class PathwayRequestResult {

    private static final Map<PathwayRequestMode, PathwayRequestResult> MODE_TO_RESULT =
            new HashMap<>();

    static {
        for (final PathwayRequestMode mode : PathwayRequestMode.values()) {
            if (mode == PathwayRequestMode.PASS) {
                continue;
            }
            MODE_TO_RESULT.put(mode, new PathwayRequestResult(mode));
        }
    }

    private static final boolean CAN_ADD_RS_PATH_TO_SAVER = ConfigHandler.GENERAL.canAddRSPathToSaver
            .get();

    private final PathwayRequestMode mode;
    private final PathwayData data;

    private PathwayRequestResult(final PathwayRequestMode mode) {
        this(mode, null);
    }

    public PathwayRequestResult(final PathwayRequestMode mode, final PathwayData data) {
        this.mode = mode;
        this.data = data == null ? PathwayData.EMPTY_DATA : data;
    }

    public static PathwayRequestResult getByMode(final PathwayRequestMode mode) {
        return MODE_TO_RESULT.getOrDefault(mode,
                new PathwayRequestResult(PathwayRequestMode.NO_PATH));
    }

    public PathwayRequestMode getMode() {
        return mode;
    }

    public String getName() {
        return mode.getName();
    }

    public PathwayData getPathwayData() {
        return data;
    }

    public boolean canBeAddedToSaver(final PathType type) {
        return mode.canBeAddedToSaver() && (type.equals(PathType.NORMAL)
                || (type.equals(PathType.SHUNTING) && CAN_ADD_RS_PATH_TO_SAVER));
    }

    public boolean wasSuccesfull() {
        return mode.isPass() && !data.equals(PathwayData.EMPTY_DATA);
    }

    @Override
    public String toString() {
        return "PathwayRequestResult: Mode=" + mode + ",data=" + data;
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, mode);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if ((obj == null) || (getClass() != obj.getClass()))
            return false;
        final PathwayRequestResult other = (PathwayRequestResult) obj;
        return Objects.equals(data, other.data) && mode == other.mode;
    }

    public enum PathwayRequestMode {

        NO_EQUAL_PATH_TYPE("no_equal_path_type"), NOT_IN_GRID("not_in_grid"),
        ALREADY_USED("already_used"), OVERSTEPPING("overstepping"),
        INPUT_BLOCKING("input_blocking"), NO_PATH("no_path"),
        NO_INTERSIGNALBOX_SELECTED("no_intersignalbox_selected"),
        SUBISIDIARY_ENABLED("subsidiary_enabled"), PASS("pass");

        private final String name;

        private PathwayRequestMode(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public boolean canBeAddedToSaver() {
            return this == ALREADY_USED || this == NO_PATH || this == INPUT_BLOCKING;
        }

        public boolean isPass() {
            return this == PASS;
        }

    }

}
