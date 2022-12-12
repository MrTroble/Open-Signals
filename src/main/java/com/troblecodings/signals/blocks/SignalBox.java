package com.troblecodings.signals.blocks;

import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.init.OSItems;
import com.troblecodings.signals.init.OSTabs;
import com.troblecodings.signals.signalbox.SignalBoxTileEntity;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

public class SignalBox extends Block implements ITileEntityProvider {

    public SignalBox() {
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
            final TileEntity entity = worldIn.getTileEntity(pos);
            if ((entity instanceof SignalBoxTileEntity)
                    && !((SignalBoxTileEntity) entity).isBlocked()) {
                OpenSignalsMain.handler.invokeGui(SignalBox.class, playerIn, worldIn, pos);
            } else {
                playerIn.sendStatusMessage(new TextComponentTranslation("msg.isblocked"), true);
            }
            return true;
        }
        return false;
    }

    @Override
    public TileEntity createNewTileEntity(final Level worldIn, final int meta) {
        return new SignalBoxTileEntity();
    }

}
