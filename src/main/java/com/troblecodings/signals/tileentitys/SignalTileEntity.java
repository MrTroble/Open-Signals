package com.troblecodings.signals.tileentitys;

import com.troblecodings.core.interfaces.NamableWrapper;
import com.troblecodings.guilib.ecs.interfaces.ISyncable;
import com.troblecodings.signals.blocks.Signal;
import com.troblecodings.signals.core.RenderOverlayInfo;
import com.troblecodings.signals.core.TileEntityInfo;
import com.troblecodings.signals.statehandler.SignalStateHandler;
import com.troblecodings.signals.statehandler.SignalStateInfo;

import net.minecraft.world.entity.player.Player;

public class SignalTileEntity extends BasicBlockEntity implements NamableWrapper, ISyncable {

    private String customName = "";

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
        return customName;
    }

    public Signal getSignal() {
        return ((Signal) getBlockState().getBlock());
    }

    public void setNameWithOutSync(final String str) {
        customName = str;

    }

    public void setCustomName(final String str) {
        setNameWithOutSync(str);
        SignalStateHandler.setState(new SignalStateInfo(level, worldPosition), Signal.CUSTOMNAME,
                str);
    }

    public String getSignalName() {
        return customName;
    }
}