package com.doat.tracklocation.datatype;

import java.util.ArrayList;
import java.util.List;

public class RegistryIdList {
	
	private List<String> registryId;
	
	private static class ListHolder{
		static final RegistryIdList instance = new RegistryIdList();
	}

	public static RegistryIdList getInstance(){
		return ListHolder.instance;
	}
	
	private RegistryIdList(){
		registryId = new ArrayList<String>();
	}
	
	public List<String> getRegistryId() {
		return registryId;
	}
}
