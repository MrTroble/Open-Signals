package net.gir.girsignals.guis;

import java.util.Collection;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.gir.girsignals.init.GIRBlocks;
import net.gir.girsignals.init.GIRNetworkHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.client.config.GuiCheckBox;

public class GuiPlacementtool extends GuiScreen {

	public GuiPlacementtool() {

	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void initGui() {

		ExtendedBlockState hVExtendedBlockState = (ExtendedBlockState) GIRBlocks.HV_SIGNAL.getBlockState();
		Collection<IUnlistedProperty<?>> unlistedProperties = hVExtendedBlockState.getUnlistedProperties();
		int maxWidth = 0;
		for (IUnlistedProperty<?> lenIUnlistedProperty : unlistedProperties) {
			int currentWidth = fontRenderer.getStringWidth(lenIUnlistedProperty.getName());
			if (currentWidth > maxWidth) {
				maxWidth = currentWidth;
			}
		}
		int yPos = 0;
		int xPos = 50;
		for (IUnlistedProperty<?> property : hVExtendedBlockState.getUnlistedProperties()) {
			yPos += 20;
			if (yPos >= 220) {
				xPos += maxWidth + 40;
				yPos = 20;
			}
			addButton(new GuiCheckBox(0, xPos, yPos, property.getName().toUpperCase(), false));
		}
	}

	@Override
	public void onGuiClosed() {

		/*
		 * NBTTagCompound tagCompound = new NBTTagCompound(); for (GuiButton button :
		 * buttonList) { GuiCheckBox buttonCheckBox = (GuiCheckBox) button;
		 * tagCompound.setBoolean(button.displayString, buttonCheckBox.isChecked()); }
		 */
		ByteBuf buffer = Unpooled.buffer();
		buffer.writeInt(GIRBlocks.HV_SIGNAL.getID()); // TODO Automatisieren für alle Signale
		for (GuiButton button : buttonList) {
			GuiCheckBox buttonCheckBox = (GuiCheckBox) button;
			buffer.writeBoolean(buttonCheckBox.isChecked());
		}
		SPacketCustomPayload payload = new SPacketCustomPayload(GIRNetworkHandler.CHANNELNAME,
				new PacketBuffer(buffer));
		mc.player.connection.sendPacket(payload);

	}

}
