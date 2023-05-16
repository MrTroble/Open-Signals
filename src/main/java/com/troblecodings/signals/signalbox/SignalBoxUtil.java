package com.troblecodings.signals.signalbox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.troblecodings.signals.enums.EnumPathUsage;
import com.troblecodings.signals.enums.PathType;
import com.troblecodings.signals.signalbox.debug.SignalBoxFactory;
import com.troblecodings.signals.signalbox.entrys.PathEntryType;

import net.minecraft.world.level.block.Rotation;

public final class SignalBoxUtil {

    public static final int FREE_COLOR = 0xFF000000;
    public static final int SELECTED_COLOR = 0xFF00FF00;
    public static final int USED_COLOR = 0xFFFF0000;

    private SignalBoxUtil() {
    }

    private static double calculateHeuristic(final Point p1, final Point p2) {
        final int dX = p2.getX() - p1.getX();
        final int dY = p2.getY() - p1.getY();
        return Math.hypot(dX, dY);
    }

    public static Rotation getRotationFromDelta(final Point delta) {
        if (delta.getX() > 0) {
            return Rotation.CLOCKWISE_180;
        } else if (delta.getX() < 0) {
            return Rotation.NONE;
        } else if (delta.getY() > 0) {
            return Rotation.COUNTERCLOCKWISE_90;
        } else {
            return Rotation.CLOCKWISE_90;
        }
    }

    public static class ConnectionChecker {

        public Path path;
        public SignalBoxNode nextNode;
        public PathType type;
        public SignalBoxNode lastNode;
        public Point previous;
        public Set<Path> visited;

        public boolean check() {
            if (nextNode == null || !nextNode.canMakePath(path, type))
                return false;
            final Optional<EnumPathUsage> optional = nextNode.getOption(path)
                    .flatMap(entry -> entry.getEntry(PathEntryType.PATHUSAGE));
            if (optional.isPresent() && !optional.get().equals(EnumPathUsage.FREE))
                return false;
            if (nextNode.equals(lastNode))
                return true;
            return previous.equals(path.point1) && !visited.contains(path);
        }
    }

    public static Optional<SignalBoxPathway> requestWay(final Map<Point, SignalBoxNode> modeGrid,
            final Point p1, final Point p2) {
        if (!modeGrid.containsKey(p1) || !modeGrid.containsKey(p2))
            return Optional.empty();
        final SignalBoxNode lastNode = modeGrid.get(p2);
        final SignalBoxNode firstNode = modeGrid.get(p1);
        final PathType pathType = firstNode.getPathType(lastNode);
        if (pathType.equals(PathType.NONE))
            return Optional.empty();

        final Map<Point, Point> closedList = new HashMap<>();
        final Map<Point, Double> fscores = new HashMap<>();
        final Map<Point, Double> gscores = new HashMap<>();

        final List<Point> openList = new ArrayList<>();
        final Set<Path> visitedPaths = new HashSet<>();

        openList.add(p1);
        gscores.put(p1, 0.0);
        fscores.put(p1, calculateHeuristic(p1, p2));

        final SignalBoxFactory factory = SignalBoxFactory.getFactory();
        final ConnectionChecker checker = factory.getConnectionChecker();
        checker.visited = visitedPaths;
        checker.lastNode = lastNode;
        checker.type = pathType;

        while (!openList.isEmpty()) {
            final Point currentNode = openList.stream().min((n1, n2) -> {
                return Double.compare(fscores.getOrDefault(n1, Double.MAX_VALUE),
                        fscores.getOrDefault(n2, Double.MAX_VALUE));
            }).get();
            openList.remove(currentNode);
            final SignalBoxNode nextSignalnode = modeGrid.get(currentNode);
            if (currentNode.equals(p2)) {
                final ArrayList<SignalBoxNode> nodes = new ArrayList<>();
                for (Point point = currentNode; point != null; point = closedList.get(point)) {
                    final SignalBoxNode boxNode = modeGrid.get(point);
                    nodes.add(boxNode);
                }
                return Optional.of(factory.getPathway(modeGrid, nodes, pathType));
            }
            if (nextSignalnode == null)
                continue;
            checker.nextNode = nextSignalnode;
            for (final Path entry : nextSignalnode.connections()) {
                final Point neighbour = entry.point2;
                checker.previous = closedList.get(currentNode);
                if (checker.previous != null)
                    checker.path = new Path(checker.previous, neighbour);
                if (currentNode.equals(p1) || checker.check()) {
                    final double tScore = gscores.getOrDefault(currentNode, Double.MAX_VALUE - 1)
                            + 1;
                    if (tScore < gscores.getOrDefault(neighbour, Double.MAX_VALUE)) {
                        closedList.put(neighbour, currentNode);
                        gscores.put(neighbour, tScore);
                        fscores.put(neighbour, tScore + calculateHeuristic(neighbour, p2));
                        visitedPaths.add(entry);
                        visitedPaths.add(entry.getInverse());
                        if (!openList.contains(neighbour)) {
                            openList.add(neighbour);
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }
}