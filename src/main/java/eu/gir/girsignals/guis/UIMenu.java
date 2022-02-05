package eu.gir.girsignals.guis;

import eu.gir.girsignals.guis.UISignalBoxTile.EnumMode;
import eu.gir.girsignals.guis.guilib.entitys.UIBorder;
import eu.gir.girsignals.guis.guilib.entitys.UIBox;
import eu.gir.girsignals.guis.guilib.entitys.UIColor;
import eu.gir.girsignals.guis.guilib.entitys.UIComponent;
import eu.gir.girsignals.guis.guilib.entitys.UIEntity;
import eu.gir.girsignals.guis.guilib.entitys.UIEntity.MouseEvent;

public class UIMenu extends UIComponent {
	
	private int mX, mY, selection = 0;
	
	@Override
	public void draw(int mouseX, int mouseY) {
	}
	
	@Override
	public void update() {
	}
	
	@Override
	public void postDraw(int mouseX, int mouseY) {
		if (this.isVisible()) {
			final UIEntity selection = new UIEntity();
			selection.setX(mX);
			selection.setY(mY);
			selection.setHeight(20);
			selection.setWidth(20 * EnumMode.values().length);
			selection.add(new UIBox(UIBox.HBOX, 1));
			for (EnumMode mode : EnumMode.values()) {
				final UIEntity preview = new UIEntity();
				preview.add(new UIColor(0xFFAFAFAF));
				preview.add(new UISignalBoxTile(mode));
				preview.setInheritHeight(true);
				preview.setInheritWidth(true);
				if (mode.ordinal() == this.selection)
					preview.add(new UIBorder(0xFF000000, 2));
				selection.add(preview);
			}
			selection.updateEvent(parent.getLastUpdateEvent());
			selection.draw(mouseX, mouseY);
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
			this.selection = Math.max(0, Math.min(EnumMode.values().length - 1, (int) ((event.x - this.mX) / 20.0f)));
			this.setVisible(true);
			break;
		case RELEASE:
			this.setVisible(false);
			break;
		default:
			break;
		}
	}
}
