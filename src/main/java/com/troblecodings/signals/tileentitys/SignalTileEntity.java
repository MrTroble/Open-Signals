package com.troblecodings.signals.tileentitys;

import java.util.Map;

import com.troblecodings.core.interfaces.NamableWrapper;
import com.troblecodings.guilib.ecs.interfaces.ISyncable;
import com.troblecodings.signals.SEProperty;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.core.RenderOverlayInfo;
import com.troblecodings.signals.core.TileEntityInfo;
import com.troblecodings.signals.handler.SignalStateHandler;
import com.troblecodings.signals.handler.SignalStateInfo;
import com.troblecodings.signals.models.ModelInfoWrapper;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelDataMap.Builder;

public class SignalTileEntity extends BasicBlockEntity implements NamableWrapper, ISyncable {

    public SignalTileEntity(final TileEntityInfo info) {
        super(info);
    }

    @Override
    public boolean isValid(final Player player) {
        return true;
    }

    public void renderOverlay(final RenderOverlayInfo info) {
        getSignal().renderOverlay(info.with(this));
    }

    @Override
    public String getNameWrapper() {
        if (hasCustomName())
            return customName;
        return getSignal().getSignalTypeName();

    }

    public Signal getSignal() {
        return ((Signal) getBlockState().getBlock());
    }

    @Override
    public IModelData getModelData() {
        final Map<SEProperty, String> states = SignalStateHandler
                .getStates(new SignalStateInfo(level, worldPosition));
        final Builder builder = new ModelDataMap.Builder();
        states.forEach((property, value) -> {
            builder.withInitial(property, value);
        });
        return new ModelInfoWrapper(builder.build());
    }
}