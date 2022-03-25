package eu.gir.girsignals.signalbox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import eu.gir.guilib.ecs.entitys.UIEntity;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;

public class SignalBoxUtil {
	
	public static final int FREE_COLOR = 0xFF000000;
	public static final int SELECTED_COLOR = 0xFF00FF00;
	public static final int USED_COLOR = 0xFFFF0000;
	
	public static final String REQUEST_WAY = "requestWay";
	public static final String RESET_WAY = "resetWay";
	public static final String POINT0 = "P";
	public static final String POINT1 = "P1";
	public static final String POINT2 = "P2";
	
	public static Point fromNBT(NBTTagCompound comp, String name) {
		return new Point(comp.getInteger("x" + name), comp.getInteger("y" + name));
	}
	
	public static void toNBT(NBTTagCompound comp, String name, Point point) {
		comp.setInteger("x" + name, point.getX());
		comp.setInteger("y" + name, point.getY());
	}
	
	private static double calculateHeuristic(Point p1, Point p2) {
		final int dX = p2.getX() - p1.getX();
		final int dY = p2.getY() - p1.getY();
		return Math.hypot(dX, dY);
	}
	
	private static boolean isRS(final SignalNode node, final SignalNode other) {
		return (node.has(EnumGuiMode.RS) || node.has(EnumGuiMode.RA10)) && (other.has(EnumGuiMode.RS) || other.has(EnumGuiMode.RA10));
	}
	
	private static boolean isHP(final SignalNode node, final SignalNode other) {
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
		final int x = Rotation.CLOCKWISE_180.equals(rotation) ? -1 : (Rotation.NONE.equals(rotation) ? 1 : 0);
		final int y = Rotation.COUNTERCLOCKWISE_90.equals(rotation) ? -1 : (Rotation.CLOCKWISE_90.equals(rotation) ? 1 : 0);
		return new Point(x + point.getX(), y + point.getY());
	}
	
	public static boolean checkApplicable(final SignalNode neighbour, final SignalNode previouse, final boolean isRS) {
		return checkApplicable(neighbour, previouse, isRS, Rotation.NONE);
	}
	
