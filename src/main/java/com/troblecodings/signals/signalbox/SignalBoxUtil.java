package com.troblecodings.signals.signalbox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.RedstoneIO;
import com.troblecodings.signals.config.ConfigHandler;
import com.troblecodings.signals.core.ModeIdentifier;
import com.troblecodings.signals.enums.EnumGuiMode;
import com.troblecodings.signals.enums.PathType;
import com.troblecodings.signals.enums.PathwayRequestResult;
import com.troblecodings.signals.enums.PathwayRequestResult.PathwayRequestMode;
import com.troblecodings.signals.signalbox.entrys.PathEntryType;

import net.minecraft.block.BlockState;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class SignalBoxUtil {

    public static final int FREE_COLOR = ConfigHandler.CLIENT.signalboxFreeColor.get();
    public static final int SELECTED_COLOR = ConfigHandler.CLIENT.signalboxSelectColor.get();
    public static final int USED_COLOR = ConfigHandler.CLIENT.signalboxUsedColor.get();
    public static final int PREPARED_COLOR = ConfigHandler.CLIENT.signalboxPreparedColor.get();
    public static final int SHUNTING_COLOR = ConfigHandler.CLIENT.signalboxShuntingColor.get();

    private static List<Point> debugPointList = new ArrayList<>();

    private SignalBoxUtil() {
    }

    private static double calculateHeuristic(final Point p1, final Point p2) {
        final int dX = p2.getX() - p1.getX();
        final int dY = p2.getY() - p1.getY();
        return Math.hypot(dX, dY);
    }

    public static Rotation getRotationFromDelta(final Point delta) {
        if (delta.getX() > 0)
            return Rotation.CLOCKWISE_180;
        if (delta.getX() < 0)
            return Rotation.NONE;
        if (delta.getY() > 0)
            return Rotation.COUNTERCLOCKWISE_90;
        return Rotation.CLOCKWISE_90;
    }

    public static Point getDeltaFromRotation(final Rotation rot) {
        switch (rot) {
            case NONE:
                return new Point(1, 0);
            case CLOCKWISE_90:
                return new Point(0, 1);
            case CLOCKWISE_180:
                return new Point(-1, 0);
            case COUNTERCLOCKWISE_90:
                return new Point(0, -1);
        }
        return new Point();
    }

    public static String getDegreeStringFromRotation(final Rotation rot) {
        return String.valueOf(rot.ordinal() * 90) + "°";
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
            return PathwayRequestResult.getByMode(PathwayRequestMode.NOT_IN_GRID);
        final SignalBoxNode firstNode = modeGrid.get(p1);
        if (pathType.equals(PathType.NONE))
            return PathwayRequestResult.getByMode(PathwayRequestMode.NO_EQUAL_PATH_TYPE);

        final Map<Point, Point> closedList = new HashMap<>();
        final Map<PathIdentifier, Double> scores = new HashMap<>();
        final Set<Path> visited = new HashSet<>();
        final List<SignalBoxNode> passedProtectionWay = new ArrayList<>();

        final ConnectionChecker checker = ConnectionChecker.getCheckerForType(pathType);
        checker.type = pathType;
        checker.visited = visited;
        checker.grid = grid;
        checker.passedProtectionWay = passedProtectionWay;
        PathwayRequestMode mode = PathwayRequestMode.NO_PATH;

        for (final PathIdentifier pathIdent : firstNode.getStartIdentifiers()) {
            scores.put(pathIdent, getCosts(pathIdent.getMode(), firstNode, p1, p2));
        }

        while (!scores.isEmpty()) {
            final PathIdentifier currentPath = scores.entrySet().stream()
                    .min((ident1, ident2) -> Double.compare(ident1.getValue(), ident2.getValue()))
                    .get().getKey();
            scores.remove(currentPath);

            final Point previousPoint = currentPath.getPoint();
            debugPointList.add(previousPoint);
            final Point nextPoint = currentPath.path.point2;
            if (previousPoint.equals(p2)) {
                final ArrayList<SignalBoxNode> nodes = new ArrayList<>();
                for (Point point = previousPoint; point != null; point = closedList.get(point)) {
                    final SignalBoxNode boxNode = modeGrid.get(point);
                    nodes.add(boxNode);
                }
                if (!checkForPreviousProtectionWay(grid, p1, passedProtectionWay.stream()
                        .filter(nodes::contains).collect(Collectors.toList())))
                    return PathwayRequestResult.getByMode(PathwayRequestMode.ALREADY_USED);
                if (ConfigHandler.GENERAL.debugMode.get()) {
                    grid.sendDebugPointUpdates(debugPointList);
                    debugPointList.clear();
                }
                if (nodes.size() < 2)
                    return PathwayRequestResult.getByMode(PathwayRequestMode.NO_PATH);
                if (!checkForValidEnd(pathType, nodes.get(0), nodes.get(1)))
                    return PathwayRequestResult.getByMode(PathwayRequestMode.NO_EQUAL_PATH_TYPE);
                return new PathwayRequestResult(PathwayRequestMode.PASS,
                        PathwayData.of(grid, nodes, pathType));
            }
            checker.previousPoint = previousPoint;
            final SignalBoxNode nextNode = modeGrid.get(nextPoint);
            if (nextNode == null) {
                mode = PathwayRequestMode.NO_PATH;
                continue;
            }

            checker.nextNode = nextNode;
            for (final PathIdentifier pathIdent : nextNode.toPathIdentifier()) {
                checker.path = pathIdent.path;
                mode = checker.check();
                if (nextPoint.equals(p2) || mode.isPass()) {
                    scores.put(pathIdent, getCosts(pathIdent.getMode(), nextNode, nextPoint, p2));
                    closedList.put(nextPoint, previousPoint);
                    visited.add(pathIdent.path);
                    visited.add(pathIdent.path.getInverse());
                }
            }
        }
        if (ConfigHandler.GENERAL.debugMode.get()) {
            grid.sendDebugPointUpdates(debugPointList);
            debugPointList.clear();
        }
        return PathwayRequestResult.getByMode(mode);
    }

    public static List<SignalBoxNode> requestProtectionWay(final Point p1, final Point p2,
            final SignalBoxGrid grid) {
        final Map<Point, SignalBoxNode> modeGrid = grid.modeGrid;
        final SignalBoxNode firstNode = modeGrid.get(p1);
        final Map<Point, Point> closedList = new HashMap<>();
        final Map<PathIdentifier, Double> scores = new HashMap<>();
        final Set<Path> visited = new HashSet<>();
        final List<SignalBoxNode> passedProtectionWayNodes = new ArrayList<>();

        final ConnectionChecker checker = ConnectionChecker.getCheckerForType(PathType.NORMAL);
        checker.type = PathType.NORMAL;
        checker.visited = visited;
        checker.grid = grid;
        checker.passedProtectionWay = passedProtectionWayNodes;
        PathwayRequestMode mode = PathwayRequestMode.NO_PATH;

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
                if (!checkForPreviousProtectionWay(grid, p1, passedProtectionWayNodes.stream()
                        .filter(nodes::contains).collect(Collectors.toList())))
                    return ImmutableList.of();
                return ImmutableList.copyOf(nodes);
            }
            checker.previousPoint = previousPoint;
            final SignalBoxNode nextNode = modeGrid.get(nextPoint);
            if (nextNode == null) {
                mode = PathwayRequestMode.NO_PATH;
                continue;
            }

            checker.nextNode = nextNode;
            for (final PathIdentifier pathIdent : nextNode.toPathIdentifier()) {
                checker.path = pathIdent.path;
                mode = checker.check();
                if (nextPoint.equals(p2) || mode.isPass()) {
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

    private static boolean checkForValidEnd(final PathType type, final SignalBoxNode lastNode,
            final SignalBoxNode previous) {
        final Point delta = lastNode.getPoint().delta(previous.getPoint());
        final Rotation rotation =
                SignalBoxUtil.getRotationFromDelta(delta).getRotated(Rotation.CLOCKWISE_180);
        for (final EnumGuiMode mode : type.getModes()) {
            if (!mode.getModeType().isValidEnd()) {
                continue;
            }
            for (final Rotation rot : getModeRots(rotation, mode)) {
                final ModeSet modeSet = new ModeSet(mode, rot);
                if (lastNode.has(modeSet))
                    return true;
            }
        }
        return false;
    }

    private static Rotation[] getModeRots(final Rotation rot, final EnumGuiMode mode) {
        if (mode.equals(EnumGuiMode.OUT_CONNECTION))
            return new Rotation[] {
                    rot.getRotated(Rotation.CLOCKWISE_180)
            };
        if (mode.equals(EnumGuiMode.END))
            return new Rotation[] {
                    rot, rot.getRotated(Rotation.CLOCKWISE_180)
            };
        return new Rotation[] {
                rot
        };
    }

    public static boolean isPathBlocked(final SignalBoxGrid grid, final SignalBoxNode node,
            final Path path) {
        final AtomicBoolean bool = new AtomicBoolean(false);
        node.getOption(path).filter(entry -> entry.containsEntry(PathEntryType.BLOCKING))
                .ifPresent(option -> option.getEntry(PathEntryType.BLOCKING).ifPresent(pos -> {
                    if (isPowerd(grid.tile, pos)) {
                        bool.set(true);
                    }
                }));
        return bool.get();
    }

    private static boolean isPowerd(final SignalBoxTileEntity tile, final BlockPos pos) {
        if (tile == null)
            return false;
        final World world = tile.getWorld();
        if (world == null) {
            OpenSignalsMain.getLogger()
                    .error("The world is null when trying to load a blockstate to create a pathway!"
                            + " This should't be so!");
            return false;
        }
        final BlockState state = world.getBlockState(pos);
        if (state == null || !(state.getBlock() instanceof RedstoneIO))
            return false;
        return state.getValue(RedstoneIO.POWER);
    }

    private static boolean checkForPreviousProtectionWay(final SignalBoxGrid grid,
            final Point start, final List<SignalBoxNode> passedProtectionWayNodes) {
        if (passedProtectionWayNodes.isEmpty())
            return true;
        final SignalBoxPathway previous = grid.getPathwayByLastPoint(start);
        if (previous == null)
            return false;
        return previous.getProtectionWayNodes().containsAll(passedProtectionWayNodes);
    }

    public static int getDefaultCosts(final ModeSet mode) {
        final EnumGuiMode guiMode = mode.mode;
        switch (guiMode) {
            case STRAIGHT:
            case END:
            case IN_CONNECTION:
            case ARROW:
            case OUT_CONNECTION:
            case CROSSING: {
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
        return !possilbeTypes.isEmpty() ? possilbeTypes.get(0) : PathType.NONE;
    }

}