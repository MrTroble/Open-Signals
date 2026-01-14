package com.troblecodings.signals.signalbox;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.troblecodings.signals.enums.EnumPathUsage;
import com.troblecodings.signals.enums.PathType;
import com.troblecodings.signals.enums.PathwayRequestResult.PathwayRequestMode;
import com.troblecodings.signals.signalbox.debug.SignalBoxFactory;
import com.troblecodings.signals.signalbox.entrys.PathEntryType;

public abstract class ConnectionChecker {

    private static final SignalBoxFactory FACTORY = SignalBoxFactory.getFactory();

    public SignalBoxGrid grid;
    public PathType type;
    public SignalBoxNode nextNode;
    public Point previousPoint;
    public Path path;
    public Set<Path> visited;
    public List<SignalBoxNode> passedProtectionWay;

    public abstract PathwayRequestMode check();

    @Override
    public int hashCode() {
        return Objects.hash(nextNode, path, previousPoint, type, visited);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if ((obj == null) || (getClass() != obj.getClass()))
            return false;
        final ConnectionChecker other = (ConnectionChecker) obj;
        return Objects.equals(nextNode, other.nextNode) && Objects.equals(path, other.path)
                && Objects.equals(previousPoint, other.previousPoint) && type == other.type
                && Objects.equals(visited, other.visited);
    }

    public static ConnectionChecker getCheckerForType(final PathType type) {
        switch (type) {
            case NORMAL:
                return FACTORY.getConnectionCheckerNormal();
            case SHUNTING:
                return FACTORY.getConnectionCheckerShunting();
            default:
                return null;
        }
    }

    public static class ConnectionCheckerNormal extends ConnectionChecker {

        @Override
        public PathwayRequestMode check() {
            if (nextNode == null)
                return PathwayRequestMode.NO_PATH;
            final PathwayRequestMode nodeResult = nextNode.canMakePath(path, type);
            if (!nodeResult.isPass())
                return nodeResult;
            final EnumPathUsage usage = nextNode.getOption(path)
                    .flatMap(entry -> entry.getEntry(PathEntryType.PATHUSAGE))
                    .orElse(EnumPathUsage.FREE);
            if (!(usage.equals(EnumPathUsage.FREE) || usage.equals(EnumPathUsage.PROTECTED)))
                return PathwayRequestMode.ALREADY_USED;
            if (usage.equals(EnumPathUsage.PROTECTED)
                    && !this.passedProtectionWay.contains(nextNode)) {
                this.passedProtectionWay.add(nextNode);
            }
            if (SignalBoxUtil.isPathBlocked(grid, nextNode, path))
                return PathwayRequestMode.INPUT_BLOCKING;
            final boolean isValid = path.point1.equals(previousPoint) && !visited.contains(path);
            return isValid ? PathwayRequestMode.PASS : PathwayRequestMode.NO_PATH;
        }
    }

    public static class ConnectionCheckerShunting extends ConnectionChecker {

        @Override
        public PathwayRequestMode check() {
            if (nextNode == null)
                return PathwayRequestMode.NO_PATH;
            final PathwayRequestMode nodeResult = nextNode.canMakePath(path, type);
            if (!nodeResult.isPass())
                return nodeResult;
            final EnumPathUsage usage = nextNode.getOption(path)
                    .flatMap(entry -> entry.getEntry(PathEntryType.PATHUSAGE))
                    .orElse(EnumPathUsage.FREE);
            if (!(usage.equals(EnumPathUsage.FREE) || usage.equals(EnumPathUsage.PROTECTED)
                    || usage.equals(EnumPathUsage.BLOCKED)))
                return PathwayRequestMode.ALREADY_USED;
            if (usage.equals(EnumPathUsage.PROTECTED)
                    && !this.passedProtectionWay.contains(nextNode)) {
                this.passedProtectionWay.add(nextNode);
            }
            final boolean isValid = path.point1.equals(previousPoint) && !visited.contains(path);
            return isValid ? PathwayRequestMode.PASS : PathwayRequestMode.NO_PATH;
        }

    }

}