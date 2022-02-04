package eu.gir.girsignals.guis;

import eu.gir.girsignals.guis.guilib.GuiBase;
import eu.gir.girsignals.guis.guilib.GuiElements;
import eu.gir.girsignals.guis.guilib.entitys.UIBox;
import eu.gir.girsignals.guis.guilib.entitys.UIEntity;
import eu.gir.girsignals.guis.guilib.entitys.UILabel;
import eu.gir.girsignals.guis.guilib.entitys.UIScale;
import eu.gir.girsignals.tileentitys.SignalBoxTileEntity;
import net.minecraft.client.resources.I18n;

public class GuiSignalBox extends GuiBase {
	
	private final UIEntity lowerEntity = new UIEntity();
	private final SignalBoxTileEntity box;
	
	public GuiSignalBox(SignalBoxTileEntity box) {
		this.box = box;
		init();
	}
	
	private void initSettings() {
		lowerEntity.clear();
		lowerEntity.add(new UIBox(UIBox.VBoxMode.INSTANCE, 2));
		box.forEach(p -> lowerEntity.add(GuiElements.createButton(p.toString(), e -> {})));
		box.iterator().forEachRemaining(System.out::println);
	}
	
	private void initEdit() {
		lowerEntity.clear();
		
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
		header.add(new UIBox(UIBox.HBoxMode.INSTANCE, 4));
		header.add(titel);
		header.add(GuiElements.createSpacerH(80));		
		header.add(GuiElements.createButton(I18n.format("btn.settings"), e -> initSettings()));
		header.add(GuiElements.createButton(I18n.format("btn.edit"), e -> initEdit()));
		
		final UIEntity middlePart = new UIEntity();
		middlePart.setInheritHeight(true);
		middlePart.setInheritWidth(true);
		middlePart.add(new UIBox(UIBox.VBoxMode.INSTANCE, 4));
		middlePart.add(header);
		middlePart.add(lowerEntity);
		
		this.entity.add(GuiElements.createSpacerH(10));
		this.entity.add(middlePart);
		this.entity.add(GuiElements.createSpacerH(10));
		this.entity.add(new UIBox(UIBox.HBoxMode.INSTANCE, 1));
		
		this.entity.read(compound);
	}
	
}
