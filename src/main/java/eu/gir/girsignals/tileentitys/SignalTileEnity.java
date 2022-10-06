package eu.gir.girsignals.tileentitys;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;

import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.guilib.ecs.interfaces.ISyncable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IWorldNameable;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SignalTileEnity extends SyncableTileEntity implements IWorldNameable, ISyncable {

    private final HashMap<SEProperty<?>, Object> map = new HashMap<>();

    public static final String PROPERTIES = "properties";
    public static final String CUSTOMNAME = "customname";
    public static final String BLOCKID = "blockid";

    private String formatCustomName = null;
    private int blockID = -1;

    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound compound) {
        final NBTTagCompound comp = new NBTTagCompound();
        map.forEach((prop, in) -> prop.writeToNBT(comp, in));
        if (formatCustomName != null)
            comp.setString(CUSTOMNAME, formatCustomName);
        compound.setInteger(BLOCKID, blockID);
        compound.setTag(PROPERTIES, comp);
        super.writeToNBT(compound);
        return compound;
    }

    private NBTTagCompound temporary = null;

    @Override
    public void readFromNBT(final NBTTagCompound compound) {
        final NBTTagCompound comp = compound.getCompoundTag(PROPERTIES);
        super.readFromNBT(compound);
        blockID = comp.getInteger(BLOCKID);
        if (world == null) {
            temporary = comp.copy();
        } else {
            read(comp);
        }
    }

    private void read(final NBTTagCompound comp) {
        ((ExtendedBlockState) world.getBlockState(pos).getBlock().getBlockState())
                .getUnlistedProperties().stream().forEach(prop -> {
                    final SEProperty<?> sep = SEProperty.cst(prop);
                    sep.readFromNBT(comp).ifPresent(obj -> map.put(sep, obj));
                });
        setBlockID();
        if (comp.hasKey(CUSTOMNAME))
            setCustomName(comp.getString(CUSTOMNAME));
    }

    @Override
    public void onLoad() {
        if (temporary != null) {
            read(temporary);
            if (!world.isRemote) {
                final IBlockState state = world.getBlockState(pos);
                world.notifyBlockUpdate(pos, state, state, 3);
            }
            temporary = null;
        }
    }

    public <T extends Comparable<T>> void setProperty(final SEProperty<T> prop, final T opt) {
        map.put(prop, opt);
        this.markDirty();
    }

    public Map<SEProperty<?>, Object> getProperties() {
        return ImmutableMap.copyOf(map);
    }

    public Optional<?> getProperty(final SEProperty<?> prop) {
        if (map.containsKey(prop))
            return Optional.of(map.get(prop));
        return Optional.empty();
    }

    @Override
    public String getName() {
        if (formatCustomName == null)
            return getSignal().getSignalTypeName();
        return formatCustomName;
    }

    @Override
    public boolean hasCustomName() {
        return formatCustomName != null && getSignal().canHaveCustomname(this.map);
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentString(String.format(formatCustomName));
    }

    public void setCustomName(final String str) {
        this.formatCustomName = str;
        if (str == null && map.containsKey(Signal.CUSTOMNAME)) {
            map.remove(Signal.CUSTOMNAME);
        } else if (str != null) {
            map.put(Signal.CUSTOMNAME, true);
        }
        this.markDirty();
        this.syncClient();
    }

    @SideOnly(Side.CLIENT)
    public void renderOverlay(final double x, final double y, final double z,
            final FontRenderer font) {
        getSignal().renderOverlay(x, y, z, this, font);
    }

    public void setBlockID() {
        blockID = getSignal().getID();
    }

    public Signal getSignal() {
        return (Signal) super.getBlockType();
    }

    public int getBlockID() {
        return blockID;
    }

    @Override
    public void updateTag(final NBTTagCompound compound) {
        if (compound.hasKey(CUSTOMNAME)) {
            setCustomName(compound.getString(CUSTOMNAME));
            this.syncClient();
        }
    }

    @Override
    public NBTTagCompound getTag() {
        return null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(formatCustomName, map);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final SignalTileEnity other = (SignalTileEnity) obj;
        return Objects.equals(formatCustomName, other.formatCustomName)
                && Objects.equals(map, other.map);
    }

    @Override
    public boolean isValid(final EntityPlayer player) {
        return true;
    }
}
