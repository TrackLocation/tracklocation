package com.doat.tracklocation.datatype;

public class PermissionsData {
	private String email;
	private int isLocationSharePermitted;
	private int command; // reserved
	private int adminCommand;  // reserved
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public int getIsLocationSharePermitted() {
		return isLocationSharePermitted;
	}
	public void setIsLocationSharePermitted(int isLocationSharePermitted) {
		this.isLocationSharePermitted = isLocationSharePermitted;
	}
	public int getCommand() {
		return command;
	}
	public void setCommand(int command) {
		this.command = command;
	}
	public int getAdminCommand() {
		return adminCommand;
	}
	public void setAdminCommand(int adminCommand) {
		this.adminCommand = adminCommand;
	}
}
