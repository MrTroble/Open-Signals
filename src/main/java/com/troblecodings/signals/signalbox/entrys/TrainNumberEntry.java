package com.troblecodings.signals.signalbox.entrys;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.signals.core.TrainNumber;

public class TrainNumberEntry extends IPathEntry<TrainNumber> {

    private TrainNumber number = TrainNumber.DEFAULT;

    @Override
    public void readNetwork(final ReadBuffer buffer) {
        number = TrainNumber.of(buffer);
    }

    @Override
    public void writeNetwork(final WriteBuffer buffer) {
        number.writeNetwork(buffer);
    }

    @Override
    public void write(final NBTWrapper tag) {
        number.writeTag(tag);
    }

    @Override
    public void read(final NBTWrapper tag) {
        number = TrainNumber.of(tag);
    }

    @Override
    public TrainNumber getValue() {
        return number;
    }

    @Override
    public void setValue(final TrainNumber value) {
        number = value;
    }

}
