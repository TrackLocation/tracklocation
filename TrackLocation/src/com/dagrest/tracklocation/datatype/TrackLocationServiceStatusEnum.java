package com.dagrest.tracklocation.datatype;

import java.util.HashMap;
import java.util.Map;

public enum TrackLocationServiceStatusEnum {
	na("N/A"), available("available"), started("started"), stopped("stopped");
	
	private final String name;       
	private static Map<String, TrackLocationServiceStatusEnum> valueMap;
	
    private TrackLocationServiceStatusEnum(String s) {
        name = s;
    }

    public boolean equalsName(String otherName){
        return (otherName == null)? false:name.equals(otherName);
    } 

    public String toString(){
       return name;
    }
    
    public static TrackLocationServiceStatusEnum getValue(String value){
    	if (valueMap == null)
        {
            valueMap = new HashMap<String, TrackLocationServiceStatusEnum>();
            for(TrackLocationServiceStatusEnum provider: values())
                valueMap.put(provider.toString(), provider);
        }
    	
        return valueMap.get(value);
    }
}
