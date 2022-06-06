package eu.gir.girsignals.test;

import static eu.gir.girsignals.blocks.signals.SignalHV.DISTANTSIGNAL;
import static eu.gir.girsignals.blocks.signals.SignalHV.HPBLOCK;
import static eu.gir.girsignals.blocks.signals.SignalHV.HPHOME;
import static eu.gir.girsignals.blocks.signals.SignalHV.STOPSIGNAL;
import static eu.gir.girsignals.blocks.signals.SignalHV.ZS3;
import static eu.gir.girsignals.blocks.signals.SignalHV.ZS3V;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import eu.gir.girsignals.EnumSignals.HP;
import eu.gir.girsignals.EnumSignals.HPBlock;
import eu.gir.girsignals.EnumSignals.HPHome;
import eu.gir.girsignals.EnumSignals.VR;
import eu.gir.girsignals.EnumSignals.ZS32;
import eu.gir.girsignals.signalbox.config.HVSignalConfig;
import eu.gir.girsignals.signalbox.config.ISignalAutoconfig.ConfigInfo;
import eu.gir.girsignals.test.DummySignal.DummyBuilder;

public class GIRConfigtestHV {

    final HVSignalConfig config = HVSignalConfig.INSTANCE;

    @Test
    public void testHVConfig() {

        for (ZS32 zs32 : ZS32.values()) {
            if (zs32.ordinal() > 32 && zs32.ordinal() < 43) {
                ConfigtestHV(HP.HP1, HPHome.HP1, HPBlock.HP1, VR.VR0, zs32, ZS32.OFF, HP.HP0,
                        HPHome.HP0, HPBlock.HP0, VR.VR0, ZS32.OFF, ZS32.OFF, zs32.ordinal() - 26);
                ConfigtestHV(HP.HP1, HPHome.HP1, HPBlock.HP1, VR.VR1, zs32, zs32, HP.HP1,
                        HPHome.HP1, HPBlock.HP1, VR.VR1, zs32, ZS32.OFF, zs32.ordinal() - 26);
            }

        }
        for (ZS32 zs32 : ZS32.values()) {
            if (zs32.ordinal() > 26 && zs32.ordinal() < 33 && zs32.ordinal() != 30) {
                ConfigtestHV(HP.HP2, HPHome.HP2, HPBlock.HP1, VR.VR0, zs32, ZS32.OFF, HP.HP0,
                        HPHome.HP0, HPBlock.HP0, VR.VR0, ZS32.OFF, ZS32.OFF, zs32.ordinal() - 26);
                ConfigtestHV(HP.HP2, HPHome.HP2, HPBlock.HP1, VR.VR2, zs32, zs32, HP.HP2,
                        HPHome.HP2, HPBlock.HP1, VR.VR2, zs32, ZS32.OFF, zs32.ordinal() - 26);
            }

        }

        ConfigtestHV(HP.HP1, HPHome.HP1, HPBlock.HP1, VR.VR0, ZS32.OFF, ZS32.OFF, HP.HP0,
                HPHome.HP0, HPBlock.HP0, VR.VR0, ZS32.OFF, ZS32.OFF, 0);
        ConfigtestHV(HP.HP1, HPHome.HP1, HPBlock.HP1, VR.VR0, ZS32.OFF, ZS32.OFF, HP.HP0,
                HPHome.HP0, HPBlock.HP0, VR.VR0, ZS32.OFF, ZS32.OFF, 0);
        ConfigtestHV(HP.HP2, HPHome.HP2, HPBlock.HP1, VR.VR0, ZS32.OFF, ZS32.OFF, HP.HP0,
                HPHome.HP0, HPBlock.HP0, VR.VR0, ZS32.OFF, ZS32.OFF, 4);
        ConfigtestHV(HP.HP2, HPHome.HP2, HPBlock.HP1, VR.VR0, ZS32.Z6, ZS32.OFF, HP.HP0, HPHome.HP0,
                HPBlock.HP0, VR.VR0, ZS32.OFF, ZS32.OFF, 6);
        ConfigtestHV(HP.HP1, HPHome.HP1, HPBlock.HP1, VR.VR0, ZS32.Z10, ZS32.OFF, HP.HP0,
                HPHome.HP0, HPBlock.HP0, VR.VR0, ZS32.OFF, ZS32.OFF, 10);

    }

    private void ConfigtestHV(final HP hpcurrent, final HPHome hphomecurrent,
            final HPBlock hpblockcurrent, final VR vrcurrent, final ZS32 zs3current,
            final ZS32 zs3vcurrent, final HP hpnext, final HPHome hphomenext,
            final HPBlock hpblocknext, final VR vrnext, final ZS32 zs3next, final ZS32 zs3vnext,
            final int speed) {

        final DummySignal signalBase = DummyBuilder.start(STOPSIGNAL, HP.HP0).of(HPHOME, HPHome.HP0)
                .of(HPBLOCK, HPBlock.HP0).of(DISTANTSIGNAL, VR.VR0).of(ZS3, ZS32.OFF)
                .of(ZS3V, ZS32.OFF).build();
        final DummySignal DummySignal = signalBase.copy();
        config.reset(DummySignal);

        final DummySignal signalDummy = DummyBuilder.start(STOPSIGNAL, hpcurrent)
                .of(HPHOME, hphomecurrent).of(HPBLOCK, hpblockcurrent).of(DISTANTSIGNAL, vrcurrent)
                .of(ZS3, zs3current).of(ZS3V, zs3vcurrent).build();

        final DummySignal signalnext = DummyBuilder.start(STOPSIGNAL, hpnext).of(HPHOME, hphomenext)
                .of(HPBLOCK, hpblocknext).of(DISTANTSIGNAL, vrnext).of(ZS3, zs3next)
                .of(ZS3V, zs3vnext).build();
        config.change(new ConfigInfo(DummySignal, signalnext, speed));
        assertEquals(signalDummy, DummySignal);
    }

}
