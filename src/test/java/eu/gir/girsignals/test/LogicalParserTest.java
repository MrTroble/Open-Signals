package eu.gir.girsignals.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import eu.gir.girsignals.EnumSignals.HP;
import eu.gir.girsignals.EnumSignals.HPType;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.blocks.signals.SignalHV;
import eu.gir.girsignals.init.GIRBlocks;
import eu.gir.girsignals.models.parser.FunctionParsingInfo;
import eu.gir.girsignals.models.parser.LogicParser;
import eu.gir.girsignals.models.parser.LogicalParserException;
import eu.gir.girsignals.models.parser.ParameterInfo;
import eu.gir.girsignals.models.parser.ValuePack;
import net.minecraft.init.Bootstrap;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class LogicalParserTest {

	@BeforeAll
	public static void setup() {
		Bootstrap.register();
	}

	@Test
	public void testFunctionParser() throws Exception {
		for (final Signal signal : List.of(GIRBlocks.HV_SIGNAL, GIRBlocks.SH_SIGNAL, GIRBlocks.KS_SIGNAL,
				GIRBlocks.LF_SIGNAL)) {
			final ParameterInfo info = new ParameterInfo("", signal);
			for (final IUnlistedProperty property : info.properties) {
				info.argument = property.getName();
				final Object object = FunctionParsingInfo.getProperty(info);
				assertEquals(property, object);
			}
		}
		final ParameterInfo hvSignalInfo = new ParameterInfo("", GIRBlocks.HV_SIGNAL);
		for (final SEProperty property : List.of(SignalHV.HPHOME, SignalHV.HPBLOCK, SignalHV.ZS3_PLATE)) {
			final Object def = property.getDefault();
			hvSignalInfo.argument = property.getName() + "." + def;
			final Object object = FunctionParsingInfo.getPredicate(hvSignalInfo);
			assertTrue(object instanceof ValuePack);
			final ValuePack pack = (ValuePack) object;
			assertEquals(pack.property, property);
			assertTrue(pack.predicate.test(def));
			assertFalse(pack.predicate.negate().test(def));
		}

		final FunctionParsingInfo parsingInfo = new FunctionParsingInfo(GIRBlocks.HV_SIGNAL);
		final Object[] parsed = parsingInfo.getParameter(
				new Class[] { ValuePack.class, IUnlistedProperty.class, IUnlistedProperty.class },
				new String[] { "stopsignal.HP0", "hptype", "stopsignal" });
		assertEquals(3, parsed.length);
		assertTrue(parsed[0] instanceof ValuePack);
		final ValuePack pack = (ValuePack) parsed[0];
		assertEquals(SignalHV.STOPSIGNAL, pack.property);
		assertTrue(pack.predicate.test(HP.HP0));
		assertFalse(pack.predicate.negate().test(HP.HP0));

		assertTrue(parsed[1] instanceof IUnlistedProperty);
		final IUnlistedProperty property = (IUnlistedProperty) parsed[1];
		assertEquals(SignalHV.HPTYPE, property);

		assertTrue(parsed[2] instanceof IUnlistedProperty);
		final IUnlistedProperty property2 = (IUnlistedProperty) parsed[2];
		assertEquals(SignalHV.STOPSIGNAL, property2);

		assertThrows(LogicalParserException.class,
				() -> FunctionParsingInfo.getProperty(new ParameterInfo("", GIRBlocks.HV_SIGNAL)));
		assertThrows(LogicalParserException.class,
				() -> FunctionParsingInfo.getProperty(new ParameterInfo("asdansdkjnkls", GIRBlocks.HV_SIGNAL)));
		assertThrows(LogicalParserException.class,
				() -> FunctionParsingInfo.getProperty(new ParameterInfo("stopsignal.HP0", GIRBlocks.HV_SIGNAL)));
		assertThrows(LogicalParserException.class,
				() -> FunctionParsingInfo.getPredicate(new ParameterInfo("asdansdkjnkls", GIRBlocks.HV_SIGNAL)));
		assertThrows(LogicalParserException.class,
				() -> FunctionParsingInfo.getPredicate(new ParameterInfo("stopsignal", GIRBlocks.HV_SIGNAL)));
		assertThrows(LogicalParserException.class,
				() -> FunctionParsingInfo.getPredicate(new ParameterInfo("asdasdasd.asdasd", GIRBlocks.HV_SIGNAL)));
		assertThrows(LogicalParserException.class,
				() -> FunctionParsingInfo.getPredicate(new ParameterInfo("stopsignal.asdasds", GIRBlocks.HV_SIGNAL)));
		assertThrows(LogicalParserException.class,
				() -> FunctionParsingInfo.getPredicate(new ParameterInfo("hptype.HP0", GIRBlocks.HV_SIGNAL)));
	}

	public void assertFunction(final String name, final FunctionParsingInfo info, final IUnlistedProperty property,
			final Object value, final String parsing) {
		final Predicate<IExtendedBlockState> statePredicate = LogicParser.nDegreeFunctionParser(name, info, parsing);
		final IExtendedBlockState state = new DummyBlockState(property, value);
		assertTrue(statePredicate.test(state));
		assertFalse(statePredicate.negate().test(state));
	}

	@Test
	public void testMethods() {
		final FunctionParsingInfo parsingInfo = new FunctionParsingInfo(GIRBlocks.HV_SIGNAL);
		assertFunction("with", parsingInfo, SignalHV.STOPSIGNAL, HP.HP0, "stopsignal.HP0");
		assertFunction("with", parsingInfo, SignalHV.STOPSIGNAL, HP.HP1, "stopsignal.HP1");
		assertFunction("with", parsingInfo, SignalHV.HPTYPE, HPType.STOPSIGNAL, "hptype.STOPSIGNAL");
		assertFunction("has", parsingInfo, SignalHV.HPTYPE, HPType.STOPSIGNAL, "hptype");
		assertFunction("has", parsingInfo, SignalHV.STOPSIGNAL, HP.HP0, "stopsignal");
		assertFunction("hasandis", parsingInfo, SignalHV.ZS1, true, "zs1");
		assertFunction("hasandisnot", parsingInfo, SignalHV.ZS7, false, "zs7");
		assertFunction("has", parsingInfo, SignalHV.ZS7, false, "zs7");
		
		assertThrows(LogicalParserException.class, () -> LogicParser.nDegreeFunctionParser("", parsingInfo, ""));
		assertThrows(LogicalParserException.class, () -> LogicParser.nDegreeFunctionParser("dasdasdasd", parsingInfo, ""));
		assertThrows(LogicalParserException.class, () -> LogicParser.nDegreeFunctionParser("with", parsingInfo, ""));
		assertThrows(LogicalParserException.class, () -> LogicParser.nDegreeFunctionParser("with", parsingInfo, "stopsignal.HP0", ""));
		assertThrows(LogicalParserException.class, () -> LogicParser.nDegreeFunctionParser("with", parsingInfo, ""));
		assertThrows(LogicalParserException.class, () -> LogicParser.nDegreeFunctionParser("has", parsingInfo, "stopsignal", ""));
		assertThrows(LogicalParserException.class, () -> LogicParser.nDegreeFunctionParser("has", parsingInfo, ""));
		assertThrows(LogicalParserException.class, () -> LogicParser.nDegreeFunctionParser("hasandis", parsingInfo, "stopsignal", ""));
		assertThrows(LogicalParserException.class, () -> LogicParser.nDegreeFunctionParser("hasandis", parsingInfo, ""));
		assertThrows(LogicalParserException.class, () -> LogicParser.nDegreeFunctionParser("hasandisnot", parsingInfo, "stopsignal", ""));
		assertThrows(LogicalParserException.class, () -> LogicParser.nDegreeFunctionParser("hasandisnot", parsingInfo, ""));
	}

}
