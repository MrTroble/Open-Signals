package com.troblecodings.signals.signalbox.entrys;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.core.ReadBuffer;
import com.troblecodings.core.WriteBuffer;
import com.troblecodings.signals.core.PosIdentifier;

public class ListBlockPosEntry extends IPathEntry<List<PosIdentifier>> {

    private List<PosIdentifier> list = new ArrayList<>();

    @Override
    public void readNetwork(final ReadBuffer buffer) {
        list.clear();
        final int size = buffer.getInt();
        for (int i = 0; i < size; i++) {
            list.add(PosIdentifier.of(buffer));
        }
    }

    @Override
    public void writeNetwork(final WriteBuffer buffer) {
        buffer.putInt(list.size());
        list.forEach(pos -> pos.writeNetwork(buffer));
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
        tag.getList(getName()).stream().map(posTag -> PosIdentifier.of(posTag)).forEach(list::add);
    }

    @Override
    public List<PosIdentifier> getValue() {
        return new ArrayList<>(list);
    }

    @Override
    public void setValue(final List<PosIdentifier> value) {
        this.list = new ArrayList<>(value);
    }

    public void add(final PosIdentifier pos) {
        list.add(pos);
    }

    public void remove(final PosIdentifier pos) {
        list.remove(pos);
    }

}