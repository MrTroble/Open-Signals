package eu.gir.girsignals.signalbox;

import java.io.Serializable;

public class Point implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -4441798278173012509L;

    private int x, y;

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

    @Override
    public boolean equals(final Object obj) {
        if (obj == null || !(obj instanceof Point))
            return false;
        final Point other = (Point) obj;
        return this.x == other.x && this.y == other.y;
    }

    @Override
    public int hashCode() {
        final int sum = x + y;
        return sum * (sum + 1) / 2 + x;
    }

    @Override
    public String toString() {
        return "Point[x=" + this.x + ",y=" + this.y + "]";
    }
}
