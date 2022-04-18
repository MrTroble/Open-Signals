package eu.gir.girsignals.signalbox.config;

import java.util.HashMap;
import java.util.Optional;

import eu.gir.girsignals.EnumSignals.KS;
import eu.gir.girsignals.EnumSignals.KSDistant;
import eu.gir.girsignals.EnumSignals.KSMain;
import eu.gir.girsignals.EnumSignals.ZS32;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.blocks.signals.SignalKS;
import eu.gir.girsignals.tileentitys.SignalTileEnity;

public final class KSSignalConfig implements ISignalAutoconfig {

    public static final KSSignalConfig INSTANCE = new KSSignalConfig();

    private KSSignalConfig() {
    }

    @SuppressWarnings({
            "unchecked", "rawtypes"
    })
    @Override
    public void change(final int speed, final SignalTileEnity current, final SignalTileEnity next) {
        final HashMap<SEProperty, Object> values = new HashMap<>();

        if (next != null) {
            current.getProperty(SignalKS.ZS3V).ifPresent(_u -> next.getProperty(SignalKS.ZS3)
                    .ifPresent(value -> current.setProperty(SignalKS.ZS3V, (ZS32) value)));
            if (speed <= 16 && speed > 0) {
                final ZS32 zs32 = ZS32.values()[ZS32.Z.ordinal() + speed];
                current.getProperty(SignalKS.ZS3)
                        .ifPresent(_u -> current.setProperty(SignalKS.ZS3, zs32));
            }
            final boolean changes = next.getProperty(SignalKS.ZS3)
                    .filter(e -> ((ZS32) e).ordinal() > ZS32.Z.ordinal()
                            && (((ZS32) e).ordinal() - ZS32.Z.ordinal()) < speed)
                    .isPresent();
            values.put(SignalKS.MAINSIGNAL, KSMain.KS1);
            final Optional opt = next.getProperty(SignalKS.STOPSIGNAL);
            final boolean stop = next.getProperty(SignalKS.MAINSIGNAL).filter(KSMain.HP0::equals)
                    .isPresent() || opt.filter(KS.HP0::equals).isPresent();
            if (stop) {
                values.put(SignalKS.STOPSIGNAL, KS.KS2);
                values.put(SignalKS.DISTANTSIGNAL, KSDistant.KS2);
            } else if (changes) {
                values.put(SignalKS.STOPSIGNAL, KS.KS1_BLINK);
                values.put(SignalKS.DISTANTSIGNAL, KSDistant.KS1_BLINK);
            } else {
                values.put(SignalKS.STOPSIGNAL, KS.KS1);
                values.put(SignalKS.DISTANTSIGNAL, KSDistant.KS1);
            }
        } else {
            values.put(SignalKS.DISTANTSIGNAL, KSDistant.KS2);
            values.put(SignalKS.STOPSIGNAL, KS.KS2);
            values.put(SignalKS.MAINSIGNAL, KSMain.KS1);
            values.put(SignalKS.ZS2, ZS32.OFF);
            values.put(SignalKS.ZS3, ZS32.Z3);
            values.put(SignalKS.ZS2V, ZS32.OFF);
            values.put(SignalKS.ZS3V, ZS32.OFF);
        }
        this.changeIfPresent(values, current);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void reset(final SignalTileEnity current) {
        final HashMap<SEProperty, Object> values = new HashMap<>();
        values.put(SignalKS.DISTANTSIGNAL, KSDistant.KS2);
        values.put(SignalKS.STOPSIGNAL, KS.HP0);
        values.put(SignalKS.MAINSIGNAL, KSMain.HP0);
        values.put(SignalKS.ZS2, ZS32.OFF);
        values.put(SignalKS.ZS3, ZS32.OFF);
        values.put(SignalKS.ZS2V, ZS32.OFF);
        values.put(SignalKS.ZS3V, ZS32.OFF);
        this.changeIfPresent(values, current);
    }

}
