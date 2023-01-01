package com.troblecodings.signals.statehandler;

import java.nio.file.Path;
import java.util.Objects;

import io.netty.buffer.ByteBuf;
import net.minecraft.world.level.Level;

public class SignalStateFile {

    private final Level level;
    private final Path path;

    public SignalStateFile(final Level level, final Path path) {
        this.level = level;
        this.path = path;
    }

    public SignalStatePos find() {
        // TODO read out files
        return null;
    }

    public ByteBuf read(final SignalStatePos pos) {
        // TODO read out files
        return null;
    }

    public void write(final SignalStatePos pos, final ByteBuf buffer) {
        // TODO write into files
    }

    @Override
    public int hashCode() {
        return Objects.hash(level, path);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SignalStateFile other = (SignalStateFile) obj;
        return Objects.equals(level, other.level) && Objects.equals(path, other.path);
    }

}
