package net.gir.girsignals;

import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.common.property.IUnlistedProperty;

public class SEProperty<T extends Comparable<T>> implements IUnlistedProperty<T>{

	public enum ChangeableStage {
		APISTAGE, GUISTAGE;
	}
	
    private final IProperty<T> parent;
    private final T defaultValue;
    private final ChangeableStage stage;

	public SEProperty(IProperty<T> parent, T defaultValue, ChangeableStage stage) {
		this.parent = parent;
		this.defaultValue = defaultValue;
		this.stage = stage;
	}
	
    @Override
    public String getName()
    {
        return parent.getName();
    }

    @Override
    public boolean isValid(T value)
    {
        return parent.getAllowedValues().contains(value);
    }

    @Override
    public Class<T> getType()
    {
        return parent.getValueClass();
    }

    @Override
    public String valueToString(T value)
    {
        return parent.getName(value);
    }
    
    @SuppressWarnings("unchecked")
	public T cast(Object value) {
		return (T) value;
    }

	public T getDefault() {
		return defaultValue;
	}
	
	public boolean isChangabelAtStage(ChangeableStage stage) {
		return this.stage.equals(stage);
	}
	
	public static <T extends Comparable<T>> SEProperty<T> cast(IUnlistedProperty<T> iup) {
		return (SEProperty<T>) iup;
	}
	
	public static <T extends Enum<T> & IStringSerializable> SEProperty<T> of(String name, T defaultValue) {
		return of(name, defaultValue, ChangeableStage.APISTAGE);
	}
	
	public static SEProperty<Boolean> of(String name, boolean defaultValue) {
		return of(name, defaultValue, ChangeableStage.APISTAGE);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Enum<T> & IStringSerializable> SEProperty<T> of(String name, T defaultValue, ChangeableStage stage) {
		return new SEProperty<T>(PropertyEnum.create(name, (Class<T>) defaultValue.getClass()), defaultValue, stage);
	}

	public static SEProperty<Boolean> of(String name, boolean defaultValue, ChangeableStage stage) {
		return new SEProperty<Boolean>(PropertyBool.create(name), defaultValue, stage);
	}
}
