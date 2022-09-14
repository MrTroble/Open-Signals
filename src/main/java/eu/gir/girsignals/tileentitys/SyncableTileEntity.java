package eu.gir.girsignals.tileentitys;

import java.util.ArrayList;

import eu.gir.guilib.ecs.GuiSyncNetwork;
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

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        return new SPacketUpdateTileEntity(getPos(), 0, getUpdateTag());
    }

    @Override
    public void onDataPacket(final NetworkManager net, final SPacketUpdateTileEntity pkt) {
        this.handleUpdateTag(pkt.getNbtCompound());
    }

    @Override
    public void handleUpdateTag(final NBTTagCompound tag) {
        this.readFromNBT(tag);
        final BlockPos pos = getPos();
        getWorld().markBlockRangeForRenderUpdate(pos, pos);
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    public void syncClient() {
        syncClient(getWorld(), getPos());
    }

    public void syncClient(final World world, final BlockPos pos) {
        final IBlockState state = world.getBlockState(pos);
        world.notifyBlockUpdate(pos, state, state, 3);
        world.markBlockRangeForRenderUpdate(pos, pos);
    }

    public boolean add(final UIClientSync sync) {
        return this.clientSyncs.add(sync);
    }

    public boolean remove(final UIClientSync sync) {
        return this.clientSyncs.remove(sync);
    }

    public void sendToAll(final NBTTagCompound compound) {
        this.clientSyncs.forEach(sync -> GuiSyncNetwork.sendToClient(compound, sync.getPlayer()));
    }

}
