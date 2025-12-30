package com.troblecodings.signals.signalbox;

import java.util.Objects;
import java.util.Optional;
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

    public abstract PathwayRequestMode check();

    @Override
    public int hashCode() {
        return Objects.hash(nextNode, path, previousPoint, type, visited);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
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
            final Optional<EnumPathUsage> optional = nextNode.getOption(path)
                    .flatMap(entry -> entry.getEntry(PathEntryType.PATHUSAGE));
            if (optional.isPresent() && !(optional.get().equals(EnumPathUsage.FREE)
                    || optional.get().equals(EnumPathUsage.PROTECTED)))
                return PathwayRequestMode.ALREADY_USED;
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
            final Optional<EnumPathUsage> optional = nextNode.getOption(path)
                    .flatMap(entry -> entry.getEntry(PathEntryType.PATHUSAGE));
            if (optional.isPresent() && !(optional.get().equals(EnumPathUsage.BLOCKED)
                    || optional.get().equals(EnumPathUsage.PROTECTED)
                    || optional.get().equals(EnumPathUsage.FREE)))
                return PathwayRequestMode.ALREADY_USED;
            final boolean isValid = path.point1.equals(previousPoint) && !visited.contains(path);
            return isValid ? PathwayRequestMode.PASS : PathwayRequestMode.NO_PATH;
        }

    }

}