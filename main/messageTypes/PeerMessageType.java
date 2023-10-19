package main.messageTypes;

import java.io.Serializable;

public interface PeerMessageType extends Serializable {

	int getType();

	int getLength();

	int getMessageNumber();
}
