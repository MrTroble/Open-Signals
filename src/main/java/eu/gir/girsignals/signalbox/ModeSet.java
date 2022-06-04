package eu.gir.girsignals.signalbox;

import java.util.Objects;

import eu.gir.girsignals.signalbox.entrys.PathOptionEntry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;

public class ModeSet {

    private static final String MODE = "mode";
    private static final String ROTATION = "rotation";

    public final EnumGuiMode mode;
    public final Rotation rotation;

    public ModeSet(final EnumGuiMode mode, final Rotation rotation) {
        this.mode = Objects.requireNonNull(mode);
        this.rotation = Objects.requireNonNull(rotation);
    }

    public NBTTagCompound writeToNBT(final PathOptionEntry pEntry) {
        final NBTTagCompound entry = new NBTTagCompound();
        entry.setString(MODE, this.mode.name());
        entry.setString(ROTATION, this.rotation.name());
        pEntry.write(entry);
        return entry;
    }

    public static ModeSet readFromNBT(final PathOptionEntry pEntry, final NBTTagCompound tag) {
        final ModeSet mode = new ModeSet(EnumGuiMode.valueOf(tag.getString(MODE)),
                Rotation.valueOf(tag.getString(ROTATION)));
        pEntry.read(tag);
        return mode;
    }
}
