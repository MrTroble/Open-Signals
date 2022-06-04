package eu.gir.girsignals.signalbox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import eu.gir.guilib.ecs.entitys.UIEntity;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;

public final class SignalBoxUtil {

    private SignalBoxUtil() {
    }

    public static final int FREE_COLOR = 0xFF000000;
    public static final int SELECTED_COLOR = 0xFF00FF00;
    public static final int USED_COLOR = 0xFFFF0000;

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

    private static boolean isRS(final SignalBoxNode node, final SignalBoxNode other) {
        return (node.has(EnumGuiMode.RS) || node.has(EnumGuiMode.RA10))
                && (other.has(EnumGuiMode.RS) || other.has(EnumGuiMode.RA10));
    }

    private static boolean isHP(final SignalBoxNode node, final SignalBoxNode other) {
        return node.has(EnumGuiMode.HP) && (other.has(EnumGuiMode.HP));
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

    public static boolean checkApplicable(final SignalBoxNode neighbour,
            final SignalBoxNode previouse, final boolean isRS) {
        return checkApplicable(neighbour, previouse, isRS, Rotation.NONE);
    }

    public static boolean checkApplicable(final SignalBoxNode neighbour,
            final SignalBoxNode previouse, final boolean isRS, final Rotation apply) {
        if (previouse == null)
            return false;
        final Point prev = previouse.getPoint();
        final Point next = neighbour.getPoint();
        final Point delta = new Point(prev.getX() - next.getX(), prev.getY() - next.getY());
        final ArrayList<Rotation> list = new ArrayList<>();
        final Rotation rot = getRotationFromDelta(delta).add(apply);
        if (isRS) {
            list.addAll(neighbour.getRotations(EnumGuiMode.RS));
            list.addAll(neighbour.getRotations(EnumGuiMode.RA10));
        } else {
            list.addAll(neighbour.getRotations(EnumGuiMode.HP));
        }
        return list.contains(rot);
    }

    private static boolean connectionCheck(final Point p1, final Point p2,
            final SignalBoxNode cSNode, final Point currentNode, final Point neighbour,
            final SignalBoxNode next, final Point previouse, final Map<Point, Point> closedList,
            final Path entry, final boolean isRS) {
        if (next == null || next.isUsed())
            return false;
        if (currentNode.equals(p1) && checkApplicable(cSNode, next, isRS))
            return false;
        if (neighbour.equals(p2))
            return true;
        if (checkApplicable(next, cSNode, isRS))
            return false;
        return previouse == null
                || previouse.equals(entry.point1) && !closedList.containsKey(entry.point2);
    }

    public static Optional<ArrayList<SignalBoxNode>> requestWay(
            final Map<Point, SignalBoxNode> modeGrid, final Point p1, final Point p2) {
        if (!modeGrid.containsKey(p1) || !modeGrid.containsKey(p2))
            return Optional.empty();
        final SignalBoxNode lastNode = modeGrid.get(p2);
        final SignalBoxNode firstNode = modeGrid.get(p1);
        final boolean isrs = isRS(lastNode, firstNode);
        if (!(isrs || isHP(lastNode, firstNode)) && !lastNode.has(EnumGuiMode.END)) {
            return Optional.empty();
        }
        final HashMap<Point, Point> closedList = new HashMap<>();
        final Set<Point> openList = new HashSet<Point>();
        final HashMap<Point, Double> fscores = new HashMap<>();
        final HashMap<Point, Double> gscores = new HashMap<>();
        final List<Path> entryImpl = Lists.newArrayList(null, null);

        openList.add(p1);
        gscores.put(p1, 0.0);
        fscores.put(p1, calculateHeuristic(p1, p2));
        while (!openList.isEmpty()) {
            final Point currentNode = openList.stream().min((n1, n2) -> {
                return Double.compare(fscores.getOrDefault(n1, Double.MAX_VALUE),
                        fscores.getOrDefault(n2, Double.MAX_VALUE));
            }).get();
            openList.remove(currentNode);
            final SignalBoxNode cSNode = modeGrid.get(currentNode);
            if (currentNode.equals(p2)) {
                if (!checkApplicable(cSNode, modeGrid.get(closedList.get(currentNode)), isrs)
                        && !lastNode.has(EnumGuiMode.END))
                    return Optional.empty();
                final ArrayList<SignalBoxNode> nodes = new ArrayList<>();
                for (Point point = currentNode; point != null; point = closedList.get(point)) {
                    nodes.add(modeGrid.get(point));
                }
                return Optional.of(nodes);
            }
            if (cSNode == null)
                continue;
            for (final Path e : cSNode.connections()) {
                entryImpl.set(0, e);
                entryImpl.set(1, e.getInverse());
                for (final Path entry : entryImpl) {
                    final Point neighbour = entry.point2;
                    final Point previouse = closedList.get(currentNode);
                    final SignalBoxNode next = modeGrid.get(neighbour);
                    if (connectionCheck(p1, p2, cSNode, currentNode, neighbour, next, previouse,
                            closedList, entry, isrs)) {
                        final double tScore = gscores.getOrDefault(currentNode,
                                Double.MAX_VALUE - 1) + 1;
                        if (tScore < gscores.getOrDefault(neighbour, Double.MAX_VALUE)) {
                            closedList.put(neighbour, currentNode);
                            gscores.put(neighbour, tScore);
                            fscores.put(neighbour, tScore + calculateHeuristic(neighbour, p2));
                            if (!openList.contains(neighbour)) {
                                openList.add(neighbour);
                            }
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }

    static void drawRect(final int left, final int top, final int right, final int bottom,
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

    static void drawTextured(final UIEntity entity, final int textureID) {
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

    static void drawLines(final int x1, final int x2, final int y1, final int y2, final int color) {
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
