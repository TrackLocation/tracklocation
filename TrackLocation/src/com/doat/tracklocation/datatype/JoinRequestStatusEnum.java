package com.doat.tracklocation.datatype;

import java.util.HashMap;
import java.util.Map;

public enum JoinRequestStatusEnum {
	SENT("sent"), ACCEPTED("accepted"), DECLINED("declined");
	
	private final String name;       
	private static Map<String, JoinRequestStatusEnum> valueMap;
	
    private JoinRequestStatusEnum(String s) {
        name = s;
    }

    public boolean equalsName(String otherName){
        return (otherName == null)? false:name.equals(otherName);
    } 

    public String toString(){
       return name;
    }
    
    public static JoinRequestStatusEnum getValue(String value){
    	if (valueMap == null)
        {
            valueMap = new HashMap<String, JoinRequestStatusEnum>();
            for(JoinRequestStatusEnum provider: values())
                valueMap.put(provider.toString(), provider);
        }
    	
        return valueMap.get(value);
    }
}
