package eu.gir.girsignals.tileentitys;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.Gson;

import eu.gir.girsignals.linkableApi.ILinkableTile;
import net.minecraft.block.BlockLever;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class SignalBoxTileEntity extends TileEntity implements ILinkableTile {

	public static final class PlanElement {
		public int type;
		public int xPos;
		public int yPos;
	}

	public static final class TrackPlan {
		public String name;
		public int version;
		public HashMap<String, PlanElement> elements;
		public HashMap<String, String[]> connections;
	}

	public static final Gson GSON = new Gson();

	private final ArrayList<BlockPos> linkedPositions = new ArrayList<>();
	private final ArrayList<SignalControllerTileEntity> controller = new ArrayList<>();
	private final ArrayList<BlockPos> rsInput = new ArrayList<>();
	private final ArrayList<BlockPos> rsOutput = new ArrayList<>();
	
	public final ArrayList<String> strings = new ArrayList<>();

	private static final String POS_LIST = "poslist";
	private static final String RS_IN_LIST = "rsIn";
	private static final String RS_OUT_LIST = "rsOut";
	private static final String NAME = "name";
	private static final String JSONDATA = "jsondata";
	private static final String NAMELIST = "namelist";

	private static void writeList(ArrayList<BlockPos> posList, NBTTagCompound compound, String name) {
		final NBTTagList list = new NBTTagList();
		posList.forEach(pos -> list.appendTag(NBTUtil.createPosTag(pos)));
		compound.setTag(name, list);
	}

	private static void readList(ArrayList<BlockPos> posList, NBTTagCompound compound, String name) {
		final NBTTagList list = compound.getTagList(POS_LIST, 10);
		list.forEach(nbt -> posList.add(NBTUtil.getPosFromTag((NBTTagCompound) nbt)));
	}

	private TrackPlan plan = null;
	private String name = null;

	public void loadPlan(final String name) {
		final Path path = Paths.get("trackplans", name);
		try (final BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
			plan = GSON.fromJson(GSON.newJsonReader(reader), TrackPlan.class);
			if (world != null) {
				final IBlockState state = world.getBlockState(pos);
				world.notifyBlockUpdate(pos, state, state, 3);
			}
		} catch (IOException e) {
			e.printStackTrace(); // TODO default behaviour
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		readList(linkedPositions, compound, POS_LIST);
		readList(rsInput, compound, RS_IN_LIST);
		readList(rsOutput, compound, RS_OUT_LIST);
		if (compound.hasKey(NAME) && world != null && !world.isRemote) {
			loadPlan(name);
		}
		super.readFromNBT(compound);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		writeList(linkedPositions, compound, POS_LIST);
		writeList(rsInput, compound, RS_IN_LIST);
		writeList(rsOutput, compound, RS_OUT_LIST);
		if (name != null)
			compound.setString(NAME, name);
		return super.writeToNBT(compound);
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		final NBTTagCompound comp = getUpdateTag();
		final NBTTagList list = new NBTTagList();
		try {
			Files.list(Paths.get("trackplans")).forEach(s -> list.appendTag(new NBTTagString(s.getFileName().toString())));
		} catch (IOException e) {
			e.printStackTrace();
		}
		comp.setTag(NAMELIST, list);
		if (plan != null)
			comp.setByteArray(JSONDATA, GSON.toJson(plan).getBytes());
		return new SPacketUpdateTileEntity(pos, 0, comp);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		final NBTTagCompound compound = pkt.getNbtCompound();
		this.readFromNBT(compound);
		if (compound.hasKey(JSONDATA)) {
			plan = GSON.fromJson(new String(compound.getByteArray(JSONDATA), StandardCharsets.UTF_8), TrackPlan.class);
		}
		if(compound.hasKey(NAMELIST)) {
			final NBTTagList list = compound.getTagList(NAMELIST, 8);
			strings.clear();
			list.forEach(d -> strings.add(((NBTTagString)d).getString()));
		}
	}

	private void onLink(final BlockPos pos) {
		final SignalControllerTileEntity entity = new SignalControllerTileEntity();
		entity.setWorld(world);
		entity.setPos(this.pos);
		entity.link(pos);
		controller.add(entity);
	}

	@Override
	public void onLoad() {
		controller.clear();
		linkedPositions.forEach(this::onLink);
		final IBlockState state = world.getBlockState(pos);
		world.notifyBlockUpdate(pos, state, state, 3);
		if (name != null && world != null && !world.isRemote) {
			loadPlan(name);
		}
	}

	@Override
	public boolean hasLink() {
		return !linkedPositions.isEmpty();
	}

	@Override
	public boolean link(BlockPos pos) {
		if (world.getTileEntity(pos) instanceof SignalTileEnity) {
			if (linkedPositions.contains(pos))
				return false;
			linkedPositions.add(pos);
			this.onLink(pos);
		} else {
			if (rsInput.contains(pos) || rsOutput.contains(pos))
				return false;
			// TODO compatibility with RS Mod
			if (world.getBlockState(pos).getBlock() instanceof BlockLever)
				rsOutput.add(pos);
			else
				rsInput.add(pos);
		}
		name = "test.json";
		loadPlan("test.json");
		return true;
	}

	@Override
	public boolean unlink() {
		return false;
	}

	public TrackPlan getPlan() {
		return plan;
	}

}
