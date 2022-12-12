package com.troblecodings.signals.blocks;

import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.init.OSItems;
import com.troblecodings.signals.init.OSTabs;
import com.troblecodings.signals.tileentitys.SignalControllerTileEntity;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.EnumHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public class SignalController extends Block implements ITileEntityProvider {

    public SignalController() {
        super(Material.ROCK);
        setCreativeTab(OSTabs.TAB);
    }

    @Override
    public boolean onBlockActivated(final Level worldIn, final BlockPos pos,
            final BlockState state, final Player playerIn, final EnumHand hand,
            final Direction facing, final float hitX, final float hitY, final float hitZ) {
        if (!playerIn.getHeldItemMainhand().getItem().equals(OSItems.LINKING_TOOL)) {
            if (worldIn.isRemote)
                return true;
            OpenSignalsMain.handler.invokeGui(SignalController.class, playerIn, worldIn, pos);
            return true;
        }
        return false;
    }

    @Override
    public TileEntity createNewTileEntity(final Level worldIn, final int meta) {
        return new SignalControllerTileEntity();
    }

    @Override
    public void neighborChanged(final BlockState state, final Level world, final BlockPos pos,
            final Block blockIn, final BlockPos fromPos) {
        final TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof SignalControllerTileEntity) {
            ((SignalControllerTileEntity) tile).redstoneUpdate();
        }
    }
}
