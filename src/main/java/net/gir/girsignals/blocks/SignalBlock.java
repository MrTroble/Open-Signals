package net.gir.girsignals.blocks;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;

import net.gir.girsignals.init.GIRTabs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
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

public class SignalBlock extends Block implements ITileEntityProvider {

	public static final PropertyDirection FACING = BlockHorizontal.FACING;

	private final String signalTypeName;
	
	public SignalBlock(String signalTypeName) {
		super(Material.ROCK);
		this.signalTypeName = signalTypeName;
		setCreativeTab(GIRTabs.tab);
		setDefaultState(getDefaultState().withProperty(FACING, EnumFacing.NORTH));
	}

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
	public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
		return layer.equals(BlockRenderLayer.CUTOUT);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		IExtendedBlockState ebs = (IExtendedBlockState) super.getExtendedState(state, world, pos);
		SignalTileEnity entity = (SignalTileEnity) world.getTileEntity(pos);
		if (entity != null)
			return entity.foreach((b, p, o) -> b.withProperty((IUnlistedProperty) p, o), ebs);
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
			if (propcache[i].equals(propertyIn))
				return i;
		return -1;
	}

	public IUnlistedProperty<?> getPropertyFromID(int id) {
		buildCacheIfNull();
		return propcache[id];
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected BlockStateContainer createBlockState() {
		ArrayList<IUnlistedProperty> prop = new ArrayList<>();
		for (Field f : this.getClass().getDeclaredFields()) {
			int mods = f.getModifiers();
			if (Modifier.isFinal(mods) && Modifier.isStatic(mods) && Modifier.isPublic(mods)) {
				try {
					prop.add((IUnlistedProperty) f.get(null));
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		return new ExtendedBlockState(this, new IProperty<?>[] { FACING },
				prop.toArray(new IUnlistedProperty[prop.size()]));
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new SignalTileEnity();
	}

	public String getSignalTypeName() {
		return signalTypeName;
	}
}
