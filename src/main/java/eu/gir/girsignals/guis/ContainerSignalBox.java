package eu.gir.girsignals.guis;

import java.util.ArrayList;

import eu.gir.girsignals.guis.guilib.UIClientSync;
import eu.gir.girsignals.signalbox.SignalBoxTileEntity;
import eu.gir.girsignals.signalbox.SignalNode;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class ContainerSignalBox extends Container implements UIClientSync {
	
	private EntityPlayerMP player;
	private SignalBoxTileEntity tile;
	private Runnable run;
	public final ArrayList<SignalNode> nodeList = new ArrayList<>();
	
	public ContainerSignalBox(SignalBoxTileEntity tile) {
		this.tile = tile;
		this.tile.add(this);
	}
	
	public ContainerSignalBox(Runnable run) {
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
		this.nodeList.clear();
		final NBTTagList list = (NBTTagList) compound.getTag(SignalBoxTileEntity.SIGNALS);
		if (list != null) {
			list.forEach(comp -> nodeList.add(new SignalNode((NBTTagCompound) comp)));
		}
		this.run.run();
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
