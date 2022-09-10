package eu.gir.girsignals.models.parser;

import java.util.function.Function;
import java.util.function.Predicate;

import net.minecraftforge.common.property.IExtendedBlockState;

public class MethodInfo {

	public final String name;
	@SuppressWarnings("rawtypes")
	public final Class[] parameter;
	public final Function<Object[], Predicate<IExtendedBlockState>> blockState;

	@SuppressWarnings("rawtypes")
	public MethodInfo(final String name, final Function<Object[], Predicate<IExtendedBlockState>> blockState, final Class... parameter) {
		this.name = name;
		this.parameter = parameter;
		this.blockState = blockState;
	}

}
