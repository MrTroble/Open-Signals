package eu.gir.girsignals.guis;

import static eu.gir.girsignals.signalbox.SignalBoxUtil.POINT1;
import static eu.gir.girsignals.signalbox.SignalBoxUtil.POINT2;
import static eu.gir.girsignals.signalbox.SignalBoxUtil.toNBT;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import org.lwjgl.util.Point;

import eu.gir.girsignals.guis.guilib.DrawUtil.DisableIntegerable;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.guis.guilib.GuiBase;
import eu.gir.girsignals.guis.guilib.GuiElements;
import eu.gir.girsignals.guis.guilib.GuiSyncNetwork;
import eu.gir.girsignals.guis.guilib.entitys.UIBorder;
import eu.gir.girsignals.guis.guilib.entitys.UIBox;
import eu.gir.girsignals.guis.guilib.entitys.UIClickable;
import eu.gir.girsignals.guis.guilib.entitys.UIColor;
import eu.gir.girsignals.guis.guilib.entitys.UIDrag;
import eu.gir.girsignals.guis.guilib.entitys.UIEntity;
import eu.gir.girsignals.guis.guilib.entitys.UIEnumerable;
import eu.gir.girsignals.guis.guilib.entitys.UILabel;
import eu.gir.girsignals.guis.guilib.entitys.UIScale;
import eu.gir.girsignals.guis.guilib.entitys.UIScissor;
import eu.gir.girsignals.guis.guilib.entitys.UIScroll;
import eu.gir.girsignals.guis.guilib.entitys.UIStack;
import eu.gir.girsignals.signalbox.PathOption;
import eu.gir.girsignals.signalbox.PathOption.EnumPathUsage;
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
import net.minecraft.util.math.BlockPos;

public class GuiSignalBox extends GuiBase {
	
	private static final int SELECTION_COLOR = 0x2900FF00;
	private static final int BACKGROUND_COLOR = 0xFF8B8B8B;
	
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
		this.entity.findRecursive(UIColor.class).stream().filter(color -> color.getColor() == SELECTION_COLOR).forEach(color -> color.getParent().remove(color));
	}
	
	private void modeInit(UIEntity parent, EnumGUIMode mode, Rotation rotation, PathOption option) {
		final String modeName = I18n.format("property." + mode.name());
		final String rotationName = I18n.format("property." + rotation.name() + ".rotation");
		final UIEntity entity = new UIEntity();
		entity.setInheritWidth(true);
		entity.setHeight(20);
		entity.add(new UIColor(BACKGROUND_COLOR));
		entity.add(new UIScale(1.1f, 1.1f, 1));
		final UILabel modeLabel = new UILabel(modeName + " - " + rotationName);
		modeLabel.setCenterX(false);
		entity.add(modeLabel);
		parent.add(entity);
		
		if (mode.equals(EnumGUIMode.CORNER) || mode.equals(EnumGUIMode.STRAIGHT)) {
			final EnumPathUsage path = option.getPathUsage();
			final UIEntity stateEntity = new UIEntity();
			stateEntity.setInheritWidth(true);
			stateEntity.setHeight(15);
			final String pathUsageName = I18n.format("property.status") + ": ";
			final String pathUsage = I18n.format("property." + path);
			stateEntity.add(new UILabel(pathUsageName + pathUsage));
			parent.add(stateEntity);
			if (path.equals(EnumPathUsage.SELECTED) && path.equals(EnumPathUsage.USED)) {
				parent.add(GuiElements.createButton("Reset", e -> option.setPathUsage(EnumPathUsage.FREE)));
			}
		}
		
		if (mode.ordinal() >= EnumGUIMode.HP.ordinal() || mode.equals(EnumGUIMode.CORNER)) {
			final DisableIntegerable<BlockPos> integerable = new DisableIntegerable<BlockPos>(this.box);
			
			IntConsumer consumer = e -> {
			};
			Optional<UIEntity> listOfPropertiesOptional = Optional.empty();
			if (mode.equals(EnumGUIMode.HP) || mode.equals(EnumGUIMode.VP) || mode.equals(EnumGUIMode.RS)) {
				final UIEntity listOfProperties = new UIEntity();
				listOfProperties.setInheritWidth(true);
				listOfProperties.setInheritHeight(true);
				listOfProperties.setMinHeight(100);
				listOfProperties.add(new UIBox(UIBox.VBOX, 2));
				consumer = e -> {
					listOfProperties.write(compound);
					listOfProperties.clearChildren();
					final BlockPos pos = integerable.getObjFromID(e);
					final Map<BlockPos, Map<SEProperty<?>, Object>> properties = this.container.getProperties();
					if (pos == null || properties == null)
						return;
					final Map<SEProperty<?>, Object> map = properties.get(pos);
					map.forEach((seproperty, value) -> {
						if (!seproperty.test(map.entrySet()))
							return;
						if (!seproperty.isChangabelAtStage(ChangeableStage.APISTAGE) && !seproperty.isChangabelAtStage(ChangeableStage.APISTAGE_NONE_CONFIG))
							return;
						final UIEntity element = GuiElements.createEnumElement(seproperty, _u -> {
						});
						element.findRecursive(UIEnumerable.class).forEach(enumerable -> {
							enumerable.setID(seproperty.getName() + "-" + pos.getX() + "-" + pos.getY() + "-" + pos.getZ());
						});
						listOfProperties.add(element);
					});
					listOfProperties.read(compound);
				};
				listOfPropertiesOptional = Optional.of(listOfProperties);
			}
			final UIEntity selection = GuiElements.createEnumElement(integerable, consumer);
			selection.findRecursive(UIEnumerable.class).forEach(en -> en.setMin(-1));
			parent.add(selection);
			listOfPropertiesOptional.ifPresent(parent::add);
		}
		
	}
	
	public void initTileConfig(final SignalNode node) {
		if (node.isEmpty())
			return;
		this.reset();
		final UIEntity list = new UIEntity();
		list.setInheritHeight(true);
		list.setInheritWidth(true);
		final UIBox box = new UIBox(UIBox.VBOX, 1);
		list.add(box);
		lowerEntity.add(new UIBox(UIBox.VBOX, 3));
		lowerEntity.add(new UIClickable(e -> initMain(this::initTile), 2));
		lowerEntity.add(list);
		node.forEach((e, opt) -> modeInit(list, e.getKey(), e.getValue(), opt));
		lowerEntity.add(GuiElements.createPageSelect(box));
		this.entity.read(compound);
	}
	
	private void updateButton(UIEntity button) {
		final SoundHandler handler = Minecraft.getMinecraft().getSoundHandler();
		button.findRecursive(UIClickable.class).forEach(c -> {
			final Consumer<UIEntity> old = c.getCallback();
			c.setCallback(e -> {
				initMain(this::initTile);
				c.setCallback(old);
				handler.playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			});
		});
	}
	
	private void initSettings(UIEntity entity) {
		this.reset();
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
		tile.add(new UIClickable(e -> initTileConfig(currentTile.getNode()), 1));
	}
	
	private void reset() {
		this.entity.write(compound);
		GuiSyncNetwork.sendToPosServer(compound, this.box.getPos());
		lowerEntity.clear();
	}
	
	private void initMain(BiConsumer<UIEntity, UISignalBoxTile> consumer) {
		reset();
		lowerEntity.add(new UIColor(BACKGROUND_COLOR));
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
		initMain(this::initTile);
		
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
