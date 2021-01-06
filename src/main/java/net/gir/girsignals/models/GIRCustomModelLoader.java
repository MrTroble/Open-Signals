package net.gir.girsignals.models;

import java.util.HashMap;
import java.util.function.Consumer;

import net.gir.girsignals.GirsignalsMain;
import net.gir.girsignals.blocks.SignalHV;
import net.gir.girsignals.blocks.SignalKS;
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
			cm.register("hv_zs1", ebs -> ebs.getValue(SignalHV.ZS1) != null, 5f);
			cm.register("hv_zs7", ebs -> true, 4.6f);
			cm.register("hv_hp", ebs -> {
				System.out.println(ebs.toString());
				return ebs.getValue(SignalHV.HAUPTSIGNAL) != null;
			}, 5.4f);
			cm.register("hv_zs3", ebs -> true, 6.9f);
		});
		registeredModels.put("kssignal", cm -> {
			cm.register("ks_base", ebs -> true, 0);
			cm.register("ks_ne2", ebs -> true, 0);
			cm.register("ks_mast1", ebs -> true, 1);
			cm.register("ks_sign_distant", ebs -> true, 1);
			cm.register("ks_mast2", ebs -> true, 2);
			cm.register("ks_sign", ebs -> true, 2);
			cm.register("ks_mast3", ebs -> true, 3);
			cm.register("ks_zs2", ebs -> true, 3);
			cm.register("ks_mast4", ebs -> true, 4);
			cm.register("ks_zs3v", ebs -> true, 4);
			cm.register("ks_number", ebs -> true, 4);
			cm.register("ks_signal", ebs -> true, 5);
			// cm.register("ks_signal", ebs -> {
			// System.out.println(ebs.toString());
			// return ebs.getValue(SignalKS.KOMBISIGNAL) != null;
			// }, 5);
			cm.register("ks_zs3", ebs -> true, 6);
		});
		registeredModels.put("hlsignal", cm -> {
			cm.register("hl_base", ebs -> true, 0);
			cm.register("hl_ne2", ebs -> true, 0);
			cm.register("hl_number", ebs -> true, 0);
			cm.register("hl_mast1", ebs -> true, 1);
			cm.register("hl_ne2_2", ebs -> true, 1);
			cm.register("hl_sign_distant", ebs -> true, 1);
			cm.register("hl_mast2", ebs -> true, 2);
			cm.register("hl_sign_main", ebs -> true, 2);
			cm.register("hl_mast3", ebs -> true, 3);
			cm.register("hl_zs2", ebs -> true, 3);
			cm.register("hl_mast4", ebs -> true, 4);
			cm.register("hl_shield2", ebs -> true, 4);
			cm.register("hl_shield1", ebs -> true, 5);
		});
	}

	@Override
	public boolean accepts(ResourceLocation modelLocation) {
		if (!modelLocation.getResourceDomain().equals(GirsignalsMain.MODID))
			return false;
		return registeredModels.containsKey(modelLocation.getResourcePath());
	}

	@Override
	public IModel loadModel(ResourceLocation modelLocation) throws Exception {
		ModelResourceLocation mrl = (ModelResourceLocation) modelLocation;
		return new SignalCustomModel(registeredModels.get(modelLocation.getResourcePath()),
				EnumFacing.byName(mrl.getVariant().split("=")[1]));
	}

}
