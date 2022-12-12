package com.troblecodings.signals.signalbox;

import java.util.Objects;

import com.troblecodings.signals.signalbox.entrys.ISaveable;

import net.minecraft.nbt.CompoundTag;

public class Path implements ISaveable {

    private static final String POINT_1 = "point1";
    private static final String POINT_2 = "point2";

    public final Point point1;
    public final Point point2;

    public Path() {
        this.point1 = new Point();
        this.point2 = new Point();
    }

    public Path(final Point point1, final Point point2) {
        this.point1 = Objects.requireNonNull(point1);
        this.point2 = Objects.requireNonNull(point2);
    }

    /**
     * @return the point1
     */
    public Point getPoint1() {
        return point1;
    }

    /**
     * @return the point2
     */
    public Point getPoint2() {
        return point2;
    }

    public Path getInverse() {
        return new Path(this.point2, this.point1);
    }

    @Override
    public void write(final CompoundTag tag) {
        final CompoundTag compound1 = new CompoundTag();
        this.point1.write(compound1);
        tag.put(POINT_1, compound1);

        final CompoundTag compound2 = new CompoundTag();
        this.point2.write(compound2);
        tag.put(POINT_2, compound2);
    }

    @Override
    public void read(final CompoundTag tag) {
        this.point1.read(tag.getCompound(POINT_1));
        this.point2.read(tag.getCompound(POINT_2));
    }

    @Override
    public int hashCode() {
        return Objects.hash(point1, point2);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Path other = (Path) obj;
        return Objects.equals(point1, other.point1) && Objects.equals(point2, other.point2);
    }

    @Override
    public String toString() {
        return "Path [point1=" + point1 + ", point2=" + point2 + "]";
    }

}
