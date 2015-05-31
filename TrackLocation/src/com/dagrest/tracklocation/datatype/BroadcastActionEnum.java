package com.dagrest.tracklocation.datatype;

import java.util.HashMap;
import java.util.Map;

public enum BroadcastActionEnum {
	KEEP_ALIVE("KEEP_ALIVE"),
 	BROADCAST_LOCATION_UPDATED("com.dagrest.tracklocation.service.GcmIntentService.LOCATION_UPDATED"),
 	BROADCAST_JOIN("com.dagrest.tracklocation.JoinContactList.BROADCAST_JOIN"),
 	BROADCAST_LOCATION_KEEP_ALIVE("com.dagrest.tracklocation.Map.KEEP_ALIVE"),
 	BROADCAST_MESSAGE("com.dagrest.tracklocation.MESSAGE"),
	BROADCAST_TURN_OFF_RING("com.dagrest.tracklocation.TURN_OFF_RING"),
	BROADCAST_FINISH_ACITIVTY_DIALOG_RING("com.dagrest.tracklocation.FINISH_ACITIVTY_DIALOG_RING");
	
	private final String name;       
	private static Map<String, BroadcastActionEnum> valueMap;
	
    private BroadcastActionEnum(String s) {
        name = s;
    }

    public boolean equalsName(String otherName){
        return (otherName == null)? false:name.equals(otherName);
    } 

    public String toString(){
       return name;
    }
    
    public static BroadcastActionEnum getValue(String value){
    	if (valueMap == null)
        {
            valueMap = new HashMap<String, BroadcastActionEnum>();
            for(BroadcastActionEnum provider: values())
                valueMap.put(provider.toString(), provider);
        }
    	
        return valueMap.get(value);
    }
}
