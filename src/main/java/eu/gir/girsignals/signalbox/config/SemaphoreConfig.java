package eu.gir.girsignals.signalbox.config;

import java.util.HashMap;
import java.util.Optional;

import eu.gir.girsignals.EnumSignals.HLExit;
import eu.gir.girsignals.EnumSignals.HLLightbar;
import eu.gir.girsignals.EnumSignals.HP;
import eu.gir.girsignals.EnumSignals.HPBlock;
import eu.gir.girsignals.EnumSignals.HPHome;
import eu.gir.girsignals.EnumSignals.KS;
import eu.gir.girsignals.EnumSignals.KSMain;
import eu.gir.girsignals.EnumSignals.SemaDist;
import eu.gir.girsignals.EnumSignals.SemaType;
import eu.gir.girsignals.EnumSignals.ZS32;
import eu.gir.girsignals.blocks.signals.SignalHL;
import eu.gir.girsignals.blocks.signals.SignalHV;
import eu.gir.girsignals.blocks.signals.SignalKS;
import eu.gir.girsignals.blocks.signals.SignalSemaphore;
import eu.gir.girsignals.enums.PathType;
import eu.gir.girsignals.tileentitys.SignalTileEnity;

public final class SemaphoreConfig implements ISignalAutoconfig {

    @SuppressWarnings("rawtypes")
    private static void checkSpeed(final Optional<ZS32> opt,
            final HashMap<SEProperty, Object> values) {

        if (opt.isPresent()) {

            if (opt.get().ordinal() >= 32) {

                values.put(SignalSemaphore.SEMA_VR, SemaDist.VR1);

            } else {

                values.put(SignalSemaphore.SEMA_VR, SemaDist.VR2);
            }
        }
    }

    public static final SemaphoreConfig SEMAPHORE_CONFIG = new SemaphoreConfig();

    private SemaphoreConfig() {
    }

