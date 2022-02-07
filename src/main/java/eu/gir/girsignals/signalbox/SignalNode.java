package eu.gir.girsignals.signalbox;

import static eu.gir.girsignals.signalbox.SignalBoxUtil.POINT0;
import static eu.gir.girsignals.signalbox.SignalBoxUtil.fromNBT;
import static eu.gir.girsignals.signalbox.SignalBoxUtil.toNBT;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.lwjgl.util.Point;

import com.google.common.collect.Maps;

import eu.gir.girsignals.signalbox.SignalBoxUtil.EnumGUIMode;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.Rotation;

public class SignalNode {
	
	private final String POINT_LIST = "pointList";
	private final String MODE = "mode";
	private final String ROTATION = "rotation";
	private final String OPTION = "option";
	
	private final Point point;
	private HashMap<Entry<Point, Point>, Entry<EnumGUIMode, Rotation>> possibleConnections = new HashMap<>();
	private HashMap<Entry<EnumGUIMode, Rotation>, PathOption> possibleModes = new HashMap<>();
	
	public SignalNode(Point point) {
		this.point = point;
	}
	
	public SignalNode(NBTTagCompound comp) {
		this.point = fromNBT(comp, POINT0);
		final NBTTagList pointList = (NBTTagList) comp.getTag(POINT_LIST);
		pointList.forEach(e -> {
			final NBTTagCompound entry = (NBTTagCompound) e;
			final EnumGUIMode mode = EnumGUIMode.valueOf(entry.getString(MODE));
			final Rotation rotation = Rotation.valueOf(entry.getString(ROTATION));
			final Entry<EnumGUIMode, Rotation> modeRotation = Maps.immutableEntry(mode, rotation);
			possibleModes.put(modeRotation, new PathOption(entry.getCompoundTag(OPTION)));
		});
	}
		
	public void add(EnumGUIMode mode, Rotation rot) {
		possibleModes.put(Maps.immutableEntry(mode, rot), new PathOption());
	}
	
	public NBTTagCompound writeNBT() {
		final NBTTagCompound comp = new NBTTagCompound();
		toNBT(comp, POINT0, this.point);
		final NBTTagList pointList = new NBTTagList();
		possibleModes.forEach((mode, option) -> {
			final NBTTagCompound entry = new NBTTagCompound();
			entry.setString(MODE, mode.getKey().name());
			entry.setString(ROTATION, mode.getValue().name());
			entry.setTag(OPTION, option.writeNBT());
			pointList.appendTag(entry);
		});
		comp.setTag(POINT_LIST, pointList);
		return comp;
	}
	
	public void post() {
		possibleModes.forEach((e, i) -> {
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
			possibleConnections.put(Maps.immutableEntry(p1, p2), e);
		});
	}
	
	public Point getPoint() {
		return point;
	}
	
	public Set<Entry<Point, Point>> connections() {
		return this.possibleConnections.keySet();
	}
	
	public void applyNormal(Entry<EnumGUIMode, Rotation> guimode, Consumer<PathOption> applier) {
		if(guimode != null && this.possibleModes.containsKey(guimode))
			applier.accept(this.possibleModes.get(guimode));
	}
	
	public void apply(Entry<Point, Point> entry, Consumer<PathOption> applier) {
		Entry<EnumGUIMode, Rotation> guimode = null;
		if (this.possibleConnections.containsKey(entry)) {
			guimode = this.possibleConnections.get(entry);
		} else {
			Entry<Point, Point> points = Maps.immutableEntry(entry.getValue(), entry.getKey());
			if (this.possibleConnections.containsKey(points)) {
				guimode = this.possibleConnections.get(points);
			}
		}
		applyNormal(guimode, applier);
	}

	public void forEach(BiConsumer<Entry<EnumGUIMode, Rotation>, PathOption> applier) {
		possibleModes.forEach(applier);
	}
}
