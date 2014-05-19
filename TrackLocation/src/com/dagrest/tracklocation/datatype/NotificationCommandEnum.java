package com.dagrest.tracklocation.datatype;

import java.util.HashMap;
import java.util.Map;

public enum NotificationCommandEnum {
	pushNotificationServiceStatus("pushNotificationServiceStatus"), trackLocationServiceStatus("trackLocationServiceStatus");
	
	private final String name;       
	private static Map<String, NotificationCommandEnum> valueMap;
	
    private NotificationCommandEnum(String s) {
        name = s;
    }

    public boolean equalsName(String otherName){
        return (otherName == null)? false:name.equals(otherName);
    } 

    public String toString(){
       return name;
    }
    
    public static NotificationCommandEnum getValue(String value){
    	if (valueMap == null)
        {
            valueMap = new HashMap<String, NotificationCommandEnum>();
            for(NotificationCommandEnum provider: values())
                valueMap.put(provider.toString(), provider);
        }
    	
        return valueMap.get(value);
    }
}
