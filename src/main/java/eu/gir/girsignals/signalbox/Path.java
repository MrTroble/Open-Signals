package eu.gir.girsignals.signalbox;

public class Path {

    public final Point point1;
    public final Point point2;

    public Path(final Point point1, final Point point2) {
        this.point1 = point1;
        this.point2 = point2;
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

}
