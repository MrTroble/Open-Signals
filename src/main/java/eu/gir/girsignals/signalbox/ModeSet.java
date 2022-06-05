package eu.gir.girsignals.signalbox;

import java.util.Objects;

import eu.gir.girsignals.enums.EnumGuiMode;
import eu.gir.girsignals.signalbox.entrys.ISaveable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;

public class ModeSet implements ISaveable {

    private static final String MODE = "mode";
    private static final String ROTATION = "rotation";

    public EnumGuiMode mode;
    public Rotation rotation;

    public ModeSet(final NBTTagCompound compound) {
        this.read(Objects.requireNonNull(compound));
    }

    public ModeSet(final EnumGuiMode mode, final Rotation rotation) {
        this.mode = Objects.requireNonNull(mode);
        this.rotation = Objects.requireNonNull(rotation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mode, rotation);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final ModeSet other = (ModeSet) obj;
        return mode == other.mode && rotation == other.rotation;
    }

    @Override
    public String toString() {
        return "ModeSet [mode=" + mode + ", rotation=" + rotation + "]";
    }

    @Override
    public void write(final NBTTagCompound tag) {
        tag.setString(MODE, this.mode.name());
        tag.setString(ROTATION, this.rotation.name());
    }

    @Override
    public void read(final NBTTagCompound tag) {
        this.mode = EnumGuiMode.valueOf(tag.getString(MODE));
        this.rotation = Rotation.valueOf(tag.getString(ROTATION));
    }

}
