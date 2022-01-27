package eu.gir.girsignals.guis;

import java.util.Arrays;

import eu.gir.girsignals.tileentitys.SignalControllerTileEntity;
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
	protected int[] guiStageSigStates;
	protected boolean hasLink = false;
	protected int signalType;
	protected int[] facingRedstoneModes;
	protected EnumFacing faceUsed = EnumFacing.DOWN;
	protected int indexCurrentlyUsed = 0;
	
	public ContainerSignalController(final SignalControllerTileEntity entity) {
		this.entity = entity;
		this.supportedSigTypes = this.entity.getSupportedSignalTypesImpl();
		this.facingRedstoneModes = this.entity.getFacingData().clone();
		if (this.supportedSigTypes == null) {
			this.supportedSigStates = null;
			return;
		}
		this.signalType = entity.getSignalTypeImpl();
		this.hasLink = true;
		this.supportedSigStates = new int[this.supportedSigTypes.length];
		Arrays.fill(supportedSigStates, -1);
	}
	
	@Override
	public void addListener(IContainerListener listener) {
		super.addListener(listener);
	}
	
	@Override
	public void detectAndSendChanges() {
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void updateProgressBar(int id, int data) {
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return true;
	}
	
}
