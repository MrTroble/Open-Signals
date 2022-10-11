package eu.gir.girsignals.blocks.boards;

import java.util.Map;
import java.util.Random;

import eu.gir.girsignals.EnumSignals.ACAddition;
import eu.gir.girsignals.EnumSignals.ACCar;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.init.GIRItems;
import eu.gir.girsignals.init.GIRSounds;
import eu.gir.girsignals.tileentitys.SignalTileEnity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SignalAndreasCross extends Signal {

    public SignalAndreasCross() {
        super(builder(GIRItems.SIGN_PLACEMENT_TOOL, "andreas_cross").height(2).build());
    }

    public static final SEProperty<Boolean> ELECTRICITY = SEProperty.of("ac_electricity", false,
            ChangeableStage.GUISTAGE);
    public static final SEProperty<ACAddition> AC_ADDITION = SEProperty.of("ac_addition",
            ACAddition.OFF, ChangeableStage.GUISTAGE);
    public static final SEProperty<Boolean> AC_BLINK_LIGHT = SEProperty.of("ac_blink_light", false,
            ChangeableStage.APISTAGE_NONE_CONFIG, true,
            check(AC_ADDITION, ACAddition.BLINK1).or(check(AC_ADDITION, ACAddition.BLINK2)));
    public static final SEProperty<ACCar> AC_CAR = SEProperty.of("ac_car", ACCar.OFF,
            ChangeableStage.APISTAGE_NONE_CONFIG, true,
            check(AC_ADDITION, ACAddition.TRAFFIC_LIGHT));
    public static final SEProperty<Boolean> AC_SOUND = SEProperty.of("ac_sound", false,
            ChangeableStage.GUISTAGE);

    public boolean checkDoesSound(final IBlockState state, final World world, final BlockPos pos) {
        final TileEntity tile = world.getTileEntity(pos);

        if (!(tile instanceof SignalTileEnity))
            return false;

        final SignalTileEnity signalTE = (SignalTileEnity) tile;

        final Map<SEProperty<?>, Object> signalProperties = signalTE.getProperties();

        boolean isCarAndOn = signalTE.getProperty(AC_CAR)
                .filter(car -> ACCar.YELLOW.equals(car) || ACCar.RED.equals(car)).isPresent();

        if (signalProperties.containsKey(AC_SOUND)
                && signalProperties.containsKey(AC_BLINK_LIGHT)) {

            return (Boolean) signalProperties.get(AC_SOUND)
                    && ((Boolean) signalProperties.get(AC_BLINK_LIGHT) || isCarAndOn);
        }
        return false;
    }

    @Override
    public void getUpdate(final World world, final BlockPos pos) {
        if (world.isUpdateScheduled(pos, this)) {
            return;
        }
        if (checkDoesSound(world.getBlockState(pos), world, pos)) {
            world.scheduleUpdate(pos, this, 1);
        }
    }

    @Override
    public void updateTick(final World world, final BlockPos pos, final IBlockState state,
            final Random rand) {
        if (world.isRemote) {
            return;
        }
        if (checkDoesSound(state, world, pos)) {
            world.playSound(null, pos, GIRSounds.andreascross, SoundCategory.BLOCKS, 1.0F, 1.0F);
            world.scheduleUpdate(pos, this, 84);
        }
    }
}
