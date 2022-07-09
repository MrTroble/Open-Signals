package eu.gir.girsignals.signalbox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.lwjgl.opengl.GL11;

import eu.gir.girsignals.enums.EnumPathUsage;
import eu.gir.girsignals.enums.PathType;
import eu.gir.girsignals.signalbox.entrys.PathEntryType;
import eu.gir.guilib.ecs.entitys.UIEntity;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;

public final class SignalBoxUtil {

    public static final int FREE_COLOR = 0xFF000000;
    public static final int SELECTED_COLOR = 0xFF00FF00;
    public static final int USED_COLOR = 0xFFFF0000;

    private SignalBoxUtil() {
    }

    public static final String REQUEST_WAY = "requestWay";
    public static final String RESET_WAY = "resetWay";
    public static final String POINT0 = "P";
    public static final String POINT1 = "P1";
    public static final String POINT2 = "P2";

    public static Point fromNBT(final NBTTagCompound comp, final String name) {
        return new Point(comp.getInteger("x" + name), comp.getInteger("y" + name));
    }

    public static void toNBT(final NBTTagCompound comp, final String name, final Point point) {
        comp.setInteger("x" + name, point.getX());
        comp.setInteger("y" + name, point.getY());
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

    public static Point getOffset(final Rotation rotation, final Point point) {
        final int x = Rotation.CLOCKWISE_180.equals(rotation) ? -1
                : (Rotation.NONE.equals(rotation) ? 1 : 0);
        final int y = Rotation.COUNTERCLOCKWISE_90.equals(rotation) ? -1
                : (Rotation.CLOCKWISE_90.equals(rotation) ? 1 : 0);
        return new Point(x + point.getX(), y + point.getY());
    }

    private static class ConnectionChecker {

        public Path path;
        public SignalBoxNode nextNode;
        public PathType type;
        public SignalBoxNode lastNode;
        public Point previous;
        public Set<Path> visited;

        public boolean check() {
            if (path == null || previous == null)
                return true;
            if (nextNode == null || !nextNode.canMakePath(path, type))
                return false;
            if (!nextNode.getOption(path).map(entry -> entry.getEntry(PathEntryType.PATHUSAGE)
                    .filter(EnumPathUsage.FREE::equals)).isPresent())
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

        final List<Point> openList = new ArrayList<Point>();
        final Set<Path> visitedPaths = new HashSet<>();

        openList.add(p1);
        gscores.put(p1, 0.0);
        fscores.put(p1, calculateHeuristic(p1, p2));

        final ConnectionChecker checker = new ConnectionChecker();
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
                return Optional.of(new SignalBoxPathway(modeGrid, nodes, pathType));
            }
            if (nextSignalnode == null)
                continue;
            checker.nextNode = nextSignalnode;
            for (final Path entry : nextSignalnode.connections()) {
                final Point neighbour = entry.point2;
                checker.previous = closedList.get(currentNode);
                checker.path = checker.previous == null || neighbour == null ? null
                        : new Path(checker.previous, neighbour);
                if (checker.check()) {
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

    public static void drawRect(final int left, final int top, final int right, final int bottom,
            final int color) {
        final float f3 = (color >> 24 & 255) / 255.0F;
        final float f = (color >> 16 & 255) / 255.0F;
        final float f1 = (color >> 8 & 255) / 255.0F;
        final float f2 = (color & 255) / 255.0F;
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.color(f, f1, f2, f3);
        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        bufferbuilder.pos(left, bottom, 0.0D).endVertex();
        bufferbuilder.pos(right, bottom, 0.0D).endVertex();
        bufferbuilder.pos(right, top, 0.0D).endVertex();
        bufferbuilder.pos(left, top, 0.0D).endVertex();
        tessellator.draw();
    }

    public static void drawTextured(final UIEntity entity, final int textureID) {
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder bufferbuilder = tessellator.getBuffer();
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableTexture2D();
        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        final double offset = 0.25 * textureID;
        bufferbuilder.pos(0, entity.getHeight(), textureID).tex(offset, 0.5).endVertex();
        bufferbuilder.pos(entity.getWidth(), entity.getHeight(), textureID).tex(offset + 0.25, 0.5)
                .endVertex();
        bufferbuilder.pos(entity.getWidth(), 0, textureID).tex(offset + 0.25, 0).endVertex();
        bufferbuilder.pos(0, 0, textureID).tex(offset, 0).endVertex();
        tessellator.draw();
        GlStateManager.disableTexture2D();
    }

    public static void drawLines(final int x1, final int x2, final int y1, final int y2,
            final int color) {
        final float f3 = (color >> 24 & 255) / 255.0F;
        final float f = (color >> 16 & 255) / 255.0F;
        final float f1 = (color >> 8 & 255) / 255.0F;
        final float f2 = (color & 255) / 255.0F;
        final Tessellator tessellator = Tessellator.getInstance();
        final BufferBuilder bufferbuilder = tessellator.getBuffer();
        GL11.glLineWidth(5);
        GlStateManager.color(f, f1, f2, f3);
        bufferbuilder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
        bufferbuilder.pos(x1, y1, 0.0D).endVertex();
        bufferbuilder.pos(x2, y2, 0.0D).endVertex();
        tessellator.draw();
    }

}
