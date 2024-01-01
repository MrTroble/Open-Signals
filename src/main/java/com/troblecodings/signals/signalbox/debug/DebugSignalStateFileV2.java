package com.troblecodings.signals.signalbox.debug;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;

import javax.annotation.Nullable;

import org.apache.logging.log4j.Logger;

import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.handler.SignalStateFileV2;
import com.troblecodings.signals.handler.SignalStatePosV2;

import net.minecraft.core.BlockPos;

public class DebugSignalStateFileV2 extends SignalStateFileV2 {

    private final HashMap<BlockPos, SignalStatePosV2> createdPositions = new HashMap<>();
    private final HashMap<BlockPos, SignalStatePosV2> foundPositions = new HashMap<>();
    private final HashMap<SignalStatePosV2, byte[]> dataCache = new HashMap<>();
    private final Logger logger;

    public DebugSignalStateFileV2(final Path path) {
        super(path);
        this.logger = OpenSignalsMain.getLogger();
    }

    @Override
    @Nullable
    public synchronized SignalStatePosV2 create(final BlockPos pos) {
        if (createdPositions.containsKey(pos)) {
            this.logger.warn("{} was already created!", pos.toString());
        }
        final SignalStatePosV2 statePos = super.create(pos);
        createdPositions.put(pos, statePos);
        return statePos;
    }

    @Override
    @Nullable
    public synchronized SignalStatePosV2 find(final BlockPos pos) {
        final SignalStatePosV2 statePos = super.find(pos);
        if (foundPositions.containsKey(pos) && statePos != null) {
            final SignalStatePosV2 oldPosition = foundPositions.get(pos);
            if (!oldPosition.equals(statePos)) {
                this.logger.warn("{} unpersistent data detected!", pos.toString());
                this.logger.warn("Old: {}, New: {}", oldPosition.toString(), statePos.toString());
            }
        }
        return statePos;
    }

    @Override
    @Nullable
    public synchronized ByteBuffer read(final SignalStatePosV2 pos) {
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
    public synchronized void write(final SignalStatePosV2 pos, final ByteBuffer buffer) {
        if (buffer.array().length != STATE_BLOCK_SIZE) {
            this.logger.warn("Wrong buffer size for write: {}!", buffer.array().length);
        }
        super.write(pos, buffer);
        dataCache.put(pos, Arrays.copyOf(buffer.array(), buffer.array().length));
    }

    @Override
    public synchronized SignalStatePosV2 deleteIndex(final BlockPos pos) {
        final SignalStatePosV2 statePos = super.deleteIndex(pos);
        createdPositions.remove(pos);
        dataCache.remove(statePos);
        return statePos;
    }

}
