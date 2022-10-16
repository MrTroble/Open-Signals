package eu.gir.girsignals.blocks;

import eu.gir.girsignals.EnumSignals.ColorWeight;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.init.GIRItems;
import eu.gir.girsignals.tileentitys.SignalTileEnity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class SwitchHandle extends Signal {
    
    private boolean isPowered = false;

    public SwitchHandle() {
        super(builder(GIRItems.SIGN_PLACEMENT_TOOL, "switchhandle").height(0).build());
    }

    public static final SEProperty<Boolean> POWERED = SEProperty.of("powered", false,
            ChangeableStage.APISTAGE_NONE_CONFIG);
    public static final SEProperty<Boolean> FALLBACK_SWITCH = SEProperty.of("fallback_switch",
            false, ChangeableStage.GUISTAGE);
    public static final SEProperty<ColorWeight> WEIGHTCOLOR = SEProperty.of("weightcolor",
            ColorWeight.WHITE_BLACK, ChangeableStage.GUISTAGE);
    public static final SEProperty<Boolean> SWITCHSIDE = SEProperty.of("switchside", false,
            ChangeableStage.GUISTAGE);
    public static final SEProperty<Boolean> SWITCHPLACESIDE = SEProperty.of("switchplaceside",
            false, ChangeableStage.GUISTAGE);
    
    public boolean checkPowered(final IBlockState state, final World world, final BlockPos pos) {
        final TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof SignalTileEnity))
            return false;
        final SignalTileEnity signalTE = (SignalTileEnity) tile;
        return signalTE.getProperty(POWERED).filter(blink -> (Boolean) blink).isPresent();
    }
    
    public void setPowered(final World world, final BlockPos pos) {
        final TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof SignalTileEnity))
            return;
        final SignalTileEnity signalTE = (SignalTileEnity) tile;
        signalTE.setProperty(POWERED, true);
        return;
    }
    
    public void setUnpowered(final World world, final BlockPos pos) {
        final TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof SignalTileEnity))
            return;
        final SignalTileEnity signalTE = (SignalTileEnity) tile;
        signalTE.setProperty(POWERED, false);
        return;
    }

    @Override
    public boolean onBlockActivated(final World worldIn, final BlockPos pos,
            final IBlockState state, final EntityPlayer playerIn, final EnumHand hand,
            final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {
        if (worldIn.isRemote) {
            return true;
        } else {
            if (!checkPowered(state, worldIn, pos)) {
                isPowered = true;
                setPowered(worldIn, pos);
            } else {
                isPowered = false;
                setUnpowered(worldIn, pos);
            }
            worldIn.setBlockState(pos, state, 3);
            worldIn.notifyNeighborsOfStateChange(pos, this, false);
            worldIn.markAndNotifyBlock(pos, null, state, state, 3);
            return true;
        }
    }

    @Override
    public int getWeakPower(final IBlockState blockState, final IBlockAccess blockAccess,
            final BlockPos pos, final EnumFacing side) {
        return isPowered ? 15 : 0;
    }

    @Override
    public boolean canProvidePower(final IBlockState state) {
        return true;
    }
}
