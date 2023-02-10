package com.troblecodings.signals.handler;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import com.troblecodings.signals.OpenSignalsMain;

import net.minecraft.core.BlockPos;

public class SignalStateFile {

    public static final int HEADER_SIZE = 4;
    public static final int START_OFFSET = HEADER_SIZE + 4;
    public static final int MAX_ELEMENTS_PER_FILE = 16000;
    public static final int ALIGNMENT_PER_INDEX_ITEM = 16;
    public static final int SIZE_OF_INDEX = MAX_ELEMENTS_PER_FILE * ALIGNMENT_PER_INDEX_ITEM;
    public static final int MAX_OFFSET_OF_INDEX = SIZE_OF_INDEX + START_OFFSET;
    public static final byte HEADER_VERSION = 1;
    public static final int STATE_BLOCK_SIZE = 256;

    private static final byte[] DEFAULT_HEADER = new byte[] {
            HEADER_VERSION, 0, 0, 0
    };

    private final Path path;
    private final List<Path> pathCache = new ArrayList<>();

    public SignalStateFile(final Path path) {
        this.path = path;
        int count = 0;
        Path current = null;
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (Files.exists(current = path.resolve(String.valueOf(count++)))) {
            pathCache.add(current);
        }
        if (pathCache.isEmpty()) {
            pathCache.add(createNextFile());
        }
    }

    private Path createNextFile() {
        final Path nextFile = this.path.resolve(String.valueOf(pathCache.size()));
        try (RandomAccessFile stream = new RandomAccessFile(nextFile.toFile(), "rw")) {
            stream.write(DEFAULT_HEADER);
            byte[] zeroMemory = new byte[SIZE_OF_INDEX];
            stream.write(zeroMemory);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return nextFile;
    }

    private static long hash(BlockPos pos) {
        return (Integer.toUnsignedLong(pos.hashCode()) % MAX_ELEMENTS_PER_FILE)
                * ALIGNMENT_PER_INDEX_ITEM + START_OFFSET;
    }

    @Nullable
    public synchronized SignalStatePos find(BlockPos pos) {
        try {
            nextFile: for (int counter = 0; counter < pathCache.size(); counter++) {
                final Path next = pathCache.get(counter);
                try (RandomAccessFile stream = new RandomAccessFile(next.toFile(), "r")) {
                    byte[] header = new byte[HEADER_SIZE];
                    stream.read(header);
                    if (header[0] != HEADER_VERSION)
                        continue nextFile;
                    if (stream.readInt() == 0)
                        return null;
                    long hashOffset = hash(pos);
                    stream.seek(hashOffset);
                    BlockPos currenPosition = null;
                    long offset = 0;
                    do {
                        currenPosition = new BlockPos(stream.readInt(), stream.readInt(),
                                stream.readInt());
                        offset = Integer.toUnsignedLong(stream.readInt());
                        final long currentOffset = stream.getFilePointer();
                        if (currentOffset >= MAX_OFFSET_OF_INDEX)
                            stream.seek(START_OFFSET); // Wrap around search
                        if (currentOffset == hashOffset)
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

    @Nullable
    public synchronized ByteBuffer read(final SignalStatePos pos) {
        try (RandomAccessFile stream = new RandomAccessFile(pathCache.get(pos.file).toFile(),
                "r")) {
            ByteBuffer buffer = ByteBuffer.allocate(STATE_BLOCK_SIZE);
            stream.seek(pos.offset);
            stream.read(buffer.array());
            return buffer;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized void write(final SignalStatePos pos, final ByteBuffer buffer) {
        try (RandomAccessFile stream = new RandomAccessFile(pathCache.get(pos.file).toFile(),
                "rw")) {
            stream.seek(pos.offset);
            stream.write(buffer.array());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    public synchronized SignalStatePos create(final BlockPos pos) {
        try {
            final int lastFile = pathCache.size() - 1;
            final Path path = pathCache.get(lastFile);
            try (RandomAccessFile stream = new RandomAccessFile(path.toFile(), "rw")) {
                byte[] header = new byte[HEADER_SIZE];
                stream.read(header);
                if (header[0] != HEADER_VERSION) {
                    OpenSignalsMain.log.error("Header version miss match! No write!");
                    return null;
                }
                final int addedElements = stream.readInt();
                if (addedElements >= MAX_ELEMENTS_PER_FILE) {
                    pathCache.add(createNextFile());
                    return create(pos);
                }
                final long offsetHash = hash(pos);
                stream.seek(offsetHash);
                while ((stream.readLong() | stream.readLong()) != 0) {
                    if (stream.getFilePointer() >= MAX_OFFSET_OF_INDEX)
                        stream.seek(START_OFFSET); // Wrap around search
                    if (stream.getFilePointer() == offsetHash) {
                        OpenSignalsMain.log.error("No free space in %s this should not happen",
                                path.toString());
                        return null;
                    }
                }
                final long actualOffset = stream.getFilePointer() - ALIGNMENT_PER_INDEX_ITEM;
                stream.seek(actualOffset);
                stream.writeInt(pos.getX());
                stream.writeInt(pos.getY());
                stream.writeInt(pos.getZ());
                final int offset = addedElements * STATE_BLOCK_SIZE + MAX_OFFSET_OF_INDEX;
                stream.writeInt(offset);
                stream.seek(offset);
                stream.write(new byte[STATE_BLOCK_SIZE]);
                stream.seek(HEADER_SIZE);
                stream.writeInt(addedElements + 1);
                return new SignalStatePos(lastFile, offset);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
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
        return Objects.equals(path, other.path);
    }

}