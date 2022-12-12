package com.troblecodings.signals.test;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;

import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.core.BlockPos;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AABB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LevelAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.property.IModelData;
import net.minecraftforge.common.property.SEProperty;

@SuppressWarnings({
        "rawtypes", "unchecked"
})
public class DummyBlockState implements IModelData {

    private final Map<SEProperty, Object> map = new HashMap<>();

    public DummyBlockState(final SEProperty property, final Object value) {
        super();
        map.put(property, value);
    }

    public DummyBlockState(final Map<SEProperty, Object> in) {
        this.map.putAll(in);
    }

    public DummyBlockState put(final SEProperty prop, final Object obj) {
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
    public <T extends Comparable<T>, V extends T> BlockState withProperty(
            final IProperty<T> property, final V value) {
        return null;
    }

    @Override
    public <T extends Comparable<T>> BlockState cycleProperty(final IProperty<T> property) {
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
    public boolean onBlockEventReceived(final Level worldIn, final BlockPos pos, final int id,
            final int param) {
        return false;
    }

    @Override
    public void neighborChanged(final Level worldIn, final BlockPos pos, final Block blockIn,
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
    public int getLightOpacity(final LevelAccessor world, final BlockPos pos) {

        return 0;
    }

    @Override
    public int getLightValue() {

        return 0;
    }

    @Override
    public int getLightValue(final LevelAccessor world, final BlockPos pos) {

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
    public MapColor getMapColor(final LevelAccessor access, final BlockPos pos) {

        return null;
    }

    @Override
    public BlockState withRotation(final Rotation rot) {

        return null;
    }

    @Override
    public BlockState withMirror(final Mirror mirrorIn) {

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
    public int getPackedLightmapCoords(final LevelAccessor source, final BlockPos pos) {

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
    public int getWeakPower(final LevelAccessor blockAccess, final BlockPos pos,
            final Direction side) {

        return 0;
    }

    @Override
    public boolean hasComparatorInputOverride() {

        return false;
    }

    @Override
    public int getComparatorInputOverride(final Level worldIn, final BlockPos pos) {

        return 0;
    }

    @Override
    public float getBlockHardness(final Level worldIn, final BlockPos pos) {

        return 0;
    }

    @Override
    public float getPlayerRelativeBlockHardness(final Player player, final Level worldIn,
            final BlockPos pos) {

        return 0;
    }

    @Override
    public int getStrongPower(final LevelAccessor blockAccess, final BlockPos pos,
            final Direction side) {

        return 0;
    }

    @Override
    public EnumPushReaction getMobilityFlag() {

        return null;
    }

    @Override
    public BlockState getActualState(final LevelAccessor blockAccess, final BlockPos pos) {

        return null;
    }

    @Override
    public AABB getSelectedBoundingBox(final Level worldIn, final BlockPos pos) {

        return null;
    }

    @Override
    public boolean shouldSideBeRendered(final LevelAccessor blockAccess, final BlockPos pos,
            final Direction facing) {

        return false;
    }

    @Override
    public boolean isOpaqueCube() {

        return false;
    }

    @Override
    public AABB getCollisionBoundingBox(final LevelAccessor worldIn, final BlockPos pos) {

        return null;
    }

    @Override
    public void addCollisionBoxToList(final Level worldIn, final BlockPos pos,
            final AABB entityBox, final List<AABB> collidingBoxes,
            final Entity entityIn, final boolean flag) {

    }

    @Override
    public AABB getBoundingBox(final LevelAccessor blockAccess, final BlockPos pos) {

        return null;
    }

    @Override
    public RayTraceResult collisionRayTrace(final Level worldIn, final BlockPos pos,
            final Vec3d start, final Vec3d end) {

        return null;
    }

    @Override
    public boolean isTopSolid() {

        return false;
    }

    @Override
    public boolean doesSideBlockRendering(final LevelAccessor world, final BlockPos pos,
            final Direction side) {

        return false;
    }

    @Override
    public boolean isSideSolid(final LevelAccessor world, final BlockPos pos,
            final Direction side) {

        return false;
    }

    @Override
    public boolean doesSideBlockChestOpening(final LevelAccessor world, final BlockPos pos,
            final Direction side) {

        return false;
    }

    @Override
    public Vec3d getOffset(final LevelAccessor access, final BlockPos pos) {

        return null;
    }

    @Override
    public boolean causesSuffocation() {

        return false;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(final LevelAccessor worldIn, final BlockPos pos,
            final Direction facing) {

        return null;
    }

    @Override
    public Collection<SEProperty<?>> getUnlistedNames() {

        return null;
    }

    @Override
    public <V> V getValue(final SEProperty<V> property) {
        final Object value = map.get(property);
        return (V) value;
    }

    @Override
    public <V> IModelData withProperty(final SEProperty<V> property,
            final V value) {
        return null;
    }

    @Override
    public ImmutableMap<SEProperty<?>, Optional<?>> getUnlistedProperties() {

        return null;
    }

    @Override
    public BlockState getClean() {

        return null;
    }

}
