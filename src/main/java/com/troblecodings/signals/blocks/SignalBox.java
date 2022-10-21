package com.troblecodings.signals.blocks;

import com.troblecodings.signals.init.SignaIItems;
import com.troblecodings.signals.init.SignalTabs;
import com.troblecodings.signals.signalbox.SignalBoxTileEntity;

import eu.gir.guilib.ecs.GuiHandler;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class SignalBox extends Block implements ITileEntityProvider {

    public SignalBox() {
        super(Material.ROCK);
        setCreativeTab(SignalTabs.TAB);
    }

    @Override
    public boolean onBlockActivated(final World worldIn, final BlockPos pos,
            final IBlockState state, final EntityPlayer playerIn, final EnumHand hand,
            final EnumFacing facing, final float hitX, final float hitY, final float hitZ) {
        if (!playerIn.getHeldItemMainhand().getItem().equals(SignaIItems.LINKING_TOOL)) {
            if (worldIn.isRemote)
                return true;
            final TileEntity entity = worldIn.getTileEntity(pos);
            if ((entity instanceof SignalBoxTileEntity)
                    && !((SignalBoxTileEntity) entity).isBlocked()) {
                GuiHandler.invokeGui(SignalBox.class, playerIn, worldIn, pos);
            } else {
                playerIn.sendStatusMessage(new TextComponentTranslation("msg.isblocked"), true);
            }
            return true;
        }
        return false;
    }

    @Override
    public TileEntity createNewTileEntity(final World worldIn, final int meta) {
        return new SignalBoxTileEntity();
    }

}
