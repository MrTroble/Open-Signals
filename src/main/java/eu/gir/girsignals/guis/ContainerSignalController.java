package eu.gir.girsignals.guis;

import eu.gir.girsignals.tileentitys.SignalControllerTileEntity;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ContainerSignalController extends Container {

	public final SignalControllerTileEntity entity;
	
	public ContainerSignalController(final SignalControllerTileEntity entity) {
		this.entity = entity;
	}
	
	@Override
	public void detectAndSendChanges() {
		ContainerFurnace
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void updateProgressBar(int id, int data) {
		entity.changeSignalImpl(id, data);
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return true;
	}

}
