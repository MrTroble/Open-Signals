package com.troblecodings.signals.mixin;

import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.resources.model.ModelBakery;

@Mixin(ModelBakery.class)
public abstract class ModelBakeryMixin {

    @Redirect(method = "*", at = @At(value = "INVOKE",
            target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V",
            remap = false), require = 0)
    private void opensignals$suppressSignalBlockstateWarning(final Logger logger, final String format,
            final Object arg1, final Object arg2) {
        if (format != null && format.contains("Exception loading blockstate definition")
                && String.valueOf(arg1).contains("opensignals"))
            return;

        logger.warn(format, arg1, arg2);
    }
}