package com.doat.tracklocation.datatype;

import java.util.HashMap;
import java.util.Map;

public enum PushNotificationServiceStatusEnum {
	na("N/A"), available("available");
	
	private final String name;       
	private static Map<String, PushNotificationServiceStatusEnum> valueMap;
	
    private PushNotificationServiceStatusEnum(String s) {
        name = s;
    }

    public boolean equalsName(String otherName){
        return (otherName == null)? false:name.equals(otherName);
    } 

    public String toString(){
       return name;
    }
    
    public static PushNotificationServiceStatusEnum getValue(String value){
    	if (valueMap == null)
        {
            valueMap = new HashMap<String, PushNotificationServiceStatusEnum>();
            for(PushNotificationServiceStatusEnum provider: values())
                valueMap.put(provider.toString(), provider);
        }
    	
        return valueMap.get(value);
    }
}
