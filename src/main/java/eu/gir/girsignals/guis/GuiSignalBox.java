package eu.gir.girsignals.guis;

import static eu.gir.girsignals.signalbox.SignalBoxUtil.POINT1;
import static eu.gir.girsignals.signalbox.SignalBoxUtil.POINT2;
import static eu.gir.girsignals.signalbox.SignalBoxUtil.toNBT;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.lwjgl.util.Point;

import eu.gir.girsignals.guis.guilib.GuiBase;
import eu.gir.girsignals.guis.guilib.GuiElements;
import eu.gir.girsignals.guis.guilib.GuiSyncNetwork;
import eu.gir.girsignals.guis.guilib.entitys.UIBorder;
import eu.gir.girsignals.guis.guilib.entitys.UIBox;
import eu.gir.girsignals.guis.guilib.entitys.UIClickable;
import eu.gir.girsignals.guis.guilib.entitys.UIColor;
import eu.gir.girsignals.guis.guilib.entitys.UIDrag;
import eu.gir.girsignals.guis.guilib.entitys.UIEntity;
import eu.gir.girsignals.guis.guilib.entitys.UILabel;
import eu.gir.girsignals.guis.guilib.entitys.UIScale;
import eu.gir.girsignals.guis.guilib.entitys.UIScissor;
import eu.gir.girsignals.guis.guilib.entitys.UIScroll;
import eu.gir.girsignals.guis.guilib.entitys.UIStack;
import eu.gir.girsignals.signalbox.SignalBoxTileEntity;
import eu.gir.girsignals.signalbox.SignalBoxUtil;
import eu.gir.girsignals.signalbox.SignalBoxUtil.EnumGUIMode;
import eu.gir.girsignals.signalbox.SignalNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;

public class GuiSignalBox extends GuiBase {
	
	private static final int SELECTION_COLOR = 0x2900FF00;
	
	private final UIEntity lowerEntity = new UIEntity();
	private final SignalBoxTileEntity box;
	private final ContainerSignalBox container;
	private UISignalBoxTile lastTile = null;
	
	public GuiSignalBox(SignalBoxTileEntity box) {
		this.box = box;
		this.container = new ContainerSignalBox(this::update);
		Minecraft.getMinecraft().player.openContainer = this.container;
		init();
		if (this.box.getTag() != null)
			this.compound = this.box.getTag();
		this.entity.read(this.compound);
	}
	
	private void update(NBTTagCompound compound) {
		this.entity.read(compound);
		this.resetSelection();
	}
	
	private void resetSelection() {
		final UIColor[] array = (UIColor[]) this.entity.findRecursive(UIColor.class).stream().filter(color -> color.getColor() == SELECTION_COLOR).toArray(UIColor[]::new);
		for (final UIColor color : array) {
			color.getParent().remove(color);
		}
	}
	
	private void updateButton(UIEntity button) {
		final SoundHandler handler = Minecraft.getMinecraft().getSoundHandler();
		button.findRecursive(UIClickable.class).forEach(c -> {
			final Consumer<UIEntity> old = c.getCallback();
			c.setCallback(e -> {
				initMain();
				c.setCallback(old);
				handler.playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			});
		});
	}
	
	private void initSettings(UIEntity entity) {
		lowerEntity.clear();
		lowerEntity.add(new UIBox(UIBox.VBOX, 2));
		box.forEach(p -> lowerEntity.add(GuiElements.createButton(p.toString(), e -> {
		})));
		lowerEntity.update();
		updateButton(entity);
	}
	
	private void editTile(UIEntity tile, UIMenu menu, final UISignalBoxTile sbt) {
		tile.add(new UIClickable(e -> {
			final SignalNode node = sbt.getNode();
			final EnumGUIMode mode = EnumGUIMode.values()[menu.getSelection()];
			final Rotation rotation = Rotation.values()[menu.getRotation()];
			if (node.has(mode, rotation)) {
				node.remove(mode, rotation);
			} else {
				node.add(mode, rotation);
			}
		}));
	}
	
	private void initEdit(UIEntity entity) {
		final UIMenu menu = new UIMenu();
		initMain((e, name) -> this.editTile(e, menu, name));
		updateButton(entity);
		lowerEntity.add(menu);
	}
	
