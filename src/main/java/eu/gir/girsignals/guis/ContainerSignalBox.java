package eu.gir.girsignals.guis;

import java.util.ArrayList;

import eu.gir.girsignals.guis.guilib.GuiSyncNetwork;
import eu.gir.girsignals.guis.guilib.UIClientSync;
import eu.gir.girsignals.tileentitys.SignalBoxTileEntity;
import eu.gir.girsignals.tileentitys.SignalNode;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class ContainerSignalBox extends Container implements UIClientSync {
	
	private EntityPlayer player;
	private boolean send = true;
	private SignalBoxTileEntity tile;
	private Runnable run;
	public final ArrayList<SignalNode> nodeList = new ArrayList<>();
	
	public ContainerSignalBox(SignalBoxTileEntity tile) {
		this.tile = tile;
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
	public void detectAndSendChanges() {
		if (this.player != null && send && tile.isUpdated() && !this.tile.getWays().isEmpty()) {
			send = false;
			final NBTTagList list = new NBTTagList();
			this.tile.getWays().get(0).forEach(n -> list.appendTag(n.writeNBT()));
			final NBTTagCompound comp = new NBTTagCompound();
			comp.setTag("list", list);
			GuiSyncNetwork.sendToClient(comp, (EntityPlayerMP) this.player);
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		this.nodeList.clear();
		final NBTTagList list = (NBTTagList) compound.getTag("list");
		if (list != null) {
			list.forEach(comp -> nodeList.add(new SignalNode((NBTTagCompound) comp)));
		}
		this.run.run();
	}
	
}
