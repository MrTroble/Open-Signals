package com.troblecodings.signals.signalbox;

import java.util.Objects;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.signals.signalbox.entrys.INetworkSavable;

public class Point implements INetworkSavable {

    private int x, y;

    public Point() {
        x = 0;
        y = 0;
    }

    public Point(final int x, final int y) {
        this.x = x;
        this.y = y;
    }

    public Point(final Point point) {
        this.x = point.x;
        this.y = point.y;
    }

    public int getX() {
        return x;
    }

    public void setX(final int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(final int y) {
        this.y = y;
    }

    public void translate(final int x, final int y) {
        this.x += x;
        this.y += y;
    }

    public Point delta(final Point other) {
        final Point point = new Point(this);
        point.translate(-other.getX(), -other.getY());
        return point;
    }

    public static Point of(final ReadBuffer buffer) {
        return new Point(buffer.getByteToUnsignedInt(), buffer.getByteToUnsignedInt());
    }

    public static Point of(final NBTWrapper tag) {
        return new Point(Byte.toUnsignedInt(tag.getByte("x")),
                Byte.toUnsignedInt(tag.getByte("y")));
    }

    @Override
	public int hashCode() {
		return Objects.hash(x, y);
	}

    @Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Point other = (Point) obj;
		return x == other.x && y == other.y;
	}

	@Override
    public String toString() {
        return "Point[x=" + this.x + ",y=" + this.y + "]";
    }

    public String toShortString() {
        return "[x=" + this.x + ",y=" + this.y + "]";
    }

    @Override
    public void write(final NBTWrapper tag) {
        tag.putByte("x", (byte) x);
        tag.putByte("y", (byte) y);
    }

    @Override
    public void read(final NBTWrapper tag) {
        this.x = Byte.toUnsignedInt(tag.getByte("x"));
        this.y = Byte.toUnsignedInt(tag.getByte("y"));
    }

    @Override
    public void readNetwork(final ReadBuffer buffer) {
        this.x = buffer.getByteToUnsignedInt();
        this.y = buffer.getByteToUnsignedInt();

    }

    @Override
    public void writeNetwork(final WriteBuffer buffer) {
        buffer.putByte((byte) x);
        buffer.putByte((byte) y);
    }
}