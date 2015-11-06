package com.doat.tracklocation.datatype;

import java.util.HashMap;
import java.util.Map;

// {"data":{"command":"start",
// "contactDetails":{"account":"dagrest@gmail.com","regId":"...","macAddress":"88:32:9B:01:26:DD","phoneNumber":"+972544504619","batteryPercentage":100.0},
// "regIDToReturnMessageTo":"...","time":"21:48:10 21/08/2014"},
// "registration_ids":["..."],"time_to_live":0}

public enum CommandTagEnum {
	command("command"), time("time"), message("message"), interval("interval"), 
	key("key"), value("value"), contactDetails("contactDetails"), 
	regIDToReturnMessageTo("regIDToReturnMessageTo"), registration_ids("registration_ids");
	
	private final String name;       
	private static Map<String, CommandTagEnum> valueMap;
	
    private CommandTagEnum(String s) {
        name = s;
    }

    public boolean equalsName(String otherName){
        return (otherName == null)? false:name.equals(otherName);
    } 

    public String toString(){
       return name;
    }
    
    public static CommandTagEnum getValue(String value){
    	if (valueMap == null)
        {
            valueMap = new HashMap<String, CommandTagEnum>();
            for(CommandTagEnum provider: values())
                valueMap.put(provider.toString(), provider);
        }
    	
        return valueMap.get(value);
    }
}
