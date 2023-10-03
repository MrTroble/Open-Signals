package com.troblecodings.signals.signalbox.entrys;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.signals.signalbox.Point;

public class PointEntry extends IPathEntry<Point> {

    private Point point;

    @Override
    public void readNetwork(ReadBuffer buffer) {
        this.point = Point.of(buffer);
    }

    @Override
    public void writeNetwork(WriteBuffer buffer) {
        point.writeNetwork(buffer);
    }

    @Override
    public void write(NBTWrapper tag) {
        point.write(tag);
    }

    @Override
    public void read(NBTWrapper tag) {
        this.point = new Point();
        this.point.read(tag);
    }

    @Override
    public Point getValue() {
        return point;
    }

    @Override
    public void setValue(Point value) {
        this.point = value;
    }
}