package com.troblecodings.signals.init;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.BasicBlock;
import com.troblecodings.signals.blocks.GhostBlock;
import com.troblecodings.signals.blocks.Post;
import com.troblecodings.signals.blocks.RedstoneIO;
import com.troblecodings.signals.blocks.RedstoneInput;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.blocks.SignalBox;
import com.troblecodings.signals.blocks.SignalController;
import com.troblecodings.signals.core.SignalLoader;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

public final class OSBlocks {

    private OSBlocks() {
    }

    public static final BasicBlock HV_SIGNAL_CONTROLLER = new SignalController();
    public static final BasicBlock POST = new Post();
    public static final BasicBlock GHOST_BLOCK = new GhostBlock();
    public static final BasicBlock SIGNAL_BOX = new SignalBox();
    public static final BasicBlock REDSTONE_IN = new RedstoneInput();
    public static final BasicBlock REDSTONE_OUT = new RedstoneIO();

    public static final List<BasicBlock> blocksToRegister = new ArrayList<>();

    public static void init() {
        final Field[] fields = OSBlocks.class.getFields();
        for (final Field field : fields) {
            final int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers)
                    && Modifier.isPublic(modifiers)) {
                final String name = field.getName().toLowerCase().replace("_", "");
                try {
                    loadBlock((BasicBlock) field.get(null), name);
                } catch (final IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        SignalLoader.loadAllSignals();
    }

    public static void loadBlock(final BasicBlock block, final String pName) {
        if (blocksToRegister.contains(block))
            return;

        final String name = pName.toLowerCase().trim();
        block.setRegistryName(new ResourceLocation(OpenSignalsMain.MODID, name));
        blocksToRegister.add(block);
        if (block instanceof Signal)
            Signal.SIGNALS.put(name, (Signal) block);
    }

    @SubscribeEvent
    public static void registerBlock(final RegistryEvent.Register<Block> event) {
        final IForgeRegistry<Block> registry = event.getRegistry();
        blocksToRegister.forEach(registry::register);
    }

    @SubscribeEvent
    public static void registerBlockEntitys(
            final RegistryEvent.Register<BlockEntityType<?>> event) {
        final IForgeRegistry<BlockEntityType<?>> registry = event.getRegistry();
        BasicBlock.prepare();
        BasicBlock.BLOCK_ENTITYS.values().forEach(registry::register);
    }

    @SubscribeEvent
    public static void registerItem(final RegistryEvent.Register<Item> event) {
        final IForgeRegistry<Item> registry = event.getRegistry();
        blocksToRegister.forEach(
                block -> registry.register(new BlockItem(block, new Properties().tab(OSTabs.TAB))
                        .setRegistryName(block.getRegistryName())));
    }

}
