package com.troblecodings.signals.items;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.SoundEvents;

public class ItemArmorTemplate extends ArmorItem {

    public ItemArmorTemplate(final IArmorMaterial materialIn,
            final EquipmentSlotType equipmentSlotIn) {
        super(materialIn, equipmentSlotIn, new Properties().tab(ItemGroup.TAB_COMBAT));
    }

    public static final IArmorMaterial REFLECTIVE_ARMOR_MATERIAL = EnumHelper
            .addArmorMaterial("reflective", 1000, new int[] {
                    1, 1, 1, 1
            }, 30, SoundEvents.ARMOR_EQUIP_GENERIC, 0F);

    public static final IArmorMaterial DISPATCHER_ARMOR_MATERIAL = EnumHelper
            .addArmorMaterial("dispatcher", 1000, new int[] {
                    1, 1, 1, 1
            }, 30, SoundEvents.ARMOR_EQUIP_GENERIC, 0F);

    public static final IArmorMaterial STATIONMANAGER_ARMOR_MATERIAL = EnumHelper
            .addArmorMaterial("station_manager", 1000, new int[] {
                    1, 1, 1, 1
            }, 30, SoundEvents.ARMOR_EQUIP_GENERIC, 0F);

    public static final IArmorMaterial TRAINDRIVER_ARMOR_MATERIAL = EnumHelper
            .addArmorMaterial("train_driver", 1000, new int[] {
                    1, 1, 1, 1
            }, 30, SoundEvents.ARMOR_EQUIP_GENERIC, 0F);

    public static final IArmorMaterial CONDUCTOR_ARMOR_MATERIAL = EnumHelper
            .addArmorMaterial("conductor", 1000, new int[] {
                    1, 1, 1, 1
            }, 30, SoundEvents.ARMOR_EQUIP_GENERIC, 0F);
}
