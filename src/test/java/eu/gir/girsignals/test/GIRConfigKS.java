package eu.gir.girsignals.test;

import static eu.gir.girsignals.blocks.signals.SignalKS.DISTANTSIGNAL;
import static eu.gir.girsignals.blocks.signals.SignalKS.MAINSIGNAL;
import static eu.gir.girsignals.blocks.signals.SignalKS.STOPSIGNAL;
import static eu.gir.girsignals.blocks.signals.SignalKS.ZS3;
import static eu.gir.girsignals.blocks.signals.SignalKS.ZS3V;
import static eu.gir.girsignals.blocks.signals.SignalKS.ZS2;
import static eu.gir.girsignals.blocks.signals.SignalKS.ZS2V;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import eu.gir.girsignals.EnumSignals.KS;
import eu.gir.girsignals.EnumSignals.KSDistant;
import eu.gir.girsignals.EnumSignals.KSMain;
import eu.gir.girsignals.EnumSignals.ZS32;
import eu.gir.girsignals.signalbox.config.ISignalAutoconfig.ConfigInfo;
import eu.gir.girsignals.signalbox.config.KSSignalConfig;
import eu.gir.girsignals.test.DummySignal.DummyBuilder;

public class GIRConfigKS {

    @Test
    public void testKSConfig() {
        final KSSignalConfig ksconfig = KSSignalConfig.INSTANCE;

        final DummySignal signalBase = DummyBuilder.start(STOPSIGNAL, KS.HP0)
                .of(MAINSIGNAL, KSMain.HP0).of(ZS3, ZS32.OFF).of(ZS3V, ZS32.OFF)
                .of(DISTANTSIGNAL, KSDistant.KS2).of(ZS2, ZS32.OFF).of(ZS2V, ZS32.OFF).build();
        final DummySignal signalDummy = signalBase.copy();
        ksconfig.reset(signalBase);
        assertEquals(signalDummy, signalBase);

        final DummySignal signalHP0next = DummyBuilder.start(STOPSIGNAL, KS.KS2)
                .of(MAINSIGNAL, KSMain.HP0).of(ZS3, ZS32.OFF).of(ZS3V, ZS32.OFF)
                .of(DISTANTSIGNAL, KSDistant.KS2).of(ZS2, ZS32.OFF).of(ZS2V, ZS32.OFF).build();
        ksconfig.change(new ConfigInfo(signalBase, signalDummy, 0));
        assertEquals(signalHP0next, signalBase);
    }
}
