package com.troblecodings.signals.init;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import com.troblecodings.linkableapi.Linkingtool;
import com.troblecodings.signals.SignalsMain;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.items.Placementtool;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

public final class SignaIItems {

    private SignaIItems() {
    }

    public static final Linkingtool LINKING_TOOL = new Linkingtool(SignalTabs.TAB, (world, pos) -> {
        final IBlockState state = world.getBlockState(pos);
        final Block block = state.getBlock();
        return block == SignalBlocks.REDSTONE_IN || block == SignalBlocks.REDSTONE_OUT
                || (block instanceof Signal && ((Signal) block).canBeLinked());
    });
    public static final Placementtool PLACEMENT_TOOL = new Placementtool();
    public static final Placementtool SIGN_PLACEMENT_TOOL = new Placementtool();

    public static ArrayList<Item> registeredItems = new ArrayList<>();

    public static void init() {
        final Field[] fields = SignaIItems.class.getFields();
        for (final Field field : fields) {
            final int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers)
                    && Modifier.isPublic(modifiers)) {
                final String name = field.getName().toLowerCase().replace("_", "");
                try {
                    final Item item = (Item) field.get(null);
                    item.setRegistryName(new ResourceLocation(SignalsMain.MODID, name));
                    item.setUnlocalizedName(name);
                    registeredItems.add(item);
                } catch (final IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @SubscribeEvent
    public static void registerItem(final RegistryEvent.Register<Item> event) {
        final IForgeRegistry<Item> registry = event.getRegistry();
        registeredItems.forEach(registry::register);
    }

}
