package com.doat.tracklocation.datatype;

import java.util.HashMap;
import java.util.Map;

public enum BroadcastKeyEnum {
//	starting_status("starting_status"), 
	start_status("start_status"),
	location_updated("location_updated"), 
	gcm_status("gcm_status"),
	join_number("join_number"),
	fetch_contacts_completed("fetch_contacts_completed"),
	resend_join_request("resend_join_request"),
	keep_alive("keep_alive"),
	message("message"),
	join_sms("join_sms"),
	turn_off("turn_off"),
	finish("finish"),
	restart_tls("restart_tls"),
	register_to_gcm("register_to_gcm");
	
	private final String name;       
	private static Map<String, BroadcastKeyEnum> valueMap;
	
    private BroadcastKeyEnum(String s) {
        name = s;
    }

    public boolean equalsName(String otherName){
        return (otherName == null)? false:name.equals(otherName);
    } 

    public String toString(){
       return name;
    }
    
    public static BroadcastKeyEnum getValue(String value){
    	if (valueMap == null)
        {
            valueMap = new HashMap<String, BroadcastKeyEnum>();
            for(BroadcastKeyEnum provider: values())
                valueMap.put(provider.toString(), provider);
        }
    	
        return valueMap.get(value);
    }
}

