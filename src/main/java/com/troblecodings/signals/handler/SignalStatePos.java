package com.troblecodings.signals.handler;

import java.util.Objects;

public class SignalStatePos {

    public final int file;
    public final long offset;

    public SignalStatePos(final int file, final long offset) {
        this.file = file;
        this.offset = offset;
    }

    @Override
    public String toString() {
        return "SignalStatePos [file=" + file + ", offset=" + offset + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(file, offset);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if ((obj == null) || (getClass() != obj.getClass()))
            return false;
        final SignalStatePos other = (SignalStatePos) obj;
        return file == other.file && offset == other.offset;
    }
}