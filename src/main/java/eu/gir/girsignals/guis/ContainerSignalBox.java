package eu.gir.girsignals.guis;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableMap.Builder;

import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.guis.guilib.GuiSyncNetwork;
import eu.gir.girsignals.guis.guilib.UIClientSync;
import eu.gir.girsignals.signalbox.SignalBoxTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.property.ExtendedBlockState;

public class ContainerSignalBox extends Container implements UIClientSync {
	
	public final static String UPDATE_SET = "update";
	public final static String SIGNAL_ID = "signal";
	public final static String POS_ID = "posid";
	
	private final AtomicReference<Map<BlockPos, Map<SEProperty<?>, Object>>> properties = new AtomicReference<>();
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
				entry.setInteger(SIGNAL_ID, tile.getSignal(pos).getID());
				this.tile.getProperties(pos).forEach((property, value) -> property.writeToNBT(entry, value));
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
			final Builder<BlockPos, Map<SEProperty<?>, Object>> immutableMap = new Builder<>();
			update.forEach(nbt -> {
				final Signal signal = Signal.SIGNALLIST.get(compound.getInteger(SIGNAL_ID));
				final Map<String, SEProperty<?>> map = new HashMap<>();
				((ExtendedBlockState) signal.getBlockState()).getUnlistedProperties().stream().map(p -> SEProperty.cst(p)).forEach(se -> map.put(se.getName(), se));
				final NBTTagCompound comp = (NBTTagCompound) nbt;
				final BlockPos pos = NBTUtil.getPosFromTag(comp.getCompoundTag(POS_ID));
				final Builder<SEProperty<?>, Object> builder = new Builder<>();
				comp.getKeySet().forEach(name -> {
					if (!map.containsKey(name))
						return;
					final SEProperty<?> property = map.get(name);
					property.readFromNBT(comp).ifPresent(value -> builder.put(property, value));
				});
				immutableMap.put(pos, builder.build());
			});
			properties.set(immutableMap.build());
			return;
		}
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
	
	public Map<BlockPos, Map<SEProperty<?>, Object>> getProperties() {
		return this.properties.get();
	}
	
}
