package eu.gir.girsignals.guis;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.IntConsumer;

import org.lwjgl.opengl.GL11;

import eu.gir.girsignals.GirsignalsMain;
import eu.gir.girsignals.SEProperty;
import eu.gir.girsignals.SEProperty.ChangeableStage;
import eu.gir.girsignals.blocks.Signal;
import eu.gir.girsignals.guis.guilib.DrawUtil;
import eu.gir.girsignals.guis.guilib.GuiBase;
import eu.gir.girsignals.guis.guilib.GuiElements;
import eu.gir.girsignals.guis.guilib.UICheckBox;
import eu.gir.girsignals.guis.guilib.UIEntity;
import eu.gir.girsignals.guis.guilib.UIEnumerable;
import eu.gir.girsignals.guis.guilib.UIVBox;
import eu.gir.girsignals.init.GIRNetworkHandler;
import eu.gir.girsignals.items.Placementtool;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

public class GuiPlacementtool extends GuiBase {

	private BlockModelShapes manager;
	private Signal currentSelectedBlock;
	private ThreadLocal<BufferBuilder> model = ThreadLocal.withInitial(() -> new BufferBuilder(500));
	private Placementtool tool;
	private float animationState = 0;
	private int oldMouse = 0;
	private boolean dragging = false;
	private UIEntity list = new UIEntity();
	private final HashMap<String, IUnlistedProperty<?>> lookup = new HashMap<String, IUnlistedProperty<?>>();

	public GuiPlacementtool(ItemStack stack) {
		super(I18n.format("property.signal.name"));
		this.compound = stack.getTagCompound();
		if (this.compound == null)
			this.compound = new NBTTagCompound();
		tool = (Placementtool) stack.getItem();
		final int usedBlock = this.compound.hasKey(GIRNetworkHandler.BLOCK_TYPE_ID)
				? this.compound.getInteger(GIRNetworkHandler.BLOCK_TYPE_ID)
				: tool.getObjFromID(0).getID();
		currentSelectedBlock = Signal.SIGNALLIST.get(usedBlock);
		manager = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes();

		init();
		this.entity.read(this.compound);
	}

	private void initList() {
		final ExtendedBlockState hVExtendedBlockState = (ExtendedBlockState) currentSelectedBlock.getBlockState();
		final Collection<IUnlistedProperty<?>> unlistedProperties = hVExtendedBlockState.getUnlistedProperties();
		for (IUnlistedProperty<?> property : unlistedProperties) {
			final SEProperty<?> prop = SEProperty.cst(property);
			of(prop, inp -> applyModelChanges());
		}
	}

	private void init() {
		initList();
		final UIVBox vbox = new UIVBox(5);
		list.add(vbox);
		list.setInheritBounds(true);
		final UIEntity entity = GuiElements.createEnumElement(tool, input -> {
			currentSelectedBlock = tool.getObjFromID(input);
			final ExtendedBlockState bsc = (ExtendedBlockState) currentSelectedBlock.getBlockState();
			lookup.clear();
			bsc.getUnlistedProperties().forEach(p -> lookup.put(p.getName(), p));
			list.clearChildren();
			initList();
			applyModelChanges();
		});
		this.entity.add(entity);
		this.entity.add(list);
		this.entity.add(new UIVBox(5));

		this.entity.add(GuiElements.createPageSelect(vbox));
	}

	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		if (dragging) {
			animationState += mouseX - oldMouse;
			oldMouse = mouseX;
		}
		mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

		GlStateManager.enableRescaleNormal();
		GlStateManager.pushMatrix();
		GlStateManager.translate(this.guiLeft + this.xSize - 70, this.guiTop + this.ySize / 2, 100.0f);
		GlStateManager.rotate(animationState, 0, 1, 0);
		GlStateManager.scale(22.0F, -22.0F, 22.0F);
		GlStateManager.translate(-0.5f, -3.5f, -0.5f);
		DrawUtil.draw(model.get());
		GlStateManager.popMatrix();
		GlStateManager.disableRescaleNormal();
	}

	public void of(SEProperty<?> property, IntConsumer consumer) {
		if (property == null)
			return;
		if (property.isChangabelAtStage(ChangeableStage.GUISTAGE)) {
			if (property.getType().equals(Boolean.class)) {
				list.add(GuiElements.createBoolElement(property, consumer));
				return;
			}
			list.add(GuiElements.createEnumElement(property, consumer));
		} else if (property.isChangabelAtStage(ChangeableStage.APISTAGE)) {
			list.add(GuiElements.createBoolElement(property, consumer));
		}
	}

	@Override
	public void initGui() {
		animationState = 180.0f;
		super.initGui();
		applyModelChanges();
	}

	@Override
	public void onGuiClosed() {
		final ByteBuf buffer = Unpooled.buffer();
		buffer.writeByte(GIRNetworkHandler.PLACEMENT_GUI_SET_NBT);
		compound.setInteger(GIRNetworkHandler.BLOCK_TYPE_ID, currentSelectedBlock.getID());
		this.entity.write(compound);
		final PacketBuffer packet = new PacketBuffer(buffer);
		packet.writeCompoundTag(compound);
		final CPacketCustomPayload payload = new CPacketCustomPayload(GIRNetworkHandler.CHANNELNAME, packet);
		GirsignalsMain.PROXY.CHANNEL.sendToServer(new FMLProxyPacket(payload));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void applyModelChanges() {
		IExtendedBlockState ebs = (IExtendedBlockState) currentSelectedBlock.getDefaultState();

		final List<UIEnumerable> enumerables = this.list.findRecursive(UIEnumerable.class);
		for (UIEnumerable enumerable : enumerables) {
			SEProperty sep = (SEProperty) lookup.get(enumerable.getId());
			if (sep == null)
				return;
			ebs = (IExtendedBlockState) ebs.withProperty(sep, sep.getObjFromID(enumerable.getIndex()));
		}

		final List<UICheckBox> checkbox = this.list.findRecursive(UICheckBox.class);
		for (UICheckBox checkb : checkbox) {
			SEProperty sep = (SEProperty) lookup.get(checkb.getId());
			if (sep == null)
				return;
			if (sep.isChangabelAtStage(ChangeableStage.GUISTAGE)) {
				ebs = (IExtendedBlockState) ebs.withProperty(sep, checkb.isChecked());
			} else if(checkb.isChecked()) {
				ebs = (IExtendedBlockState) ebs.withProperty(sep, sep.getDefault());
			}
		}

		for (Entry<IUnlistedProperty<?>, Optional<?>> prop : ebs.getUnlistedProperties().entrySet()) {
			final SEProperty property = SEProperty.cst(prop.getKey());
			if (property.isChangabelAtStage(ChangeableStage.APISTAGE_NONE_CONFIG)) {
				ebs = ebs.withProperty(property, property.getDefault());
			}
		}

		model.get().begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		DrawUtil.addToBuffer(model.get(), manager, ebs);
		model.get().finishDrawing();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		if (mouseButton == 0) {
			this.dragging = true;
			oldMouse = mouseX;
		}
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		super.mouseReleased(mouseX, mouseY, state);
		dragging = false;
	}

}
