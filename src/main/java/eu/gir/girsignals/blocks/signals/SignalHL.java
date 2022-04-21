package eu.gir.girsignals.blocks.signals;

import eu.gir.girsignals.EnumSignals.HL;
import eu.gir.girsignals.EnumSignals.HLDistant;
import eu.gir.girsignals.EnumSignals.HLExit;
import eu.gir.girsignals.EnumSignals.HLLightbar;
import eu.gir.girsignals.EnumSignals.HLType;
import eu.gir.girsignals.EnumSignals.MastSignal;
import eu.gir.girsignals.EnumSignals.ZS32;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.init.GIRItems;
import eu.gir.girsignals.signalbox.config.HLSignalConfig;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class SignalHL extends Signal {

    public SignalHL() {
        super(builder(GIRItems.PLACEMENT_TOOL, "HL").height(6).signHeight(1.15f)
                .config(HLSignalConfig.INSTANCE).build());
    }

    public static final SEProperty<HLType> HLTYPE = SEProperty.of("hltype", HLType.MAIN,
            ChangeableStage.GUISTAGE);
    public static final SEProperty<HL> STOPSIGNAL = SEProperty.of("mainlightsignal", HL.OFF,
            ChangeableStage.APISTAGE_NONE_CONFIG, true, check(HLTYPE, HLType.MAIN));
    public static final SEProperty<HLDistant> DISTANTSIGNAL = SEProperty.of(
            "mainlightsignal_distant", HLDistant.OFF, ChangeableStage.APISTAGE_NONE_CONFIG, true,
            check(HLTYPE, HLType.DISTANT));
    public static final SEProperty<HLExit> EXITSIGNAL = SEProperty.of("exitsignal", HLExit.OFF, 
    		ChangeableStage.APISTAGE_NONE_CONFIG, true, check(HLTYPE, HLType.EXIT));
    public static final SEProperty<HLLightbar> LIGHTBAR = SEProperty.of("mainlightsignallightbar",
            HLLightbar.OFF);
    public static final SEProperty<MastSignal> MASTSIGN = SEProperty.of("mastsign", MastSignal.OFF,
            ChangeableStage.GUISTAGE);
    public static final SEProperty<Boolean> MASTSIGNDISTANT = SEProperty.of("mastsigndistant",
            false, ChangeableStage.GUISTAGE);
    public static final SEProperty<Boolean> NE2 = SEProperty.of("ne2", false,
            ChangeableStage.GUISTAGE);
    public static final SEProperty<Boolean> NE2_2 = SEProperty.of("ne2_2", false,
            ChangeableStage.GUISTAGE);
    public static final SEProperty<Boolean> NE2_4 = SEProperty.of("ne2_4", false,
            ChangeableStage.GUISTAGE);
    public static final SEProperty<ZS32> ZS2 = SEProperty.of("zs2", ZS32.OFF);
    public static final SEProperty<ZS32> ZS2V = SEProperty.of("zs2v", ZS32.OFF);
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
