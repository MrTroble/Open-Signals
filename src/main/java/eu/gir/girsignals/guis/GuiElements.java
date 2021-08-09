package eu.gir.girsignals.guis;

import static eu.gir.girsignals.guis.GuiBase.BOTTOM_OFFSET;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;

import org.lwjgl.input.Keyboard;

import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.guis.DrawUtil.IntegerHolder;
import eu.gir.girsignals.guis.DrawUtil.SizeIntegerables;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.config.GuiCheckBox;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class GuiElements {

	private static final int STRING_COLOR = 14737632;

	public interface IIntegerable<T> {

		public T getObjFromID(int obj);

		public int count();

		public String getName();

		@SideOnly(Side.CLIENT)
		default public String getLocalizedName() {
			return I18n.format("property." + this.getName() + ".name");
		}

		@SideOnly(Side.CLIENT)
		default public String getNamedObj(int obj) {
			return getLocalizedName() + ": " + I18n.format("property.value." + getObjFromID(obj).toString());
		}

		@SideOnly(Side.CLIENT)
		default public String getDescription() {
			return I18n.format("property." + this.getName() + ".desc");
		}

		@SideOnly(Side.CLIENT)
		default public int getMaxWidth(final FontRenderer render) {
			int ret = 0;
			for (int i = 0; i < this.count(); i++) {
				final int newVal = render.getStringWidth(getNamedObj(i));
				if (ret < newVal)
					ret = newVal;
			}
			return ret;
		}

	}

	public static class GuiEnumerableSetting extends GuiButton {

		protected int value = 0;
		protected boolean pressed = false, lor = false, lock = true;
		protected Consumer<Integer> consumer;

		protected final IIntegerable<?> property;
		protected GuiButton leftButton;
		protected GuiButton rightButton;

		public static final int OFFSET = 2;
		public static final int BUTTON_SIZE = 20;

		public GuiEnumerableSetting(final IIntegerable<?> property, int initialValue,
				final Consumer<Integer> consumer) {
			super(-2212321, 0, 0, null);
			if (initialValue >= property.count())
				initialValue = 0;
			this.property = property;
			this.displayString = this.getValueString(initialValue);
			this.value = initialValue;
			this.width = 150;
			this.consumer = consumer;
		}

		public void update() {
			this.leftButton = new GuiButton(-130992398, x, y, "<");
			this.rightButton = new GuiButton(-130992398, x + width + BUTTON_SIZE + OFFSET * 2, y, ">");
			this.rightButton.setWidth(BUTTON_SIZE);
			this.leftButton.setWidth(BUTTON_SIZE);
			x += BUTTON_SIZE + OFFSET;
			if (this.value <= 0) {
				this.leftButton.enabled = false;
			}
			if (this.value >= property.count() - 1) {
				this.rightButton.enabled = false;
			}
		}

		public void setVisible(final boolean visible) {
			this.visible = visible;
		}

		public int getValue() {
			return this.value;
		}

		public String getValueString(int id) {
			return this.property.getNamedObj(id);
		}

		public void drawSelection(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
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
					if (this.value == property.count() - 1)
						this.rightButton.enabled = false;
					if (!this.leftButton.enabled)
						this.leftButton.enabled = true;
				}
				consumer.accept(this.value);
				this.displayString = getValueString(this.value);
			}
		}

		@Override
		public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
			if (visible) {
				super.drawButton(mc, mouseX, mouseY, partialTicks);
				drawSelection(mc, mouseX, mouseY, partialTicks);
			}
		}

		public void updatePos(final int x, final int y) {
			this.x = x;
			this.y = y;
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

		public boolean drawHoverText(final int mouseX, final int mouseY, final FontRenderer font) {
			if (!this.isMouseOver())
				return false;
			final String str = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) ? this.property.getDescription()
					: I18n.format("gui.keyprompt");
			GuiUtils.drawHoveringText(Arrays.asList(str.split(System.lineSeparator())), mouseX, mouseY, width, height,
					-1, font);
			return true;
		}

		public int getMaxWidth(FontRenderer render) {
			return property.getMaxWidth(render);
		}

		public boolean keyTyped(char typedChar, int keyCode) {
			return false;
		}
		
	}

	public static class GuiPageSelect extends GuiEnumerableSetting {

		protected final IntegerHolder holder;

		public GuiPageSelect(final ArrayList<ArrayList<GuiEnumerableSetting>> pageList) {
			super(new SizeIntegerables<String>("page", pageList.size(), idx -> null) {

				@Override
				public int count() {
					return pageList.size();
				}

				@Override
				public String getNamedObj(int obj) {
					return I18n.format("property." + this.getName() + ".name") + " " + (obj + 1) + "/"
							+ pageList.size();
				}
			}, 0, null);
			this.holder = new IntegerHolder(0);
			this.consumer = inp -> {
				pageList.get(holder.getObj()).forEach(t -> t.setVisible(false));
				pageList.get(inp).forEach(t -> t.setVisible(true));
				holder.setObj(inp);
			};
			final FontRenderer render = Minecraft.getMinecraft().fontRenderer;
			this.setWidth(this.property.getMaxWidth(render) + GuiEnumerableSetting.OFFSET * 2);
		}

		@Override
		public void update() {
			super.update();
			this.displayString = super.getValueString(this.value);
		}

		@Override
		public void updatePos(int x, int y) {
			super.updatePos(x, y - BOTTOM_OFFSET);
			this.x = x - (this.width / 2) + GuiEnumerableSetting.BUTTON_SIZE;
		}

		@Override
		public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
			if (visible) {
				this.drawCenteredString(mc.fontRenderer, this.displayString, this.x + this.width / 2,
						this.y + (this.height - 8) / 2, STRING_COLOR);
				drawSelection(mc, mouseX, mouseY, partialTicks);
			}
		}

	}

	public static class GuiSettingCheckBox extends GuiEnumerableSetting {

		private final GuiCheckBox checkBox;

		public GuiSettingCheckBox(IIntegerable<?> property, int initialValue, Consumer<Integer> consumer) {
			super(property, initialValue, consumer);
			this.checkBox = new GuiCheckBox(-34720, 0, 0, property.getLocalizedName(), initialValue == 1);
			this.height = this.checkBox.height;
		}

		@Override
		public int getMaxWidth(FontRenderer render) {
			return this.checkBox.width - (BUTTON_SIZE * 2 + OFFSET * 2);
		}

		@Override
		public void update() {
		}

		@Override
		public void updatePos(int x, int y) {
			this.checkBox.x = x;
			this.checkBox.y = y;
		}

		@Override
		public void setWidth(int width) {
			this.checkBox.width = width;
		}

		@Override
		public void setVisible(boolean visible) {
			this.checkBox.visible = visible;
		}

		@Override
		public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
			checkBox.drawButton(mc, mouseX, mouseY, partialTicks);
		}

		@Override
		public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
			boolean flag = checkBox.mousePressed(mc, mouseX, mouseY);
			if (flag)
				consumer.accept(checkBox.isChecked() ? 0 : 1);
			return flag;
		}

		public boolean isChecked() {
			return this.checkBox.isChecked();
		}

		public void setIsChecked(boolean isChecked) {
			this.checkBox.setIsChecked(isChecked);
		}

	}

	public static class GuiSettingTextbox extends GuiEnumerableSetting {

		private final GuiTextField textfield = new GuiTextField(0, Minecraft.getMinecraft().fontRenderer, 0, 0, 100, 20);

		public GuiSettingTextbox(String name, Consumer<Integer> consumer) {
			super(new SizeIntegerables<>("", 0, i -> ""), 0, consumer);
			if(name != null)
				textfield.setText(name);
		}
		
		public String getText() {
			return textfield.getText();
		}
		
		@Override
		public String getValueString(int id) {
			return "";
		}
		
		@Override
		public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
			textfield.drawTextBox();
		}
		
		@Override
		public void update() {
		}
		
		@Override
		public void updatePos(int x, int y) {
			textfield.x = x;
			textfield.y = y;
		}
		
		@Override
		public void setWidth(int width) {
			textfield.width = width;
		}
		
		@Override
		public void setVisible(boolean visible) {
			textfield.setVisible(visible);
		}
		
		@Override
		public boolean keyTyped(char typedChar, int keyCode) {
			boolean flag = textfield.textboxKeyTyped(typedChar, keyCode);
			if(flag)
				consumer.accept(0);
			return flag;
		}
		
		@Override
		public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
			return textfield.mouseClicked(mouseX, mouseY, 0);
		}

	}

	public static Optional<GuiEnumerableSetting> of(SEProperty<?> property, int initialValue,
			Consumer<Integer> consumer, ChangeableStage stage) {
		if (property == null)
			return Optional.empty();
		if (ChangeableStage.GUISTAGE == stage) {
			if (property.isChangabelAtStage(ChangeableStage.GUISTAGE)) {
				if (property.getType().equals(Boolean.class))
					return Optional.of(new GuiSettingCheckBox(property, initialValue, consumer));
				return Optional.of(new GuiEnumerableSetting(property, initialValue, consumer));
			} else if (property.isChangabelAtStage(ChangeableStage.APISTAGE)) {
				return Optional.of(new GuiSettingCheckBox(property, initialValue, consumer));
			}
		} else if (ChangeableStage.APISTAGE == stage) {
			if (property.isChangabelAtStage(ChangeableStage.GUISTAGE)
					|| property.isChangabelAtStage(ChangeableStage.APISTAGE_NONE_CONFIG)) {
				return Optional.of(new GuiEnumerableSetting(property, initialValue, consumer));
			}
		}
		return Optional.empty();
	}

}