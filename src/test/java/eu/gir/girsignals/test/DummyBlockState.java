package eu.gir.girsignals.test;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;

import net.minecraft.block.Block;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

@SuppressWarnings({
        "rawtypes", "unchecked"
})
public class DummyBlockState implements IExtendedBlockState {

    private final Map<IUnlistedProperty, Object> map = new HashMap<>();

    public DummyBlockState(final IUnlistedProperty property, final Object value) {
        super();
        map.put(property, value);
    }

    public DummyBlockState(final Map<IUnlistedProperty, Object> in) {
        this.map.putAll(in);
    }

    public DummyBlockState put(final IUnlistedProperty prop, final Object obj) {
        map.put(prop, obj);
        return this;
    }

    @Override
    public Collection<IProperty<?>> getPropertyKeys() {
        return null;
    }

    @Override
    public <T extends Comparable<T>> T getValue(final IProperty<T> property) {
        return null;
    }

    @Override
    public <T extends Comparable<T>, V extends T> IBlockState withProperty(
            final IProperty<T> property, final V value) {
        return null;
    }

    @Override
    public <T extends Comparable<T>> IBlockState cycleProperty(final IProperty<T> property) {
        return null;
    }

    @Override
    public ImmutableMap<IProperty<?>, Comparable<?>> getProperties() {
        return null;
    }

    @Override
    public Block getBlock() {
        return null;
    }

    @Override
    public boolean onBlockEventReceived(final World worldIn, final BlockPos pos, final int id,
            final int param) {
        return false;
    }

    @Override
    public void neighborChanged(final World worldIn, final BlockPos pos, final Block blockIn,
            final BlockPos fromPos) {

    }

    @Override
    public Material getMaterial() {

        return null;
    }

    @Override
    public boolean isFullBlock() {

        return false;
    }

    @Override
    public boolean canEntitySpawn(final Entity entityIn) {

        return false;
    }

    @Override
    public int getLightOpacity() {

        return 0;
    }

    @Override
    public int getLightOpacity(final IBlockAccess world, final BlockPos pos) {

        return 0;
    }

    @Override
    public int getLightValue() {

        return 0;
    }

    @Override
    public int getLightValue(final IBlockAccess world, final BlockPos pos) {

        return 0;
    }

    @Override
    public boolean isTranslucent() {

        return false;
    }

    @Override
    public boolean useNeighborBrightness() {

        return false;
    }

    @Override
    public MapColor getMapColor(final IBlockAccess access, final BlockPos pos) {

        return null;
    }

    @Override
    public IBlockState withRotation(final Rotation rot) {

        return null;
    }

    @Override
    public IBlockState withMirror(final Mirror mirrorIn) {

        return null;
    }

    @Override
    public boolean isFullCube() {

        return false;
    }

    @Override
    public boolean hasCustomBreakingProgress() {

        return false;
    }

    @Override
    public EnumBlockRenderType getRenderType() {

        return null;
    }

    @Override
    public int getPackedLightmapCoords(final IBlockAccess source, final BlockPos pos) {

        return 0;
    }

    @Override
    public float getAmbientOcclusionLightValue() {

        return 0;
    }

    @Override
    public boolean isBlockNormalCube() {

        return false;
    }

    @Override
    public boolean isNormalCube() {

        return false;
    }

    @Override
    public boolean canProvidePower() {

        return false;
    }

    @Override
    public int getWeakPower(final IBlockAccess blockAccess, final BlockPos pos,
            final EnumFacing side) {

        return 0;
    }

    @Override
    public boolean hasComparatorInputOverride() {

        return false;
    }

    @Override
    public int getComparatorInputOverride(final World worldIn, final BlockPos pos) {

        return 0;
    }

    @Override
    public float getBlockHardness(final World worldIn, final BlockPos pos) {

        return 0;
    }

    @Override
    public float getPlayerRelativeBlockHardness(final EntityPlayer player, final World worldIn,
            final BlockPos pos) {

        return 0;
    }

    @Override
    public int getStrongPower(final IBlockAccess blockAccess, final BlockPos pos,
            final EnumFacing side) {

        return 0;
    }

    @Override
    public EnumPushReaction getMobilityFlag() {

        return null;
    }

    @Override
    public IBlockState getActualState(final IBlockAccess blockAccess, final BlockPos pos) {

        return null;
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBox(final World worldIn, final BlockPos pos) {

        return null;
    }

    @Override
    public boolean shouldSideBeRendered(final IBlockAccess blockAccess, final BlockPos pos,
            final EnumFacing facing) {

        return false;
    }

    @Override
    public boolean isOpaqueCube() {

        return false;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(final IBlockAccess worldIn, final BlockPos pos) {

        return null;
    }

    @Override
    public void addCollisionBoxToList(final World worldIn, final BlockPos pos,
            final AxisAlignedBB entityBox, final List<AxisAlignedBB> collidingBoxes,
            final Entity entityIn, final boolean flag) {

    }

    @Override
    public AxisAlignedBB getBoundingBox(final IBlockAccess blockAccess, final BlockPos pos) {

        return null;
    }

    @Override
    public RayTraceResult collisionRayTrace(final World worldIn, final BlockPos pos,
            final Vec3d start, final Vec3d end) {

        return null;
    }

    @Override
    public boolean isTopSolid() {

        return false;
    }

    @Override
    public boolean doesSideBlockRendering(final IBlockAccess world, final BlockPos pos,
            final EnumFacing side) {

        return false;
    }

    @Override
    public boolean isSideSolid(final IBlockAccess world, final BlockPos pos,
            final EnumFacing side) {

        return false;
    }

    @Override
    public boolean doesSideBlockChestOpening(final IBlockAccess world, final BlockPos pos,
            final EnumFacing side) {

        return false;
    }

    @Override
    public Vec3d getOffset(final IBlockAccess access, final BlockPos pos) {

        return null;
    }

    @Override
    public boolean causesSuffocation() {

        return false;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(final IBlockAccess worldIn, final BlockPos pos,
            final EnumFacing facing) {

        return null;
    }

    @Override
    public Collection<IUnlistedProperty<?>> getUnlistedNames() {

        return null;
    }

    @Override
    public <V> V getValue(final IUnlistedProperty<V> property) {
        final Object value = map.get(property);
        return (V) value;
    }

    @Override
    public <V> IExtendedBlockState withProperty(final IUnlistedProperty<V> property,
            final V value) {
        return null;
    }

    @Override
    public ImmutableMap<IUnlistedProperty<?>, Optional<?>> getUnlistedProperties() {

        return null;
    }

    @Override
    public IBlockState getClean() {

        return null;
    }

}
