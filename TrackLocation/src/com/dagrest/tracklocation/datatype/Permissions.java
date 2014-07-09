package com.dagrest.tracklocation.datatype;

public class Permissions {
	private String email;
	private int isLocationSharePermitted;
	private int command; // reserved
	private int admin_command;  // reserved
	
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
	public int getAdmin_command() {
		return admin_command;
	}
	public void setAdmin_command(int admin_command) {
		this.admin_command = admin_command;
	}
}
