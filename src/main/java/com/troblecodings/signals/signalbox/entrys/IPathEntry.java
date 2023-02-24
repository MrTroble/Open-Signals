package com.troblecodings.signals.signalbox.entrys;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.troblecodings.signals.core.Observable;
import com.troblecodings.signals.core.Observer;

public abstract class IPathEntry<T> implements INetworkSavable, Observable {

    protected final List<Observer> observers = new ArrayList<>();

    private String name = "defaultEntry";

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Returns the value to hold
     *
     * @return the value
     */
    public abstract T getValue();

    /**
     * Sets the value of this entry
     *
     * @param the value to set
     */
    public abstract void setValue(T value);

    public abstract int getMinBufferSize();

    @Override
    public int hashCode() {
        return Objects.hash(name, this.getValue());
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if ((obj == null) || (getClass() != obj.getClass()))
            return false;
        final IPathEntry<?> other = (IPathEntry<?>) obj;
        return Objects.equals(name, other.name) && Objects.equals(getValue(), other.getValue());
    }

    @Override
    public String toString() {
        return "IPathEntry [name=" + name + ", value=" + this.getValue() + "]";
    }

    @Override
    public void addListener(final Observer observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void removeListener(final Observer observer) {
        observers.remove(observer);
    }

    public void updateValue(final int capacity) {
        final ByteBuffer buffer = ByteBuffer.allocate(capacity);
        readNetwork(buffer);
        this.observers.forEach(observer -> observer.update(buffer));
    }
}
