package com.troblecodings.signals.models;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.core.SignalAngel;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.ForgeModelBakery;

@OnlyIn(Dist.CLIENT)
public class SignalCustomModel {

    public static final Random RANDOM = new Random();

    private static BakedModelPair transform(final SignalModelLoaderInfo info) {
        return new BakedModelPair(info.state, null);
    }

    public static BakedModel getModel(String name, List<SignalModelLoaderInfo> list,
            ForgeModelBakery bakery, SignalAngel angel) {
        list.forEach(info -> {
            if (info.model == null)
                info.with(bakery.getModel(new ResourceLocation(OpenSignalsMain.MODID, info.name)));
        });
        return new SignalBakedModel(list.stream().map(SignalCustomModel::transform)
                .collect(Collectors.toUnmodifiableList()));
    }

}