package main.messageTypes;

/**
 * Peer class represents information about a peer.
 */
public class Peer {

	private String ID, address;
	private int port;
	private boolean isFilePresent;

    /**
     * Constructor to create a Peer object with provided information.
     *
     * @param ID        The peer ID
     * @param address   The peer's IP address
     * @param port      The port number
     * @param fileParam A parameter indicating whether the peer has the file (1 for yes, 0 for no)
     */
    public Peer(String ID, String address, String port, String fileParam) {
        this.ID = ID;
        this.address = address;
        this.port = Integer.parseInt(port);
        this.isFilePresent = fileParam.equals("1");
    }

    /**
     * Set the IP address of the peer.
     *
     * @param address The IP address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Get the IP address of the peer.
     *
     * @return The IP address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Set the port number of the peer.
     *
     * @param port The port number to set
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Set the port number of the peer from a string.
     *
     * @param port The port number as a string
     */
    public void setPort(String port) {
        this.port = Integer.parseInt(port);
    }

    /**
     * Get the port number of the peer.
     *
     * @return The port number
     */
    public int getPort() {
        return port;
    }

    /**
     * Set the peer's ID.
     *
     * @param ID The peer ID to set
     */
    public void setID(String ID) {
        this.ID = ID;
    }

    /**
     * Get the peer's ID.
     *
     * @return The peer ID
     */
    public String getPeerId() {
        return this.ID;
    }

    /**
     * Set the file presence status for the peer.
     *
     * @param fileStatus A boolean indicating whether the peer has the file
     */
    public void setFileExist(boolean fileStatus) {
        this.isFilePresent = fileStatus;
    }

    /**
     * Check if the peer has the file.
     *
     * @return true if the peer has the file, false otherwise
     */
    public boolean hasFile() {
        return this.isFilePresent;
    }
}
