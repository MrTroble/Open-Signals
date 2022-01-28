package eu.gir.girsignals.guis;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

import eu.gir.girsignals.EnumSignals.EnumMode;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.blocks.Signal.SignalAngel;
import eu.gir.girsignals.guis.guilib.DrawUtil.EnumIntegerable;
import eu.gir.girsignals.guis.guilib.GuiBase;
import eu.gir.girsignals.guis.guilib.GuiElements;
import eu.gir.girsignals.guis.guilib.GuiSyncNetwork;
import eu.gir.girsignals.guis.guilib.entitys.UIBlockRender;
import eu.gir.girsignals.guis.guilib.entitys.UIBox;
import eu.gir.girsignals.guis.guilib.entitys.UIDrag;
import eu.gir.girsignals.guis.guilib.entitys.UIEntity;
import eu.gir.girsignals.guis.guilib.entitys.UIIndependentTranslate;
import eu.gir.girsignals.guis.guilib.entitys.UILabel;
import eu.gir.girsignals.guis.guilib.entitys.UIRotate;
import eu.gir.girsignals.guis.guilib.entitys.UIScale;
import eu.gir.girsignals.guis.guilib.entitys.UIScissor;
import eu.gir.girsignals.tileentitys.SignalControllerTileEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiSignalController extends GuiBase {
	
	private final BlockPos pos;
	private final SignalControllerTileEntity tile;
	private final UIBlockRender blockRender = new UIBlockRender();
	private final AtomicReference<HashMap<SEProperty<?>, Object>> reference = new AtomicReference<>();
	private final AtomicReference<IBlockState> referenceBlockState = new AtomicReference<>();
	
	private UIEntity list;
	private String name;
	
	public GuiSignalController(final SignalControllerTileEntity tile) {
		super("TestTitle");
		this.pos = tile.getPos();
		this.compound = tile.getTag();
		this.tile = tile;
		tile.loadChunkAndGetTile((t, c) -> {
			reference.set(t.getProperties());
			final IBlockState state = c.getBlockState(t.getPos()).withProperty(Signal.ANGEL, SignalAngel.ANGEL0);
			referenceBlockState.set(state.getBlock().getExtendedState(state, c.getWorld(), t.getPos()));
		});
		init();
	}
	
	private void initMode(EnumMode mode, Signal signal) {
		switch (mode) {
		case MANUELL:
			addManuellMode(signal);
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
		final IBlockState currentState = referenceBlockState.get();
		final int typeId = this.tile.getSignalTypeImpl();
		if (typeId < 0 || currentState == null) {
			this.entity.add(new UILabel("Not connected"));
			return;
		}
		
		final Signal signal = Signal.SIGNALLIST.get(typeId);
		this.name = I18n.format("tile." + signal.getRegistryName().getResourcePath() + ".name") + (this.tile.hasCustomName() ? " - " + this.tile.getName() : "");
		
		this.list = new UIEntity();
		this.list.setInheritHeight(true);
		this.list.setInheritWidth(true);
		
		final UIBox vbox = new UIBox(UIBox.VBoxMode.INSTANCE, 1);
		this.list.add(vbox);
		
		final EnumIntegerable<EnumMode> enumMode = new EnumIntegerable<EnumMode>(EnumMode.class);
		final UIEntity rsMode = GuiElements.createEnumElement(enumMode, in -> {
			list.clearChildren();
			initMode(enumMode.getObjFromID(in), signal);
			this.list.read(compound);
		});
		final UIEntity leftSide = new UIEntity();
		leftSide.setInheritHeight(true);
		leftSide.setInheritWidth(true);
		leftSide.add(rsMode);
		leftSide.add(list);
		leftSide.add(GuiElements.createPageSelect(vbox));
		leftSide.add(new UIBox(UIBox.VBoxMode.INSTANCE, 5));
		this.entity.add(leftSide);
				
		final UIEntity rightSide = new UIEntity();
		rightSide.setWidth(60);
		rightSide.setInheritHeight(true);
		final UIRotate rotation = new UIRotate();
		rotation.setRotateY(180);
		rightSide.add(new UIDrag((x, y) -> rotation.setRotateY(rotation.getRotateY() + x)));
		
		blockRender.setBlockState(currentState);
		
		rightSide.add(new UIScissor());
		rightSide.add(new UIIndependentTranslate(35, 150, 40));
		rightSide.add(rotation);
		rightSide.add(new UIIndependentTranslate(-0.5, -3.5, -0.5));
		rightSide.add(new UIScale(20, -20, 20));
		rightSide.add(blockRender);
		
		this.entity.add(rightSide);
		
		this.entity.add(new UIBox(UIBox.HBoxMode.INSTANCE, 1));
		this.entity.read(compound);
	}
	
	private void addManuellMode(final Signal signal) {
		final HashMap<SEProperty<?>, Object> map = reference.get();
		if(map == null)
			return;
		for (SEProperty<?> entry : map.keySet()) {
			if ((entry.isChangabelAtStage(ChangeableStage.APISTAGE) || entry.isChangabelAtStage(ChangeableStage.APISTAGE_NONE_CONFIG)) && entry.test(map.entrySet())) {
				final UIEntity guiEnum = GuiElements.createEnumElement(entry, e -> {
				});
				list.add(guiEnum);
			}
		}
	}
		
	@Override
	public void onGuiClosed() {
		entity.write(compound);
		GuiSyncNetwork.sendToPosServer(compound, pos);
	}
	
	@Override
	public String getTitle() {
		return this.name;
	}
	
}