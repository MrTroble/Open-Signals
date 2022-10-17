package eu.gir.girsignals.signalbox.config;

import java.util.HashMap;
import java.util.Optional;

import eu.gir.girsignals.EnumSignals.HP;
import eu.gir.girsignals.EnumSignals.HPBlock;
import eu.gir.girsignals.EnumSignals.HPHome;
import eu.gir.girsignals.EnumSignals.KS;
import eu.gir.girsignals.EnumSignals.KSMain;
import eu.gir.girsignals.EnumSignals.SemaDist;
import eu.gir.girsignals.EnumSignals.ZS32;
import eu.gir.girsignals.SEProperty;
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

    @SuppressWarnings("rawtypes")
    private static void checkStop(final boolean stop, final HashMap<SEProperty, Object> values) {

        if (stop) {

            values.put(SignalSemaphore.SEMA_VR, SemaDist.VR0);

        } else {

            values.put(SignalSemaphore.SEMA_VR, SemaDist.VR1);
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
                    .getProperty(SignalSemaphore.WING1);

            final boolean ksStop = info.next.getProperty(SignalKS.MAINSIGNAL)
                    .filter(KSMain.HP0::equals).isPresent()
                    || info.next.getProperty(SignalKS.STOPSIGNAL).filter(KS.HP0::equals)
                            .isPresent();

            final boolean hvStop1 = info.next.getProperty(SignalHV.HPBLOCK)
                    .filter(HPBlock.HP0::equals).isPresent()
                    || info.next.getProperty(SignalHV.HPHOME).filter(HPHome.HP0::equals).isPresent()
                    || info.next.getProperty(SignalHV.HPHOME)
                            .filter(HPHome.HP0_ALTERNATE_RED::equals).isPresent();

            final boolean hvStop2 = info.next.getProperty(SignalHV.STOPSIGNAL)
                    .filter(HP.HP0::equals).isPresent()
                    || info.next.getProperty(SignalHV.STOPSIGNAL).filter(HP.SHUNTING::equals)
                            .isPresent();

            final boolean hlStop = info.next.getProperty(SignalHL.STOPSIGNAL)
                    .filter(a -> Signallists.HL_STOP.contains(a)).isPresent()
                    || info.next.getProperty(SignalHL.EXITSIGNAL)
                            .filter(d -> Signallists.HLEXIT_STOP.contains(d)).isPresent();

            final boolean semaStop = !(wing2.filter(wing -> (Boolean) wing).isPresent())
                    && !(wing1.filter(s -> (Boolean) s).isPresent());

            final boolean semaHP2 = wing1.filter(wing -> (Boolean) wing).isPresent()
                    && wing2.filter(wing -> (Boolean) wing).isPresent();

            final boolean hvStop = hvStop1 || hvStop2;

            checkStop(ksStop, values);
            checkStop(hlStop, values);
            checkStop(hvStop, values);

            checkSpeed(nexthlZS3PLATE, values);
            checkSpeed(hvZS3, values);
            checkSpeed(speedHVZS3plate, values);
            checkSpeed(speedHVZS3plate, values);
            checkSpeed(speedKSplate, values);
            checkSpeed(speedKS, values);

            values.put(SignalSemaphore.SEMA_VR, SemaDist.VR1);

            if (semaHP2) {
                values.put(SignalSemaphore.SEMA_VR, SemaDist.VR2);
            }

            final boolean anyStop = hlStop || semaStop || hvStop;

            if (anyStop) {

                values.put(SignalSemaphore.SEMA_VR, SemaDist.VR0);
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
