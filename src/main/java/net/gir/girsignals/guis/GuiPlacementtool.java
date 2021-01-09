package net.gir.girsignals.guis;

import java.util.Collection;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.gir.girsignals.GirsignalsMain;
import net.gir.girsignals.init.GIRBlocks;
import net.gir.girsignals.init.GIRNetworkHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.client.config.GuiCheckBox;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

public class GuiPlacementtool extends GuiScreen {

	@SuppressWarnings({ "rawtypes" })
	private IUnlistedProperty[] properties;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		Tessellator tes = Tessellator.getInstance();
		BufferBuilder builder = tes.getBuffer();
		// TODO Block change
		IExtendedBlockState ebs =  (IExtendedBlockState) GIRBlocks.HV_SIGNAL.getDefaultState();
		int i = 0;
		for (GuiButton button : buttonList) {
			if(!(button instanceof GuiCheckBox)) continue;
			GuiCheckBox buttonCheckBox = (GuiCheckBox) button;
			if(buttonCheckBox.isChecked()) {
				IUnlistedProperty property = properties[i++];
				if(property.getType().isEnum())
					ebs.withProperty((IUnlistedProperty)property, property.getType().getEnumConstants()[0]);
				else
					ebs.withProperty((IUnlistedProperty)property, false);
			}
		}
		this.mc.getBlockRendererDispatcher().renderBlock(ebs, BlockPos.ORIGIN, this.mc.world, builder);
		tes.draw();
	}

	@Override
	public void initGui() {
		// TODO Block change
		ExtendedBlockState hVExtendedBlockState = (ExtendedBlockState) GIRBlocks.HV_SIGNAL.getBlockState();
		Collection<IUnlistedProperty<?>> unlistedProperties = hVExtendedBlockState.getUnlistedProperties();
		properties = unlistedProperties.toArray(new IUnlistedProperty[unlistedProperties.size()]);
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
			addButton(new GuiCheckBox((yPos / 20) * (xPos / 50), xPos, yPos, property.getName().toUpperCase(), false));
		}
	}
	
	
	
	@Override
	public void onGuiClosed() {
		ByteBuf buffer = Unpooled.buffer();
		buffer.writeInt(GIRBlocks.HV_SIGNAL.getID()); // TODO Automatisieren für alle Signale
		for (GuiButton button : buttonList) {
			if(!(button instanceof GuiCheckBox)) continue;
			GuiCheckBox buttonCheckBox = (GuiCheckBox) button;
			buffer.writeBoolean(buttonCheckBox.isChecked());
		}
		CPacketCustomPayload payload = new CPacketCustomPayload(GIRNetworkHandler.CHANNELNAME,
				new PacketBuffer(buffer));
		GirsignalsMain.PROXY.CHANNEL.sendToServer(new FMLProxyPacket(payload));
	}

}
