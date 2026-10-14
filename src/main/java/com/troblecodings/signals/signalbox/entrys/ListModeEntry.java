package com.troblecodings.signals.signalbox.entrys;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.signals.core.ModeIdentifier;

public class ListModeEntry extends IPathEntry<List<ModeIdentifier>> {

    private List<ModeIdentifier> list = new ArrayList<>();

    @Override
    public void readNetwork(final ReadBuffer buffer) {
        list.clear();
        list.addAll(buffer.getList(ReadBuffer.getINetworkSaveableFunction(ModeIdentifier.class)));
    }

    @Override
    public void writeNetwork(final WriteBuffer buffer) {
        buffer.putISaveableList(list);
    }

    @Override
    public void write(final NBTWrapper tag) {
        tag.putList(getName(), list.stream().map(pos -> {
            final NBTWrapper posTag = new NBTWrapper();
            pos.write(posTag);
            return posTag;
        }).collect(Collectors.toList()));
    }

    @Override
    public void read(final NBTWrapper tag) {
        list.clear();
        tag.getList(getName()).stream().map(posTag -> ModeIdentifier.of(posTag)).forEach(list::add);
    }

    @Override
    public List<ModeIdentifier> getValue() {
        return new ArrayList<>(list);
    }

    @Override
    public void setValue(final List<ModeIdentifier> value) {
        this.list = new ArrayList<>(value);
    }

    public void add(final ModeIdentifier pos) {
        list.add(pos);
    }

    public void remove(final ModeIdentifier pos) {
        list.remove(pos);
    }

    @Override
    public List<ModeIdentifier> getDefaultValue() {
        return new ArrayList<>();
    }

}