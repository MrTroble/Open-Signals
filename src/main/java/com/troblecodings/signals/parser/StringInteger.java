package com.troblecodings.signals.parser;

import java.util.Objects;

public class StringInteger {

    public final String string;
    public final int integer;

    public StringInteger(final String str, final int integer) {
        super();
        this.string = str;
        this.integer = integer;
    }

    @Override
    public int hashCode() {
        return Objects.hash(integer, string);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final StringInteger other = (StringInteger) obj;
        return integer == other.integer && Objects.equals(string, other.string);
    }

}
