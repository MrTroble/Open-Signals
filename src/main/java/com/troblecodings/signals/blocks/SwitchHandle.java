package com.troblecodings.signals.blocks;

import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.EnumSignals.ColorWeight;
import com.troblecodings.signals.EnumSignals.WNMech;
import com.troblecodings.signals.enums.ChangeableStage;
import com.troblecodings.signals.init.OSItems;
import com.troblecodings.signals.tileentitys.SignalTileEnity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class SwitchHandle extends Signal {

    public SwitchHandle() {
        super(builder(OSItems.SIGN_PLACEMENT_TOOL, "switchhandle").height(0).build());
    }

    public static final SEProperty<Boolean> MANUAL = SEProperty.of("manual", false,
            ChangeableStage.GUISTAGE);
    public static final SEProperty<Boolean> POWERED = SEProperty.of("powered", false,
            ChangeableStage.APISTAGE_NONE_CONFIG, true, check(MANUAL, true));
    public static final SEProperty<Boolean> FALLBACK_SWITCH = SEProperty.of("fallback_switch",
            false, ChangeableStage.GUISTAGE);
    public static final SEProperty<ColorWeight> WEIGHTCOLOR = SEProperty.of("weightcolor",
            ColorWeight.WHITE_BLACK, ChangeableStage.GUISTAGE);
    public static final SEProperty<Boolean> SWITCHSIDE = SEProperty.of("switchside", false,
            ChangeableStage.GUISTAGE);
    public static final SEProperty<Boolean> SWITCHPLACESIDE = SEProperty.of("switchplaceside",
            false, ChangeableStage.GUISTAGE);
    public static final SEProperty<WNMech> WN_MECH = SEProperty.of("wn_mech", WNMech.WN1,
            ChangeableStage.APISTAGE_NONE_CONFIG, true, check(MANUAL, false));

    @Override
    public boolean onBlockActivated(final World worldIn, final BlockPos pos,
            final IBlockState state, final EntityPlayer playerIn, final EnumHand hand,
            final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {
        if (worldIn.isRemote) {
            return true;
        } else {
            final TileEntity tile = worldIn.getTileEntity(pos);
            if (!(tile instanceof SignalTileEnity))
                return false;
            final SignalTileEnity signalTE = (SignalTileEnity) tile;
            if (signalTE.getProperty(MANUAL).filter(manual -> (Boolean) manual).isPresent()) {
                signalTE.getProperty(POWERED)
                        .ifPresent(power -> signalTE.setProperty(POWERED, !(Boolean) power));
            }
        }
        worldIn.setBlockState(pos, state, 3);
        worldIn.notifyNeighborsOfStateChange(pos, this, false);
        worldIn.markAndNotifyBlock(pos, null, state, state, 3);
        return true;
    }

    @Override
    public int getWeakPower(final IBlockState blockState, final IBlockAccess blockAccess,
            final BlockPos pos, final EnumFacing side) {
        final TileEntity tile = blockAccess.getTileEntity(pos);
        if (!(tile instanceof SignalTileEnity))
            return 0;
        final SignalTileEnity signalTE = (SignalTileEnity) tile;
        return signalTE.getProperty(POWERED).filter(power -> (Boolean) power).isPresent()
                && signalTE.getProperty(MANUAL).filter(manual -> (Boolean) manual).isPresent() ? 15
                        : 0;
    }

    @Override
    public boolean canProvidePower(final IBlockState state) {
        return true;
    }
}