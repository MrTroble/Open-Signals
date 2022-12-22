package com.troblecodings.signals.blocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.GlStateManager;
import com.troblecodings.properties.FloatProperty;
import com.troblecodings.properties.HeightProperty;
import com.troblecodings.properties.SoundProperty;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.contentpacks.ContentPackException;
import com.troblecodings.signals.contentpacks.SoundPropertyParser;
import com.troblecodings.signals.enums.ChangeableStage;
import com.troblecodings.signals.init.OSItems;
import com.troblecodings.signals.init.OSSounds;
import com.troblecodings.signals.items.Placementtool;
import com.troblecodings.signals.parser.FunctionParsingInfo;
import com.troblecodings.signals.parser.LogicParser;
import com.troblecodings.signals.parser.LogicalParserException;
import com.troblecodings.signals.parser.ValuePack;
import com.troblecodings.signals.tileentitys.SignalTileEnity;
import com.troblecodings.signals.utils.JsonEnum;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.gui.Font;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class Signal extends Block implements BlockColor {

	public static enum SignalAngel implements StringRepresentable {

		ANGEL0, ANGEL22P5, ANGEL45, ANGEL67P5, ANGEL90, ANGEL112P5, ANGEL135, ANGEL157P5, ANGEL180, ANGEL202P5,
		ANGEL225, ANGEL247P5, ANGEL270, ANGEL292P5, ANGEL315, ANGEL337P5;

		public float getDegree() {
			return this.ordinal() * 22.5f;
		}

		public double getRadians() {
			return (this.ordinal() / 16.0) * Math.PI * 2.0;
		}

		@Override
		public String getSerializedName() {
			return String.valueOf(getRadians());
		}
	}

	public static class SignalProperties {

		public final Placementtool placementtool;
		public final float customNameRenderHeight;
		public final int defaultHeight;
		public final List<HeightProperty> signalHeights;
		public final List<FloatProperty> customRenderHeights;
		public final float signWidth;
		public final float offsetX;
		public final float offsetY;
		public final float signScale;
		public final boolean canLink;
		public final List<Integer> colors;
		public final List<SoundProperty> sounds;
		public final List<ValuePack> redstoneOutputs;

		public SignalProperties(final Placementtool placementtool, final float customNameRenderHeight, final int height,
				final List<HeightProperty> signalHeights, final float signWidth, final float offsetX,
				final float offsetY, final float signScale, final boolean canLink, final List<Integer> colors,
				final List<FloatProperty> renderheights, final List<SoundProperty> sounds,
				final List<ValuePack> redstoneOutputs) {
			this.placementtool = placementtool;
			this.customNameRenderHeight = customNameRenderHeight;
			this.defaultHeight = height;
			this.signWidth = signWidth;
			this.offsetX = offsetX;
			this.offsetY = offsetY;
			this.signScale = signScale;
			this.canLink = canLink;
			this.colors = colors;
			this.signalHeights = signalHeights;
			this.customRenderHeights = renderheights;
			this.sounds = sounds;
			this.redstoneOutputs = redstoneOutputs;
		}

	}

	public static class SignalPropertiesBuilder {

		private transient Placementtool placementtool = null;
		private String placementToolName = null;
		private int defaultHeight = 1;
		private Map<String, Integer> signalHeights;
		private float customNameRenderHeight = -1;
		private Map<String, Float> renderHeights;
		private float signWidth = 22;
		private float offsetX = 0;
		private float offsetY = 0;
		private float signScale = 1;
		private boolean canLink = true;
		private List<Integer> colors;
		private Map<String, SoundPropertyParser> sounds;
		private Map<String, String> redstoneOutputs;

		public SignalPropertiesBuilder() {
		}

		public SignalPropertiesBuilder(final Placementtool placementtool, final String signalTypeName) {
			this.placementtool = placementtool;
		}

		public SignalProperties build() {
			return this.build(null);
		}

		@SuppressWarnings("rawtypes")
		public SignalProperties build(final @Nullable FunctionParsingInfo info) {
			if (placementToolName != null) {
				OSItems.registeredItems.forEach(item -> {
					if (item instanceof Placementtool) {
						if (item.getRegistryName().toString().replace(OpenSignalsMain.MODID + ":", "")
								.equalsIgnoreCase(placementToolName)) {
							placementtool = (Placementtool) item;
							return;
						}
					}
				});
				if (placementtool == null)
					OpenSignalsMain.getLogger()
							.error("There doesn't exists a placementtool with the name '" + placementToolName + "'!");
			}

			final List<HeightProperty> signalheights = new ArrayList<>();
			if (signalHeights != null) {
				signalHeights.forEach((property, height) -> {
					if (info != null) {
						try {
							signalheights.add(new HeightProperty(LogicParser.predicate(property, info), height));
						} catch (final LogicalParserException e) {
							OpenSignalsMain.getLogger()
									.error("Something went wrong during the registry of a predicate in "
											+ info.signalName + "!\nWith statement:" + property);
							e.printStackTrace();
						}
					}

				});
			}

			final List<FloatProperty> renderheights = new ArrayList<>();
			if (renderHeights != null) {
				renderHeights.forEach((property, height) -> {
					if (info != null) {
						try {
							renderheights.add(new FloatProperty(LogicParser.predicate(property, info), height));
						} catch (final LogicalParserException e) {
							OpenSignalsMain.getLogger()
									.error("Something went wrong during the registry of a predicate in "
											+ info.signalName + "!\nWith statement:" + property);
							e.printStackTrace();
						}
					}
				});
			}

			final List<SoundProperty> soundProperties = new ArrayList<>();
			if (sounds != null) {
				for (final Map.Entry<String, SoundPropertyParser> soundProperty : sounds.entrySet()) {
					final SoundPropertyParser soundProp = soundProperty.getValue();
					final SoundEvent sound = OSSounds.SOUNDS.get(soundProp.getName().toLowerCase());
					if (sound == null) {
						OpenSignalsMain.getLogger()
								.error("The sound with the name " + soundProp.getName() + " doesn't exists!");
						continue;
					}
					try {
						soundProperties.add(new SoundProperty(sound,
								LogicParser.predicate(soundProperty.getKey(), info), soundProp.getLength()));
					} catch (final LogicalParserException e) {
						OpenSignalsMain.getLogger().error("Something went wrong during the registry of a predicate in "
								+ info.signalName + "!\nWith statement:" + soundProperty.getKey());
						e.printStackTrace();
					}
				}
			}

			final List<ValuePack> rsOutputs = new ArrayList<>();
			if (redstoneOutputs != null) {
				for (final Map.Entry<String, String> outputs : redstoneOutputs.entrySet()) {
					final SEProperty property = (SEProperty) info.getProperty(outputs.getValue());
					if (!property.getParent().getClass().equals(BooleanProperty.class)) {
						throw new ContentPackException("The proprty " + outputs.getValue()
								+ " needs to be an bool property to us it with an RS output but it wasn't!");
					}
					rsOutputs.add(new ValuePack(property, LogicParser.predicate(outputs.getKey(), info)));
				}
			}

			this.colors = this.colors == null ? new ArrayList<>() : this.colors;

			return new SignalProperties(placementtool, customNameRenderHeight, defaultHeight,
					ImmutableList.copyOf(signalheights), signWidth, offsetX, offsetY, signScale, canLink, colors,
					ImmutableList.copyOf(renderheights), ImmutableList.copyOf(soundProperties),
					ImmutableList.copyOf(rsOutputs));
		}
	}

	public static final SignalPropertiesBuilder builder(final Placementtool placementtool,
			final String signalTypeName) {
		return new SignalPropertiesBuilder(placementtool, signalTypeName);
	}

	public static final ArrayList<Signal> SIGNALLIST = new ArrayList<Signal>();

	public static final Map<String, Signal> SIGNALS = new HashMap<>();

	public static final EnumProperty<SignalAngel> ANGEL = EnumProperty.create("angel", SignalAngel.class);
	public static final SEProperty CUSTOMNAME = new SEProperty("customname", JsonEnum.BOOLEAN, "false",
			ChangeableStage.AUTOMATICSTAGE, _u -> false);

	private final int id;
	protected final SignalProperties prop;

	@SuppressWarnings("rawtypes")
	public static Consumer<List<SEProperty>> nextConsumer = _u -> {
	};

	public Signal(final SignalProperties prop) {
		super(Properties.of(Material.METAL));
		this.prop = prop;
		registerDefaultState(this.defaultBlockState().setValue(ANGEL, SignalAngel.ANGEL0));
		id = SIGNALLIST.size();
		SIGNALLIST.add(this);
		prop.placementtool.addSignal(this);
	}

	@SuppressWarnings("rawtypes")
	private ArrayList<SEProperty> signalProperties;

	public int getIDFromProperty(final SEProperty propertyIn) {
		return signalProperties.indexOf(propertyIn);
	}

	public SEProperty getPropertyFromID(final int id) {
		return signalProperties.get(id);
	}

	@SuppressWarnings("rawtypes")
	public ImmutableList<SEProperty> getProperties() {
		return ImmutableList.copyOf(this.signalProperties);
	}

	public int getID() {
		return id;
	}

	@Override
	public void destroy(LevelAccessor worldIn, BlockPos pos, BlockState state) {
		super.destroy(worldIn, pos, state);

		if (!worldIn.isClientSide())
			GhostBlock.destroyUpperBlock(worldIn, pos);
	}

	@SuppressWarnings("unchecked")
	public int getHeight(final Map<SEProperty, Object> map) {
		for (final HeightProperty property : this.prop.signalHeights) {
			if (property.predicate.test(map))
				return property.height;
		}
		return this.prop.defaultHeight;
	}

	public boolean canHaveCustomname(final Map<SEProperty, Object> map) {
		return this.prop.customNameRenderHeight != -1 || !this.prop.customRenderHeights.isEmpty();
	}

	public final boolean canBeLinked() {
		return this.prop.canLink;
	}

	@OnlyIn(Dist.CLIENT)
	public boolean hasCostumColor() {
		return !this.prop.colors.isEmpty();
	}

	@OnlyIn(Dist.CLIENT)
	public void renderOverlay(final double x, final double y, final double z, final SignalTileEnity te,
			final Font font) {
		this.renderOverlay(x, y, z, te, font, this.prop.customNameRenderHeight);
	}

	@SuppressWarnings("unchecked")
	@OnlyIn(Dist.CLIENT)
	public void renderOverlay(final double x, final double y, final double z, final SignalTileEnity te, final Font font,
			final float renderHeight) {
		float customRenderHeight = renderHeight;
		final Map<SEProperty, Object> map = te.getProperties();
		for (final FloatProperty property : this.prop.customRenderHeights) {
			if (property.predicate.test(map)) {
				customRenderHeight = property.height;
			}
		}
		if (customRenderHeight == -1)
			return;
		final Level world = te.getLevel();
		final BlockPos pos = te.getBlockPos();
		final BlockState state = world.getBlockState(pos);
		if (!(state.getBlock() instanceof Signal)) {
			return;
		}
		final ITextComponent name = te.getDisplayName();
		final SignalAngel face = state.getValue(Signal.ANGEL);
		final float angel = face.getDegree();

		final String[] display = name.getFormattedText().split("\\[n\\]");

		final float scale = this.prop.signScale;

		GlStateManager.enableAlpha();
		GlStateManager.pushMatrix();
		GlStateManager.translate(x + 0.5f, y + customRenderHeight, z + 0.5f);
		GlStateManager.scale(0.015f * scale, -0.015f * scale, 0.015f * scale);
		GlStateManager.rotate(angel, 0, 1, 0);

		renderSingleOverlay(display, font, te);

		GlStateManager.popMatrix();
	}

	@OnlyIn(Dist.CLIENT)
	public void renderSingleOverlay(final String[] display, final Font font, final SignalTileEnity te) {
		final float width = this.prop.signWidth;
		final float offsetX = this.prop.offsetX;
		final float offsetZ = this.prop.offsetY;
		final float scale = this.prop.signScale;
		GlStateManager.pushMatrix();
		GlStateManager.translate(width / 2 + offsetX, 0, -4.2f + offsetZ);
		GlStateManager.scale(-1f, 1f, 1f);
		for (int i = 0; i < display.length; i++) {
			font.drawSplitString(display[i], 0, (int) (i * scale * 2.8f), (int) width, 0);
		}
		GlStateManager.popMatrix();
	}

	public Placementtool getPlacementtool() {
		return this.prop.placementtool;
	}

	@SuppressWarnings("rawtypes")
	private SEProperty powerProperty = null;

	@Override
	public boolean onBlockActivated(final Level worldIn, final BlockPos pos, final BlockState state,
			final Player playerIn, final EnumHand hand, final Direction facing, final float hitX, final float hitY,
			final float hitZ) {
		final BlockEntity tile = worldIn.getBlockEntity(pos);
		if (!(tile instanceof SignalTileEnity)) {
			return false;
		}
		final SignalTileEnity signalTile = (SignalTileEnity) tile;
		if (loadRedstoneOutput(worldIn, state, pos, signalTile) && worldIn.isClientSide) {
			return true;
		}
		final boolean customname = canHaveCustomname(signalTile.getProperties());
		if (!playerIn.getHeldItemMainhand().getItem().equals(OSItems.LINKING_TOOL) && (canBeLinked() || customname)) {
			OpenSignalsMain.handler.invokeGui(Signal.class, playerIn, worldIn, pos);
			return true;
		}
		return false;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private boolean loadRedstoneOutput(final Level worldIn, final BlockState state, final BlockPos pos,
			final SignalTileEnity tile) {
		if (!this.prop.redstoneOutputs.isEmpty()) {
			final Map<SEProperty, Object> properties = tile.getProperties();
			this.powerProperty = null;
			for (final ValuePack pack : this.prop.redstoneOutputs) {
				if (pack.predicate.test(properties)) {
					final SEProperty seProperty = (SEProperty) pack.property;
					this.powerProperty = seProperty;
					tile.getProperty(seProperty).ifPresent(power -> tile.setProperty(seProperty, !(Boolean) power));
					break;
				}
			}
			if (this.powerProperty == null) {
				return false;
			}
			worldIn.setBlockState(pos, state, 3);
			worldIn.notifyNeighborsOfStateChange(pos, this, false);
			worldIn.markAndNotifyBlock(pos, null, state, state, 3);
			return true;
		}
		return false;
	}

	@Override
	public boolean canProvidePower(BlockState state) {
		return !this.prop.redstoneOutputs.isEmpty();
	}

	@SuppressWarnings("unchecked")
	@Override
	public int getWeakPower(BlockState blockState, LevelAccessor blockAccess, BlockPos pos, Direction side) {
		if (this.prop.redstoneOutputs.isEmpty() || this.powerProperty == null)
			return 0;

		final SignalTileEnity tile = (SignalTileEnity) blockAccess.getTileEntity(pos);
		if (tile.getProperty(powerProperty).filter(power -> !(Boolean) power).isPresent()) {
			return 0;
		}
		final Map<SEProperty, Object> properties = tile.getProperties();
		for (final ValuePack pack : this.prop.redstoneOutputs) {
			if (pack.predicate.test(properties)) {
				return 15;
			}
		}
		return 0;
	}

	@SuppressWarnings("unchecked")
	public void getUpdate(final Level world, final BlockPos pos) {
		if (this.prop.sounds.isEmpty())
			return;

		final SignalTileEnity tile = (SignalTileEnity) world.getBlockEntity(pos);
		final Map<SEProperty, Object> properties = tile.getProperties();
		final SoundProperty sound = getSound(properties);
		if (sound.duration < 1)
			return;

		if (sound.duration == 1) {
			world.playSound(null, pos, sound.sound, SoundSource.BLOCKS, 1.0F, 1.0F);
		} else {
			if (world.getBlockTicks().hasScheduledTick(pos, this)) {
				return;
			} else {
				if (sound.predicate.test(properties)) {
					world.scheduleTick(pos, this, 1);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public SoundProperty getSound(final Map<SEProperty, Object> map) {
		for (final SoundProperty property : this.prop.sounds) {
			if (property.predicate.test(map)) {
				return property;
			}
		}
		return new SoundProperty();
	}
	
	@Override
	public void tick(BlockState state, ServerLevel world, BlockPos pos, Random rand) {
		if (this.prop.sounds.isEmpty() || world.isClientSide) {
			return;
		}
		final SignalTileEnity tile = (SignalTileEnity) world.getBlockEntity(pos);
		final SoundProperty sound = getSound(tile.getProperties());
		if (sound.duration <= 1) {
			return;
		}
		world.playSound(null, pos, sound.sound, SoundSource.BLOCKS, 1.0F, 1.0F);
		world.scheduleTick(pos, this, sound.duration);
	}

	@Override
	public int getColor(BlockState state, BlockAndTintGetter tint, BlockPos pos, int index) {
		return this.prop.colors.get(index);
	}
}