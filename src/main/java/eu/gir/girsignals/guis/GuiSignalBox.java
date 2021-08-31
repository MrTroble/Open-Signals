package eu.gir.girsignals.guis;

import eu.gir.girsignals.guis.guilib.GuiBase;
import net.minecraft.client.resources.I18n;

public class GuiSignalBox extends GuiBase {

	private String name;
	
	public GuiSignalBox() {
		this.name = I18n.format("tile.signalbox.name");
	}
	
	@Override
	public String getTitle() {
		return this.name;
	}
	
}
