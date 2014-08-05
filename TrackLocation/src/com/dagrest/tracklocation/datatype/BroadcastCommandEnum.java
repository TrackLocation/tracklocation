package com.dagrest.tracklocation.datatype;

import java.util.HashMap;
import java.util.Map;

public enum BroadcastCommandEnum {
	location_updated("location_updated"), gcm_status("gcm_status"), location_service_status("location_service_status"), 
	join_number("join_number"), fetch_contacts_completed("fetch_contacts_completed"), keep_alive("keep_alive"), 
	resend_join_request("resend_join_request"), message("message"), location("location"), 
	contcat_details("contcat_details");
	
	private final String name;       
	private static Map<String, BroadcastCommandEnum> valueMap;
	
    private BroadcastCommandEnum(String s) {
        name = s;
    }

    public boolean equalsName(String otherName){
        return (otherName == null)? false:name.equals(otherName);
    } 

    public String toString(){
       return name;
    }
    
    public static BroadcastCommandEnum getValue(String value){
    	if (valueMap == null)
        {
            valueMap = new HashMap<String, BroadcastCommandEnum>();
            for(BroadcastCommandEnum provider: values())
                valueMap.put(provider.toString(), provider);
        }
    	
        return valueMap.get(value);
    }
}
