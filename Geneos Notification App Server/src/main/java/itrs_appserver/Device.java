package itrs_appserver;

public class Device {
	
	private final String android_id;
	private String reg_id;
	
	public Device(String a_id, String r_id)
	{
		this.android_id = a_id;
		this.setReg_id(r_id);
	}

	private String getAndroid_id() {
		return android_id;
	}

	private String getReg_id() {
		return reg_id;
	}

	private void setReg_id(String reg_id) {
		this.reg_id = reg_id;
	}

}
