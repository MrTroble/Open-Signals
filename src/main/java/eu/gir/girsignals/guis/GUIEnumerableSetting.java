package eu.gir.girsignals.guis;

import java.util.function.Consumer;

import eu.gir.girsignals.EnumSignals.IIntegerable;
import eu.gir.girsignals.guis.GuiPlacementtool.InternalUnlocalized;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;

public class GUIEnumerableSetting extends GuiButton implements InternalUnlocalized {

	protected int value = 0;
	protected boolean pressed = false, lor = false, lock = true;
	protected Consumer<Integer> consumer;

	protected final IIntegerable<?> property;
	protected final int max;
	protected final String buttonText;
	protected final String unlocalized;
	protected final GuiButton leftButton;
	protected final GuiButton rightButton;

	public static final int OFFSET = 2;
	public static final int BUTTON_SIZE = 20;

	public GUIEnumerableSetting(final IIntegerable<?> property, final int id, final int x, final int y, final int width,
			final String buttonText, final int initialValue, final Consumer<Integer> consumer) {
		super(id, x + BUTTON_SIZE + OFFSET, y, I18n.format("property." + buttonText + ".name"));
		this.buttonText = I18n.format("property." + buttonText + ".name");
		this.displayString = this.buttonText + ": " + property.getObjFromID(initialValue).toString();
		this.property = property;
		this.value = initialValue;
		this.max = property.count() - 1;
		this.width = width;
		this.consumer = consumer;
		this.unlocalized = buttonText;
		this.leftButton = new GuiButton(-130992398, x, y, "<");
		this.rightButton = new GuiButton(-130992398, x + width + BUTTON_SIZE + OFFSET * 2, y, ">");
		this.rightButton.setWidth(BUTTON_SIZE);
		this.leftButton.setWidth(BUTTON_SIZE);
		if (initialValue <= 0) {
			this.leftButton.enabled = false;
		}
		if (initialValue >= max) {
			this.rightButton.enabled = false;
		}
	}

	public int getValue() {
		return this.value;
	}

	private String getValueString(int id) {
		return buttonText + ": " + property.getObjFromID(id).toString();
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		super.drawButton(mc, mouseX, mouseY, partialTicks);
		if (visible) {
			this.rightButton.drawButton(mc, mouseX, mouseY, partialTicks);
			this.leftButton.drawButton(mc, mouseX, mouseY, partialTicks);
			if (lock && pressed) {
				lock = false;
				if (lor) {
					this.value--;
					if (this.value == 0)
						this.leftButton.enabled = false;
					if (!this.rightButton.enabled)
						this.rightButton.enabled = true;
				} else {
					this.value++;
					if (this.value == this.max)
						this.rightButton.enabled = false;
					if (!this.leftButton.enabled)
						this.leftButton.enabled = true;
				}
				consumer.accept(this.value);
				this.displayString = getValueString(this.value);
			}
		}
	}

	@Override
	public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
		if (this.visible && !pressed && this.enabled) {
			lor = this.leftButton.mousePressed(mc, mouseX, mouseY);
			pressed = lor || this.rightButton.mousePressed(mc, mouseX, mouseY);
			return pressed;
		}
		return false;
	}

	@Override
	public void mouseReleased(int mouseX, int mouseY) {
		lock = true;
		pressed = false;
	}

	@Override
	public String getUnlocalized() {
		return this.unlocalized;
	}

}
