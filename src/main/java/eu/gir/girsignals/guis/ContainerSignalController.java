package eu.gir.girsignals.guis;

import java.util.Arrays;

import eu.gir.girsignals.tileentitys.SignalControllerTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ContainerSignalController extends Container {

	public final SignalControllerTileEntity entity;
	protected final int[] supportedSigTypes;
	protected final int[] supportedSigStates;
	protected boolean hasLink = false;
	protected int signalType;

	private GuiSignalController guiSig;

	public ContainerSignalController(final SignalControllerTileEntity entity) {
		this(entity, null);
	}

	public ContainerSignalController(final SignalControllerTileEntity entity, final GuiSignalController guiSig) {
		this.entity = entity;
		this.supportedSigTypes = this.entity.getSupportedSignalTypesImpl();
		this.signalType = this.entity.getSignalTypeImpl();
		if (this.supportedSigTypes == null) {
			this.supportedSigStates = null;
			return;
		}
		this.hasLink = true;
		this.supportedSigStates = new int[this.supportedSigTypes.length];
		Arrays.fill(supportedSigStates, -1);
		this.guiSig = guiSig;
	}

	private static final int LINK_MSG = -1, SIGNAL_TYPE_MSG = -2;

	@Override
	public void addListener(IContainerListener listener) {
		super.addListener(listener);
		if (supportedSigStates != null) {
			for (int i = 0; i < supportedSigStates.length; i++) {
				final int signalType = this.supportedSigTypes[i];
				final int signalState = this.entity.getSignalStateImpl(signalType);
				listener.sendWindowProperty(this, i, signalState);
			}
		}
		listener.sendWindowProperty(this, LINK_MSG, hasLink ? 1 : 0);
		listener.sendWindowProperty(this, SIGNAL_TYPE_MSG, signalType);
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
		if (id >= 0)
			this.supportedSigStates[id] = data;
		if (id == LINK_MSG) {
			this.hasLink = data != 0;
		} else if (id == SIGNAL_TYPE_MSG) {
			this.signalType = data;
		}
		this.guiSig.initGui();
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return true;
	}

}
