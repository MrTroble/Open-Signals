package eu.gir.girsignals.guis;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import eu.gir.girsignals.guis.GuiSignalController.EnumMode;
import eu.gir.girsignals.tileentitys.SignalControllerTileEntity;
import eu.gir.girsignals.tileentitys.SignalControllerTileEntity.EnumRedstoneMode;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ContainerSignalController extends Container {

	public final SignalControllerTileEntity entity;
	protected int[] supportedSigTypes;
	protected int[] supportedSigStates;
	protected boolean hasLink = false;
	protected int signalType;
	protected EnumRedstoneMode rsMode;
	private final ArrayList<Map.Entry<Integer, Integer>> stateCacheList = new ArrayList<>();
	private final ArrayList<Map.Entry<Integer, Integer>> typeCacheList = new ArrayList<>();
	protected int[] facingRedstoneModes;
	protected EnumFacing faceUsed = EnumFacing.DOWN;
	protected EnumMode indexMode = EnumMode.MANUELL;
	protected int indexCurrentlyUsed = 0;

	private final GuiSignalController guiSig;

	public ContainerSignalController(final SignalControllerTileEntity entity) {
		this(entity, null);
	}

	public ContainerSignalController(final SignalControllerTileEntity entity, final GuiSignalController guiSig) {
		this.entity = entity;
		this.supportedSigTypes = this.entity.getSupportedSignalTypesImpl();
		this.facingRedstoneModes = this.entity.getFacingData();
		this.guiSig = guiSig;
		if (this.supportedSigTypes == null) {
			this.supportedSigStates = null;
			return;
		}
		this.signalType = entity.getSignalTypeImpl();
		this.hasLink = true;
		this.supportedSigStates = new int[this.supportedSigTypes.length];
		Arrays.fill(supportedSigStates, -1);
	}

	private static final int LINK_MSG = -1, SIGNAL_TYPE_MSG = -2, UPDATE_ARRAY = -3, TYPE_OFFSET = 4096,
			RS_MODE_MSG = -4, RS_MODES_OFFSET = 8192, UI_MODE = -5, UI_FACE = -6, UI_INDEX = -7;

	@Override
	public void addListener(IContainerListener listener) {
		super.addListener(listener);
		if(this.supportedSigTypes != null)
			listener.sendWindowProperty(this, UPDATE_ARRAY, this.supportedSigTypes.length);
		if (supportedSigStates != null) {
			for (int i = 0; i < supportedSigStates.length; i++) {
				final int signalType = this.supportedSigTypes[i];
				final int signalState = this.entity.getSignalStateImpl(signalType);
				listener.sendWindowProperty(this, i, signalState);
			}
		}
		if (supportedSigTypes != null) {
			for (int i = 0; i < supportedSigTypes.length; i++) {
				final int signalType = this.supportedSigTypes[i];
				listener.sendWindowProperty(this, i + TYPE_OFFSET, signalType);
			}
		}
		for (int i = 0; i < facingRedstoneModes.length; i++) {
			listener.sendWindowProperty(this, i + RS_MODES_OFFSET, facingRedstoneModes[i]);
		}
		listener.sendWindowProperty(this, LINK_MSG, hasLink ? 1 : 0);
		listener.sendWindowProperty(this, SIGNAL_TYPE_MSG, signalType);
		listener.sendWindowProperty(this, RS_MODE_MSG, this.entity.getRsMode().ordinal());

		listener.sendWindowProperty(this, UI_MODE, this.entity.mode.ordinal());
		listener.sendWindowProperty(this, UI_FACE, this.entity.face.ordinal());
		listener.sendWindowProperty(this, UI_INDEX, this.entity.indexUsed);
	}

	@Override
	public void detectAndSendChanges() {
		final boolean newLink = this.entity.getSupportedSignalTypesImpl() != null;
		if (newLink != hasLink) {
			hasLink = newLink;
			for (final IContainerListener listener : listeners) {
				listener.sendWindowProperty(this, LINK_MSG, hasLink ? 1 : 0);
			}
		}
		if (!hasLink)
			return;

		for (final IContainerListener listener : listeners) {
			final EnumRedstoneMode nmode = this.entity.getRsMode();
			if (this.rsMode != nmode) {
				this.rsMode = nmode;
				listener.sendWindowProperty(this, RS_MODE_MSG, this.rsMode.ordinal());
			}
		}

		for (int i = 0; i < supportedSigStates.length; i++) {
			final int signalType = this.supportedSigTypes[i];
			final int signalState = this.entity.getSignalStateImpl(signalType);
			if (this.supportedSigStates[i] != signalState) {
				for (final IContainerListener listener : listeners) {
					listener.sendWindowProperty(this, i, signalState);
				}
				this.supportedSigStates[i] = signalState;
			}
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void updateProgressBar(int id, int data) {
		if (id >= 0 && id < 4096) {
			if (this.supportedSigStates != null) {
				this.supportedSigStates[id] = data;
			} else {
				stateCacheList.add(new AbstractMap.SimpleEntry<>(id, data));
			}
		} else if (id >= TYPE_OFFSET && id < RS_MODES_OFFSET) {
			if (this.supportedSigTypes != null) {
				this.supportedSigTypes[id - TYPE_OFFSET] = data;
			} else {
				typeCacheList.add(new AbstractMap.SimpleEntry<>(id - TYPE_OFFSET, data));
			}
		} else if (id >= RS_MODES_OFFSET) {
			facingRedstoneModes[id - RS_MODES_OFFSET] = data;
		} else {
			switch (id) {
			case LINK_MSG:
				this.hasLink = data != 0;
				break;
			case SIGNAL_TYPE_MSG:
				this.signalType = data;
				break;
			case UPDATE_ARRAY:
				this.supportedSigStates = new int[data];
				this.supportedSigTypes = new int[data];
				for (Map.Entry<Integer, Integer> entry : stateCacheList) {
					this.supportedSigStates[entry.getKey()] = entry.getValue();
				}
				for (Map.Entry<Integer, Integer> entry : typeCacheList) {
					this.supportedSigTypes[entry.getKey()] = entry.getValue();
				}
				stateCacheList.clear();
				typeCacheList.clear();
				break;
			case RS_MODE_MSG:
				this.rsMode = EnumRedstoneMode.values()[data];
				break;
			case UI_FACE:
				this.faceUsed = EnumFacing.values()[data];
				break;
			case UI_MODE:
				this.indexMode = EnumMode.values()[data];
				break;
			case UI_INDEX:
				this.indexCurrentlyUsed = data;
			}
		}
		this.guiSig.initGui();
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return true;
	}

}
