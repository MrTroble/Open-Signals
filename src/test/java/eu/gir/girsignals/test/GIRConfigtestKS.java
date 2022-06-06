package eu.gir.girsignals.test;

import static eu.gir.girsignals.blocks.signals.SignalKS.DISTANTSIGNAL;
import static eu.gir.girsignals.blocks.signals.SignalKS.MAINSIGNAL;
import static eu.gir.girsignals.blocks.signals.SignalKS.STOPSIGNAL;
import static eu.gir.girsignals.blocks.signals.SignalKS.ZS3;
import static eu.gir.girsignals.blocks.signals.SignalKS.ZS3V;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import eu.gir.girsignals.EnumSignals.KS;
import eu.gir.girsignals.EnumSignals.KSDistant;
import eu.gir.girsignals.EnumSignals.KSMain;
import eu.gir.girsignals.EnumSignals.ZS32;
import eu.gir.girsignals.signalbox.config.ISignalAutoconfig.ConfigInfo;
import eu.gir.girsignals.signalbox.config.KSSignalConfig;
import eu.gir.girsignals.test.DummySignal.DummyBuilder;

public class GIRConfigtestKS {

    final KSSignalConfig config = KSSignalConfig.INSTANCE;

    @Test
    public void testKSConfig() {

        for(ZS32 zs32 : ZS32.values()) {
            if (zs32.ordinal() > 26 && zs32.ordinal() < 43) {
                ConfigtestKS(KS.KS2, KSMain.KS1, KSDistant.KS2, zs32, ZS32.OFF, KS.HP0, KSMain.HP0,
                        KSDistant.KS2, ZS32.OFF, ZS32.OFF, zs32.ordinal() - 26);
                ConfigtestKS(KS.KS1, KSMain.KS1, KSDistant.KS1, zs32, ZS32.OFF, KS.KS2, KSMain.KS1,
                        KSDistant.KS1, zs32, ZS32.OFF, zs32.ordinal() - 26);
            }
            
        }
        ConfigtestKS(KS.KS2, KSMain.KS1, KSDistant.KS2, ZS32.OFF, ZS32.OFF, KS.HP0, KSMain.HP0,
                KSDistant.KS2, ZS32.OFF, ZS32.OFF, 0);
    }

    private void ConfigtestKS(final KS kscurrent, final KSMain ksmaincurrent,
            final KSDistant distantcurrent, final ZS32 zs3current, final ZS32 zs3vcurrent,
            final KS ksnext, final KSMain ksmainnext, final KSDistant distantnext,
            final ZS32 zs3next, final ZS32 zs3vnext, final int speed) {

        final DummySignal signalBase = DummyBuilder.start(STOPSIGNAL, KS.HP0)
                .of(MAINSIGNAL, KSMain.HP0).of(DISTANTSIGNAL, KSDistant.KS2).of(ZS3, ZS32.OFF)
                .of(ZS3V, ZS32.OFF).build();
        final DummySignal DummySignal = signalBase.copy();
        config.reset(DummySignal);

        final DummySignal signalDummy = DummyBuilder.start(STOPSIGNAL, kscurrent)
                .of(MAINSIGNAL, ksmaincurrent).of(DISTANTSIGNAL, distantcurrent).of(ZS3, zs3current)
                .of(ZS3V, zs3vcurrent).build();

        final DummySignal signalnext = DummyBuilder.start(STOPSIGNAL, ksnext)
                .of(MAINSIGNAL, ksmainnext).of(DISTANTSIGNAL, distantnext).of(ZS3, zs3next)
                .of(ZS3V, zs3vnext).build();
        config.change(new ConfigInfo(DummySignal, signalnext, speed));
        assertEquals(signalDummy, DummySignal);
    }
}
