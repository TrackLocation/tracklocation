package com.doat.tracklocation.datatype;

import java.util.ArrayList;
import java.util.List;

public class PermissionsDataList {
	private List<PermissionsData> permissionsData;

	public List<PermissionsData> getPermissionsData() {
		if( permissionsData == null){
			permissionsData = new ArrayList<PermissionsData>();
		} 
		return permissionsData;	
	}
}
