package com.dagrest.tracklocation.json;

import java.util.HashMap;
import java.util.Map;

public enum DeviceTypeEnum {
	phone("phone"), tablet("tablet"), computer("computer");
	
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
}
