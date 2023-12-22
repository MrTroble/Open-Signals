package com.troblecodings.signals.core;

import java.util.Objects;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.WriteBuffer;

public class TrainNumber {

    private static final String TRAIN_NUMBER = "trainNumber";

    public final String trainNumber;

    public TrainNumber() {
        this("");
    }

    public TrainNumber(final String trainNumber) {
        this.trainNumber = trainNumber;
    }

    public static TrainNumber of(final ReadBuffer buffer) {
        return new TrainNumber(buffer.getString());
    }

    public static TrainNumber of(final NBTWrapper wrapper) {
        return new TrainNumber(wrapper.getString(TRAIN_NUMBER));
    }

    public void writeTag(final NBTWrapper wrapper) {
        wrapper.putString(TRAIN_NUMBER, trainNumber);
    }

    public void writeNetwork(final WriteBuffer buffer) {
        buffer.putString(trainNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trainNumber);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final TrainNumber other = (TrainNumber) obj;
        return Objects.equals(trainNumber, other.trainNumber);
    }

    @Override
    public String toString() {
        return trainNumber;
    }

}