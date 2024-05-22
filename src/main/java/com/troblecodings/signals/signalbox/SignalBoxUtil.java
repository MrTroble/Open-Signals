package com.troblecodings.signals.signalbox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.troblecodings.signals.config.ConfigHandler;
import com.troblecodings.signals.core.ModeIdentifier;
import com.troblecodings.signals.enums.EnumGuiMode;
import com.troblecodings.signals.enums.EnumPathUsage;
import com.troblecodings.signals.enums.PathType;
import com.troblecodings.signals.enums.PathwayRequestResult;
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

        public PathwayRequestResult check() {
            if (nextNode == null)
                return PathwayRequestResult.NO_PATH;
            final PathwayRequestResult nodeResult = nextNode.canMakePath(path, type);
            if (!nodeResult.isPass())
                return nodeResult;
            final Optional<EnumPathUsage> optional = nextNode.getOption(path)
                    .flatMap(entry -> entry.getEntry(PathEntryType.PATHUSAGE));
            if (optional.isPresent() && !optional.get().equals(EnumPathUsage.FREE))
                return PathwayRequestResult.ALREADY_USED;
            final boolean isValid = path.point1.equals(previousPoint) && !visited.contains(path);
            return isValid ? PathwayRequestResult.PASS : PathwayRequestResult.NO_PATH;
        }

    }

    public static PathwayRequestResult requestPathway(final SignalBoxGrid grid, final Point p1,
            final Point p2) {
        final Map<Point, SignalBoxNode> modeGrid = grid.modeGrid;
        if (!modeGrid.containsKey(p1) || !modeGrid.containsKey(p2))
            return PathwayRequestResult.NOT_IN_GRID;
        final SignalBoxNode lastNode = modeGrid.get(p2);
        final SignalBoxNode firstNode = modeGrid.get(p1);
        final PathType pathType = firstNode.getPathType(lastNode);
        if (pathType.equals(PathType.NONE))
            return PathwayRequestResult.NO_EQUAL_PATH_TYPE;

        final Map<Point, Point> closedList = new HashMap<>();
        final Map<PathIdentifier, Double> scores = new HashMap<>();
        final Set<Path> visited = new HashSet<>();

        final SignalBoxFactory factory = SignalBoxFactory.getFactory();
        final ConnectionChecker checker = factory.getConnectionChecker();
        checker.type = pathType;
        checker.visited = visited;
        PathwayRequestResult result = PathwayRequestResult.NO_PATH;

        for (final PathIdentifier pathIdent : firstNode.toPathIdentifier()) {
            scores.put(pathIdent, getCosts(pathIdent.getMode(), firstNode, p1, p2));
        }

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
                result = PathwayRequestResult.PASS;
                return result.setPathwayData(PathwayData.of(grid, nodes, pathType));
            }
            checker.previousPoint = previousPoint;
            final SignalBoxNode nextNode = modeGrid.get(nextPoint);
            if (nextNode == null) {
                result = PathwayRequestResult.NO_PATH;
                continue;
            }

            checker.nextNode = nextNode;
            for (final PathIdentifier pathIdent : nextNode.toPathIdentifier()) {
                checker.path = pathIdent.path;
                result = checker.check();
                if (nextPoint.equals(p2) || result.isPass()) {
                    scores.put(pathIdent, getCosts(pathIdent.getMode(), nextNode, nextPoint, p2));
                    closedList.put(nextPoint, previousPoint);
                    visited.add(pathIdent.path);
                    visited.add(pathIdent.path.getInverse());
                }
            }
        }
        return result;
    }

    private static final int MAX_COSTS = 100000;

    private static double getCosts(final ModeSet mode, final SignalBoxNode currentNode,
            final Point currentPoint, final Point endPoint) {
        return calculateHeuristic(currentPoint, endPoint) + currentNode.getOption(mode).get()
                .getEntry(PathEntryType.PATHWAY_COSTS).orElse(getDefaultCosts(mode));
    }

    public static int getDefaultCosts(final ModeSet mode) {
        final EnumGuiMode guiMode = mode.mode;
        switch (guiMode) {
            case STRAIGHT:
            case END:
            case IN_CONNECTION:
            case OUT_CONNECTION: {
                return 0;
            }
            case CORNER: {
                return 1;
            }
            default: {
                return MAX_COSTS;
            }
        }
    }

}