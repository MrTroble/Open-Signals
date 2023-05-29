package com.troblecodings.signals.blocks;

import java.util.Optional;

import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.core.PosIdentifier;
import com.troblecodings.signals.core.TileEntitySupplierWrapper;
import com.troblecodings.signals.handler.SignalBoxHandler;
import com.troblecodings.signals.init.OSItems;
import com.troblecodings.signals.signalbox.SignalBoxTileEntity;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class SignalBox extends BasicBlock {

    public static final TileEntitySupplierWrapper SUPPLIER = SignalBoxTileEntity::new;

    public SignalBox() {
        super(Material.ROCK);
    }

    @Override
    public boolean onBlockActivated(final World worldIn, final BlockPos pos,
            final IBlockState state, final EntityPlayer playerIn, final EnumHand hand,
            final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {
        if (!playerIn.getHeldItemMainhand().getItem().equals(OSItems.LINKING_TOOL)) {
            if (worldIn.isRemote)
                return true;
            final TileEntity entity = worldIn.getTileEntity(pos);
            if ((entity instanceof SignalBoxTileEntity)
                    && !((SignalBoxTileEntity) entity).isBlocked()) {
                OpenSignalsMain.handler.invokeGui(SignalBox.class, playerIn, worldIn, pos,
                        "signalbox");
            } else {
                playerIn.sendStatusMessage(new TextComponentTranslation("msg.isblocked"), true);
            }
            return true;
        }
        return false;
    }

    @Override
    public Optional<TileEntitySupplierWrapper> getSupplierWrapper() {
        return Optional.of(SUPPLIER);
    }

    @Override
    public Optional<String> getSupplierWrapperName() {
        return Optional.of("signalbox");
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        if (!worldIn.isRemote) {
            // TODO ((SignalBoxTileEntity) worldIn.getTileEntity(pos)).unlink();
            SignalBoxHandler.removeSignalBox(new PosIdentifier(pos, worldIn));
        }
        super.breakBlock(worldIn, pos, state);
    }
}