package eu.gir.girsignals.test;

import static eu.gir.girsignals.blocks.signals.SignalHV.DISTANTSIGNAL;
import static eu.gir.girsignals.blocks.signals.SignalHV.HPBLOCK;
import static eu.gir.girsignals.blocks.signals.SignalHV.HPHOME;
import static eu.gir.girsignals.blocks.signals.SignalHV.STOPSIGNAL;
import static eu.gir.girsignals.blocks.signals.SignalHV.ZS1;
import static eu.gir.girsignals.blocks.signals.SignalHV.ZS3;
import static eu.gir.girsignals.blocks.signals.SignalHV.ZS3V;
import static eu.gir.girsignals.blocks.signals.SignalHV.ZS7;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import eu.gir.girsignals.EnumSignals.HP;
import eu.gir.girsignals.EnumSignals.HPBlock;
import eu.gir.girsignals.EnumSignals.HPHome;
import eu.gir.girsignals.EnumSignals.VR;
import eu.gir.girsignals.EnumSignals.ZS32;
import eu.gir.girsignals.signalbox.config.HVSignalConfig;
import eu.gir.girsignals.test.DummySignal.DummyBuilder;

public class GIRConfigTest {

    @Test
    public void testHVConfig() {
        final HVSignalConfig config = HVSignalConfig.INSTANCE;

        final DummySignal signalBase = DummyBuilder.start(STOPSIGNAL, HP.HP0).of(HPHOME, HPHome.HP0)
                .of(DISTANTSIGNAL, VR.VR0).of(ZS3, ZS32.OFF).of(ZS3V, ZS32.OFF)
                .of(HPBLOCK, HPBlock.HP0).of(ZS1, false).of(ZS7, false).build();

        final DummySignal signalDummy = signalBase.copy();
        config.reset(signalBase);
        assertEquals(signalDummy, signalBase);

        final DummySignal signalHP0Next = DummyBuilder.start(STOPSIGNAL, HP.HP1)
                .of(DISTANTSIGNAL, VR.VR0).of(ZS1, false).of(ZS7, false).of(HPHOME, HPHome.HP1)
                .of(ZS3, ZS32.OFF).of(ZS3V, ZS32.OFF).of(HPBLOCK, HPBlock.HP1).build();
        config.change(0, signalBase, signalDummy);
        assertEquals(signalHP0Next, signalBase);

        final DummySignal signalZS1Next = DummyBuilder.start(STOPSIGNAL, HP.HP0)
                .of(DISTANTSIGNAL, VR.VR0).of(ZS1, true).of(ZS7, false).of(HPHOME, HPHome.HP0)
                .of(ZS3, ZS32.OFF).of(ZS3V, ZS32.OFF).of(HPBLOCK, HPBlock.HP0).build();
        ;
    }

}
