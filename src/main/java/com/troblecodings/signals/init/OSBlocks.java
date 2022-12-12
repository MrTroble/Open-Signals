package com.troblecodings.signals.init;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.GhostBlock;
import com.troblecodings.signals.blocks.Post;
import com.troblecodings.signals.blocks.RedstoneIO;
import com.troblecodings.signals.blocks.RedstoneInput;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.blocks.SignalBox;
import com.troblecodings.signals.blocks.SignalController;
import com.troblecodings.signals.blocks.SignalLoader;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

public final class OSBlocks {

    private OSBlocks() {
    }

    public static final SignalController HV_SIGNAL_CONTROLLER = new SignalController();
    public static final Post POST = new Post();
    public static final GhostBlock GHOST_BLOCK = new GhostBlock();
    public static final SignalBox SIGNAL_BOX = new SignalBox();
    public static final RedstoneIO REDSTONE_IN = new RedstoneInput();
    public static final RedstoneIO REDSTONE_OUT = new RedstoneIO();

    public static ArrayList<Block> blocksToRegister = new ArrayList<>();

    public static void init() {
        final Field[] fields = OSBlocks.class.getFields();
        for (final Field field : fields) {
            final int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers)
                    && Modifier.isPublic(modifiers)) {
                final String name = field.getName().toLowerCase().replace("_", "");
                try {
                    loadBlock((Block) field.get(null), name);
                } catch (final IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        SignalLoader.loadInternSignals();
    }

    public static void loadBlock(final Block block, final String pName) {
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
    public static void registerItem(final RegistryEvent.Register<Item> event) {
        final IForgeRegistry<Item> registry = event.getRegistry();
        blocksToRegister.forEach(block -> registry
                .register(new BlockItem(block, new Properties().tab(OSTabs.TAB)).setRegistryName(block.getRegistryName())));
    }

}
