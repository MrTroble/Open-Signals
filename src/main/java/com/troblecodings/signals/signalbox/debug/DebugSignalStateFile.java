package com.troblecodings.signals.signalbox.debug;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;

import javax.annotation.Nullable;

import org.apache.logging.log4j.Logger;

import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.handler.SignalStateFile;
import com.troblecodings.signals.handler.SignalStatePos;

import net.minecraft.core.BlockPos;

public class DebugSignalStateFile extends SignalStateFile {

    private final HashMap<BlockPos, SignalStatePos> createdPositions = new HashMap<>();
    private final HashMap<BlockPos, SignalStatePos> foundPositions = new HashMap<>();
    private final HashMap<SignalStatePos, byte[]> dataCache = new HashMap<>();
    private final Logger logger;

    public DebugSignalStateFile(final Path path) {
        super(path);
        this.logger = OpenSignalsMain.getLogger();
    }

    @Override
    @Nullable
    public synchronized SignalStatePos create(final BlockPos pos) {
        if (createdPositions.containsKey(pos)) {
            this.logger.warn("{} was already created!", pos.toString());
        }
        final SignalStatePos statePos = super.create(pos);
        createdPositions.put(pos, statePos);
        return statePos;
    }

    @Override
    @Nullable
    public synchronized SignalStatePos find(final BlockPos pos) {
        final SignalStatePos statePos = super.find(pos);
        if (foundPositions.containsKey(pos) && statePos != null) {
            final SignalStatePos oldPosition = foundPositions.get(pos);
            if (!oldPosition.equals(statePos)) {
                this.logger.warn("{} unpersistent data detected!", pos.toString());
                this.logger.warn("Old: {}, New: {}", oldPosition.toString(), statePos.toString());
            }
        }
        return statePos;
    }

    @Override
    @Nullable
    public synchronized ByteBuffer read(final SignalStatePos pos) {
        final ByteBuffer buffer = super.read(pos);
        if (dataCache.containsKey(pos)) {
            final byte[] array = dataCache.get(pos);
            if (!Arrays.equals(array, buffer.array())) {
                this.logger.warn("Data wrong from read!");
                this.logger.warn("Old: {}, New: {}", Arrays.toString(array),
                        Arrays.toString(buffer.array()));
            }
        }
        if (buffer.array().length != STATE_BLOCK_SIZE) {
            this.logger.warn("Wrong buffer size for read: {}!", buffer.array().length);
        }
        return buffer;
    }

    @Override
    public synchronized void write(final SignalStatePos pos, final ByteBuffer buffer) {
        if (buffer.array().length != STATE_BLOCK_SIZE) {
            this.logger.warn("Wrong buffer size for write: {}!", buffer.array().length);
        }
        super.write(pos, buffer);
        dataCache.put(pos, Arrays.copyOf(buffer.array(), buffer.array().length));
    }

    @Override
    public synchronized SignalStatePos deleteIndex(final BlockPos pos) {
        final SignalStatePos statePos = super.deleteIndex(pos);
        createdPositions.remove(pos);
        dataCache.remove(statePos);
        return statePos;
    }
}