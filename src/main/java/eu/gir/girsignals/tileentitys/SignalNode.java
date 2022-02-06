package eu.gir.girsignals.tileentitys;

import java.util.ArrayList;
import java.util.Map.Entry;

import org.lwjgl.util.Point;

import com.google.common.collect.Maps;

import eu.gir.girsignals.tileentitys.SignalBoxTileEntity.EnumGUIMode;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.Rotation;

public class SignalNode {
	
	private final Point point;
	private ArrayList<Entry<Point, Point>> possibleConnections = new ArrayList<Entry<Point, Point>>();
	private ArrayList<Entry<EnumGUIMode, Rotation>> possibleModes = new ArrayList<Entry<EnumGUIMode, Rotation>>();
	
	public SignalNode(Point point) {
		this.point = point;
	}
	
	public SignalNode(NBTTagCompound comp) {
		this.point = new Point(comp.getInteger("xP"), comp.getInteger("yP"));
		final NBTTagList pointList = (NBTTagList) comp.getTag("pointList");
		pointList.forEach(e -> {
			final NBTTagCompound entry = (NBTTagCompound) e;
			final Point p1 = new Point(entry.getInteger("xP1"), entry.getInteger("yP1"));
			final Point p2 = new Point(entry.getInteger("xP2"), entry.getInteger("yP2"));
			possibleConnections.add(Maps.immutableEntry(p1, p2));
		});
	}
	
	public void add(EnumGUIMode mode, Rotation rot) {
		possibleModes.add(Maps.immutableEntry(mode, rot));
	}
	
	public NBTTagCompound writeNBT() {
		final NBTTagCompound comp = new NBTTagCompound();
		comp.setInteger("xP", point.getX());
		comp.setInteger("yP", point.getY());
		final NBTTagList pointList = new NBTTagList();
		possibleConnections.forEach(e -> {
			final NBTTagCompound entry = new NBTTagCompound();
			entry.setInteger("xP1", e.getKey().getX());
			entry.setInteger("yP1", e.getKey().getY());
			entry.setInteger("xP2", e.getValue().getX());
			entry.setInteger("yP2", e.getValue().getY());
			pointList.appendTag(entry);
		});
		comp.setTag("pointList", pointList);
		return comp;
	}
	
	public void post() {
		possibleModes.forEach(e -> {
			final Point p1 = new Point(this.point);
			final Point p2 = new Point(this.point);
			switch (e.getKey()) {
			case CORNER:
				switch (e.getValue()) {
				case NONE:
					p1.translate(0, 1);
					p2.translate(-1, 0);
					break;
				case CLOCKWISE_90:
					p1.translate(0, -1);
					p2.translate(-1, 0);
					break;
				case CLOCKWISE_180:
					p1.translate(0, -1);
					p2.translate(1, 0);
					break;
				case COUNTERCLOCKWISE_90:
					p1.translate(0, 1);
					p2.translate(1, 0);
					break;
				default:
					return;
				}
				break;
			case STRAIGHT:
				switch (e.getValue()) {
				case NONE:
				case CLOCKWISE_180:
					p1.translate(1, 0);
					p2.translate(-1, 0);
					break;
				case CLOCKWISE_90:
				case COUNTERCLOCKWISE_90:
					p1.translate(0, 1);
					p2.translate(0, -1);
					break;
				default:
					return;
				}
				break;
			default:
				return;
			}
			possibleConnections.add(Maps.immutableEntry(p1, p2));
		});
	}
	
	public Point getPoint() {
		return point;
	}
	
	public ArrayList<Entry<Point, Point>> connections() {
		return this.possibleConnections;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof SignalNode))
			return false;
		return this.point.equals(((SignalNode) obj).getPoint());
	}
}
