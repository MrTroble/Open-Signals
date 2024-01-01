package com.troblecodings.signals.items;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;

public class ItemArmorTemplate extends ArmorItem {

    public ItemArmorTemplate(final ArmorMaterial materialIn, final Type equipmentSlotIn) {
        super(materialIn, equipmentSlotIn, new Properties());
    }

    public static final ArmorMaterial REFLECTIVE_ARMOR_MATERIAL = EnumHelper
            .addArmorMaterial("reflective", 1000, new int[] {
                    1, 1, 1, 1
            }, 30, SoundEvents.ARMOR_EQUIP_GENERIC, 0F);

    public static final ArmorMaterial DISPATCHER_ARMOR_MATERIAL = EnumHelper
            .addArmorMaterial("dispatcher", 1000, new int[] {
                    1, 1, 1, 1
            }, 30, SoundEvents.ARMOR_EQUIP_GENERIC, 0F);

    public static final ArmorMaterial STATIONMANAGER_ARMOR_MATERIAL = EnumHelper
            .addArmorMaterial("station_manager", 1000, new int[] {
                    1, 1, 1, 1
            }, 30, SoundEvents.ARMOR_EQUIP_GENERIC, 0F);

    public static final ArmorMaterial TRAINDRIVER_ARMOR_MATERIAL = EnumHelper
            .addArmorMaterial("train_driver", 1000, new int[] {
                    1, 1, 1, 1
            }, 30, SoundEvents.ARMOR_EQUIP_GENERIC, 0F);

    public static final ArmorMaterial CONDUCTOR_ARMOR_MATERIAL = EnumHelper
            .addArmorMaterial("conductor", 1000, new int[] {
                    1, 1, 1, 1
            }, 30, SoundEvents.ARMOR_EQUIP_GENERIC, 0F);
}