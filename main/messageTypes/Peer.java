package main.messageTypes;

/**
 * PeerInfo
 */
public class Peer {

	private String ID, address;
	private int port;
	private boolean isFilePresent;

	public Peer(String ID, String address, String port, String fileParam) {
		this.ID = ID;
		this.address = address;
		this.port = Integer.parseInt(port);
		this.isFilePresent = fileParam.equals("1");
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getAddress() {
		return address;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setPort(String port) {
		this.port = Integer.parseInt(port);
	}

	public int getPort() {
		return port;
	}

	public void setID(String ID) {
		this.ID = ID;
	}

	public String getPeerId() {
		return this.ID;
	}

	public void setFileExist(boolean fileStatus) {
		this.isFilePresent = fileStatus;
	}

	public boolean hasFile() {
		return this.isFilePresent;
	}
}
