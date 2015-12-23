package com.doat.tracklocation.datatype;

public class ActionMenuObj {
	private int key;
	private int mIcon;
	private String sCaption;

	public ActionMenuObj(int key, String caption, int icon) {
		this.key = key;
		this.sCaption = caption;
		this.setmIcon(icon);
	}

	public int getKey() {
		return key;
	}

	public String getCaption() {
		return sCaption;
	}

	public int getIcon() {
		return mIcon;
	}

	private void setmIcon(int mIcon) {
		this.mIcon = mIcon;
	}
}
