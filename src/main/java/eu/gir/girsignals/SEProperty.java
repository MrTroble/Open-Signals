package eu.gir.girsignals;

import com.google.common.base.Optional;

import eu.gir.girsignals.EnumSignals.IIntegerable;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.common.property.IUnlistedProperty;

public class SEProperty<T extends Comparable<T>> implements IUnlistedProperty<T>, IIntegerable<T>{
	
	public enum ChangeableStage {
		APISTAGE, GUISTAGE, APISTAGE_NONE_CONFIG(/*Not configurable in UI*/), AUTOMATICSTAGE(/*Special stage, does nothing*/);
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
    
    public Optional<T> readFromNBT(NBTTagCompound comp) {
    	if(comp.hasKey(this.getName())) {
    		return Optional.of(getObjFromID(comp.getInteger(this.getName())));
    	}
    	return Optional.absent();
    }
    
    @SuppressWarnings("unchecked")
	public NBTTagCompound writeToNBT(NBTTagCompound comp, Object value) {
    	if(value != null && isValid((T) value))
    		comp.setInteger(getName(), getIDFromObj(value));
    	return comp;
    }
    
    public boolean isValid(int id) {
    	if(getType().isEnum()) {
    		return getType().getEnumConstants().length > id && id > -1;
    	} else if(getType().equals(Boolean.class)) {
    		return id == 0 || id == 1;
    	}
    	return false;
    }
    
    @SuppressWarnings("rawtypes")
	public static int getIDFromObj(Object obj) {
    	if(obj instanceof Enum) {
    		return ((Enum) obj).ordinal();
    	} else if(obj instanceof Boolean) {
    		return ((Boolean) obj).booleanValue() ? 1:0;
    	}
    	throw new IllegalArgumentException("Given paramter is not a exceptable value!");
    }
    
    @SuppressWarnings("unchecked")
	public T getObjFromID(int obj) {
    	if(!isValid(obj))
        	throw new IllegalArgumentException("Given paramter is not a exceptable value!");
    	if(getType().isEnum()) {
    		return (T) getType().getEnumConstants()[obj];
    	} else if(getType().equals(Boolean.class)) {
    		return (T)(Boolean.valueOf(obj == 1));
    	}
    	throw new IllegalArgumentException("Wrong generic type!");
    }
    
    public int count() {
    	if(getType().isEnum()) {
    		return getType().getEnumConstants().length;
    	} else if(getType().equals(Boolean.class)) {
    		return 2;
    	}
    	throw new IllegalArgumentException("Wrong generic type!");
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
	
	@Override
	public String toString() {
		return "SEP[" + this.getName() + "]";
	}
	
	@SuppressWarnings("rawtypes")
	public static SEProperty<?> cst(Object iup) {
		return (SEProperty) iup;
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
