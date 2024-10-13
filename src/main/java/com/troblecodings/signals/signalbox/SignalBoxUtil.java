package com.troblecodings.signals.signalbox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.troblecodings.signals.config.ConfigHandler;
import com.troblecodings.signals.core.ModeIdentifier;
import com.troblecodings.signals.enums.EnumGuiMode;
import com.troblecodings.signals.enums.PathType;
import com.troblecodings.signals.enums.PathwayRequestResult;
import com.troblecodings.signals.signalbox.entrys.PathEntryType;

import net.minecraft.util.Rotation;

public final class SignalBoxUtil {

    public static final int FREE_COLOR = ConfigHandler.CLIENT.signalboxFreeColor.get();
    public static final int SELECTED_COLOR = ConfigHandler.CLIENT.signalboxSelectColor.get();
    public static final int USED_COLOR = ConfigHandler.CLIENT.signalboxUsedColor.get();
    public static final int PREPARED_COLOR = ConfigHandler.CLIENT.signalboxPreparedColor.get();
    public static final int SHUNTING_COLOR = ConfigHandler.CLIENT.signalboxShuntingColor.get();

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

    public static PathwayRequestResult requestPathway(final SignalBoxGrid grid, final Point p1,
            final Point p2, final PathType pathType) {
        final Map<Point, SignalBoxNode> modeGrid = grid.modeGrid;
        if (!modeGrid.containsKey(p1) || !modeGrid.containsKey(p2))
            return PathwayRequestResult.NOT_IN_GRID;
        final SignalBoxNode firstNode = modeGrid.get(p1);
        if (pathType.equals(PathType.NONE))
            return PathwayRequestResult.NO_EQUAL_PATH_TYPE;

        final Map<Point, Point> closedList = new HashMap<>();
        final Map<PathIdentifier, Double> scores = new HashMap<>();
        final Set<Path> visited = new HashSet<>();

        final ConnectionChecker checker = ConnectionChecker.getCheckerForType(pathType);
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

    public static List<SignalBoxNode> requestProtectionWay(final Point p1, final Point p2,
            final SignalBoxGrid grid) {
        final Map<Point, SignalBoxNode> modeGrid = grid.modeGrid;
        final SignalBoxNode firstNode = modeGrid.get(p1);
        final Map<Point, Point> closedList = new HashMap<>();
        final Map<PathIdentifier, Double> scores = new HashMap<>();
        final Set<Path> visited = new HashSet<>();

        final ConnectionChecker checker = ConnectionChecker.getCheckerForType(PathType.NORMAL);
        checker.type = PathType.NORMAL;
        checker.visited = visited;

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
                return ImmutableList.copyOf(nodes);
            }
            checker.previousPoint = previousPoint;
            final SignalBoxNode nextNode = modeGrid.get(nextPoint);
            if (nextNode == null) {
                continue;
            }

            checker.nextNode = nextNode;
            for (final PathIdentifier pathIdent : nextNode.toPathIdentifier()) {
                checker.path = pathIdent.path;
                final PathwayRequestResult result = checker.check();
                if (nextPoint.equals(p2) || result.isPass()) {
                    scores.put(pathIdent, getCosts(pathIdent.getMode(), nextNode, nextPoint, p2));
                    closedList.put(nextPoint, previousPoint);
                    visited.add(pathIdent.path);
                    visited.add(pathIdent.path.getInverse());
                }
            }
        }
        return ImmutableList.of();
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
            case ARROW:
            case OUT_CONNECTION: {
                return 0;
            }
            case CORNER: {
                return 5;
            }
            default: {
                return MAX_COSTS;
            }
        }
    }

    public static PathType getPathTypeFrom(final SignalBoxNode start, final SignalBoxNode end) {
        final List<PathType> possilbeTypes = start.getPossibleTypes(end);
        if (!possilbeTypes.isEmpty()) {
            return possilbeTypes.get(0);
        } else {
            return PathType.NONE;
        }
    }

}