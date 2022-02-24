package eu.gir.girsignals.guis;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableMap.Builder;

import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.signalbox.SignalBoxTileEntity;
import eu.gir.guilib.ecs.GuiSyncNetwork;
import eu.gir.guilib.ecs.interfaces.UIClientSync;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;

public class ContainerSignalBox extends Container implements UIClientSync {
	
	public final static String UPDATE_SET = "update";
	public final static String SIGNAL_ID = "signal";
	public final static String POS_ID = "posid";
	
	private final AtomicReference<Map<BlockPos, Signal>> properties = new AtomicReference<>();
	private EntityPlayerMP player;
	private SignalBoxTileEntity tile;
	private Consumer<NBTTagCompound> run;
	private boolean send = true;
	
	public ContainerSignalBox(SignalBoxTileEntity tile) {
		this.tile = tile;
		this.tile.add(this);
	}
	
	public ContainerSignalBox(Consumer<NBTTagCompound> run) {
		this.run = run;
	}
	
	@Override
	public void detectAndSendChanges() {
		if (this.player != null && send) {
			send = false;
			final NBTTagCompound compound = new NBTTagCompound();
			final NBTTagList typeList = new NBTTagList();
			this.tile.forEach(pos -> {
				final NBTTagCompound entry = new NBTTagCompound();
				entry.setTag(POS_ID, NBTUtil.createPosTag(pos));
				final Signal signal = tile.getSignal(pos);
				if (signal == null)
					return;
				entry.setInteger(SIGNAL_ID, signal.getID());
				typeList.appendTag(entry);
			});
			compound.setTag(UPDATE_SET, typeList);
			GuiSyncNetwork.sendToClient(compound, getPlayer());
		}
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		if (playerIn instanceof EntityPlayerMP && this.player == null) {
			this.player = (EntityPlayerMP) playerIn;
		}
		return true;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		if (compound.hasKey(UPDATE_SET)) {
			final NBTTagList update = (NBTTagList) compound.getTag(UPDATE_SET);
			final Builder<BlockPos, Signal> immutableMap = new Builder<>();
			update.forEach(nbt -> {
				final Signal signal = Signal.SIGNALLIST.get(compound.getInteger(SIGNAL_ID));
				final NBTTagCompound comp = (NBTTagCompound) nbt;
				final BlockPos pos = NBTUtil.getPosFromTag(comp.getCompoundTag(POS_ID));
				immutableMap.put(pos, signal);
			});
			properties.set(immutableMap.build());
			return;
		}
		this.run.accept(compound);
	}
	
	@Override
	public void onContainerClosed(EntityPlayer playerIn) {
		playerIn.openContainer = null;
		this.tile.remove(this);
	}
	
	@Override
	public EntityPlayerMP getPlayer() {
		return this.player;
	}
	
	public Map<BlockPos, Signal> getProperties() {
		return this.properties.get();
	}
	
}
