package eu.gir.girsignals.blocks;

import eu.gir.girsignals.tileentitys.SignalTileEnity;

public interface ISignalAutoconifig {
	
	void change(final int speed, final SignalTileEnity current, final SignalTileEnity next);
	
	void reset(final SignalTileEnity current, final SignalTileEnity prev);
	
}
