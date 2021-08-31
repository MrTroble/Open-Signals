package eu.gir.girsignals.guis;

import java.util.Optional;
import java.util.function.Consumer;

import eu.gir.girsignals.EnumSignals;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.guis.guilib.GuiElements.GuiEnumerableSetting;
import eu.gir.girsignals.guis.guilib.GuiElements.GuiSettingCheckBox;
import eu.gir.girsignals.tileentitys.SignalControllerTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class GuiHandler implements IGuiHandler {

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID == EnumSignals.GUI_SIGNAL_CONTROLLER) {
			final TileEntity entity = world.getTileEntity(new BlockPos(x, y, z));
			if (entity instanceof SignalControllerTileEntity)
				return new ContainerSignalController((SignalControllerTileEntity) entity);
		} else if(ID == EnumSignals.GUI_SIGNAL_BOX) {
			return new ContainerSignalBox();
		}
		return null;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		switch (ID) {
		case EnumSignals.GUI_PLACEMENTTOOL:
			return new GuiPlacementtool(player.getHeldItemMainhand());
		case EnumSignals.GUI_SIGNAL_CONTROLLER: {
			final TileEntity entity = world.getTileEntity(new BlockPos(x, y, z));
			if (entity instanceof SignalControllerTileEntity)
				return new GuiSignalController((SignalControllerTileEntity) entity);
		}
		case EnumSignals.GUI_SIGNAL_BOX:
			return new GuiSignalBox();
		default:
			return null;
		}
	}

	@SideOnly(Side.CLIENT)
	public static Optional<GuiEnumerableSetting> of(SEProperty<?> property, int initialValue,
			Consumer<Integer> consumer, ChangeableStage stage) {
		if (property == null)
			return Optional.empty();
		if (ChangeableStage.GUISTAGE == stage) {
			if (property.isChangabelAtStage(ChangeableStage.GUISTAGE)) {
				if (property.getType().equals(Boolean.class))
					return Optional.of(new GuiSettingCheckBox(property, initialValue, consumer));
				return Optional.of(new GuiEnumerableSetting(property, initialValue, consumer));
			} else if (property.isChangabelAtStage(ChangeableStage.APISTAGE)) {
				return Optional.of(new GuiSettingCheckBox(property, initialValue, consumer));
			}
		} else if (ChangeableStage.APISTAGE == stage) {
			if (property.isChangabelAtStage(ChangeableStage.APISTAGE)
					|| property.isChangabelAtStage(ChangeableStage.APISTAGE_NONE_CONFIG)) {
				return Optional.of(new GuiEnumerableSetting(property, initialValue, consumer));
			}
		}
		return Optional.empty();
	}
}
