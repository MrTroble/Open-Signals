package eu.gir.girsignals.models.parser;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import eu.gir.girsignals.blocks.Signal;
import net.minecraftforge.common.property.IUnlistedProperty;

@SuppressWarnings("rawtypes")
public class FunctionParsingInfo {

	private static final HashMap<Class, Function<ParameterInfo, Object>> paramaterParser = new HashMap<>();
	private static final HashMap<EntryPack, IUnlistedProperty> propertyCache = new HashMap<>();
	private static final HashMap<EntryPack, ValuePack> predicateCache = new HashMap<>();

	static {
		paramaterParser.put(IUnlistedProperty.class, FunctionParsingInfo::getProperty);
		paramaterParser.put(ValuePack.class, FunctionParsingInfo::getPredicate);
	}

	public final ParameterInfo info;
	
	public FunctionParsingInfo(final Signal signalSystem) {
		info = new ParameterInfo("", signalSystem);
	}

	public Object[] getParameter(final Class[] parameter, final String[] arguments) {
		final Object[] parameters = new Object[arguments.length];
		for (int i = 0; i < arguments.length; i++) {
			info.argument = arguments[i];
			parameters[i] = paramaterParser.get(parameter[i]).apply(info);
		}
		return parameters;
	}

	public static Object getProperty(final ParameterInfo info) {
		final IUnlistedProperty property = propertyCache.computeIfAbsent(new EntryPack(info.argument, info.system),
				_u -> {
					final List<IUnlistedProperty> properties = info.system.getProperties();
					return properties.stream().filter(noneCache -> noneCache.getName().equals(info.argument)).findAny()
							.orElse(null);
				});
		if (property == null)
			throw new LogicalParserException(String.format("Could not find property=%s in system=%S!", info.argument,
					info.system.getSignalTypeName()));
		return property;
	}

	@SuppressWarnings("unchecked")
	public static Object getPredicate(final ParameterInfo info) {
		final ValuePack predicate = predicateCache.computeIfAbsent(new EntryPack(info.argument, info.system), _u -> {
			final String[] parts = info.argument.split("\\.");
			if (parts.length != 2)
				throw new LogicalParserException(String.format(
						"Syntax error predicate need to have the form PROPERTY.NAME but was %s", info.argument));
			final IUnlistedProperty property = (IUnlistedProperty) getProperty(new ParameterInfo(parts[0], info));
			final Class clazz = property.getType();
			if (!clazz.isEnum())
				throw new LogicalParserException(String.format("Property=%s is not a enum property but must be",
						info.argument, info.system.getSignalTypeName()));
			try {
				final Object value = Enum.valueOf(clazz, parts[1]);
				return new ValuePack(property, ext -> ext.equals(value));
			} catch (final IllegalArgumentException e) {
				throw new LogicalParserException(e);
			}
		});
		if (predicate == null)
			throw new LogicalParserException(String.format("Could not make predicate=%s with system=%S!", info.argument,
					info.system.getSignalTypeName()));
		return predicate;
	}

	private static class EntryPack {
		private final String name;
		private final String signal;

		public EntryPack(final String name, final Signal signal) {
			this.name = name;
			this.signal = signal.getSignalTypeName();
		}

		@Override
		public int hashCode() {
			return Objects.hash(name, signal);
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final EntryPack other = (EntryPack) obj;
			return Objects.equals(name, other.name) && Objects.equals(signal, other.signal);
		}

	}

}
