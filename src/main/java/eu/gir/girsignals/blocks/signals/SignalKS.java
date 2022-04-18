package eu.gir.girsignals.blocks.signals;

import eu.gir.girsignals.EnumSignals.KS;
import eu.gir.girsignals.EnumSignals.KSDistant;
import eu.gir.girsignals.EnumSignals.KSMain;
import eu.gir.girsignals.EnumSignals.KSType;
import eu.gir.girsignals.EnumSignals.MastSignal;
import eu.gir.girsignals.EnumSignals.ZS32;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.init.GIRItems;
import eu.gir.girsignals.signalbox.config.KSSignalConfig;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class SignalKS extends Signal {

    public SignalKS() {
        super(builder(GIRItems.PLACEMENT_TOOL, "KS").height(6).signHeight(4.95f)
                .config(KSSignalConfig.INSTANCE).build());
    }

    public static final SEProperty<KSType> KSTYPE = SEProperty.of("kombitype", KSType.MAIN,
            ChangeableStage.GUISTAGE);
    public static final SEProperty<KS> STOPSIGNAL = SEProperty.of("kombisignal", KS.OFF,
            ChangeableStage.APISTAGE_NONE_CONFIG, true, check(KSTYPE, KSType.STOPSIGNAL));
    public static final SEProperty<KSMain> MAINSIGNAL = SEProperty.of("kombisignal_main",
            KSMain.OFF, ChangeableStage.APISTAGE_NONE_CONFIG, true, check(KSTYPE, KSType.MAIN));
    public static final SEProperty<KSDistant> DISTANTSIGNAL = SEProperty.of("kombisignal_distant",
            KSDistant.OFF, ChangeableStage.APISTAGE_NONE_CONFIG, true,
            check(KSTYPE, KSType.DISTANT));
    public static final SEProperty<MastSignal> MASTSIGN = SEProperty.of("mastsign", MastSignal.OFF,
            ChangeableStage.GUISTAGE);
    public static final SEProperty<Boolean> MASTSIGNDISTANT = SEProperty.of("mastsigndistant",
            false, ChangeableStage.GUISTAGE);
    public static final SEProperty<Boolean> NE2 = SEProperty.of("ne2", false,
            ChangeableStage.GUISTAGE);
    public static final SEProperty<ZS32> ZS2 = SEProperty.of("zs2", ZS32.OFF);
    public static final SEProperty<ZS32> ZS2V = SEProperty.of("zs2v", ZS32.OFF);
    public static final SEProperty<ZS32> ZS3 = SEProperty.of("zs3", ZS32.OFF);
    public static final SEProperty<ZS32> ZS3V = SEProperty.of("zs3v", ZS32.OFF);
    public static final SEProperty<ZS32> ZS3_PLATE = SEProperty.of("zs3plate", ZS32.OFF,
            ChangeableStage.GUISTAGE);
    public static final SEProperty<ZS32> ZS3V_PLATE = SEProperty.of("zs3vplate", ZS32.OFF,
            ChangeableStage.GUISTAGE);

    @Override
    public boolean hasCostumColor() {
        return true;
    }

    @Override
    public int colorMultiplier(final IBlockState state, final IBlockAccess worldIn,
            final BlockPos pos, final int tintIndex) {
        return tintIndex == 1 ? 0xFFC200 : 0xFFFFFF;
    }

}
