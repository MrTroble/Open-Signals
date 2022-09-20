package eu.gir.girsignals;

import net.minecraft.util.IStringSerializable;

public final class EnumSignals {

    public static final int GUI_PLACEMENTTOOL = 0;
    public static final int GUI_SIGNAL_CONTROLLER = 1;

    private EnumSignals() {
    }

    public interface DefaultName<T extends Enum<T>> extends IStringSerializable, Comparable<T> {

        @Override
        public default String getName() {
            return this.toString();
        }
    }

    public interface Offable<T extends Enum<T>> extends DefaultName<T> {

        @SuppressWarnings("unchecked")
        public default T getOffState() {
            return Enum.valueOf((Class<T>) this.getClass(), "OFF");
        }

    }

    public enum HPType implements Offable<HPType> {
        OFF, STOPSIGNAL, HPHOME, HPBLOCK;
    }

    public enum HP implements Offable<HP> {
        OFF, HP0, HP1, HP2, SHUNTING;
    }

    public enum HPHome implements Offable<HPHome> {
        OFF, HP0, HP0_ALTERNATE_RED, HP1, HP2;
    }

    public enum HPBlock implements Offable<HPBlock> {
        OFF, HP0, HP1;
    }

    public enum VR implements Offable<VR> {
        OFF, VR0, VR1, VR2;
    }

    public enum ZS32 implements Offable<ZS32> {
        OFF, A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z, Z1, Z2,
        Z3, Z4, Z5, Z6, Z7, Z8, Z9, Z10, Z11, Z12, Z13, Z14, Z15, Z16, Z17, Z18, Z19, Z20, ZS6, ZS8,
        ZS13, ZP8, ZP9;
    }

    public enum MastSignal implements Offable<MastSignal> {
        OFF, WRW, WYWYW, WBWBW;
    }

    public enum KSType implements Offable<KSType> {
        OFF, STOPSIGNAL, MAIN, DISTANT;
    }

    public enum KS implements Offable<KS> {
        OFF, HP0, KS1, KS1_BLINK, KS1_BLINK_LIGHT, KS2, KS2_LIGHT, KS_ZS1, KS_ZS7, KS_SHUNTING,
        KS_STATUS_LIGHT;
    }

    public enum KSMain implements Offable<KSMain> {
        OFF, HP0, KS1, KS_ZS1, KS_ZS7, KS_SHUNTING, KS_STATUS_LIGHT;
    }

    public enum KSDistant implements Offable<KSDistant> {
        OFF, KS1, KS1_REPEAT, KS1_BLINK, KS1_BLINK_LIGHT, KS1_BLINK_REPEAT, KS2, KS2_LIGHT,
        KS2_REPEAT, KS_STATUS_LIGHT;
    }

    public enum HLType implements Offable<HLType> {
        OFF, MAIN, DISTANT, EXIT;
    }

    public enum HL implements Offable<HL> {
        OFF, HP0, HP0_ALTERNATE_RED, HL1, HL2_3, HL4, HL5_6, HL7, HL8_9, HL10, HL11_12, HL_ZS1,
        HL_SHUNTING, HL_STATUS_LIGHT;
    }

    public enum HLDistant implements Offable<HLDistant> {
        OFF, HL1, HL4, HL7, HL10, HL_STATUS_LIGHT;
    }

    public enum HLExit implements Offable<HLExit> {
        OFF, HP0, HP0_ALTERNATE_RED, HL1, HL2_3, HL_ZS1, HL_SHUNTING, HL_STATUS_LIGHT;
    }

    public enum HLLightbar implements Offable<HLLightbar> {
        OFF, GREEN, YELLOW;
    }

    public enum SHLight implements Offable<SHLight> {
        OFF, SH0, SH1, STATUS_LIGHT;
    }

    public enum TramType implements DefaultName<TramType> {
        TRAM, CAR, PEDESTRIAN, SWITCH;
    }

