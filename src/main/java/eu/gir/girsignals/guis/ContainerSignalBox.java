package eu.gir.girsignals.guis;

import java.util.function.Consumer;

import eu.gir.girsignals.guis.guilib.UIClientSync;
import eu.gir.girsignals.signalbox.SignalBoxTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;

public class ContainerSignalBox extends Container implements UIClientSync {
	
	private EntityPlayerMP player;
	private SignalBoxTileEntity tile;
	private Consumer<NBTTagCompound> run;
	
	public ContainerSignalBox(SignalBoxTileEntity tile) {
		this.tile = tile;
		this.tile.add(this);
	}
	
	public ContainerSignalBox(Consumer<NBTTagCompound> run) {
		this.run = run;
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		if (playerIn instanceof EntityPlayerMP) {
			this.player = (EntityPlayerMP) playerIn;
			return true;
		}
		return true;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		this.run.accept(compound);
	}
	
	@Override
	public void onContainerClosed(EntityPlayer playerIn) {
		this.tile.remove(this);
	}
	
	@Override
	public EntityPlayerMP getPlayer() {
		return this.player;
	}
	
}
