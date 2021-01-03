package net.gir.girsignals.guis;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

public class PTUIContainer extends Container {

	public final EntityPlayer player;

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return false;
	}

	public PTUIContainer(EntityPlayer playerIn) {

		player = playerIn;
	}

}