	private void initTile(UIEntity tile, final UISignalBoxTile currentTile) {
		tile.add(new UIClickable(c -> {
			c.add(new UIColor(SELECTION_COLOR));
			if (lastTile == null) {
				lastTile = currentTile;
			} else {
				if (lastTile == currentTile) {
					lastTile = null;
					this.resetSelection();
					return;
				}
				final NBTTagCompound comp = new NBTTagCompound();
				final NBTTagCompound way = new NBTTagCompound();
				toNBT(way, POINT1, lastTile.getNode().getPoint());
				toNBT(way, POINT2, currentTile.getNode().getPoint());
				comp.setTag(SignalBoxUtil.REQUEST_WAY, way);
				GuiSyncNetwork.sendToPosServer(comp, box.getPos());
				lastTile = null;
			}
		}));
	}
	
	private void initMain() {
		initMain(this::initTile);
	}
	
	private void initMain(BiConsumer<UIEntity, UISignalBoxTile> consumer) {
		this.entity.write(compound);
		lowerEntity.clear();
		lowerEntity.add(new UIColor(0xFF8B8B8B));
		lowerEntity.add(new UIStack());
		lowerEntity.add(new UIScissor());
		
		final UIEntity plane = new UIEntity();
		plane.setInheritWidth(true);
		plane.setInheritHeight(true);
		lowerEntity.add(new UIScroll(s -> {
			final float newScale = plane.getScaleX() + s * 0.001f;
			if (newScale <= 0)
				return;
			plane.setScaleX(newScale);
			plane.setScaleY(newScale);
			plane.update();
		}));
		lowerEntity.add(new UIDrag((x, y) -> {
			plane.setX(plane.getX() + x);
			plane.setY(plane.getY() + y);
			plane.update();
		}, 2));
		final UIBox vbox = new UIBox(UIBox.VBOX, 0);
		vbox.setPageable(false);
		plane.add(vbox);
		for (int x = 0; x < 50; x++) {
			final UIEntity row = new UIEntity();
			final UIBox hbox = new UIBox(UIBox.HBOX, 0);
			hbox.setPageable(false);
			row.add(hbox);
			row.setHeight(10);
			row.setWidth(10);
			for (int y = 0; y < 50; y++) {
				final UIEntity tile = new UIEntity();
				tile.setHeight(10);
				tile.setWidth(10);
				tile.add(new UIBorder(0xFF7F7F7F, 2));
				final Point name = new Point(y, x);
				final SignalNode node = new SignalNode(name);
				final UISignalBoxTile sbt = new UISignalBoxTile(node);
				tile.add(sbt);
				consumer.accept(tile, sbt);
				row.add(tile);
			}
			plane.add(row);
		}
		lowerEntity.add(plane);
		
		final UIEntity frame = new UIEntity();
		frame.setInheritHeight(true);
		frame.setInheritWidth(true);
		frame.add(new UIBorder(0xFF000000, 6));
		lowerEntity.add(frame);
		this.entity.read(compound);
	}
	
	private void init() {
		final String name = I18n.format("tile.signalbox.name");
		
		final UILabel titlelabel = new UILabel(name);
		titlelabel.setCenterX(false);
		
		final UIEntity titel = new UIEntity();
		titel.add(new UIScale(1.2f, 1.2f, 1));
		titel.add(titlelabel);
		titel.setInheritHeight(true);
		titel.setInheritWidth(true);
		
		final UIEntity header = new UIEntity();
		header.setInheritWidth(true);
		header.setHeight(20);
		header.add(new UIBox(UIBox.HBOX, 4));
		header.add(titel);
		header.add(GuiElements.createSpacerH(80));
		header.add(GuiElements.createButton(I18n.format("btn.settings"), e -> initSettings(e)));
		header.add(GuiElements.createButton(I18n.format("btn.edit"), e -> initEdit(e)));
		
		final UIEntity middlePart = new UIEntity();
		middlePart.setInheritHeight(true);
		middlePart.setInheritWidth(true);
		middlePart.add(new UIBox(UIBox.VBOX, 4));
		middlePart.add(header);
		middlePart.add(lowerEntity);
		
		lowerEntity.setInheritHeight(true);
		lowerEntity.setInheritWidth(true);
		initMain();
		
		this.entity.add(GuiElements.createSpacerH(10));
		this.entity.add(middlePart);
		this.entity.add(GuiElements.createSpacerH(10));
		this.entity.add(new UIBox(UIBox.HBOX, 1));
		
		this.entity.read(compound);
	}
	
	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		GuiSyncNetwork.sendToPosServer(compound, this.box.getPos());
	}
}
