package eu.gir.girsignals.models.parser;

import java.util.function.Predicate;

import eu.gir.girsignals.EnumSignals.DefaultName;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class PredicateHolder {

	public static <T extends Enum<?>> Predicate<IExtendedBlockState> has(final IUnlistedProperty<T> property) {
		return ebs -> ebs.getValue(property) != null;
	}
	
	public static <T extends Enum<?>> Predicate<IExtendedBlockState> hasNot(final IUnlistedProperty<T> property) {
		return ebs -> ebs.getValue(property) == null;
	}

	@SuppressWarnings("unchecked")
	public static Predicate<IExtendedBlockState> with(final ValuePack pack) {
		return with(pack.property, pack.predicate);
	}
	
	public static <T extends DefaultName<?>> Predicate<IExtendedBlockState> with(final IUnlistedProperty<T> property,
			final Predicate<T> t) {
		return bs -> {
			final T test = bs.getValue(property);
			return test != null && t.test(test);
		};
	}

	public static Predicate<IExtendedBlockState> hasAndIs(final IUnlistedProperty<Boolean> property) {
		return ebs -> {
			final Boolean bool = ebs.getValue(property);
			return bool != null && bool.booleanValue();
		};
	}

	public static Predicate<IExtendedBlockState> hasAndIsNot(final IUnlistedProperty<Boolean> property) {
		return ebs -> {
			final Boolean bool = ebs.getValue(property);
			return bool != null && !bool.booleanValue();
		};
	}

}