	public static boolean checkApplicable(final SignalNode neighbour, final SignalNode previouse, final boolean isRS, final Rotation apply) {
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
	
	private static boolean connectionCheck(final Point p1, final Point p2, final SignalNode cSNode, final Point currentNode, final Point neighbour, final SignalNode next, final Point previouse, final Map<Point, Point> closedList, final Entry<Point, Point> entry, final boolean isRS) {
		if (next == null || next.isUsed())
			return false;
		if (currentNode.equals(p1) && checkApplicable(cSNode, next, isRS))
			return false;
		if (neighbour.equals(p2))
			return true;
		if (checkApplicable(next, cSNode, isRS))
			return false;
		return previouse == null || previouse.equals(entry.getKey()) && !closedList.containsKey(entry.getValue());
	}
	
	public static Optional<ArrayList<SignalNode>> requestWay(final Map<Point, SignalNode> modeGrid, final Point p1, final Point p2) {
		if (!modeGrid.containsKey(p1) || !modeGrid.containsKey(p2))
			return Optional.empty();
		final SignalNode lastNode = modeGrid.get(p2);
		final SignalNode firstNode = modeGrid.get(p1);
		final boolean isrs = isRS(lastNode, firstNode);
		if (!(isrs || isHP(lastNode, firstNode)) && !lastNode.has(EnumGuiMode.END)) {
			return Optional.empty();
		}
		final HashMap<Point, Point> closedList = new HashMap<>();
		final Set<Point> openList = new HashSet<Point>();
		final HashMap<Point, Double> fscores = new HashMap<>();
		final HashMap<Point, Double> gscores = new HashMap<>();
		final List<Entry<Point, Point>> entryImpl = Lists.newArrayList(null, null);
		
		openList.add(p1);
		gscores.put(p1, 0.0);
		fscores.put(p1, calculateHeuristic(p1, p2));
		while (!openList.isEmpty()) {
			final Point currentNode = openList.stream().min((n1, n2) -> {
				return Double.compare(fscores.getOrDefault(n1, Double.MAX_VALUE), fscores.getOrDefault(n2, Double.MAX_VALUE));
			}).get();
			openList.remove(currentNode);
			final SignalNode cSNode = modeGrid.get(currentNode);
			if (currentNode.equals(p2)) {
				if (!checkApplicable(cSNode, modeGrid.get(closedList.get(currentNode)), isrs) && !lastNode.has(EnumGuiMode.END))
					return Optional.empty();
				final ArrayList<SignalNode> nodes = new ArrayList<>();
				for (Point point = currentNode; point != null; point = closedList.get(point)) {
					nodes.add(modeGrid.get(point));
				}
				return Optional.of(nodes);
			}
			if (cSNode == null)
				continue;
			for (final Entry<Point, Point> e : cSNode.connections()) {
				entryImpl.set(0, e);
				entryImpl.set(1, Maps.immutableEntry(e.getValue(), e.getKey()));
				for (final Entry<Point, Point> entry : entryImpl) {
					if (entry.getKey() == null || entry.getValue() == null)
						continue;
					final Point neighbour = entry.getValue();
					final Point previouse = closedList.get(currentNode);
					final SignalNode next = modeGrid.get(neighbour);
					if (connectionCheck(p1, p2, cSNode, currentNode, neighbour, next, previouse, closedList, entry, isrs)) {
						final double tScore = gscores.getOrDefault(currentNode, Double.MAX_VALUE - 1) + 1;
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
	
	static void drawRect(int left, int top, int right, int bottom, int color) {
		float f3 = (float) (color >> 24 & 255) / 255.0F;
		float f = (float) (color >> 16 & 255) / 255.0F;
		float f1 = (float) (color >> 8 & 255) / 255.0F;
		float f2 = (float) (color & 255) / 255.0F;
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		GlStateManager.color(f, f1, f2, f3);
		bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
		bufferbuilder.pos((double) left, (double) bottom, 0.0D).endVertex();
		bufferbuilder.pos((double) right, (double) bottom, 0.0D).endVertex();
		bufferbuilder.pos((double) right, (double) top, 0.0D).endVertex();
		bufferbuilder.pos((double) left, (double) top, 0.0D).endVertex();
		tessellator.draw();
	}
	
	static void drawTextured(UIEntity entity, int textureID) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		GlStateManager.color(1, 1, 1, 1);
		GlStateManager.enableTexture2D();
		bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		final double offset = 0.25 * textureID;
		bufferbuilder.pos((double) 0, (double) entity.getHeight(), textureID).tex(offset, 1).endVertex();
		bufferbuilder.pos((double) entity.getWidth(), (double) entity.getHeight(), textureID).tex(offset + 0.25, 1).endVertex();
		bufferbuilder.pos((double) entity.getWidth(), (double) 0, textureID).tex(offset + 0.25, 0).endVertex();
		bufferbuilder.pos((double) 0, (double) 0, textureID).tex(offset, 0).endVertex();
		tessellator.draw();
		GlStateManager.disableTexture2D();
	}
	
	static void drawLines(int x1, int x2, int y1, int y2, int color) {
		float f3 = (float) (color >> 24 & 255) / 255.0F;
		float f = (float) (color >> 16 & 255) / 255.0F;
		float f1 = (float) (color >> 8 & 255) / 255.0F;
		float f2 = (float) (color & 255) / 255.0F;
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		GL11.glLineWidth(5);
		GlStateManager.color(f, f1, f2, f3);
		bufferbuilder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
		bufferbuilder.pos(x1, y1, 0.0D).endVertex();
		bufferbuilder.pos(x2, y2, 0.0D).endVertex();
		tessellator.draw();
	}
	
}
