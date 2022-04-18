package eu.gir.girsignals.tileentitys;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;

import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.blocks.Signal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IWorldNameable;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SignalTileEnity extends SyncableTileEntity implements IWorldNameable {

    private HashMap<SEProperty<?>, Object> map = new HashMap<>();

    public static final String PROPERTIES = "properties";
    public static final String CUSTOMNAME = "customname";
    public static final String BLOCKID = "blockid";

    private String formatCustomName = null;
    private int blockID = -1;

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagCompound comp = new NBTTagCompound();
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
    public void readFromNBT(NBTTagCompound compound) {
        NBTTagCompound comp = compound.getCompoundTag(PROPERTIES);
        super.readFromNBT(compound);
        blockID = comp.getInteger(BLOCKID);
        if (world == null) {
            temporary = comp.copy();
        } else {
            read(comp);
        }
    }

    private void read(NBTTagCompound comp) {
        ((ExtendedBlockState) world.getBlockState(pos).getBlock().getBlockState())
                .getUnlistedProperties().stream().forEach(prop -> {
                    SEProperty<?> sep = SEProperty.cst(prop);
                    sep.readFromNBT(comp).ifPresent(obj -> map.put(sep, obj));
                });
        if (comp.hasKey(CUSTOMNAME))
            setCustomName(comp.getString(CUSTOMNAME));
        setBlockID();
    }

    @Override
    public void onLoad() {
        if (temporary != null) {
            read(temporary);
            if (!world.isRemote) {
                IBlockState state = world.getBlockState(pos);
                world.notifyBlockUpdate(pos, state, state, 3);
            }
            temporary = null;
        }
    }

    public <T extends Comparable<T>> void setProperty(SEProperty<T> prop, T opt) {
        map.put(prop, opt);
        this.markDirty();
    }

    public Map<SEProperty<?>, Object> getProperties() {
        return ImmutableMap.copyOf(map);
    }

    public Optional<?> getProperty(SEProperty<?> prop) {
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

    public void setCustomName(String str) {
        if (!getSignal().canHaveCustomname(this.map))
            return;
        this.formatCustomName = str;
        if (str == null && map.containsKey(Signal.CUSTOMNAME)) {
            map.remove(Signal.CUSTOMNAME);
        } else if (str != null) {
            map.put(Signal.CUSTOMNAME, true);
        }
        this.markDirty();
        world.markBlockRangeForRenderUpdate(pos, pos);

    }

    @SideOnly(Side.CLIENT)
    public void renderOverlay(final double x, final double y, final double z,
            final FontRenderer font) {
        getSignal().renderOverlay(x, y, z, this, font);
    }

    public void setBlockID() {
        blockID = ((Signal) world.getBlockState(pos).getBlock()).getID();
    }

    public Signal getSignal() {
        return (Signal) super.getBlockType();
    }

    public int getBlockID() {
        return blockID;
    }

}
