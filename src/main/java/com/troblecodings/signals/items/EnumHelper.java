package com.troblecodings.signals.items;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

public class EnumHelper {
	
	private static class ImplArmorMat implements ArmorMaterial{

		int[] defense;
		int durability;
		int enchant;
		SoundEvent event;
		Ingredient ingrediant;
		String name;
		float toughness;
		float knock;
		
		public ImplArmorMat(int[] defense, int durability, int enchant, SoundEvent event, Ingredient ingrediant,
				String name, float toughness, float knock) {
			super();
			this.defense = defense;
			this.durability = durability;
			this.enchant = enchant;
			this.event = event;
			this.ingrediant = ingrediant;
			this.name = name;
			this.toughness = toughness;
			this.knock = knock;
		}
		

		@Override
		public int getDurabilityForSlot(EquipmentSlot slot) {
			return durability;
		}

		@Override
		public int getDefenseForSlot(EquipmentSlot slot) {
			return this.defense[slot.getIndex()];
		}

		@Override
		public int getEnchantmentValue() {
			return this.enchant;
		}

		@Override
		public SoundEvent getEquipSound() {
			return this.event;
		}

		@Override
		public Ingredient getRepairIngredient() {
			return this.ingrediant;
		}

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public float getToughness() {
			return this.toughness;
		}

		@Override
		public float getKnockbackResistance() {
			return this.knock;
		}
		
	}

	public static ArmorMaterial addArmorMaterial(String name, int durability, int[] defense, int enchant, SoundEvent event,
			float toughness) {
		return new ImplArmorMat(defense, durability, enchant, event, Ingredient.EMPTY, name, toughness, toughness);
	}
}
