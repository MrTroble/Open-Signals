package net.gir.girsignals.blocks;

import java.util.Collection;

import net.gir.girsignals.EnumsHV.BinaryExtensionSignals;
import net.gir.girsignals.EnumsHV.HPVR;
import net.gir.girsignals.EnumsHV.ZS2;
import net.gir.girsignals.EnumsHV.ZS3;
import net.gir.girsignals.init.GIRTabs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.property.Properties.PropertyAdapter;

public class HVSignal extends Block implements ITileEntityProvider {

	public HVSignal() {
		super(Material.ROCK);
		setCreativeTab(GIRTabs.tab);
		setDefaultState(getDefaultState().withProperty(FACING, EnumFacing.NORTH));
	}

	public static final PropertyDirection FACING = BlockHorizontal.FACING;
	public static final PropertyAdapter<HPVR> HAUPTSIGNAL = new PropertyAdapter<HPVR>(
			PropertyEnum.create("hauptsignale", HPVR.class));
	public static final PropertyAdapter<HPVR> VORSIGNAL = new PropertyAdapter<HPVR>(
			PropertyEnum.create("vorsignale", HPVR.class));
	public static final PropertyAdapter<Boolean> RANGIERSIGNAL = new PropertyAdapter<Boolean>(
			PropertyBool.create("rangiersignale"));
	public static final PropertyAdapter<ZS3> ZS3 = new PropertyAdapter<ZS3>(PropertyEnum.create("zs3", ZS3.class));
	public static final PropertyAdapter<Boolean> ZS3LS = new PropertyAdapter<Boolean>(PropertyBool.create("zs3ls"));
	public static final PropertyAdapter<ZS3> ZS3V = new PropertyAdapter<ZS3>(PropertyEnum.create("zs3v", ZS3.class));
	public static final PropertyAdapter<Boolean> ZS3VLS = new PropertyAdapter<Boolean>(PropertyBool.create("zs3vls"));
	public static final PropertyAdapter<BinaryExtensionSignals> ZS1 = new PropertyAdapter<BinaryExtensionSignals>(
			PropertyEnum.create("zs1", BinaryExtensionSignals.class));
	public static final PropertyAdapter<BinaryExtensionSignals> ZS6 = new PropertyAdapter<BinaryExtensionSignals>(
			PropertyEnum.create("zs6", BinaryExtensionSignals.class));
	public static final PropertyAdapter<BinaryExtensionSignals> ZS8 = new PropertyAdapter<BinaryExtensionSignals>(
			PropertyEnum.create("zs8", BinaryExtensionSignals.class));
	public static final PropertyAdapter<ZS2> ZS2 = new PropertyAdapter<ZS2>(PropertyEnum.create("zs2", ZS2.class));
	public static final PropertyAdapter<ZS2> ZS2V = new PropertyAdapter<ZS2>(PropertyEnum.create("zs2v", ZS2.class));
	public static final PropertyAdapter<BinaryExtensionSignals> ZS7 = new PropertyAdapter<BinaryExtensionSignals>(
			PropertyEnum.create("zs7", BinaryExtensionSignals.class));

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY,
			float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
		return getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer,
			ItemStack stack) {
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
		((SignalTileEnity)worldIn.getTileEntity(pos)).setProprty(HAUPTSIGNAL, HPVR.HpVr0);
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
	public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
		return layer.equals(BlockRenderLayer.CUTOUT);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		IExtendedBlockState ebs = (IExtendedBlockState) super.getExtendedState(state, world, pos);
		SignalTileEnity entity = (SignalTileEnity) world.getTileEntity(pos);
		if(entity != null)
			return entity.foreach((b, p, o) -> b.withProperty((IUnlistedProperty)p, o), ebs);
		return ebs;
	}
	
	@Override
	public boolean isTranslucent(IBlockState state) {
		return true;
	}
 	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	private IUnlistedProperty<?>[] propcache = null;
	
	private void buildCacheIfNull() {
		if (propcache == null) {
			Collection<IUnlistedProperty<?>> props = ((ExtendedBlockState) this.getBlockState())
					.getUnlistedProperties();
			propcache = props.toArray(new IUnlistedProperty[props.size()]);
		}
	}

	public int getIDFromProperty(final IUnlistedProperty<?> propertyIn) {
		buildCacheIfNull();
		for (int i = 0; i < propcache.length; i++)
			if(propcache[i].equals(propertyIn))
				return i;
		return -1;
	}
	
	public IUnlistedProperty<?> getPropertyFromID(int id) {
		buildCacheIfNull();
		return propcache[id];
	}
		
	@Override
	protected BlockStateContainer createBlockState() {
		return new ExtendedBlockState(this, new IProperty<?>[] { FACING }, new IUnlistedProperty<?>[] { HAUPTSIGNAL,
				VORSIGNAL, RANGIERSIGNAL, ZS1, ZS2, ZS2V, ZS3, ZS3LS, ZS3V, ZS3VLS, ZS6, ZS7, ZS8 });
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new SignalTileEnity();
	}

}
