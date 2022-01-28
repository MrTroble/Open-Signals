package eu.gir.girsignals.guis;

import java.util.HashMap;
import java.util.Map.Entry;

import eu.gir.girsignals.EnumSignals.EnumMode;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.guis.guilib.DrawUtil.EnumIntegerable;
import eu.gir.girsignals.guis.guilib.GuiBase;
import eu.gir.girsignals.guis.guilib.GuiElements;
import eu.gir.girsignals.guis.guilib.GuiSyncNetwork;
import eu.gir.girsignals.guis.guilib.entitys.UIBlockRender;
import eu.gir.girsignals.guis.guilib.entitys.UIBox;
import eu.gir.girsignals.guis.guilib.entitys.UIClickable;
import eu.gir.girsignals.guis.guilib.entitys.UIDrag;
import eu.gir.girsignals.guis.guilib.entitys.UIEntity;
import eu.gir.girsignals.guis.guilib.entitys.UIEnumerable;
import eu.gir.girsignals.guis.guilib.entitys.UIIndependentTranslate;
import eu.gir.girsignals.guis.guilib.entitys.UILabel;
import eu.gir.girsignals.guis.guilib.entitys.UIRotate;
import eu.gir.girsignals.guis.guilib.entitys.UIScale;
import eu.gir.girsignals.guis.guilib.entitys.UIScissor;
import eu.gir.girsignals.guis.guilib.entitys.UIToolTip;
import eu.gir.girsignals.tileentitys.SignalControllerTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiSignalController extends GuiBase {
	
	private final BlockPos pos;
	private final ContainerSignalController controller;
	private final UIEntity lowerEntity = new UIEntity();
	private boolean previewMode = false;

	public GuiSignalController(final SignalControllerTileEntity tile) {
		this.pos = tile.getPos();
		this.controller = new ContainerSignalController(this::init);
		Minecraft.getMinecraft().player.openContainer = this.controller;
		this.compound = tile.getTag();
		init();
	}
	
	private void initMode(EnumMode mode, Signal signal) {
		switch (mode) {
		case MANUELL:
			addManuellMode();
			break;
		case SINGLE:
			break;
		case MUX:
			break;
		default:
			break;
		}
	}
	
	private void init() {
		this.entity.clear();
		
		final Signal signal = this.controller.getSignal();
		if (signal == null) {
			this.entity.add(new UILabel("Not connected"));
			return;
		}
		lowerEntity.setInheritHeight(true);
		lowerEntity.setInheritWidth(true);
		
		final String name = I18n.format("tile." + signal.getRegistryName().getResourcePath() + ".name");
		
		final UILabel titlelabel = new UILabel(name);
		titlelabel.setCenterX(false);
		
		final UIEntity titel = new UIEntity();
		titel.add(new UIScale(1.2f, 1.2f, 1));
		titel.add(titlelabel);
		titel.setInheritHeight(true);
		titel.setInheritWidth(true);
		
		final UIEntity header = new UIEntity();
		header.setInheritWidth(true);
		header.setHeight(45);
		header.add(new UIBox(UIBox.VBoxMode.INSTANCE, 1));
		header.add(titel);
		final EnumIntegerable<EnumMode> enumMode = new EnumIntegerable<EnumMode>(EnumMode.class);
		final UIEntity rsMode = GuiElements.createEnumElement(enumMode, in -> {
			lowerEntity.clearChildren();
			initMode(enumMode.getObjFromID(in), signal);
		});
		header.add(rsMode);
		
		final UIEntity middlePart = new UIEntity();
		middlePart.setInheritHeight(true);
		middlePart.setInheritWidth(true);
		middlePart.add(new UIBox(UIBox.VBoxMode.INSTANCE, 1));
		middlePart.add(header);
		middlePart.add(lowerEntity);
		
		this.entity.add(GuiElements.createSpacerH(10));
		this.entity.add(middlePart);
		this.entity.add(GuiElements.createSpacerH(10));
		this.entity.add(new UIBox(UIBox.HBoxMode.INSTANCE, 1));
		
		this.entity.read(compound);
	}
	
	private void addManuellMode() {
		final UIEntity list = new UIEntity();
		list.setInheritHeight(true);
		list.setInheritWidth(true);
		final UIBox vbox = new UIBox(UIBox.VBoxMode.INSTANCE, 1);
		list.add(vbox);
		
		final UIEntity leftSide = new UIEntity();
		leftSide.setInheritHeight(true);
		leftSide.setInheritWidth(true);
		leftSide.add(list);
		leftSide.add(GuiElements.createPageSelect(vbox));
		leftSide.add(new UIBox(UIBox.VBoxMode.INSTANCE, 5));
		lowerEntity.add(leftSide);
		
		final UIBlockRender blockRender = new UIBlockRender();

		final UIToolTip tooltip = new UIToolTip(I18n.format("controller.preview", previewMode));
		
		final UIEntity rightSide = new UIEntity();
		rightSide.setWidth(60);
		rightSide.setInheritHeight(true);
		final UIRotate rotation = new UIRotate();
		rotation.setRotateY(180);
		rightSide.add(new UIClickable(e -> {
			previewMode = !previewMode;
			applyModelChange(blockRender);
			tooltip.setDescripton(I18n.format("controller.preview", previewMode));
		}, 1));
		rightSide.add(new UIDrag((x, y) -> rotation.setRotateY(rotation.getRotateY() + x)));
		rightSide.add(tooltip);
		
		rightSide.add(new UIScissor());
		rightSide.add(new UIIndependentTranslate(35, 150, 40));
		rightSide.add(rotation);
		rightSide.add(new UIIndependentTranslate(-0.5, -3.5, -0.5));
		rightSide.add(new UIScale(20, -20, 20));
		rightSide.add(blockRender);
		
		lowerEntity.add(rightSide);
		lowerEntity.add(new UIBox(UIBox.HBoxMode.INSTANCE, 1));

		final HashMap<SEProperty<?>, Object> map = this.controller.getReference();
		if (map == null)
			return;
		for (SEProperty<?> entry : map.keySet()) {
			if ((entry.isChangabelAtStage(ChangeableStage.APISTAGE) || entry.isChangabelAtStage(ChangeableStage.APISTAGE_NONE_CONFIG)) && entry.test(map.entrySet())) {
				list.add(GuiElements.createEnumElement(entry, e -> applyModelChange(blockRender)));
			}
		}
		applyModelChange(blockRender);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void applyModelChange(final UIBlockRender blockRender) {
		IExtendedBlockState currentState = (IExtendedBlockState) this.controller.getSignal().getDefaultState();
		for(Entry<SEProperty<?>, Object> e : controller.getReference().entrySet()) {
			currentState = currentState.withProperty((SEProperty)e.getKey(), e.getValue());
		}

		if (!previewMode) {
			for(UIEnumerable property : lowerEntity.findRecursive(UIEnumerable.class)) {
				final SEProperty sep = SEProperty.cst(controller.lookup.get(property.getId()));
				if(sep != null)
					currentState = currentState.withProperty(sep, sep.getObjFromID(property.getIndex()));
			}
		}
		blockRender.setBlockState(currentState);
	}
	
	@Override
	public void onGuiClosed() {
		this.entity.write(compound);
		GuiSyncNetwork.sendToPosServer(compound, pos);
	}
	
}