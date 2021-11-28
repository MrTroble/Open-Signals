package eu.gir.girsignals.guis;

import eu.gir.girsignals.EnumSignals;
import eu.gir.girsignals.tileentitys.SignalControllerTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class GuiHandler implements IGuiHandler {

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID == EnumSignals.GUI_SIGNAL_CONTROLLER) {
			final TileEntity entity = world.getTileEntity(new BlockPos(x, y, z));
			if (entity instanceof SignalControllerTileEntity)
				return new ContainerSignalController((SignalControllerTileEntity) entity);
		}
		return null;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		switch (ID) {
		case EnumSignals.GUI_PLACEMENTTOOL:
			return new GuiPlacementtool(player.getHeldItemMainhand());
		case EnumSignals.GUI_SIGNAL_CONTROLLER: {
			final TileEntity entity = world.getTileEntity(new BlockPos(x, y, z));
			if (entity instanceof SignalControllerTileEntity)
				return new GuiSignalController((SignalControllerTileEntity) entity);
		}
		default:
			return null;
		}
	}
	}
