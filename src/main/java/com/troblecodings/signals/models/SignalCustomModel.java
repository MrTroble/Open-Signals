package com.troblecodings.signals.models;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.client.model.SimpleModelState;

@OnlyIn(Dist.CLIENT)
public class SignalCustomModel {

    public static final Random RANDOM = new Random();

    private static BakedModelPair transform(final SignalModelLoaderInfo info,
            final ModelBakeEvent event, ModelResourceLocation location) {
        return new BakedModelPair(info.state, info.model.bake(event.getModelLoader(),
                ForgeModelBakery.defaultTextureGetter(), SimpleModelState.IDENTITY, location));
    }

    public static BakedModel getModel(ModelResourceLocation location,
            List<SignalModelLoaderInfo> list, ModelBakeEvent event) {
        return new SignalBakedModel(list.stream().map(info -> transform(info, event, location))
                .collect(Collectors.toUnmodifiableList()));
    }

}