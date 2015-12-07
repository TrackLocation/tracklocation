package com.doat.tracklocation.datatype;

import java.util.HashMap;
import java.util.Map;

public enum CommandValueEnum {
	success("success"), 
	error("error"), 
	wait("wait"), 
	not_defined("not_defined"), 
	not_permitted("not_permitted"),
	// start Track Location Service command received on recipient
	start_track_location_service_received("start_tls_received"), // tls - Track Location Service
	online("online")
	; 
	
	private final String name;       
	private static Map<String, CommandValueEnum> valueMap;
	
    private CommandValueEnum(String s) {
        name = s;
    }

    public boolean equalsName(String otherName){
        return (otherName == null)? false:name.equals(otherName);
    } 

    public String toString(){
       return name;
    }
    
    public static CommandValueEnum getValue(String value){
    	if (valueMap == null)
        {
            valueMap = new HashMap<String, CommandValueEnum>();
            for(CommandValueEnum provider: values())
                valueMap.put(provider.toString(), provider);
        }
    	
        return valueMap.get(value);
    }
}
