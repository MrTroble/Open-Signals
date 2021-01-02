package net.gir.girsignals.models;

import java.util.HashMap;
import java.util.function.Consumer;

import net.gir.girsignals.GirsignalsMain;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GIRCustomModelLoader implements ICustomModelLoader {
	
	private static HashMap<String, Consumer<SignalCustomModel>> registeredModels = new HashMap<>(); 
	
	@Override
	public void onResourceManagerReload(IResourceManager resourceManager) {
		registeredModels.clear();
		registeredModels.put("hvsignal", cm -> {
			cm.register("hv_base", ebs -> true, 0);
			cm.register("hv_mast_sign", ebs -> true, 1);
			cm.register("hv_mast_number", ebs -> true, 2);
			cm.register("hv_zs3v", ebs -> true, 3);
			cm.register("hv_vr", ebs -> true, 4);
			cm.register("hv_zs1", ebs -> false, 4.4f);
			cm.register("hv_zs7", ebs -> true, 4.6f);
			cm.register("hv_hp", ebs -> true, 5.4f);
			cm.register("hv_zs3", ebs -> true, 6.9f);
			
			// HP 2
			cm.register("lamp_black", ebs -> true, (3.5f/32.0f), 5 - (1/32.0f), -((6/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			// HP 0
			cm.register("lamp_black", ebs -> true, (3.5f/32.0f), 5 + (23/32.0f), -((6/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			cm.register("lamp_black", ebs -> true, -(6.5f/32.0f), 5 + (23/32.0f), -((6/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			// HP 1/2 (green)
			cm.register("lamp_black", ebs -> true, (3.5f/32.0f), 6 + (1/32.0f), -((6/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			
			// VR0
			cm.register("lamp_black", ebs -> true, (10.5f/32.0f), 3 + (12.5f/32.0f), -((6/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			cm.register("lamp_black", ebs -> true, -(5.5f/32.0f), 3 + (30.5f/32.0f), -((6/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			
			// VR1
			cm.register("lamp_black", ebs -> true, (2.5f/32.0f), 3 + (12.5f/32.0f), -((6/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			cm.register("lamp_black", ebs -> true, -(13.5f/32.0f), 3 + (30.5f/32.0f), -((6/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			
			// RS
			cm.register("lamp_black_small", ebs -> true, -(6.5f/32.0f), 5 + (15/32.0f), -((6/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			cm.register("lamp_black_small", ebs -> true, (3.5f/32.0f), 5 + (15/32.0f), -((6/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			
			// Status light
			cm.register("lamp_black_small", ebs -> true, (3.5f/32.0f), 5 + (7/32.0f), -((6/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			
			// ZS 1
			cm.register("lamp_black_small", ebs -> false, -(1.5f/32.0f), 4 + (21/32.0f), -((8/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			cm.register("lamp_black_small", ebs -> false, -(4.5f/32.0f), 4 + (15.3f/32.0f), -((8/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			cm.register("lamp_black_small", ebs -> false, (1.5f/32.0f), 4 + (15.3f/32.0f), -((8/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			
			// VR Short distance lamp
			cm.register("lamp_black_small", ebs -> true, (8.5f/32.0f), 3 + (30.5f/32.0f), -((6/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			
			// ZS 7
			cm.register("lamp_black_small", ebs -> true, -(1.5f/32.0f), 4 + (15.5f/32.0f), -((8/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			cm.register("lamp_black_small", ebs -> true, -(4.5f/32.0f), 4 + (21.5f/32.0f), -((8/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
			cm.register("lamp_black_small", ebs -> true, (1.5f/32.0f), 4 + (21.5f/32.0f), -((8/32.0f) + 0.01f), 0.1f, 0.1f, 0f);
		});
	}

	@Override
	public boolean accepts(ResourceLocation modelLocation) {
		if(!modelLocation.getResourceDomain().equals(GirsignalsMain.MODID)) return false;
		return registeredModels.containsKey(modelLocation.getResourcePath());
	}

	@Override
	public IModel loadModel(ResourceLocation modelLocation) throws Exception {
		ModelResourceLocation mrl = (ModelResourceLocation) modelLocation;
		return new SignalCustomModel(registeredModels.get(modelLocation.getResourcePath()), EnumFacing.byName(mrl.getVariant().split("=")[1]));
	}

}
