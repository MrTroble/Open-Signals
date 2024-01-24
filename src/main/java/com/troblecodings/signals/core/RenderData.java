package com.troblecodings.signals.core;

import java.util.Objects;

import com.troblecodings.signals.blocks.BasicBlock;
import com.troblecodings.signals.models.ModelInfoWrapper;

import net.minecraft.core.Vec3i;
import net.minecraftforge.client.model.data.EmptyModelData;

public class RenderData {

    public static final ModelInfoWrapper EMPTY_WRAPPER = new ModelInfoWrapper(
            EmptyModelData.INSTANCE);

    public final Vec3i vector;
    public final BasicBlock block;
    public final ModelInfoWrapper modelInfo;

    public RenderData(final Vec3i vector, final BasicBlock block) {
        this(vector, block, EMPTY_WRAPPER);
    }

    public RenderData(final Vec3i vector, final BasicBlock block,
            final ModelInfoWrapper modelInfo) {
        this.vector = vector;
        this.block = block;
        this.modelInfo = modelInfo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(block, modelInfo, vector);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final RenderData other = (RenderData) obj;
        return Objects.equals(block, other.block) && Objects.equals(modelInfo, other.modelInfo)
                && Objects.equals(vector, other.vector);
    }

    @Override
    public String toString() {
        return "RenderData [vector=" + vector + ",block=" + block + ",modeldata=" + modelInfo;
    }

}