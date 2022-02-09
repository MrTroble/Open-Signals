package eu.gir.girsignals.guis;

import java.util.Map;
import java.util.function.Consumer;

import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.guis.guilib.GuiSyncNetwork;
import eu.gir.girsignals.guis.guilib.UIClientSync;
import eu.gir.girsignals.signalbox.SignalBoxTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;

public class ContainerSignalBox extends Container implements UIClientSync {
	
	public final static String UPDATE_SET = "update";
	
	private Map<BlockPos, Map<SEProperty<?>, Object>> properties;
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
		if (playerIn instanceof EntityPlayerMP && this.player != null) {
			this.player = (EntityPlayerMP) playerIn;
			final NBTTagCompound compound = new NBTTagCompound();
			final NBTTagCompound typeList = new NBTTagCompound();
			this.tile.forEach(pos -> this.tile.getProperties(pos).forEach((property, value) -> property.writeToNBT(typeList, value)));
			compound.setTag(UPDATE_SET, typeList);
			GuiSyncNetwork.sendToClient(compound, getPlayer());
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
