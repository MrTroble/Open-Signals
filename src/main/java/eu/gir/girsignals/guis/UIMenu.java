package eu.gir.girsignals.guis;

import eu.gir.girsignals.guis.guilib.entitys.UIBorder;
import eu.gir.girsignals.guis.guilib.entitys.UIBox;
import eu.gir.girsignals.guis.guilib.entitys.UIColor;
import eu.gir.girsignals.guis.guilib.entitys.UIComponent;
import eu.gir.girsignals.guis.guilib.entitys.UIEntity;
import eu.gir.girsignals.guis.guilib.entitys.UIEntity.KeyEvent;
import eu.gir.girsignals.guis.guilib.entitys.UIEntity.MouseEvent;
import eu.gir.girsignals.signalbox.SignalBoxUtil.EnumGUIMode;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.Rotation;

public class UIMenu extends UIComponent {
	
	private int mX, mY, selection = 0, rotation = 0;
	
	@Override
	public void draw(int mouseX, int mouseY) {
	}
	
	@Override
	public void update() {
	}
	
	@Override
	public void postDraw(int mouseX, int mouseY) {
		if (this.isVisible()) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 0, 5);
			final UIEntity selection = new UIEntity();
			selection.setX(mX);
			selection.setY(mY);
			selection.setHeight(20);
			selection.setWidth(22 * EnumGUIMode.values().length);
			selection.add(new UIBox(UIBox.HBOX, 2));
			for (EnumGUIMode mode : EnumGUIMode.values()) {
				final UIEntity preview = new UIEntity();
				preview.add(new UIColor(0xFFAFAFAF));
				final UISignalBoxTile sbt = new UISignalBoxTile();
				sbt.toggle(mode, Rotation.values()[this.rotation]);
				preview.add(sbt);
				preview.setHeight(20);
				preview.setWidth(20);
				if (mode.ordinal() == this.selection)
					preview.add(new UIBorder(0xFF00FF00, 4));
				selection.add(preview);
			}
			selection.updateEvent(parent.getLastUpdateEvent());
			selection.draw(mouseX, mouseY);
			GlStateManager.popMatrix();
		}
	}
	
	public int getSelection() {
		return selection;
	}

	@Override
	public void mouseEvent(MouseEvent event) {
		switch (event.state) {
		case CLICKED:
			if (event.key != 1)
				return;
			if (!this.isVisible()) {
				this.mX = event.x;
				this.mY = event.y;
			}
			this.selection = Math.max(0, Math.min(EnumGUIMode.values().length - 1, (int) ((event.x - this.mX) / 22.0f)));
			this.setVisible(true);
			break;
		case RELEASE:
			this.setVisible(false);
			break;
		default:
			break;
		}
	}
	
	@Override
	public void keyEvent(KeyEvent event) {
		super.keyEvent(event);
		if(event.typed == 'r') {
			this.rotation++;
			if(this.rotation >= Rotation.values().length)
				this.rotation = 0;
		}
	}
	

	public int getRotation() {
		return rotation;
	}
}
