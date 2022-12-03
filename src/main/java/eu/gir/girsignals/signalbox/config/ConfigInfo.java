package eu.gir.girsignals.signalbox.config;

import eu.gir.girsignals.enums.PathType;
import eu.gir.girsignals.tileentitys.SignalTileEnity;

public class ConfigInfo {

    public final SignalTileEnity current;
    public final SignalTileEnity next;
    public final int speed;
    public PathType type = PathType.NONE;

    public ConfigInfo(final SignalTileEnity current, final SignalTileEnity next, final int speed) {
        this.current = current;
        this.next = next;
        this.speed = speed;
    }

}