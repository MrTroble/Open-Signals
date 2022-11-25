package com.troblecodings.signals.signalbox.config;

import java.util.HashMap;
import java.util.Optional;

import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.EnumSignals.HP;
import com.troblecodings.signals.EnumSignals.HPBlock;
import com.troblecodings.signals.EnumSignals.HPHome;
import com.troblecodings.signals.EnumSignals.KSMain;
import com.troblecodings.signals.EnumSignals.SemaDist;
import com.troblecodings.signals.EnumSignals.ZS32;
import com.troblecodings.signals.blocks.signals.SignalHL;
import com.troblecodings.signals.blocks.signals.SignalHV;
import com.troblecodings.signals.blocks.signals.SignalKS;
import com.troblecodings.signals.blocks.signals.SignalSemaphore;
import com.troblecodings.signals.enums.PathType;
import com.troblecodings.signals.tileentitys.SignalTileEnity;

public final class SemaphoreConfig {

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

    /**
     * @param if the bool is true, the next signal shows something that isn't red.
     */

    @SuppressWarnings("rawtypes")
    private static void checkStop(final boolean bool, final HashMap<SEProperty, Object> values) {

        if (bool) {

            values.put(SignalSemaphore.SEMA_VR, SemaDist.VR1);

        } else {

            values.put(SignalSemaphore.SEMA_VR, SemaDist.VR2);
        }

    }

    public static final SemaphoreConfig SEMAPHORE_CONFIG = new SemaphoreConfig();

    private SemaphoreConfig() {
    }

    @SuppressWarnings({
            "rawtypes", "unchecked"
    })

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

            final boolean ksgo = info.next.getProperty(SignalKS.STOPSIGNAL)
                    .filter(a -> Signallists.KS_GO.contains(a)).isPresent()
                    || info.next.getProperty(SignalKS.MAINSIGNAL).filter(KSMain.KS1::equals)
                            .isPresent();

            final boolean hvgo = info.next.getProperty(SignalHV.STOPSIGNAL).filter(HP.HP1::equals)
                    .isPresent()
                    || info.next.getProperty(SignalHV.STOPSIGNAL).filter(HP.HP2::equals).isPresent()
                    || info.next.getProperty(SignalHV.HPHOME).filter(HPHome.HP1::equals)
                            .isPresent();

            final boolean hvgo2 = info.next.getProperty(SignalHV.HPHOME).filter(HPHome.HP2::equals)
                    .isPresent()
                    || info.next.getProperty(SignalHV.HPBLOCK).filter(HPBlock.HP1::equals)
                            .isPresent();

            final boolean hlstop = info.next.getProperty(SignalHL.STOPSIGNAL)
                    .filter(a -> Signallists.HL_STOP.contains(a)).isPresent()
                    || info.next.getProperty(SignalHL.EXITSIGNAL)
                            .filter(d -> Signallists.HLEXIT_STOP.contains(d)).isPresent();

            checkStop(ksgo, values);
            checkStop(!hlstop, values);
            checkStop(hvgo2, values);
            checkStop(hvgo, values);
            checkStop(ksgo, values);

            checkSpeed(nexthlZS3PLATE, values);
            checkSpeed(hvZS3, values);
            checkSpeed(speedHVZS3plate, values);
            checkSpeed(speedHVZS3plate, values);
            checkSpeed(speedKSplate, values);
            checkSpeed(speedKS, values);

            if (hlstop) {

                values.put(SignalSemaphore.SEMA_VR, SemaDist.VR0);

            } else if (!ksgo || !(hvgo || hvgo2)) {

                values.put(SignalSemaphore.SEMA_VR, SemaDist.VR0);

            } else {

                values.put(SignalSemaphore.SEMA_VR, SemaDist.VR1);
            }

        } else {

            values.put(SignalSemaphore.SEMA_VR, SemaDist.VR0);
            values.put(SignalSemaphore.ZS3, ZS32.OFF);
        }

    }

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

    }

}
