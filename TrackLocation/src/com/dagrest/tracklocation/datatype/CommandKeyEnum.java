package com.dagrest.tracklocation.datatype;

import java.util.HashMap;
import java.util.Map;

public enum CommandKeyEnum {
	starting_status("starting_status"), start_status("start_status"),
	mutualId("mutualId"), permissions("permissions"),
	updated_reg_id("updated_reg_id");
	
	private final String name;       
	private static Map<String, CommandKeyEnum> valueMap;
	
    private CommandKeyEnum(String s) {
        name = s;
    }

    public boolean equalsName(String otherName){
        return (otherName == null)? false:name.equals(otherName);
    } 

    public String toString(){
       return name;
    }
    
    public static CommandKeyEnum getValue(String value){
    	if (valueMap == null)
        {
            valueMap = new HashMap<String, CommandKeyEnum>();
            for(CommandKeyEnum provider: values())
                valueMap.put(provider.toString(), provider);
        }
    	
        return valueMap.get(value);
    }
}
