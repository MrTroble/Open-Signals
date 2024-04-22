package com.troblecodings.signals.signalbox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.troblecodings.signals.config.ConfigHandler;
import com.troblecodings.signals.core.ModeIdentifier;
import com.troblecodings.signals.enums.EnumPathUsage;
import com.troblecodings.signals.enums.PathType;
import com.troblecodings.signals.signalbox.debug.SignalBoxFactory;
import com.troblecodings.signals.signalbox.entrys.PathEntryType;

import net.minecraft.world.level.block.Rotation;

public final class SignalBoxUtil {

    public static final int FREE_COLOR = ConfigHandler.CLIENT.signalboxFreeColor.get();
    public static final int SELECTED_COLOR = ConfigHandler.CLIENT.signalboxSelectColor.get();
    public static final int USED_COLOR = ConfigHandler.CLIENT.signalboxUsedColor.get();
    public static final int PREPARED_COLOR = ConfigHandler.CLIENT.signalboxPreparedColor.get();

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

    public static class PathIdentifier {

        public Path path;
        public ModeIdentifier identifier;

        public PathIdentifier(final Path path, final Point point, final ModeSet mode) {
            this.path = path;
            this.identifier = new ModeIdentifier(point, mode);
        }

        public ModeSet getMode() {
            return identifier.mode;
        }

        public Point getPoint() {
            return identifier.point;
        }

    }

    public static class ConnectionChecker {

        public PathType type;
        public SignalBoxNode nextNode;
        public Point previousPoint;
        public Path path;
        public Set<Path> visited;

        public boolean check() {
            if (nextNode == null || !nextNode.canMakePath(path, type))
                return false;
            final Optional<EnumPathUsage> optional = nextNode.getOption(path)
                    .flatMap(entry -> entry.getEntry(PathEntryType.PATHUSAGE));
            if (optional.isPresent() && !optional.get().equals(EnumPathUsage.FREE))
                return false;
            return path.point1.equals(previousPoint) && !visited.contains(path);
        }

    }

    public static Optional<SignalBoxPathway> requestPathway(
            final Map<Point, SignalBoxNode> modeGrid, final Point p1, final Point p2) {
        if (!modeGrid.containsKey(p1) || !modeGrid.containsKey(p2))
            return Optional.empty();
        final SignalBoxNode lastNode = modeGrid.get(p2);
        final SignalBoxNode firstNode = modeGrid.get(p1);
        final PathType pathType = firstNode.getPathType(lastNode);
        if (pathType.equals(PathType.NONE))
            return Optional.empty();

        final Map<Point, Point> closedList = new HashMap<>();
        final Map<PathIdentifier, Double> scores = new HashMap<>();
        final Set<Path> visited = new HashSet<>();

        final SignalBoxFactory factory = SignalBoxFactory.getFactory();
        final ConnectionChecker checker = factory.getConnectionChecker();
        checker.type = pathType;
        checker.visited = visited;

        for (final PathIdentifier pathIdent : firstNode.toPathIdentifier())
            scores.put(pathIdent, getCosts(pathIdent.getMode(), p1, p2));

        while (!scores.isEmpty()) {
            final PathIdentifier currentPath = scores.entrySet().stream()
                    .min((ident1, ident2) -> Double.compare(ident1.getValue(), ident2.getValue()))
                    .get().getKey();
            scores.remove(currentPath);

            final Point previousPoint = currentPath.getPoint();
            final Point nextPoint = currentPath.path.point2;
            if (previousPoint.equals(p2)) {
                final ArrayList<SignalBoxNode> nodes = new ArrayList<>();
                for (Point point = previousPoint; point != null; point = closedList.get(point)) {
                    final SignalBoxNode boxNode = modeGrid.get(point);
                    nodes.add(boxNode);
                }
                return Optional.of(factory.getPathway(modeGrid, nodes, pathType));
            }
            checker.previousPoint = previousPoint;
            final SignalBoxNode nextNode = modeGrid.get(nextPoint);
            if (nextNode == null)
                continue;

            checker.nextNode = nextNode;
            for (final PathIdentifier pathIdent : nextNode.toPathIdentifier()) {
                checker.path = pathIdent.path;
                if (nextPoint.equals(p2) || checker.check()) {
                    scores.put(pathIdent, getCosts(pathIdent.getMode(), nextPoint, p2));
                    closedList.put(nextPoint, previousPoint);
                    visited.add(pathIdent.path);
                    visited.add(pathIdent.path.getInverse());
                }
            }
        }
        return Optional.empty();
    }

    private static double getCosts(final ModeSet mode, final Point currentPoint,
            final Point endPoint) {
        switch (mode.mode) {
            case STRAIGHT:
            case IN_CONNECTION:
            case END: {
                return calculateHeuristic(currentPoint, endPoint);
            }
            case CORNER: {
                return calculateHeuristic(currentPoint, endPoint) + 1;
            }
            default:
                break;
        }
        return Double.MAX_VALUE;
    }

}