package com.dagrest.tracklocation.datatype;

import java.util.HashMap;
import java.util.Map;

public enum CommandEnum {
	Start("Start"), Stop("Stop"), SetInterval("SetInterval"),
	Location("Location");
	
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
