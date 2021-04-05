package eu.gir.girsignals.items;

import java.util.ArrayList;

import eu.gir.girsignals.EnumSignals.IIntegerable;
import eu.gir.girsignals.GirsignalsMain;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.guis.GUIHandler;
import eu.gir.girsignals.init.GIRBlocks;
import eu.gir.girsignals.init.GIRNetworkHandler;
import eu.gir.girsignals.init.GIRTabs;
import eu.gir.girsignals.tileentitys.SignalTileEnity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;

public class Placementtool extends Item implements IIntegerable<Signal> {

	private final ArrayList<Integer> signalids = new ArrayList<>();

	public Placementtool() {
		setCreativeTab(GIRTabs.tab);
	}

	public void addSignal(final Signal sig) {
		signalids.add(sig.getID());
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (player.isSneaking()) {
			if (!worldIn.isRemote)
				return EnumActionResult.SUCCESS;
			player.openGui(GirsignalsMain.MODID, GUIHandler.GUI_PLACEMENTTOOL, worldIn, pos.getX(), pos.getY(),
					pos.getZ());
			return EnumActionResult.SUCCESS;
		} else {
			final BlockPos setPosition = pos.offset(facing);
			if (!worldIn.isAirBlock(setPosition))
				return EnumActionResult.FAIL;

			NBTTagCompound compound = player.getHeldItemMainhand().getTagCompound();
			if (compound == null || !compound.hasKey(GIRNetworkHandler.BLOCK_TYPE_ID)) {
				player.sendMessage(new TextComponentTranslation("pt.itemnotset"));
				return EnumActionResult.FAIL;
			}
			Signal block = Signal.SIGNALLIST.get(compound.getInteger(GIRNetworkHandler.BLOCK_TYPE_ID));
			int height = block.getHeight(compound);
			BlockPos lastPos = setPosition;
			for (int i = 0; i < height; i++)
				if (!worldIn.isAirBlock(lastPos = lastPos.up()))
					return EnumActionResult.FAIL;

			worldIn.setBlockState(setPosition,
					block.getStateForPlacement(worldIn, lastPos, facing, hitX, hitY, hitZ, 0, player, hand));
			lastPos = setPosition;

			for (int i = 0; i < height; i++)
				worldIn.setBlockState(lastPos = lastPos.up(), GIRBlocks.GHOST_BLOCK.getDefaultState());
			SignalTileEnity sig = (SignalTileEnity) worldIn.getTileEntity(setPosition);
			ExtendedBlockState ebs = ((ExtendedBlockState) block.getBlockState());
			ebs.getUnlistedProperties().forEach(iup -> {
				if (!compound.hasKey(iup.getName()))
					return;
				SEProperty sep = SEProperty.cst(iup);
				if (sep.isChangabelAtStage(ChangeableStage.GUISTAGE)) {
					sig.setProperty(sep, sep.getObjFromID(compound.getInteger(iup.getName())));
				} else if ((sep.isChangabelAtStage(ChangeableStage.APISTAGE) && compound.getBoolean(iup.getName()))
						|| sep.isChangabelAtStage(ChangeableStage.APISTAGE_NONE_CONFIG)) {
					sig.setProperty(sep, sep.getDefault());
				}
			});
			String str = compound.getString(GIRNetworkHandler.SIGNAL_CUSTOMNAME);
			if (!str.isEmpty())
				sig.setCustomName(str);
			sig.setBlockID();
			worldIn.notifyBlockUpdate(setPosition, ebs.getBaseState(), ebs.getBaseState(), 3);
			return EnumActionResult.SUCCESS;
		}
	}

	@Override
	public Signal getObjFromID(int obj) {
		return Signal.SIGNALLIST.get(signalids.get(obj));
	}

	@Override
	public int count() {
		return signalids.size();
	}

}
