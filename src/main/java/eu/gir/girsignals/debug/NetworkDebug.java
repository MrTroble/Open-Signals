package eu.gir.girsignals.debug;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class NetworkDebug {

	private static JsonArray json = null;
	private static final Gson GSON = new Gson();
	private static final JsonParser PARSER = new JsonParser();

	public static void trigger() {
		if(json == null) {
			json = new JsonArray();
		} else {
			@SuppressWarnings("deprecation")
			Path path = Paths.get("NetworkLog " + new Date().toLocaleString());
			try {
				JsonWriter writer = new JsonWriter(Files.newBufferedWriter(path));
				writer.setIndent("    ");
				Streams.write(json, writer);
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			json = null;
		}
	}
	
	public static void networkReadHook(NBTTagCompound compound, World world, Object additionalInfo) {
		if(json == null)
			return;
		JsonObject obj = combine(compound, additionalInfo);
		obj.addProperty("Client", world.isRemote);
		obj.addProperty("Mode", "read");
		json.add(obj);
	}
	
	public static void networkWriteHook(NBTTagCompound compound, World world, Object additionalInfo) {
		if(json == null)
			return;
		JsonObject obj = combine(compound, additionalInfo);
		obj.addProperty("Client", world.isRemote);
		obj.addProperty("Mode", "write");
		json.add(obj);
	}
	
	@SuppressWarnings("deprecation")
	private static JsonObject combine(NBTTagCompound compound, Object additionalInfo) {
		JsonObject newjson = new JsonObject();
		newjson.addProperty("Time", new Date().toLocaleString());
		newjson.add("NBT", writeToJson(compound));
		newjson.add("Additional", writeToJson(compound));
		newjson.addProperty("ClassName", additionalInfo.getClass().getSimpleName());
		return newjson;
	}

	private static JsonElement toJson(Object additionalInfo) {
		String str = GSON.toJson(additionalInfo);
		return PARSER.parse(str);
	}
	
	private static JsonObject writeToJson(NBTTagCompound compound) {
		JsonObject object = new JsonObject();
		compound.getKeySet().forEach(str -> object.add(str, toJson(compound.getTag(str))));
		return object;
	}

}