    public enum Tram implements Offable<Tram> {
        OFF, F0, F1, F2, F3, F4, F5;
    }

    public enum TramAdd implements Offable<TramAdd> {
        OFF, A, T, AT;
    }

    public enum TramSwitch implements Offable<TramSwitch> {
        OFF, W1, W2, W3, W11, W12, W13, W14;
    }

    public enum CAR implements Offable<CAR> {
        OFF, RED, YELLOW, GREEN, RED_YELLOW;
    }

    public enum PED implements Offable<PED> {
        OFF, RED, GREEN, YELLOW;
    }

    public enum LF implements DefaultName<LF> {

        Z1, Z2, Z3, Z4, Z5, Z6, Z7, Z8, Z9, Z10, Z11, Z12, Z13, Z14, Z15, Z16, Z17, Z18, Z19, Z20,
        A, E;

        public String[] getOverlayRename() {
            return new String[] {
                    "overlay", "girsignals:blocks/zs3/" + this.getName().toLowerCase()
            };
        }

    }

    public enum LFBachground implements DefaultName<LFBachground> {
        LF1, LF2, LF3_5, LF4, LF6, LF7;
    }

    public enum EL implements DefaultName<EL> {
        EL1V, EL1, EL2, EL3, EL4, EL5, EL6;
    }

    public enum ELArrow implements Offable<ELArrow> {
        OFF, LEFT, RIGHT, LEFT_RIGHT, UP;
    }

    public enum Arrow implements Offable<Arrow> {
        OFF, ARROW_LEFT, ARROW_RIGHT, ARROW_LEFT_RIGHT;
    }

    public enum RA implements DefaultName<RA> {
        RA10, RA11A, RA11B, RA12, RA6_9;
    }

    public enum RALight implements Offable<RALight> {
        OFF, RA6, RA7, RA8, RA9;
    }

    public enum BUE implements DefaultName<BUE> {
        BUE2_1, BUE2_2, BUE2_3, BUE2_4, BUE3, BUE4, BUE5;
    }

    public enum BUEAdd implements Offable<BUEAdd> {
        OFF, ADD, BUE4;
    }

    public enum OtherSignal implements DefaultName<OtherSignal> {
        HM, OB, CROSS;
    }

    public enum STNumber implements DefaultName<STNumber> {

        Z1, Z2, Z3, Z4, Z5, Z6, Z7, Z8, Z9, Z10, Z11, Z12, Z13, Z14, Z15, Z16, Z17, Z18, Z19, Z20,
        Z21, Z22, Z23, Z24, A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X,
        Y, Z;

        public String[] getOverlayRename() {
            return new String[] {
                    "overlay", "girsignals:blocks/zs3/" + this.getName().toLowerCase()
            };
        }

    }

    public enum NE implements DefaultName<NE> {
        NE1, NE2, NE2_1, NE3_1, NE3_2, NE3_3, NE3_4, NE3_5, NE4, NE4_small, NE5, NE6;
    }

    public enum NEAddition implements Offable<NEAddition> {
        OFF, PRE1, PRE2;
    }

    public enum WNNormal implements Offable<WNNormal> {
        OFF, WN1, WN2, BLINK;
    }

    public enum WNCross implements Offable<WNCross> {
        OFF, WN3, WN4, WN5, WN6, BLINK;
    }

    public static enum EnumMode {
        MANUELL, SINGLE, MUX
    }

    public static enum EnumState {
        DISABLED, OFFSTATE, ONSTATE
    }

    public enum SortOptions implements Offable<SortOptions> {
        DISABLED, NAME_ASSENDING, NAME_DESCENIDNG, TYPE_ASSANDING, TYPE_DESCENDING,
        DISTANCE_ASSANDING, DISTANCE_DSECENDING;
    }
    
    public enum SemaDist implements DefaultName<SemaDist> {
        VR0, VR1, VR2;
    }
}
