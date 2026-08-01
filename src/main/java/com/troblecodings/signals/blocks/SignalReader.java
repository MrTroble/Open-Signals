package com.troblecodings.signals.blocks;

import java.util.Optional;

import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.core.TileEntitySupplierWrapper;
import com.troblecodings.signals.init.OSItems;
import com.troblecodings.signals.tileentitys.SignalReaderTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;

public class SignalReader extends BasicBlock {

    public static final TileEntitySupplierWrapper SUPPLIER = SignalReaderTileEntity::new;

    public SignalReader() {
        super(Properties.of(Material.STONE).noOcclusion());
    }

    @Override
    public boolean isSignalSource(final BlockState state) {
        return true;
    }

    @Override
    public int getSignal(final BlockState state, final BlockGetter getter, final BlockPos pos,
            final Direction direction) {
        return getDirectSignal(state, getter, pos, direction);
    }

    @Override
    public int getDirectSignal(final BlockState state, final BlockGetter getter, final BlockPos pos,
            final Direction direction) {
        final SignalReaderTileEntity tile = (SignalReaderTileEntity) getter.getBlockEntity(pos);
        return tile.enableRedstoneForDirection(direction.getOpposite()) ? 15 : 0;
    }

    @Override
    public InteractionResult use(final BlockState state, final Level worldIn, final BlockPos pos,
            final Player playerIn, final InteractionHand hand, final BlockHitResult hit) {
        final Item item = playerIn.getItemInHand(InteractionHand.MAIN_HAND).getItem();
        if (!(item.equals(OSItems.LINKING_TOOL) || item.equals(OSItems.MULTI_LINKING_TOOL))) {
            OpenSignalsMain.handler.invokeGui(SignalReader.class, playerIn, worldIn, pos,
                    "signalreader");
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    @Override
    public Optional<TileEntitySupplierWrapper> getSupplierWrapper() {
        return Optional.of(SUPPLIER);
    }

    @Override
    public Optional<String> getSupplierWrapperName() {
        return Optional.of("signalreader");
    }

}