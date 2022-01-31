package eu.gir.girsignals.guis;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.guis.guilib.GuiSyncNetwork;
import eu.gir.girsignals.guis.guilib.UIClientSync;
import eu.gir.girsignals.tileentitys.SignalControllerTileEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ContainerSignalController extends Container implements UIClientSync {
	
	private final AtomicReference<HashMap<SEProperty<?>, Object>> reference = new AtomicReference<>();
	private final AtomicReference<Signal> referenceBlock = new AtomicReference<>();
	private boolean send = false;
	private EntityPlayerMP player;
	private Runnable onUpdate;
	
	public final HashMap<String, SEProperty<?>> lookup = new HashMap<String, SEProperty<?>>();
	
	public ContainerSignalController(SignalControllerTileEntity tile) {
		tile.loadChunkAndGetTile((t, c) -> {
			reference.set(t.getProperties());
			final IBlockState state = c.getBlockState(t.getPos());
			referenceBlock.set((Signal) state.getBlock());
		});
	}
	
	public ContainerSignalController(Runnable onUpdate) {
		this.onUpdate = onUpdate;
	}
	
	private NBTTagCompound writeToNBT(NBTTagCompound compound) {
		final Signal state = getSignal();
		if (state != null) {
			compound.setInteger("state", state.getID());
			final NBTTagCompound comp = new NBTTagCompound();
			reference.get().forEach((p, o) -> comp.setInteger(p.getName(), SEProperty.getIDFromObj(o)));
			compound.setTag("list", comp);
		}
		return compound;
	}
	
	@Override
	public void detectAndSendChanges() {
		if (this.player != null && !send) {
			GuiSyncNetwork.sendToClient(writeToNBT(new NBTTagCompound()), this.player);
			send = true;
		}
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		referenceBlock.set(Signal.SIGNALLIST.get(compound.getInteger("state")));
		final ExtendedBlockState hVExtendedBlockState = (ExtendedBlockState) referenceBlock.get().getBlockState();
		hVExtendedBlockState.getUnlistedProperties().forEach(p -> lookup.put(p.getName(), (SEProperty<?>) p));
		
		final NBTTagCompound comp = (NBTTagCompound) compound.getTag("list");
		if (comp != null) {
			final HashMap<SEProperty<?>, Object> map = new HashMap<>();
			comp.getKeySet().forEach(str -> {
				@SuppressWarnings("rawtypes")
				final SEProperty property = lookup.get(str);
				map.put(property, property.getObjFromID(comp.getInteger(str)));
			});
			reference.set(map);
		}
		this.onUpdate.run();
	}
	
	public HashMap<SEProperty<?>, Object> getReference() {
		return reference.get();
	}
	
	public Signal getSignal() {
		return referenceBlock.get();
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		if (playerIn instanceof EntityPlayerMP) {
			this.player = (EntityPlayerMP) playerIn;
			return true;
		}
		return false;
	}
}
