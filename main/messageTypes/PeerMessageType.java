package main.messageTypes;

import java.io.*;

public interface PeerMessageType extends Serializable {
	int length();
	int messageType();
	int messageNumber();
}
