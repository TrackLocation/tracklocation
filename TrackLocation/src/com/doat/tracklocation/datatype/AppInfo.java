package com.doat.tracklocation.datatype;

public class AppInfo {
	private int versionNumber;
	private String versionName;
	
	public AppInfo(int versionNumber, String versionName) {
		super();
		this.versionNumber = versionNumber;
		this.versionName = versionName;
	}
	public int getVersionNumber() {
		return versionNumber;
	}
	public void setVersionNumber(int versionNumber) {
		this.versionNumber = versionNumber;
	}
	public String getVersionName() {
		return versionName;
	}
	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}
}
