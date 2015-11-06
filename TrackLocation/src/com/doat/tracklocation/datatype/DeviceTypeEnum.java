package com.doat.tracklocation.datatype;

import java.util.HashMap;
import java.util.Map;

import android.os.Parcel;
import android.os.Parcelable;

public enum DeviceTypeEnum implements Parcelable{
	phone("phone"), tablet("tablet"), computer("computer"), unknown("unknown");
	
	private final String name;       
	private static Map<String, DeviceTypeEnum> valueMap;
	
    private DeviceTypeEnum(String s) {
        name = s;
    }

    public boolean equalsName(String otherName){
        return (otherName == null)? false:name.equals(otherName);
    } 

    public String toString(){
       return name;
    }
    
    public static DeviceTypeEnum getValue(String value){
    	if (valueMap == null)
        {
            valueMap = new HashMap<String, DeviceTypeEnum>();
            for(DeviceTypeEnum provider: values())
                valueMap.put(provider.toString(), provider);
        }
    	
        return valueMap.get(value);
    }

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(ordinal());
	}
	
	public static final Creator<DeviceTypeEnum> CREATOR = new Creator<DeviceTypeEnum>() {
        @Override
        public DeviceTypeEnum createFromParcel(final Parcel source) {
            return DeviceTypeEnum.values()[source.readInt()];
        }

        @Override
        public DeviceTypeEnum[] newArray(final int size) {
            return new DeviceTypeEnum[size];
        }
    };
}
