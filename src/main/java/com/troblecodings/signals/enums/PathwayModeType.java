package com.troblecodings.signals.enums;

public enum PathwayModeType {

    NONE, START, END, START_END;

    public boolean isValidStart() {
        return this == START || this == START_END;
    }

    public boolean isValidEnd() {
        return this == END || this == START_END;
    }

}