package eu.gir.girsignals.models;

import java.util.Objects;
import java.util.function.Predicate;

import net.minecraftforge.common.property.IExtendedBlockState;

public class ImplAutoBlockstatePredicate implements Predicate<IExtendedBlockState> {

    private final int id;

    private static int COUNTER = 0;

    public ImplAutoBlockstatePredicate() {
        this.id = COUNTER++;
    }

    @Override
    public boolean test(IExtendedBlockState t) {
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ImplAutoBlockstatePredicate other = (ImplAutoBlockstatePredicate) obj;
        return id == other.id;
    }
}
