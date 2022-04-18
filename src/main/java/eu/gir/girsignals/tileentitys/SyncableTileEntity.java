package eu.gir.girsignals.tileentitys;

import java.util.ArrayList;

import eu.gir.guilib.ecs.interfaces.UIClientSync;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SyncableTileEntity extends TileEntity {

    protected final ArrayList<UIClientSync> clientSyncs = new ArrayList<>();

    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(getPos(), 0, getUpdateTag());
    }

    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.getNbtCompound());
        final BlockPos pos = getPos();
        getWorld().markBlockRangeForRenderUpdate(pos, pos);
    }

    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    public void syncClient() {
        syncClient(getWorld(), getPos());
    }

    public void syncClient(World world, BlockPos pos) {
        final IBlockState state = world.getBlockState(pos);
        world.notifyBlockUpdate(pos, state, state, 3);
    }

    public boolean add(UIClientSync sync) {
        return this.clientSyncs.add(sync);
    }

    public boolean remove(UIClientSync sync) {
        return this.clientSyncs.remove(sync);
    }

}
