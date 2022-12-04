package com.troblecodings.signals.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.troblecodings.signals.EnumSignals.HP;
import com.troblecodings.signals.EnumSignals.HPType;
import com.troblecodings.signals.EnumSignals.MastSignal;
import com.troblecodings.signals.EnumSignals.NE;
import com.troblecodings.signals.EnumSignals.VR;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.blocks.boards.SignalNE;
import com.troblecodings.signals.blocks.signals.SignalHV;
import com.troblecodings.signals.models.parser.FunctionParsingInfo;
import com.troblecodings.signals.models.parser.IntermidiateLogic;
import com.troblecodings.signals.models.parser.LogicParser;
import com.troblecodings.signals.models.parser.LogicalParserException;
import com.troblecodings.signals.models.parser.PredicateHolder;
import com.troblecodings.signals.models.parser.ValuePack;
import com.troblecodings.signals.models.parser.interm.EvaluationLevel;
import com.troblecodings.signals.models.parser.interm.IntermidiateAnd;
import com.troblecodings.signals.models.parser.interm.IntermidiateNegate;
import com.troblecodings.signals.models.parser.interm.IntermidiateNode;
import com.troblecodings.signals.models.parser.interm.IntermidiateOr;

import net.minecraft.init.Bootstrap;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

@SuppressWarnings({
        "rawtypes", "unchecked"
})
public class LogicalParserTest {

    @BeforeAll
    public static void setup() {
        Bootstrap.register();
    }

    public static FunctionParsingInfo function(final String name, final Signal signal) {
        final FunctionParsingInfo parsing = new FunctionParsingInfo(signal);
        parsing.argument = name;
        return parsing;
    }

