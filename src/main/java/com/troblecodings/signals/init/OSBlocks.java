package com.troblecodings.signals.init;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.GhostBlock;
import com.troblecodings.signals.blocks.IConfigUpdatable;
import com.troblecodings.signals.blocks.Post;
import com.troblecodings.signals.blocks.RedstoneIO;
import com.troblecodings.signals.blocks.RedstoneInput;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.blocks.SignalBox;
import com.troblecodings.signals.blocks.SignalController;
import com.troblecodings.signals.blocks.SignalLoader;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config.Type;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
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
        block.setUnlocalizedName(name);
        blocksToRegister.add(block);
        if (block instanceof Signal)
            Signal.SIGNALS.put(name, (Signal) block);
        if (block instanceof ITileEntityProvider) {
            final ITileEntityProvider provider = (ITileEntityProvider) block;
            try {
                final Class<? extends TileEntity> tileclass = provider.createNewTileEntity(null, 0)
                        .getClass();
                if (TileEntity.getKey(tileclass) == null)
                    TileEntity.register(tileclass.getSimpleName().toLowerCase(), tileclass);
            } catch (final NullPointerException ex) {
                OpenSignalsMain.log.trace(
                        "All tileentity provide need to call back a default entity if the world is null!",
                        ex);
            }
        }
    }

    @SubscribeEvent
    public static void registerBlock(final RegistryEvent.Register<Block> event) {
        updateConfigs();
        final IForgeRegistry<Block> registry = event.getRegistry();
        blocksToRegister.forEach(registry::register);
    }

    @SubscribeEvent
    public static void registerItem(final RegistryEvent.Register<Item> event) {
        final IForgeRegistry<Item> registry = event.getRegistry();
        blocksToRegister.forEach(block -> registry
                .register(new ItemBlock(block).setRegistryName(block.getRegistryName())));
    }

    private static void updateConfigs() {
        blocksToRegister.forEach(b -> {
            if (b instanceof IConfigUpdatable) {
                final IConfigUpdatable configUpdate = (IConfigUpdatable) b;
                configUpdate.updateConfigValues();
            }
        });
    }

    @SubscribeEvent
    public static void onConfigChangedEvent(final OnConfigChangedEvent event) {
        if (event.getModID().equals(OpenSignalsMain.MODID)) {
            ConfigManager.sync(OpenSignalsMain.MODID, Type.INSTANCE);
            updateConfigs();
        }
    }
}
