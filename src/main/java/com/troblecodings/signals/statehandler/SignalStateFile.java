package com.troblecodings.signals.statehandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.io.input.RandomAccessFileInputStream;

import com.troblecodings.signals.OpenSignalsMain;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class SignalStateFile {

    public static final int START_OFFSET = 4;
    public static final int MAX_ELEMENTS_PER_FILE = 4000;
    public static final int ALIGNMENT_PER_INDEX_ITEM = 16;
    public static final long MAX_OFFSET_OF_INDEX = 4000 * ALIGNMENT_PER_INDEX_ITEM + START_OFFSET;
    public static final byte HEADER_VERSION = 1;

    private final Level level;
    private final Path path;

    public SignalStateFile(final Level level, final Path path) {
        this.level = level;
        this.path = path;
    }

    private static int hash(BlockPos pos) {
        return (pos.hashCode() % MAX_ELEMENTS_PER_FILE) * ALIGNMENT_PER_INDEX_ITEM;
    }

    public synchronized SignalStatePos find(BlockPos pos) {
        try {
            int counter = 0;
            Path next = null;
            nextFile: while (Files.exists(next = path.resolve(String.valueOf(counter++)))) {
                try (RandomAccessFile stream = new RandomAccessFile(next.toFile(), "rb")) {
                    byte[] header = new byte[START_OFFSET];
                    if (header[0] != HEADER_VERSION)
                        continue nextFile;
                    stream.read(header);
                    int hashOffset = hash(pos);
                    stream.seek(hashOffset);
                    BlockPos currenPosition = null;
                    long offset = 0;
                    do {
                        currenPosition = new BlockPos(stream.readInt(), stream.readInt(),
                                stream.readInt());
                        offset = Integer.toUnsignedLong(stream.readInt());
                        if (stream.getFilePointer() >= MAX_OFFSET_OF_INDEX)
                            stream.seek(START_OFFSET); // Wrap around search
                        if (stream.getFilePointer() == hashOffset)
                            continue nextFile; // Nothing found
                    } while (!pos.equals(currenPosition));
                    return new SignalStatePos(counter, offset);
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return null;
    }

    public synchronized ByteBuffer read(final SignalStatePos pos) {
        try (RandomAccessFile stream = new RandomAccessFile(
                path.resolve(String.valueOf(pos.file)).toFile(), "rb")) {
            ByteBuffer buffer = ByteBuffer.allocate(256);
            stream.seek(pos.offset);
            stream.read(buffer.array());
            return buffer;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized void write(final SignalStatePos pos, final ByteBuffer buffer) {
        try (RandomAccessFile stream = new RandomAccessFile(
                path.resolve(String.valueOf(pos.file)).toFile(), "wb")) {
            stream.seek(pos.offset);
            stream.write(buffer.array());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized SignalStatePos create(final BlockPos pos, final ByteBuffer buffer) {
        try {
            final int lastFile = (int) (Files.list(path).count() - 1);
            try (RandomAccessFile stream = new RandomAccessFile(
                    path.resolve(String.valueOf(lastFile)).toFile(), "wrb")) {
                byte[] header = new byte[START_OFFSET];
                stream.read(header);
                if (header[0] != HEADER_VERSION) {
                    OpenSignalsMain.log.error("Header version miss match! No write!");
                    return null;
                }
                final int offsetHash = hash(pos);
                stream.seek(offsetHash);
                while ((stream.readLong() | stream.readLong()) != 0) {
                    if (stream.getFilePointer() >= MAX_OFFSET_OF_INDEX)
                        stream.seek(START_OFFSET); // Wrap around search
                    if (stream.getFilePointer() == offsetHash) {
                        // TODO new file creation
                        return null;
                    }
                }
                return new SignalStatePos(lastFile, stream.getFilePointer());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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
