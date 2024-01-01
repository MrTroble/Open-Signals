package com.troblecodings.signals.init;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.blocks.BasicBlock;
import com.troblecodings.signals.blocks.CombinedRedstoneInput;
import com.troblecodings.signals.blocks.GhostBlock;
import com.troblecodings.signals.blocks.PathwayRequester;
import com.troblecodings.signals.blocks.Post;
import com.troblecodings.signals.blocks.RedstoneIO;
import com.troblecodings.signals.blocks.RedstoneInput;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.blocks.SignalBox;
import com.troblecodings.signals.blocks.SignalController;
import com.troblecodings.signals.blocks.TrainNumberBlock;
import com.troblecodings.signals.core.SignalLoader;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item.Properties;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.RegisterEvent;

public final class OSBlocks {

    private OSBlocks() {
    }

    public static final SignalController HV_SIGNAL_CONTROLLER = new SignalController();
    public static final Post POST = new Post();
    public static final GhostBlock GHOST_BLOCK = new GhostBlock();
    public static final SignalBox SIGNAL_BOX = new SignalBox();
    public static final RedstoneInput REDSTONE_IN = new RedstoneInput();
    public static final RedstoneIO REDSTONE_OUT = new RedstoneIO();
    public static final CombinedRedstoneInput COMBI_REDSTONE_INPUT = new CombinedRedstoneInput();
    public static final PathwayRequester PATHWAY_REQUESTER = new PathwayRequester();
    public static final TrainNumberBlock TRAIN_NUMBER_BLOCK = new TrainNumberBlock();

    public static final List<BasicBlock> BLOCKS_TO_REGISTER = new ArrayList<>();

    public static void init() {
        final Field[] fields = OSBlocks.class.getFields();
        for (final Field field : fields) {
            final int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers)
                    && Modifier.isPublic(modifiers)) {
                final String name = field.getName().toLowerCase().replace("_", "");
                try {
                    final Object object = field.get(null);
                    if (object instanceof BasicBlock)
                        loadBlock((BasicBlock) object, name);
                } catch (final IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        SignalLoader.loadAllSignals();
        BasicBlock.prepare();
    }

    public static void loadBlock(final BasicBlock block, final String pName) {
        if (BLOCKS_TO_REGISTER.contains(block))
            return;
        final String name = pName.toLowerCase().trim();
        block.setBlockName(name);
        BLOCKS_TO_REGISTER.add(block);
        if (block instanceof Signal) {
            if (Signal.SIGNALS.containsKey(name)) {
                throw new IllegalArgumentException(
                        "A Signal with the name '" + name + "' alredy exists!");
            }
            Signal.SIGNALS.put(name, (Signal) block);
        }
    }

    @SubscribeEvent
    public static void registerAll(final RegisterEvent event) {
        event.register(Registries.BLOCK,
                helper -> BLOCKS_TO_REGISTER.forEach(block -> helper.register(
                        new ResourceLocation(OpenSignalsMain.MODID, block.getBlockName()), block)));
        event.register(Registries.BLOCK_ENTITY_TYPE,
                helper -> BasicBlock.BLOCK_ENTITYS.values()
                        .forEach(type -> helper.register(new ResourceLocation(OpenSignalsMain.MODID,
                                BasicBlock.TYPE_TO_NAME.get(type)), type)));
        event.register(Registries.ITEM, helper -> {
            BLOCKS_TO_REGISTER.stream()
                    .filter(block -> !(block instanceof Signal && block instanceof GhostBlock))
                    .forEach(block -> helper.register(block.getBlockName(),
                            new BlockItem(block, new Properties())));
        });
    }
}