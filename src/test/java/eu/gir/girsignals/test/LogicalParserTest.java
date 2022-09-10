package eu.gir.girsignals.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.blocks.signals.SignalHV;
import eu.gir.girsignals.init.GIRBlocks;
import eu.gir.girsignals.models.parser.FunctionParsingInfo;
import eu.gir.girsignals.models.parser.ParameterInfo;
import eu.gir.girsignals.models.parser.ValuePack;
import net.minecraft.init.Bootstrap;
import net.minecraftforge.common.property.IUnlistedProperty;

@SuppressWarnings({"rawtypes", "unchecked"})
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
	}

}
