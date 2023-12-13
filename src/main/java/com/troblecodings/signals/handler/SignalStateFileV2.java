package com.troblecodings.signals.handler;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import com.troblecodings.signals.OpenSignalsMain;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

public class SignalStateFileV2 {

    public static final int HEADER_SIZE = 4;
    public static final int START_OFFSET = HEADER_SIZE + 4;
    public static final int MAX_ELEMENTS_PER_FILE = 256;
    public static final int ALIGNMENT_PER_INDEX_ITEM = 4;
    public static final int SIZE_OF_INDEX = MAX_ELEMENTS_PER_FILE * ALIGNMENT_PER_INDEX_ITEM;
    public static final int MAX_OFFSET_OF_INDEX = SIZE_OF_INDEX + START_OFFSET;
    public static final byte HEADER_VERSION = 2;
    public static final int STATE_BLOCK_SIZE = 256;

    private static final byte[] DEFAULT_HEADER = new byte[] {
            HEADER_VERSION, 0, 0, 0
    };

    private final Map<ChunkPos, Path> pathCache = new HashMap<>();
    private final Path path;

    public SignalStateFileV2(final Path path) {
        this.path = path;
        try {
            Files.createDirectories(path);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private Path getFileForPos(final BlockPos pos) {
        final ChunkPos file = new ChunkPos(pos);
        return pathCache.computeIfAbsent(file, identifier -> {
            final Path nextFile = this.path.resolve(getFileNameForChunk(identifier));
            if (!Files.exists(nextFile)) {
                try (RandomAccessFile stream = new RandomAccessFile(nextFile.toFile(), "rw")) {
                    stream.write(DEFAULT_HEADER);
                    stream.writeInt(0);
                    final byte[] zeroMemory = new byte[SIZE_OF_INDEX];
                    stream.write(zeroMemory);
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
            return nextFile;
        });
    }

    public static byte[] getChunkPosFromPos(final ChunkPos chunk, final BlockPos pos) {
        final byte[] array = new byte[3];
        final byte chunkCoordX = (byte) Math.floor(pos.getX() - 16 * chunk.x);
        final byte chunkCoordZ = (byte) Math.floor(pos.getZ() - 16 * chunk.z);
        array[0] = (byte) ((chunkCoordX << 4) | chunkCoordZ);
        array[1] = (byte) (((pos.getY() + 64) >> 8) & 0xFF);
        array[2] = (byte) ((pos.getY() + 64) & 0xFF);
        return array;
    }

    public static BlockPos getPosFromChunkPos(final ChunkPos chunk, final byte[] array) {
        final int chunkPosX = Byte.toUnsignedInt(array[0]) >> 4;
        final int chunkPosZ = array[0] & 0x0f;
        final int blockX = chunkPosX + 16 * chunk.x;
        final int blockY = (((array[1] & 0xFF) << 8) | (array[2] & 0xFF)) - 64;
        final int blockZ = chunkPosZ + 16 * chunk.z;
        return new BlockPos(blockX, blockY, blockZ);
    }

    private static String getFileNameForChunk(final ChunkPos pos) {
        return pos.x + "." + pos.z;
    }

    public static long hash(final BlockPos pos, final ChunkPos chunk) {
        final int chunkCoordX = (int) Math.floor(pos.getX() - 16 * chunk.x);
        final int chunkCoordY = pos.getY();
        final int chunkCoordZ = (int) Math.floor(pos.getZ() - 16 * chunk.z);
        return (Integer.toUnsignedLong((chunkCoordY + chunkCoordZ * 31) * 31 + chunkCoordX)
                % MAX_ELEMENTS_PER_FILE) * ALIGNMENT_PER_INDEX_ITEM + START_OFFSET;
    }

    public synchronized SignalStatePosV2 find(final BlockPos pos) {
        return (SignalStatePosV2) internalFind(pos,
                (stream, blockPos, offset, file) -> new SignalStatePosV2(file, offset), "r");
    }

    public synchronized SignalStatePosV2 deleteIndex(final BlockPos pos) {
        return (SignalStatePosV2) internalFind(pos, (stream, blockPos, offset, file) -> {
            try {
                final long pointer = stream.getFilePointer();
                stream.seek(pointer - ALIGNMENT_PER_INDEX_ITEM);
                stream.writeInt(0);
                return new SignalStatePosV2(file, offset);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }, "rw");
    }

    private synchronized Object internalFind(final BlockPos pos, final InternalFunction function,
            final String acces) {
        final Path file = getFileForPos(pos);
        try (RandomAccessFile stream = new RandomAccessFile(file.toFile(), acces)) {
            final byte[] header = new byte[HEADER_SIZE];
            stream.read(header);
            if (header[0] != HEADER_VERSION) {
                OpenSignalsMain.getLogger()
                        .error("Header Version miss match! No file for [" + pos + "]!");
                return null;
            }
            if (stream.readInt() == 0)
                return null;
            final ChunkPos chunk = new ChunkPos(pos);
            final long hashOffset = hash(pos, chunk);
            stream.seek(hashOffset);
            BlockPos currenPosition = null;
            long offset = 0;
            do {
                final byte[] array = new byte[3];
                stream.readFully(array);
                currenPosition = getPosFromChunkPos(chunk, array);
                offset = Byte.toUnsignedInt(stream.readByte());
                final long currentOffset = stream.getFilePointer();
                if (currentOffset >= MAX_OFFSET_OF_INDEX)
                    stream.seek(START_OFFSET); // Wrap around search
                if (currentOffset == hashOffset)
                    return null; // Nothing found
            } while (!pos.equals(currenPosition));
            return function.apply(stream, currenPosition, offset, new ChunkPos(pos));
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    public synchronized ByteBuffer read(final SignalStatePosV2 pos) {
        try (RandomAccessFile stream = new RandomAccessFile(pathCache.get(pos.file).toFile(),
                "r")) {
            final ByteBuffer buffer = ByteBuffer.allocate(STATE_BLOCK_SIZE);
            stream.seek(pos.offset * STATE_BLOCK_SIZE + MAX_OFFSET_OF_INDEX);
            stream.read(buffer.array());
            return buffer;
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized void write(final SignalStatePosV2 pos, final ByteBuffer buffer) {
        try (RandomAccessFile stream = new RandomAccessFile(pathCache.get(pos.file).toFile(),
                "rw")) {
            stream.seek(pos.offset * STATE_BLOCK_SIZE + MAX_OFFSET_OF_INDEX);
            stream.write(buffer.array());
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized SignalStatePosV2 create(final BlockPos pos) {
        return create(pos, new byte[STATE_BLOCK_SIZE]);
    }

    public synchronized SignalStatePosV2 create(final BlockPos pos, final byte[] array) {
        try {
            final Path file = getFileForPos(pos);
            try (RandomAccessFile stream = new RandomAccessFile(file.toFile(), "rw")) {
                final byte[] header = new byte[HEADER_SIZE];
                stream.read(header);
                if (header[0] != HEADER_VERSION) {
                    OpenSignalsMain.getLogger().error("Header version miss match! No write!");
                    return null;
                }
                final int addedElements = stream.readInt();
                if (addedElements >= MAX_ELEMENTS_PER_FILE) {
                    OpenSignalsMain.getLogger().error("No free space in %s this should not happen",
                            path.toString());
                    return null;
                }
                final ChunkPos chunk = new ChunkPos(pos);
                final long offsetHash = hash(pos, chunk);
                stream.seek(offsetHash);
                while (stream.readInt() != 0) {
                    if (stream.getFilePointer() >= MAX_OFFSET_OF_INDEX)
                        stream.seek(START_OFFSET); // Wrap around search
                    if (stream.getFilePointer() == offsetHash) {
                        OpenSignalsMain.getLogger().error(
                                "No free space in %s this should not happen", path.toString());
                        return null;
                    }
                }
                final long actualOffset = stream.getFilePointer() - ALIGNMENT_PER_INDEX_ITEM;
                stream.seek(actualOffset);
                final int offset = addedElements * STATE_BLOCK_SIZE + MAX_OFFSET_OF_INDEX;
                stream.write(getChunkPosFromPos(chunk, pos));
                stream.writeByte(addedElements);
                stream.seek(offset);
                stream.write(array);
                stream.seek(HEADER_SIZE);
                stream.writeInt(addedElements + 1);
                return new SignalStatePosV2(chunk, addedElements);
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, pathCache);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final SignalStateFileV2 other = (SignalStateFileV2) obj;
        return Objects.equals(path, other.path) && Objects.equals(pathCache, other.pathCache);
    }

    @FunctionalInterface
    public interface InternalFunction {
        public Object apply(final RandomAccessFile stream, final BlockPos pos, final long offset,
                final ChunkPos file);
    }

}