    @Test
    public void testFunctionParser() throws Exception {
        for (final Signal signal : new Signal[] {
        }) {
            final FunctionParsingInfo info = new FunctionParsingInfo(signal);
            for (final IUnlistedProperty property : info.properties) {
                info.argument = property.getName();
                final Object object = info.getProperty();
                assertEquals(property, object);
            }
        }
        final Signal hvSignal = new SignalHV();
        final FunctionParsingInfo hvSignalInfo = new FunctionParsingInfo(hvSignal);
        for (final SEProperty property : new SEProperty[] {
                SignalHV.HPHOME, SignalHV.HPBLOCK, SignalHV.ZS3_PLATE
        }) {
            final Object def = property.getDefault();
            hvSignalInfo.argument = property.getName() + "." + def;
            final Object object = hvSignalInfo.getPredicate();
            assertTrue(object instanceof ValuePack);
            final ValuePack pack = (ValuePack) object;
            assertEquals(pack.property, property);
            assertTrue(pack.predicate.test(def));
            assertFalse(pack.predicate.negate().test(def));
        }

        final FunctionParsingInfo parsingInfo = new FunctionParsingInfo(hvSignal);
        final Object[] parsed = parsingInfo.getParameter(new Class[] {
                ValuePack.class, IUnlistedProperty.class, IUnlistedProperty.class
        }, new String[] {
                "stopsignal.HP0", "hptype", "stopsignal"
        });
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
                () -> function("", hvSignal).getProperty());
        assertThrows(LogicalParserException.class,
                () -> function("asdansdkjnkls", hvSignal).getProperty());
        assertThrows(LogicalParserException.class,
                () -> function("stopsignal.HP0", hvSignal).getProperty());
        assertThrows(LogicalParserException.class,
                () -> function("asdansdkjnkls", hvSignal).getPredicate());
        assertThrows(LogicalParserException.class,
                () -> function("stopsignal", hvSignal).getPredicate());
        assertThrows(LogicalParserException.class,
                () -> function("asdasdasd.asdasd", hvSignal).getPredicate());
        assertThrows(LogicalParserException.class,
                () -> function("stopsignal.asdasds", hvSignal).getPredicate());
        assertThrows(LogicalParserException.class,
                () -> function("hptype.HP0", hvSignal).getPredicate());
    }

    public void assertFunction(final String name, final FunctionParsingInfo info,
            final IUnlistedProperty property, final Object value, final String parsing) {
        final Predicate<IExtendedBlockState> statePredicate = LogicParser
                .nDegreeFunctionParser(name, info, parsing);
        final IExtendedBlockState state = new DummyBlockState(property, value);
        assertTrue(statePredicate.test(state));
        assertFalse(statePredicate.negate().test(state));

        final Predicate<IExtendedBlockState> statePredicate2 = LogicParser
                .nDegreeFunctionParser(name.toUpperCase(), info, parsing);
        assertTrue(statePredicate2.test(state));
        assertFalse(statePredicate2.negate().test(state));
    }

    @Test
    public void testMethods() {
        final Signal hvSignal = new SignalHV();
        final FunctionParsingInfo parsingInfo = new FunctionParsingInfo(hvSignal);
        assertFunction("with", parsingInfo, SignalHV.STOPSIGNAL, HP.HP0, "stopsignal.HP0");
        assertFunction("with", parsingInfo, SignalHV.STOPSIGNAL, HP.HP1, "stopsignal.HP1");
        assertFunction("with", parsingInfo, SignalHV.HPTYPE, HPType.STOPSIGNAL,
                "hptype.STOPSIGNAL");
        assertFunction("has", parsingInfo, SignalHV.HPTYPE, HPType.STOPSIGNAL, "hptype");
        assertFunction("has", parsingInfo, SignalHV.STOPSIGNAL, HP.HP0, "stopsignal");
        assertFunction("hasandis", parsingInfo, SignalHV.ZS1, true, "zs1");
        assertFunction("hasandisnot", parsingInfo, SignalHV.ZS7, false, "zs7");
        assertFunction("has", parsingInfo, SignalHV.ZS7, false, "zs7");

        assertThrows(LogicalParserException.class,
                () -> LogicParser.nDegreeFunctionParser("", parsingInfo, ""));
        assertThrows(LogicalParserException.class,
                () -> LogicParser.nDegreeFunctionParser("dasdasdasd", parsingInfo, ""));
        assertThrows(LogicalParserException.class,
                () -> LogicParser.nDegreeFunctionParser("with", parsingInfo, ""));
        assertThrows(LogicalParserException.class,
                () -> LogicParser.nDegreeFunctionParser("with", parsingInfo, "stopsignal.HP0", ""));
        assertThrows(LogicalParserException.class,
                () -> LogicParser.nDegreeFunctionParser("with", parsingInfo, ""));
        assertThrows(LogicalParserException.class,
                () -> LogicParser.nDegreeFunctionParser("has", parsingInfo, "stopsignal", ""));
        assertThrows(LogicalParserException.class,
                () -> LogicParser.nDegreeFunctionParser("has", parsingInfo, ""));
        assertThrows(LogicalParserException.class,
                () -> LogicParser.nDegreeFunctionParser("hasandis", parsingInfo, "stopsignal", ""));
        assertThrows(LogicalParserException.class,
                () -> LogicParser.nDegreeFunctionParser("hasandis", parsingInfo, ""));
        assertThrows(LogicalParserException.class, () -> LogicParser
                .nDegreeFunctionParser("hasandisnot", parsingInfo, "stopsignal", ""));
        assertThrows(LogicalParserException.class,
                () -> LogicParser.nDegreeFunctionParser("hasandisnot", parsingInfo, ""));
    }

    private static IntermidiateLogic buildInterm(final IntermidiateNode... nodes) {
        final IntermidiateLogic logic = new IntermidiateLogic();
        for (final IntermidiateNode node : nodes) {
            logic.add(node);
        }
        return logic;
    }

    private static IntermidiateNode with(final IUnlistedProperty prop, final Object obj) {
        return new IntermidiateNode(PredicateHolder.with(prop, t -> t.equals(obj)),
                EvaluationLevel.PRELEVEL);
    }

    private static IntermidiateNode has(final IUnlistedProperty prop) {
        return new IntermidiateNode(PredicateHolder.has(prop), EvaluationLevel.PRELEVEL);
    }

    private static IntermidiateNode hasandis(final IUnlistedProperty prop) {
        return new IntermidiateNode(PredicateHolder.hasAndIs(prop), EvaluationLevel.PRELEVEL);
    }

    private static IntermidiateNode hasandisnot(final IUnlistedProperty prop) {
        return new IntermidiateNode(PredicateHolder.hasAndIsNot(prop), EvaluationLevel.PRELEVEL);
    }

    private static IntermidiateNode not() {
        return new IntermidiateNegate();
    }

    private static IntermidiateNode or() {
        return new IntermidiateOr();
    }

    private static IntermidiateNode and() {
        return new IntermidiateAnd();
    }

    private static void testNode(final IExtendedBlockState state, final IntermidiateNode node) {
        assertTrue(node.getPredicate().test(state));
        assertFalse(node.getPredicate().negate().test(state));
    }

    private static void check(final IExtendedBlockState state,
            final IntermidiateNode... intermidiateNodes) {
        final IntermidiateNode node = buildInterm(intermidiateNodes).pop();
        testNode(state, node);
    }

    @Test
    public void testIntermidiate() {
        assertThrows(LogicalParserException.class, () -> buildInterm(and()).pop());
        assertThrows(LogicalParserException.class, () -> buildInterm(or()).pop());
        assertThrows(LogicalParserException.class, () -> buildInterm(not()).pop());
        assertThrows(LogicalParserException.class, () -> buildInterm(not(), and()).pop());
        assertThrows(LogicalParserException.class, () -> buildInterm(not(), or()).pop());

        assertThrows(LogicalParserException.class, () -> buildInterm(not(), or()).pop());
        assertThrows(LogicalParserException.class, () -> buildInterm(not(), and()).pop());

        check(new DummyBlockState(SignalHV.STOPSIGNAL, HP.HP0), has(SignalHV.STOPSIGNAL));
        check(new DummyBlockState(SignalHV.ZS1, true), hasandis(SignalHV.ZS1));
        check(new DummyBlockState(SignalHV.ZS1, false), hasandisnot(SignalHV.ZS1));
        check(new DummyBlockState(SignalHV.STOPSIGNAL, HP.HP0), with(SignalHV.STOPSIGNAL, HP.HP0));

        check(new DummyBlockState(SignalHV.STOPSIGNAL, HP.HP0), not(), has(SignalHV.DISTANTSIGNAL));
        check(new DummyBlockState(SignalHV.STOPSIGNAL, HP.HP0), not(),
                with(SignalHV.STOPSIGNAL, HP.HP1));
        check(new DummyBlockState(SignalHV.STOPSIGNAL, HP.HP0).put(SignalHV.DISTANTSIGNAL, VR.OFF),
                with(SignalHV.STOPSIGNAL, HP.HP0), and(), with(SignalHV.DISTANTSIGNAL, VR.OFF));
        check(new DummyBlockState(SignalHV.STOPSIGNAL, HP.HP0), with(SignalHV.STOPSIGNAL, HP.HP0),
                or(), with(SignalHV.DISTANTSIGNAL, VR.VR0));
        check(new DummyBlockState(SignalHV.DISTANTSIGNAL, VR.VR0),
                with(SignalHV.STOPSIGNAL, HP.HP0), or(), with(SignalHV.DISTANTSIGNAL, VR.VR0));
    }

    @Test
    public void testInput() {
        final Signal hvSignal = new SignalHV();
        final FunctionParsingInfo info = new FunctionParsingInfo(hvSignal);
        testNode(new DummyBlockState(SignalHV.STOPSIGNAL, HP.HP0),
                LogicParser.parse("has(stopsignal)", info).pop());
        testNode(new DummyBlockState(SignalHV.DISTANTSIGNAL, VR.VR0),
                LogicParser.parse("!has(stopsignal)", info).pop());
        testNode(new DummyBlockState(SignalHV.DISTANTSIGNAL, VR.VR0),
                LogicParser.parse("!has(stopsignal) && with(distantsignal.VR0)", info).pop());

        assertThrows(LogicalParserException.class, () -> LogicParser.parse("!&&", info).pop());
        assertThrows(LogicalParserException.class, () -> LogicParser.parse("!||", info).pop());
        assertThrows(LogicalParserException.class, () -> LogicParser.parse("!|", info).pop());
        assertThrows(LogicalParserException.class, () -> LogicParser.parse("!", info).pop());
        assertThrows(LogicalParserException.class,
                () -> LogicParser.parse("&&has(stopsignal)", info).pop());
        assertThrows(LogicalParserException.class,
                () -> LogicParser.parse("||has(stopsignal)", info).pop());
        assertThrows(LogicalParserException.class,
                () -> LogicParser.parse("has(stopsignal)&&", info).pop());
        assertThrows(LogicalParserException.class,
                () -> LogicParser.parse("has(stopsignal)||", info).pop());

        testNode(new DummyBlockState(SignalHV.DISTANTSIGNAL, VR.VR1),
                LogicParser.parse("!(has(stopsignal) && (with(distantsignal.VR0)))", info).pop());

        testNode(new DummyBlockState(SignalHV.STOPSIGNAL, HP.HP0),
                LogicParser.parse("has(StoPSigNal)", info).pop());

        testNode(new DummyBlockState(SignalHV.STOPSIGNAL, HP.HP0),
                LogicParser.parse("(((HAS(StoPSigNal))))", info).pop());

        testNode(new DummyBlockState(SignalHV.MASTSIGN, MastSignal.WYWYW),
                LogicParser.parse("with(mastsign.WYWYW)", info).pop());

        testNode(new DummyBlockState(SignalNE.NETYPE, NE.NE4_SMALL), LogicParser
                .parse("with(NETYPE.NE4_small)", new FunctionParsingInfo(new SignalNE())).pop());

        testNode(new DummyBlockState(SignalHV.STOPSIGNAL, HP.HP0), LogicParser
                .parse("with(STOPSIGNAL.HP0) && (with(HPTYPE.STOPSIGNAL) || !has(HPTYPE))", info)
                .pop());

        testNode(
                new DummyBlockState(SignalHV.STOPSIGNAL, HP.HP0).put(SignalHV.HPTYPE,
                        HPType.STOPSIGNAL),
                LogicParser
                        .parse("with(STOPSIGNAL.HP0) && (with(HPTYPE.STOPSIGNAL) || !has(HPTYPE))",
                                info)
                        .pop());

        assertThrows(LogicalParserException.class,
                () -> LogicParser.parse(
                        "with(STOPSIGNAL.HP0) && (with(HPTYPE.STOPSIGNAL) || hasnot(HPTYPE))", info)
                        .pop());
    }

}
