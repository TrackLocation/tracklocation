package com.dagrest.tracklocation.datatype;

import java.util.HashMap;
import java.util.Map;

public enum CommandEnum {
	start("start"), stop("stop"), setinterval("setinterval"),
	location("location"), status_request("status_request"), 
	status_response("status_response"), join_approval("join_approval"),
	join_rejected("join_rejected"),
	track_location_service_keep_alive("track_location_service_keep_alive");
	
	private final String name;       
	private static Map<String, CommandEnum> valueMap;
	
    private CommandEnum(String s) {
        name = s;
    }

    public boolean equalsName(String otherName){
        return (otherName == null)? false:name.equals(otherName);
    } 

    public String toString(){
       return name;
    }
    
    public static CommandEnum getValue(String value){
    	if (valueMap == null)
        {
            valueMap = new HashMap<String, CommandEnum>();
            for(CommandEnum provider: values())
                valueMap.put(provider.toString(), provider);
        }
    	
        return valueMap.get(value);
    }
}
