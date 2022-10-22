package eu.gir.girsignals.signalbox.config;

import java.util.ArrayList;

import com.google.common.collect.Lists;

import eu.gir.girsignals.EnumSignals.HL;
import eu.gir.girsignals.EnumSignals.HLBlock;
import eu.gir.girsignals.EnumSignals.HLBlockExit;
import eu.gir.girsignals.EnumSignals.HLExit;
import eu.gir.girsignals.EnumSignals.KS;
import eu.gir.girsignals.EnumSignals.KSMain;

public final class Signallists {

    private Signallists() {
    }

    public static final ArrayList<HL> HL_STOP = Lists.newArrayList(HL.HP0, HL.HP0_ALTERNATE_RED,
            HL.HL_ZS1, HL.HL_SHUNTING);
    public static final ArrayList<HLExit> HLEXIT_STOP = Lists.newArrayList(HLExit.HP0,
            HLExit.HP0_ALTERNATE_RED, HLExit.HL_ZS1, HLExit.HL_SHUNTING);
    public static final ArrayList<HLBlock> HLBLOCK_STOP = Lists.newArrayList(HLBlock.HP0,
            HLBlock.HP0_ALTERNATE_RED, HLBlock.HL_ZS1);
    public static final ArrayList<HLBlockExit> HLBLOCKEXIT_STOP = Lists
            .newArrayList(HLBlockExit.HP0, HLBlockExit.HL_ZS1, HLBlockExit.HL_SHUNTING);
    public static final ArrayList<HL> HL_UNCHANGED = Lists.newArrayList(HL.HL1, HL.HL4, HL.HL7,
            HL.HL10);
    public static final ArrayList<HL> HL_40_MAIN = Lists.newArrayList(HL.HL2_3, HL.HL5_6, HL.HL8_9,
            HL.HL11_12);
    public static final ArrayList<KS> KS_GO = Lists.newArrayList(KS.KS1, KS.KS1_BLINK,
            KS.KS1_BLINK_LIGHT, KS.KS2, KS.KS2_LIGHT);
    public static final ArrayList<KS> STOP_KS = Lists.newArrayList(KS.HP0, KS.KS_SHUNTING,
            KS.KS_ZS1, KS.KS_ZS7);
    public static final ArrayList<KSMain> STOP_KS_MAIN = Lists.newArrayList(KSMain.HP0,
            KSMain.KS_SHUNTING, KSMain.KS_ZS1, KSMain.KS_ZS7);

}