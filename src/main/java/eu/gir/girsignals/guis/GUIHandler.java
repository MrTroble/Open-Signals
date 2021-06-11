package eu.gir.girsignals.guis;

import eu.gir.girsignals.tileentitys.SignalControllerTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GUIHandler implements IGuiHandler {

	public static final int GUI_PLACEMENTTOOL = 0;
	public static final int GUI_SIGNAL_CONTROLLER = 1;

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID == GUI_SIGNAL_CONTROLLER) {
			final TileEntity entity = world.getTileEntity(new BlockPos(x, y, z));
			if (entity instanceof SignalControllerTileEntity)
				return new ContainerSignalController((SignalControllerTileEntity) entity);
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		switch (ID) {
		case GUI_PLACEMENTTOOL:
			return new GuiPlacementtool(player.getHeldItemMainhand());
		case GUI_SIGNAL_CONTROLLER: {
			final TileEntity entity = world.getTileEntity(new BlockPos(x, y, z));
			if (entity instanceof SignalControllerTileEntity)
				return new GuiSignalController((SignalControllerTileEntity) entity);

		}
		default:
			return null;
		}
	}

}
