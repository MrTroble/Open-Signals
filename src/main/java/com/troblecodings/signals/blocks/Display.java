package com.troblecodings.signals.blocks;

import java.time.LocalTime;
import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;

import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.core.RenderOverlayInfo;
import com.troblecodings.signals.core.TileEntitySupplierWrapper;
import com.troblecodings.signals.tileentitys.DisplayTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Display extends BasicBlock {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final TileEntitySupplierWrapper SUPPLIER = DisplayTileEntity::new;

    private final boolean isDoubleSided;

    public Display(final boolean isDoubleSided) {
        super(Properties.of(Material.STONE));
        this.isDoubleSided = isDoubleSided;
        this.registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING,
                ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    public void onPlace(final BlockState state, final Level level, final BlockPos pos,
            final BlockState newState, final boolean flag) {
        BlockPos rightBlock;
        switch (state.getValue(FACING)) {
            case NORTH:
                rightBlock = pos.west();
                break;
            case EAST:
                rightBlock = pos.north();
                break;
            case SOUTH:
                rightBlock = pos.east();
                break;
            case WEST:
                rightBlock = pos.south();
                break;
            default:
                rightBlock = pos;
                break;
        }
        // TODO Place Ghostblock
        // level.setBlockAndUpdate(rightBlock,
        // OSBlocks.GHOST_BLOCK.defaultBlockState());
    }

    @Override
    protected void createBlockStateDefinition(final Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @OnlyIn(Dist.CLIENT)
    private void renderTime(final RenderOverlayInfo info, final int x, final int y) {
        LocalTime time = LocalTime.now();
        int timeS = time.getSecond();
        int timeM = time.getMinute();
        int timeH = time.getHour();

        info.push();
        info.translate(x, y, 0);
        info.scale(0.5, 0.5, 0);
        info.font.draw(info.stack, String.format("%02d", timeH) + ":" + String.format("%02d", timeM)
                + ":" + String.format("%02d", timeS), 0, 0, 0xFFFFFFFF);
        info.pop();
    }

    @OnlyIn(Dist.CLIENT)
    private void renderAnalogClock(final RenderOverlayInfo info, final int x, final int y) {
        LocalTime time = LocalTime.now();
        int timeS = time.getSecond();
        int timeM = time.getMinute();
        int timeH = time.getHour();

        info.push();
        info.translate(x, y, 0);

        // Background
        info.push();
        info.translate(-13, -13, 0);
        info.drawTexture(new ResourceLocation(OpenSignalsMain.MODID, "gui/textures/clock.png"), 26,
                26, 0, 0, 1, 1);
        info.pop();

        // Hours
        info.push();
        info.rotate(0, 0, (float) ((timeH / 6f) * Math.PI + Math.PI));
        info.translate(-0.75, 0, -0.5);
        info.scale(1.5, 1.3, 1);
        info.font.draw(info.stack, "|", 0, 0, 0xFF000000);
        info.pop();

        // Minutes
        info.push();
        info.rotate(0, 0, (float) ((timeM / 30f) * Math.PI + Math.PI));
        info.translate(-0.5, 0, -0.5);
        info.scale(1, 1.7, 1);
        info.font.draw(info.stack, "|", 0, 0, 0xFF000000);
        info.pop();

        // Seconds
        info.push();
        info.rotate(0, 0, (float) ((timeS / 30f) * Math.PI + Math.PI));
        info.translate(-0.5, 0, -0.5);
        info.scale(1, 1.7, 1);
        info.font.draw(info.stack, "|", 0, 0, 0xFFFF0000);
        info.pop();

        info.pop();
    }

    @OnlyIn(Dist.CLIENT)
    private void renderPlatform(final RenderOverlayInfo info, final int x, final int y) {
        info.push();
        info.translate(x, y, 0);
        info.scale(3, 3, 0);
        info.font.draw(info.stack, "8", 0, 0, 0xFFFFFFFF);
        info.pop();
    }

    @OnlyIn(Dist.CLIENT)
    private void renderDestTime(final RenderOverlayInfo info, final int x, final int y) {
        info.push();
        info.translate(x, y, 0);
        info.font.draw(info.stack, "16:25", 0, 0, 0xFFFFFFFF);

        info.translate(30, 0, 0);
        info.push();
        info.scale(50, 1, 0);
        info.font.draw(info.stack, "|", 0, 0, 0xFFFFFFFF);
        info.pop();
        info.translate(6, 1.5, -0.5);
        info.scale(0.5, 0.5, 0);
        info.font.draw(info.stack, "Fällt heute aus", 0, 0, 0xFF000000);
        info.pop();
    }

    @OnlyIn(Dist.CLIENT)
    private void renderDestination(final RenderOverlayInfo info, final int x, final int y) {
        info.push();
        info.translate(x, y, 0);

        info.push();
        info.scale(1.5, 1.5, 0);
        info.font.draw(info.stack, "Rödau Hbf", 0, 0, 0xFFFFFFFF);
        info.pop();

        info.push();
        info.translate(0, 15, 0);
        info.scale(0.5, 0.5, 0);
        info.font.draw(info.stack, "Presslau - Großpostwitz", 0, 0, 0xFFFFFFFF);
        info.pop();

        info.pop();
    }

    @OnlyIn(Dist.CLIENT)
    private void renderTrain(final RenderOverlayInfo info, final int x, final int y) {
        info.push();
        info.translate(x, y, 0);
        info.scale(0.5, 0.5, 0);
        info.font.draw(info.stack, "RB 31067", 0, 0, 0xFFFFFFFF);
        info.pop();
    }

    private String fullString = "";
    private Queue<Character> shownText = new ArrayDeque<>();
    private Queue<Character> hiddenText = new ArrayDeque<>();
    private float addTransX = 0;
    private float animationSpeed = 0.4f;

    @OnlyIn(Dist.CLIENT)
    public void renderMovingText(final RenderOverlayInfo info, final int x, final int y) {
        info.push();
        info.translate(x, y, 0);

        info.push();
        info.scale(80, 1, 0);
        info.font.draw(info.stack, "|", 0, 0, 0xFFFFFFFF);
        info.pop();
        final String text = "Achtung Witterung! Halten Sie Abstand von der Bahnsteigkante!";
        final int fieldLength = 155;
        initializeTexts(info, text, fieldLength);

        final StringBuilder textToRender = new StringBuilder();
        shownText.stream().forEach(c -> textToRender.append(c));
        final String showString = textToRender.toString();

        final float scale = 0.5f;
        info.translate(addTransX + 0.5f, 2, -0.5);
        info.scale(scale, scale, 0);
        info.font.draw(info.stack, showString, 0, 0, 0xFF000000);

        if (hiddenText.isEmpty()) {
            info.pop();
            return;
        }
        addTransX -= animationSpeed;
        if (addTransX <= 0) {
            final Character toRemove = shownText.poll();
            addTransX = info.font.width(String.valueOf(toRemove)) * scale;
            hiddenText.add(toRemove);

        }
        final char last = hiddenText.element();
        if ((info.font.width(showString) + info.font.width(String.valueOf(last)) < fieldLength)) {
            shownText.add(hiddenText.poll());
        }
        info.pop();
    }

    private void initializeTexts(final RenderOverlayInfo info, final String fullText,
            final int length) {
        if (fullString.equals(fullText))
            return;
        fullString = fullText;
        shownText = new ArrayDeque<>(fullText.length());
        hiddenText = new ArrayDeque<>(fullText.length());
        int textLength = 0;
        for (final char c : fullText.toCharArray()) {
            final String s = String.valueOf(c);
            textLength += info.font.width(s);
            if (textLength > length) {
                hiddenText.add(c);
            } else {
                shownText.add(c);
            }
        }
        if (!hiddenText.isEmpty()) {
            hiddenText.add(' ');
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void renderOverlay(final RenderOverlayInfo info) {
        final float offsetX = -10;
        final float offsetZ = 35;
        final int angleMod;
        final float xMod;
        final float zMod;
        final float doubleXMod;
        final float doubleZMod;
        BlockEntity te = info.tileEntity;
        final BlockState state = te.getLevel().getBlockState(te.getBlockPos());
        if (!(state.getBlock() instanceof Display))
            return;
        switch (state.getValue(FACING)) {
            case NORTH:
                angleMod = 2;
                zMod = -70;
                xMod = -50;
                doubleZMod = -70;
                doubleXMod = -120;
                break;
            case EAST:
                angleMod = 3;
                zMod = -10;
                xMod = -60;
                doubleZMod = -72;
                doubleXMod = -115;
                break;
            case WEST:
                angleMod = 1;
                zMod = -63;
                xMod = 11;
                doubleZMod = -70;
                doubleXMod = -120;
                break;
            default:
                angleMod = 0;
                zMod = 0;
                xMod = 0;
                doubleZMod = -71;
                doubleXMod = -110;
                break;
        }

        info.push();
        info.translate(info.x, info.y + 0.95f, info.z + 0.5f);
        info.scale(-0.015f, 0.015f, 0.015f);
        info.translate(offsetX + xMod, 0, offsetZ + zMod);
        info.scale(-1f, 1f, 1f);
        info.rotate((float) Math.PI, 0, 0);
        info.rotate(0, (float) Math.PI * 0.5f * angleMod, 0);

        renderTime(info, 90, 30);
        renderDestTime(info, 0, 3);
        renderTrain(info, 0, 13);
        renderDestination(info, 0, 25);
        renderAnalogClock(info, 100, 15);
        renderPlatform(info, 92, 35);
        renderMovingText(info, 0, 48);

        if (this.isDoubleSided) {
            info.rotate(0, (float) Math.PI, 0);
            info.translate(doubleXMod, 0, doubleZMod);

            renderTime(info, 90, 30);
            renderDestTime(info, 0, 3);
            renderTrain(info, 0, 13);
            renderDestination(info, 0, 25);
            renderAnalogClock(info, 100, 15);
            renderPlatform(info, 92, 35);
            renderMovingText(info, 0, 48);
        }
        info.pop();
    }

    @Override
    public Optional<TileEntitySupplierWrapper> getSupplierWrapper() {
        return Optional.of(SUPPLIER);
    }

    @Override
    public Optional<String> getSupplierWrapperName() {
        return Optional.of("display");
    }

}
