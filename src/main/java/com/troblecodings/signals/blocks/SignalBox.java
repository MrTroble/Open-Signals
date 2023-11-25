package com.troblecodings.signals.blocks;

import java.util.Optional;

import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.core.StateInfo;
import com.troblecodings.signals.core.TileEntitySupplierWrapper;
import com.troblecodings.signals.handler.SignalBoxHandler;
import com.troblecodings.signals.init.OSItems;
import com.troblecodings.signals.signalbox.SignalBoxTileEntity;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
        final Item item = playerIn.getHeldItemMainhand().getItem();
        if (!(item.equals(OSItems.LINKING_TOOL) || item.equals(OSItems.MULTI_LINKING_TOOL))) {
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
    public void onBlockPlacedBy(final World worldIn, final BlockPos pos, final IBlockState state,
            final EntityLivingBase placer, final ItemStack stack) {
        SignalBoxHandler.relinkAllRedstoneIOs(new StateInfo(worldIn, pos));
    }

    @Override
    public void breakBlock(final World worldIn, final BlockPos pos, final IBlockState state) {
        if (!worldIn.isRemote) {
            ((SignalBoxTileEntity) worldIn.getTileEntity(pos)).unlink();
            SignalBoxHandler.removeSignalBox(new StateInfo(worldIn, pos));
            SignalBoxHandler.onPosRemove(new StateInfo(worldIn, pos));
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public TileEntity createNewTileEntity(final World worldIn, final int meta) {
        return new SignalBoxTileEntity();
    }
}