    @SuppressWarnings({
            "rawtypes", "unchecked"
    })
    @Override
    public void change(final ConfigInfo info) {

        if (info.type.equals(PathType.SHUNTING)) {
            RSSignalConfig.RS_CONFIG.change(info);
            return;
        }

        final HashMap<SEProperty, Object> values = new HashMap<>();

        values.put(SignalSemaphore.WING1, true);

        if (info.speed <= 7) {

            values.put(SignalSemaphore.WING2, true);
        }

        if (info.speed != 4 && info.speed <= 16) {

            final ZS32 zs32 = ZS32.values()[ZS32.Z.ordinal() + info.speed];
            values.put(SignalSemaphore.ZS3, zs32);
        }

        if (info.next != null) {

            final Optional<ZS32> speedKS = (Optional<ZS32>) info.next.getProperty(SignalKS.ZS3);

            final Optional<ZS32> speedKSplate = (Optional<ZS32>) info.next
                    .getProperty(SignalKS.ZS3_PLATE);

            final Optional<ZS32> speedHVZS3plate = (Optional<ZS32>) info.next
                    .getProperty(SignalHV.ZS3_PLATE);

            final Optional<ZS32> hvZS3 = (Optional<ZS32>) info.next.getProperty(SignalHV.ZS3);

            final Optional<ZS32> nexthlZS3PLATE = (Optional<ZS32>) info.next
                    .getProperty(SignalHL.ZS3_PLATE);

            final Optional<Boolean> wing1 = (Optional<Boolean>) info.next
                    .getProperty(SignalSemaphore.WING1);

            final Optional<Boolean> wing2 = (Optional<Boolean>) info.next
                    .getProperty(SignalSemaphore.WING2);

            final Optional<HPHome> nextHPHOME = (Optional<HPHome>) info.next
                    .getProperty(SignalHV.HPHOME);

            final Optional<HLLightbar> getlightbar = (Optional<HLLightbar>) info.next
                    .getProperty(SignalHL.LIGHTBAR);

            final Optional<HP> nextHP = (Optional<HP>) info.next.getProperty(SignalHV.STOPSIGNAL);

            final Optional<ZS32> ksZS3 = (Optional<ZS32>) info.next.getProperty(SignalKS.ZS3);

            final Optional<KS> ksStopsignal = (Optional<KS>) info.next
                    .getProperty(SignalKS.STOPSIGNAL);

            final Optional<SemaType> semaType = (Optional<SemaType>) info.next
                    .getProperty(SignalSemaphore.SEMATYPE);

            final boolean ksStop = info.next.getProperty(SignalKS.MAINSIGNAL)
                    .filter(KSMain.HP0::equals).isPresent()
                    || info.next.getProperty(SignalKS.STOPSIGNAL).filter(KS.HP0::equals)
                            .isPresent();

            final boolean hvStop1 = info.next.getProperty(SignalHV.HPBLOCK)
                    .filter(HPBlock.HP0::equals).isPresent()
                    || nextHPHOME.filter(HPHome.HP0::equals).isPresent()
                    || nextHPHOME.filter(HPHome.HP0_ALTERNATE_RED::equals).isPresent();

            final boolean hvStop2 = nextHP.filter(HP.HP0::equals).isPresent()
                    || nextHP.filter(HP.SHUNTING::equals).isPresent();

            final boolean hlStop = info.next.getProperty(SignalHL.STOPSIGNAL)
                    .filter(a -> Signallists.HL_STOP.contains(a)).isPresent()
                    || info.next.getProperty(SignalHL.EXITSIGNAL)
                            .filter(d -> Signallists.HLEXIT_STOP.contains(d)).isPresent()
                    || info.next.getProperty(SignalHL.BLOCKSIGNAL)
                            .filter(b -> Signallists.HLBLOCK_STOP.contains(b)).isPresent()
                    || info.next.getProperty(SignalHL.BLOCKEXITSIGNAL)
                            .filter(be -> Signallists.HLBLOCKEXIT_STOP.contains(be)).isPresent();

            final boolean hlmain40 = info.next.getProperty(SignalHL.STOPSIGNAL)
                    .filter(c -> Signallists.HL_40_MAIN.contains(c)).isPresent()
                    || info.next.getProperty(SignalHL.EXITSIGNAL).filter(HLExit.HL2_3::equals)
                            .isPresent();

            final boolean nextSema = semaType.filter(SemaType.MAIN::equals).isPresent()
                    || semaType.filter(SemaType.MAIN_SMALL::equals).isPresent();

            final boolean semaStop = !(wing1.filter(wing -> (Boolean) wing).isPresent()
                    || (wing2.filter(s -> (Boolean) s).isPresent())) && nextSema;

            final boolean semaHP2 = wing1.filter(wing -> (Boolean) wing).isPresent()
                    && wing2.filter(wing -> (Boolean) wing).isPresent() && nextSema;

            final boolean hp2 = nextHP.filter(HP.HP2::equals).isPresent()
                    || nextHPHOME.filter(HPHome.HP2::equals).isPresent();

            final boolean hlSlowDown = (hlmain40
                    && !getlightbar.filter(HLLightbar.GREEN::equals).isPresent());

            final boolean ksSlowdown = ksStopsignal.filter(Signallists.KS_GO::contains).isPresent()
                    && ksZS3.filter(zs -> zs.compareTo(ZS32.Z8) < 0 && zs.compareTo(ZS32.Z1) >= 0)
                            .isPresent();

            final boolean anySlowdown = hp2 || hlSlowDown || ksSlowdown;

            final boolean hvStop = hvStop1 || hvStop2;

            checkSpeed(nexthlZS3PLATE, values);
            checkSpeed(hvZS3, values);
            checkSpeed(speedHVZS3plate, values);
            checkSpeed(speedHVZS3plate, values);
            checkSpeed(speedKSplate, values);
            checkSpeed(speedKS, values);

            values.put(SignalSemaphore.SEMA_VR, SemaDist.VR1);

            final boolean anyStop = hlStop || semaStop || hvStop;

            if (anyStop || ksStop) {

                values.put(SignalSemaphore.SEMA_VR, SemaDist.VR0);
            }

            if (anySlowdown || semaHP2) {

                values.put(SignalSemaphore.SEMA_VR, SemaDist.VR2);
            }

        } else {

            values.put(SignalSemaphore.SEMA_VR, SemaDist.VR0);
            values.put(SignalSemaphore.ZS3, ZS32.OFF);
        }
        this.changeIfPresent(values, info.current);
    }

    @Override
    public void reset(final SignalTileEnity current) {

        @SuppressWarnings("rawtypes")
        final HashMap<SEProperty, Object> values = new HashMap<>();

        values.put(SignalSemaphore.WING1, false);
        values.put(SignalSemaphore.WING2, false);
        values.put(SignalSemaphore.ZS1, false);
        values.put(SignalSemaphore.ZS7, false);
        values.put(SignalSemaphore.RA12, false);
        values.put(SignalSemaphore.ZS3, ZS32.OFF);
        values.put(SignalSemaphore.SEMA_VR, SemaDist.VR0);
        this.changeIfPresent(values, current);

    }

}
