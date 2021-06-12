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
	private final int[] supportedSigTypes;
	private final int[] supportedSigStates;

	public ContainerSignalController(final SignalControllerTileEntity entity) {
		this.entity = entity;
		this.supportedSigTypes = this.entity.getSupportedSignalTypesImpl();
		this.supportedSigStates = new int[this.supportedSigTypes.length];
		Arrays.fill(supportedSigStates, -1);
	}

	@Override
	public void detectAndSendChanges() {
		for (int i = 0; i < supportedSigStates.length; i++) {
			final int signalType = this.supportedSigTypes[i];
			final int signalState = this.entity.getSignalStateImpl(signalType);
			if (this.supportedSigStates[i] != signalState) {
				for (final IContainerListener listener : listeners) {
					listener.sendWindowProperty(this, signalType, signalState);
				}
				this.supportedSigStates[i] = signalState;
			}
		}
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
