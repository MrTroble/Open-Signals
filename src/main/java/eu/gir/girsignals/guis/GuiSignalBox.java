package eu.gir.girsignals.guis;

import java.util.function.Consumer;

import eu.gir.girsignals.guis.UISignalBoxTile.EnumMode;
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
import eu.gir.girsignals.tileentitys.SignalBoxTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.SoundEvents;

public class GuiSignalBox extends GuiBase {
	
	private final UIEntity lowerEntity = new UIEntity();
	private final SignalBoxTileEntity box;
	
	public GuiSignalBox(SignalBoxTileEntity box) {
		this.box = box;
		init();
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
		box.forEach(p -> lowerEntity.add(GuiElements.createButton(p.toString(), e -> {})));
		lowerEntity.update();
		updateButton(entity);
	}
	
	private void editTile(UIEntity tile, UIMenu menu) {
		tile.setHeight(10);
		tile.setWidth(10);
		tile.add(new UIBorder(0xFF7F7F7F, 2));
		final UISignalBoxTile sbt = new UISignalBoxTile();
		tile.add(sbt);
		tile.add(new UIClickable(e -> {
			sbt.toggle(EnumMode.values()[menu.getSelection()]);
		}));
	}
	
	private void initEdit(UIEntity entity) {
		updateButton(entity);
		final UIMenu menu = new UIMenu();
		initMain(e -> this.editTile(e, menu));
		lowerEntity.add(menu);
	}

	private void initTile(UIEntity tile) {
		tile.setHeight(10);
		tile.setWidth(10);
		tile.add(new UIBorder(0xFF7F7F7F, 2));
		final UISignalBoxTile sbt = new UISignalBoxTile();
		tile.add(sbt);
	}
	
	private void initMain() {
		initMain(this::initTile);
	}

	private void initMain(Consumer<UIEntity> consumer) {
		lowerEntity.clear();
		lowerEntity.add(new UIColor(0xFF8B8B8B));
		lowerEntity.add(new UIStack());
		lowerEntity.add(new UIScissor());
		
		final UIEntity plane = new UIEntity();
		plane.setInheritWidth(true);
		plane.setInheritHeight(true);
		lowerEntity.add(new UIScroll(s -> {
			final float newScale = plane.getScaleX() + s * 0.001f;
			if(newScale <= 0)
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
				consumer.accept(tile);
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
