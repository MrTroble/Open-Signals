package eu.gir.girsignals.blocks.signals;

import eu.gir.girsignals.EnumSignals.HP;
import eu.gir.girsignals.EnumSignals.HPBlock;
import eu.gir.girsignals.EnumSignals.HPHome;
import eu.gir.girsignals.EnumSignals.HPType;
import eu.gir.girsignals.EnumSignals.MastSignal;
import eu.gir.girsignals.EnumSignals.VR;
import eu.gir.girsignals.EnumSignals.ZS32;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.init.GIRItems;
import eu.gir.girsignals.signalbox.config.HVSignalConfig;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class SignalHV extends Signal {

    public SignalHV() {
        super(builder(GIRItems.PLACEMENT_TOOL, "HV").height(6).signHeight(2.775f)
                .config(HVSignalConfig.INSTANCE).build());
    }

    public static final SEProperty<HPType> HPTYPE = SEProperty.of("hptype", HPType.STOPSIGNAL,
            ChangeableStage.GUISTAGE);
    public static final SEProperty<HP> STOPSIGNAL = SEProperty.of("stopsignal", HP.OFF,
            ChangeableStage.APISTAGE_NONE_CONFIG, true, check(HPTYPE, HPType.STOPSIGNAL));
    public static final SEProperty<HPHome> HPHOME = SEProperty.of("hphome", HPHome.OFF,
            ChangeableStage.APISTAGE_NONE_CONFIG, true, check(HPTYPE, HPType.HPHOME));
    public static final SEProperty<HPBlock> HPBLOCK = SEProperty.of("hpblock", HPBlock.OFF,
            ChangeableStage.APISTAGE_NONE_CONFIG, true, check(HPTYPE, HPType.HPBLOCK));
    public static final SEProperty<Boolean> IDENTIFIER = SEProperty.of("identifier", false);
    public static final SEProperty<VR> DISTANTSIGNAL = SEProperty.of("distantsignal", VR.OFF);
    public static final SEProperty<Boolean> VR_LIGHT = SEProperty.of("vrlight", false);
    public static final SEProperty<Boolean> NE2 = SEProperty.of("ne2", false,
            ChangeableStage.GUISTAGE);
    public static final SEProperty<MastSignal> MASTSIGN = SEProperty.of("mastsign", MastSignal.OFF,
            ChangeableStage.GUISTAGE);
    public static final SEProperty<ZS32> ZS3 = SEProperty.of("zs3", ZS32.OFF);
    public static final SEProperty<ZS32> ZS3V = SEProperty.of("zs3v", ZS32.OFF);
    public static final SEProperty<Boolean> ZS1 = SEProperty.of("zs1", false);
    public static final SEProperty<Boolean> ZS7 = SEProperty.of("zs7", false);
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