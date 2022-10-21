package com.troblecodings.signals.init;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import com.troblecodings.signals.GirsignalsMain;
import com.troblecodings.signals.blocks.GhostBlock;
import com.troblecodings.signals.blocks.IConfigUpdatable;
import com.troblecodings.signals.blocks.Post;
import com.troblecodings.signals.blocks.RedstoneIO;
import com.troblecodings.signals.blocks.RedstoneInput;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.blocks.SignalBox;
import com.troblecodings.signals.blocks.SignalController;
import com.troblecodings.signals.blocks.SignalLoader;
import com.troblecodings.signals.blocks.boards.SignalAndreasCross;
import com.troblecodings.signals.blocks.boards.SignalBUE;
import com.troblecodings.signals.blocks.boards.SignalBUELight;
import com.troblecodings.signals.blocks.boards.SignalEL;
import com.troblecodings.signals.blocks.boards.SignalLF;
import com.troblecodings.signals.blocks.boards.SignalNE;
import com.troblecodings.signals.blocks.boards.SignalOther;
import com.troblecodings.signals.blocks.boards.SignalRA;
import com.troblecodings.signals.blocks.boards.SignalStationName;
import com.troblecodings.signals.blocks.boards.SignalWN;
import com.troblecodings.signals.blocks.signals.SignalHL;
import com.troblecodings.signals.blocks.signals.SignalHV;
import com.troblecodings.signals.blocks.signals.SignalKS;
import com.troblecodings.signals.blocks.signals.SignalSHLight;
import com.troblecodings.signals.blocks.signals.SignalSHMech;
import com.troblecodings.signals.blocks.signals.SignalSemaphore;
import com.troblecodings.signals.blocks.signals.SignalTram;

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

public final class GIRBlocks {

    private GIRBlocks() {
    }

    public static final SignalController HV_SIGNAL_CONTROLLER = new SignalController();
    public static final Post POST = new Post();
    public static final SignalHV HV_SIGNAL = new SignalHV();
    public static final GhostBlock GHOST_BLOCK = new GhostBlock();
    public static final SignalKS KS_SIGNAL = new SignalKS();
    public static final SignalHL HL_SIGNAL = new SignalHL();
    public static final SignalSHLight SH_LIGHT = new SignalSHLight();
    public static final SignalTram TRAM_SIGNAL = new SignalTram();
    public static final SignalLF LF_SIGNAL = new SignalLF();
    public static final SignalEL EL_SIGNAL = new SignalEL();
//    public static final Signal SH_SIGNAL = new Signal(
//            Signal.builder(GIRItems.SIGN_PLACEMENT_TOOL, "shsignal").noLink().build());
    public static final SignalRA RA_SIGNAL = new SignalRA();
    public static final SignalBUE BUE_SIGNAL = new SignalBUE();
    public static final SignalBUELight BUE_LIGHT = new SignalBUELight();
    public static final SignalOther OTHER_SIGNAL = new SignalOther();
    public static final SignalNE NE_SIGNAL = new SignalNE();
//    public static final StationNumberPlate STATION_NUMBER_PLATE = new StationNumberPlate();
    public static final SignalWN WN_SIGNAL = new SignalWN();
    public static final SignalStationName STATION_NAME = new SignalStationName();
    public static final SignalBox SIGNAL_BOX = new SignalBox();
    public static final RedstoneIO REDSTONE_IN = new RedstoneInput();
    public static final RedstoneIO REDSTONE_OUT = new RedstoneIO();
    public static final SignalSemaphore SEMAPHORE_SIGNAL = new SignalSemaphore();
    public static final SignalSHMech SH_MECH = new SignalSHMech();
    public static final SignalAndreasCross ANDREAS_CROSS = new SignalAndreasCross();

    public static ArrayList<Block> blocksToRegister = new ArrayList<>();

    public static void init() {
        final Field[] fields = GIRBlocks.class.getFields();
        for (final Field field : fields) {
            final int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers)
                    && Modifier.isPublic(modifiers)) {
                final String name = field.getName().toLowerCase().replace("_", "");
                try {
                    final Block block = (Block) field.get(null);
                    block.setRegistryName(new ResourceLocation(GirsignalsMain.MODID, name));
                    block.setUnlocalizedName(name);
                    blocksToRegister.add(block);
                    if (block instanceof ITileEntityProvider) {
                        final ITileEntityProvider provider = (ITileEntityProvider) block;
                        try {
                            final Class<? extends TileEntity> tileclass = provider
                                    .createNewTileEntity(null, 0).getClass();
                            if (TileEntity.getKey(tileclass) == null)
                                TileEntity.register(tileclass.getSimpleName().toLowerCase(),
                                        tileclass);
                        } catch (final NullPointerException ex) {
                            GirsignalsMain.log.trace(
                                    "All tileentity provide need to call back a default entity if the world is null!",
                                    ex);
                        }
                    }
                } catch (final IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        final List<Signal> signals = new ArrayList<>(SignalLoader.getSignals());
        signals.forEach(signal -> {
            try {
                final Block block = (Block) signal;
                block.setRegistryName(
                        new ResourceLocation(GirsignalsMain.MODID, signal.getSignalTypeName()));
                block.setUnlocalizedName(signal.getSignalTypeName());
                blocksToRegister.add(block);
                if (block instanceof ITileEntityProvider) {
                    final ITileEntityProvider provider = (ITileEntityProvider) block;
                    try {
                        final Class<? extends TileEntity> tileclass = provider
                                .createNewTileEntity(null, 0).getClass();
                        if (TileEntity.getKey(tileclass) == null)
                            TileEntity.register(tileclass.getSimpleName().toLowerCase(), tileclass);
                    } catch (final NullPointerException ex) {
                        GirsignalsMain.log.trace(
                                "All tileentity provide need to call back a default entity if the world is null!",
                                ex);
                    }
                }
            } catch (final IllegalArgumentException e) {
                e.printStackTrace();
            }
        });
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
        if (event.getModID().equals(GirsignalsMain.MODID)) {
            ConfigManager.sync(GirsignalsMain.MODID, Type.INSTANCE);
            updateConfigs();
        }
    }
}