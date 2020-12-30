package net.gir.girsignals.blocks;

import net.gir.girsignals.EnumsHV.BinaryExtensionSignals;
import net.gir.girsignals.EnumsHV.HPVR;
import net.gir.girsignals.EnumsHV.ZS2;
import net.gir.girsignals.EnumsHV.ZS3;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class HVSignal extends Block {

	public HVSignal() {
		super(Material.ROCK);
		setDefaultState(getDefaultState().withProperty(FACING, EnumFacing.NORTH));
	}

	public static final PropertyDirection FACING = BlockHorizontal.FACING;
	public static final PropertyEnum<HPVR> HAUPTSIGNAL = PropertyEnum.create("Hauptsignale", HPVR.class);
	public static final PropertyEnum<HPVR> VORSIGNAL = PropertyEnum.create("Vorsignale", HPVR.class);
	public static final PropertyBool RANGIERSIGNAL = PropertyBool.create("Rangiersignale");
	public static final PropertyEnum<ZS3> ZUSATZSIGNAL3 = PropertyEnum.create("Zusatzsignale 3", ZS3.class);
	public static final PropertyBool ZS3VLS = PropertyBool.create("Zusatzsignale 3 VLS");
	public static final PropertyEnum<BinaryExtensionSignals> ZS1 = PropertyEnum.create("Zusatzsignale 1",
			BinaryExtensionSignals.class);
	public static final PropertyEnum<BinaryExtensionSignals> ZS6 = PropertyEnum.create("Zusatzsignale 6",
			BinaryExtensionSignals.class);
	public static final PropertyEnum<BinaryExtensionSignals> ZS8 = PropertyEnum.create("Zusatzsignale 8",
			BinaryExtensionSignals.class);
	public static final PropertyEnum<BinaryExtensionSignals> ZS7 = PropertyEnum.create("Zusatzsignale 7",
			BinaryExtensionSignals.class);
	public static final PropertyEnum<ZS2> ZS2 = PropertyEnum.create("Zusatzsignale 2", ZS2.class);

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY,
			float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
		return getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getHorizontalIndex();
	}

	@Override
	public IBlockState withRotation(IBlockState state, Rotation rot) {
		return state.withRotation(rot);
	}

	@Override
	public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
		return state.withMirror(mirrorIn);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING);
	}

}
