package com.dagrest.tracklocation.datatype;

import java.util.ArrayList;
import java.util.List;

public class CustomerDataFromFileList {
	private List<CustomerDataFromFile> customerDataFromFileList;

	public List<CustomerDataFromFile> getCustomerDataFromFileList() {
		if( customerDataFromFileList == null){
			customerDataFromFileList = new ArrayList<CustomerDataFromFile>();
		} 
		return customerDataFromFileList;
	}
}
