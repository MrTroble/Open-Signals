package com.troblecodings.signals.enums;

import com.troblecodings.signals.OpenSignalsMain;

public enum CompareValues {

    GREATER(">"), GREATEREQUALS(">="), EQUALS("=="), SMALLEREQUALS("<="), SMALLER("<"),
    UNEQUALS("!=");

    private final String compareValue;

    private CompareValues(final String compareValue) {
        this.compareValue = compareValue;
    }

    public String getCompareValue() {
        return compareValue;
    }

    public static CompareValues getValuefromString(final String name) {
        for (final CompareValues value : CompareValues.values()) {
            if (value.getCompareValue().equalsIgnoreCase(name))
                return value;
        }
        OpenSignalsMain.getLogger()
                .warn("The string [" + name + "] is not permitted for the speed function. "
                        + "EQUALS was taken as default. Please fix this in your pack!");
        return EQUALS;
    }

}
