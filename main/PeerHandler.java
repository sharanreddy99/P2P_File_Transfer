package main;

import java.io.*;
import java.net.*;

import main.constants.Constants;
import main.helper.*;
import main.messageTypes.*;

public class PeerHandler implements Runnable {
	// The unique identifier for the peer
	private String peerId;

	// Indicates whether the process of chunk processing has started
	private boolean hasStartedChunkProcessing = false;

	// Helper class for managing peer communication messages
	private MessageHelper messageHelper;

	// Helper class for logging messages
	private LogHelper logger;

	// The socket connected to the neighbor peer
	private Socket neighborSocket;

	// Indicates whether the neighbor peer is currently choking the current peer
	private boolean isNeighborPeerChoking = false;

	// Time taken to download a chunk or file
	private long timeToDownload = 0;

	// Reference to the controller managing the peer's behavior
	private PeerController controller;

	// Input stream for receiving objects from the neighbor peer
	private ObjectInputStream objectInputStream;

	// Indicates whether the handshake from the neighbor peer has been received
	private boolean hasReceivedHandshake = false;

	// Indicates whether the handshake has been sent to the neighbor peer
	private boolean hasSentHandshake = false;

	// Total size of downloaded data
	private int downloadSize = 0;

	// Indicates whether the previous message has been received
	private boolean hasReceivedPreviousMessage = true;

	// Indicates whether the peer is currently choked by the neighbor peer
	private boolean hasChoked = false;

	// Helper class for managing the next chunk request
	private NextRequestHelper nextChunkRequestHelper;

	/**
	 * Sets the choke status of the peer.
	 *
	 * @param choked True if the peer is choked, false otherwise.
	 */
	private void setChoke(boolean choked) {
		hasChoked = choked;
	}

	/**
	 * Checks if the peer is currently choked.
	 *
	 * @return True if the peer is choked, false otherwise.
	 */
	public boolean isPeerChoked() {
		return hasChoked;
	}

	/**
	 * Creates a new PeerHandler instance, attaches the provided Socket and PeerController,
	 * initializes the PeerHandler, and returns the created PeerHandler if initialization is successful.
	 *
	 * @param controller The PeerController instance associated with the PeerHandler.
	 * @param socket     The Socket associated with the PeerHandler.
	 * @return A new PeerHandler instance if initialization is successful, otherwise null.
	 * @author Adithya KNG
	 */
	public static synchronized PeerHandler createNewPeerHandler(PeerController controller, Socket socket) {
		// Create a new PeerHandler instance
		PeerHandler obj = new PeerHandler();

		// Attach the provided Socket and PeerController to the PeerHandler
		obj.attachSocketAndControllerToPeerHandler(socket, controller);

		// Initialize the PeerHandler
		boolean initializeHandler = obj.preparePeerHandler(controller);

		// Return the PeerHandler if initialization is successful, otherwise remove and return null
		return initializeHandler ? obj : removePeerHandler(obj);
	}

	/**
	 * Removes the provided PeerHandler instance.
	 *
	 * @param obj The PeerHandler instance to be removed.
	 * @return null, indicating the removal of the PeerHandler.
	 * @author Adithya KNG
	 */
	public static PeerHandler removePeerHandler(PeerHandler obj){
		obj.closeStreams();
		return obj = null;
	}

	synchronized public boolean closeStreams() {
		try {
			// Close the ObjectInputStream to release associated resources
			if (objectInputStream != null) {
				objectInputStream.close();
			}
		} catch (IOException obj) {
			// Exception might be due to objectInputStream being null, so no issue
			// Continue with the closure even if an exception occurs during closing
		}

		// Return false to indicate the closure of the PeerHandler
		return false;
	}

	/**
	 * Attaches the provided Socket and PeerController to the PeerHandler.
	 *
	 * @param socket     The Socket to attach.
	 * @param controller The PeerController to attach.
	 * @author Adithya KNG
	 */
	public void attachSocketAndControllerToPeerHandler(Socket socket, PeerController controllerInstance){
		// Implementation to attach the provided Socket and PeerController to the PeerHandler
		this.neighborSocket = socket;
		this.controller = controllerInstance;
	}

	/**
	 * Prepares the PeerHandler for communication by setting up the necessary streams
	 * and handling the initialization of the PeerHandler's message sender.
	 *
	 * @param pc The PeerController instance associated with the PeerHandler.
	 * @return true if preparation is successful, false otherwise.
	 * 
	 * @author Sharan Sai Reddy Konda
	 */
	private boolean preparePeerHandler(PeerController pc) {
		synchronized(this){
			try {
				// Check if the neighborSocket is not null
				if (neighborSocket != null) {
					// Set up output stream for sending objects
					OutputStream socketOutputStream = neighborSocket.getOutputStream();
					ObjectOutputStream peerOutputStream = new ObjectOutputStream(socketOutputStream);

					// Set up input stream for receiving objects
					InputStream socketInputStream = neighborSocket.getInputStream();
					objectInputStream = new ObjectInputStream(socketInputStream);

					// Check if the controller is not null
					if (controller != null) {
						// Handle the initialization of the PeerHandler's message sender
						return this.handlePeerMessageSender(pc, peerOutputStream);
					}

					// Close the connection if the controller is null
					return closeStreams();
				}
			} catch (Exception obj) {
				// Handle any exceptions during the preparation process
				obj.printStackTrace();
				return false;
			}

			// Return false if the neighborSocket is null
			return false;
		}
	}

	/**
	 * Handles the initialization of the PeerHandler's message sender using the provided
	 * ObjectOutputStream and starts the messageHelper. Additionally, it proceeds to handle
	 * the initialization of the NextRequestHelper and Logger for the PeerHandler.
	 *
	 * @param pc               The PeerController instance associated with the PeerHandler.
	 * @param peerOutputStream The ObjectOutputStream for sending messages to the neighbor.
	 * @return true if initialization and handling are successful, false otherwise.
	 * @author Sharan Sai Reddy Konda
	 */
	public boolean handlePeerMessageSender(PeerController pc, ObjectOutputStream peerOutputStream) {
		// Initialize the messageHelper using the provided ObjectOutputStream
		messageHelper = MessageHelper.returnPeerMessageSender(peerOutputStream);

		// Check if the messageHelper is successfully initialized
		if (messageHelper != null) {
			// Start the messageHelper
			startPeerMessageSender(messageHelper);
			// Proceed to handle the initialization of the NextRequestHelper and Logger
			return this.handleChunkRequesterAndLogger(pc);
		}

		// Close the connection if the messageHelper initialization fails
		return closeStreams();
	}


	/**
	 * Starts a new thread for the messageHelper using the given MessageHelper.
	 *
	 * This method is responsible for initiating a new thread to handle message sending using the provided MessageHelper.
	 *
	 * @param msgHelper The MessageHelper responsible for sending peer messages.
	 */
	public static void startPeerMessageSender(MessageHelper msgHelper) {
		// Start a new thread for the messageHelper using the provided MessageHelper
		new Thread(msgHelper).start();
	}

	
	/**
	 * Gets the unique identifier of the peer.
	 *
	 * @return The peer's unique identifier.
	 */
	public String getPeerId() {
		return peerId;
	}

	/**
	 * Sets the unique identifier of the peer in a synchronized manner.
	 *
	 * @param peerId The new unique identifier for the peer.
	 */
	synchronized public void setPeerId(String peerId) {
		this.peerId = peerId;
	}

	/**
	 * Checks if the provided peer ID is not null and sends a handshake message.
	 *
	 * This method is responsible for checking if the provided peer ID is not null,
	 * and if so, it initiates the process of sending a handshake message.
	 *
	 * @param id The peer ID to be checked and used for sending a handshake message.
	 * @return true if the handshake message is sent successfully, false otherwise.
	 * @author Adithya KNG
	 */
	public boolean checkAndSendHandShakeMessageFirst(String id) {
		// Check if the provided peer ID is not null, and if so, send a handshake message
		return id != null ? initiateHandShakeMessage() : false;
	}


	/**
	 * This method represents the main logic of the PeerMessageReceiver thread.
	 * It continuously listens for incoming messages from the peer through the ObjectInputStream,
	 * processes each message using the processAllMessages method, and sends a handshake message if needed.
	 * 
	 * @author Sharan Sai Reddy Konda
	 */
	public void run() {
		// Send the handshake message if not sent already
		checkAndSendHandShakeMessageFirst(peerId);
	
		try {
			// Continue listening for messages until the operation is complete
			while (!controller.checkIfOperationComplete()) {
				// Check if the operation has become complete during the loop
				if (controller.checkIfOperationComplete()) {
					break;
				}
	
				// Read an object from the ObjectInputStream and cast it to PeerMessageType
				PeerMessageType message = (PeerMessageType) objectInputStream.readObject();
	
				// Process the received message
				processMessages(message);
			}
		} catch (Exception e) {
			// Handle exceptions that might occur during message receiving
			// This catch block is intentionally left empty to ignore exceptions
		}
	}

	/**
	 * Processes a PeerMessageType, determining its type and invoking the appropriate action.
	 *
	 * This method takes a PeerMessageType as input, identifies its type, and performs the
	 * corresponding action based on the message type.
	 *
	 * @param message The PeerMessageType to be processed.
	 * @author Adithya KNG, Sharan Sai Reddy Konda, Bhavan Voram
	 */
	public void processMessages(PeerMessageType message){
		// Get the type of the message
		int type = message.messageType();

		 // Check the type and perform the corresponding action
		if( type ==  Constants.TYPE_HANDSHAKE_MESSAGE && (message instanceof HandshakeMessage)){
			// If it's a HandshakeMessage, process the handshake
			HandshakeMessage handshakeMessage = (HandshakeMessage) message;
			processHandshakeMessage(handshakeMessage);
		}
		else if( type ==  Constants.TYPE_HAVE_MESSAGE) {
			// If it's a HaveMessage, process the message
			PeerMessage pm = (PeerMessage) message;
			haveMessage(pm.getIndex(),pm);
		}
		else if( type ==  Constants.TYPE_CHOKE_MESSAGE) {
			// If it's a ChokeMessage, process the message
			chokeMessage();
		}
		else if( type ==  Constants.TYPE_SHUTDOWN_MESSAGE){
			// If it's a ShutdownMessage, process the message
			shutDownMessage();
		}
		else if( type ==  Constants.TYPE_REQUEST_MESSAGE) {
			// If it's a RequestMessage, process the message
			PeerMessage pm = (PeerMessage) message;
			requestMessage(pm.getIndex());
		}
		else if( type ==  Constants.TYPE_BITFIELD_MESSAGE){
			// If it's a BitFieldMessage, process the message
			bitFieldMessage((PeerMessage) message);
		}
		else if( type ==  Constants.TYPE_INTERESTED_MESSAGE) {
			// If it's an InterestedMessage, process the message
			interestedMessage();
		}
		else if( type ==  Constants.TYPE_PIECE_MESSAGE) {
			// If it's a PieceMessage, process the message
			pieceMessage((PeerMessage) message);
		}
		else if( type ==  Constants.TYPE_UNCHOKE_MESSAGE) {
			// If it's an UnchokeMessage, process the message
			unchokeMessage((PeerMessage) message);
		}
		else if( type ==  Constants.TYPE_NOT_INTERESTED_MESSAGE) {
			// If it's a NotInterestedMessage, process the message
			notInterestedMessage();
		}
	}

	/**
	 * Processes a Not Interested message received from the neighbor peer.
	 *
	 * This method is called when the neighbor peer sends a Not Interested message,
	 * indicating their lack of interest in exchanging pieces with the current peer.
	 *
	 * @param message The Not Interested message received from the neighbor peer.
	 * @author Sharan Sai Reddy Konda
	 */
	private void notInterestedMessage() {
		// Log the reception of the Not Interested message for monitoring purposes
		logger.logMessage(String.format(Constants.NOT_INTERESTED_LOG_MESSAGE, controller.getPeerId(), peerId));
	}

	/**
	 * Adds a PeerMessage to the message queue for further processing.
	 *
	 * This method is responsible for adding the given PeerMessage to the message queue
	 * managed by the NextRequestHelper for subsequent processing.
	 *
	 * @param message The PeerMessage to be added to the message queue.
	 * @author Bhavan Voram
	 */
	public void addMessageToQueue(PeerMessage message) {
		try {
			// Add the PeerMessage to the message queue for further processing
			nextChunkRequestHelper.addPeerMessageToQueue(message);
		} catch (Exception e) {
			// Handle any exceptions that may occur during the process
			handleException(e);
		}
	}

	/**
	 * Processes an Unchoke message received from the neighbor peer.
	 *
	 * This method is called when the neighbor peer sends an Unchoke message,
	 * indicating that it has unchoked the current peer, allowing for potential piece exchange.
	 *
	 * @param msg The Unchoke message received from the neighbor peer.
	 */
	private void unchokeMessage(PeerMessage msg) {
		// Log the reception of the Unchoke message for monitoring purposes
		logger.logMessage(String.format(Constants.UNCHOKED_LOG_MESSAGE, controller.getPeerId(), peerId));

		// Toggle the flag to indicate that the current peer is no longer choked by the neighbor peer
		toggleIsNeighborPeerChoking(false);

		// Add the Unchoke message to the message queue for further processing, if needed
		addMessageToQueue(msg);
	}

	/**
	 * Sends a Have message to all other peers except the current peer and increments download-related metrics.
	 *
	 * This method is called after processing a Piece message, and it sends a Have message
	 * indicating the availability of a new piece to all other peers in the system.
	 * Additionally, it increments the total download size and sets a flag indicating the reception of the previous message.
	 *
	 * @param message The Piece message containing the newly received piece of file data.
	 * @author Bhavan Voram
	 */
	private void sendHaveMessageAndIncrementDownload(PeerMessage message) {
		// Send a Have message to all other peers except the current peer, indicating the availability of a new piece
		controller.sendHaveToAllExcept(message.getIndex(), peerId);

		// Increment the total download size with the size of the received piece
		downloadSize += message.getData().getDataLength();

		// Set a flag indicating the reception of the previous message
		this.hasReceivedPreviousMessage = true;

		addMessageToQueue(message);
	}


	/**
	 * Processes a Piece message received from the neighbor peer.
	 *
	 * This method is called when the neighbor peer sends a Piece message,
	 * containing a piece of the file data.
	 *
	 * @param msg The Piece message received from the neighbor peer.
	 * 
	 * @author Sharan Sai Reddy Konda
	 */
	private void pieceMessage(PeerMessage msg) {
		// Receive and store the piece of file data in the controller
		controller.receiveAndStorePiece(msg, peerId);

		// Send a Have message to the neighbor peer and increment download-related metrics
		sendHaveMessageAndIncrementDownload(msg);
	}
	


	/**
	 * Handles the reception of a choke message from the neighbor peer.
	 * This method logs the reception of the choke message, updates the 'isNeighborPeerChoking' status,
	 * and performs any additional actions related to being choked by the neighbor peer.
	 * 
	 * @author Sharan Sai Reddy Konda
	 */
	private void chokeMessage() {
		// Log the reception of the choke message
		logger.logMessage(String.format(Constants.CHOKED_LOG_MESSAGE, controller.getPeerId(), peerId));

		// Update the 'isNeighborPeerChoking' status
		toggleIsNeighborPeerChoking(true);
	}


	/**
	 * Handles the reception of a BitField message from the neighbor peer.
	 *
	 * @param message The BitField message received from the neighbor peer.
	 * @author Bhavan Voram
	 */
	private void bitFieldMessage(PeerMessage message) {
		try {
			// Add the received BitField message to the chunk requester's message queue
			nextChunkRequestHelper.addPeerMessageToQueue(message);

			// Optionally log the reception of the BitField message
			if (Constants.SHOW_OPTIONAL_LOG_MESSAGES) {
				controller.getLoggerInstance().logMessage(
						String.format(Constants.BITFIELD_LOG_MESSAGE, controller.getPeerId(),
								getPeerId(), message.getManageBitFields().fetchBitFieldMap()));
			}

			// Check if the handshake process is done and chunk status is not complete
			boolean handshakeProcessDone = hasReceivedHandshake && hasSentHandshake;
			if (handshakeProcessDone && !chunkStatus()) {
				// Process the BitField message
				handleBitFieldMessageProcess();
			}

		} catch (Exception e) {
			// Print the stack trace for exceptions that might occur during the process
			handleException(e);
		}
	}

	/**
	 * Handles the initialization of the NextRequestHelper and Logger for the PeerHandler
	 * using the provided PeerController.
	 *
	 * @param pc The PeerController instance associated with the PeerHandler.
	 * @return true if initialization is successful, false otherwise.
	 * @author Sharan Sai Reddy Konda
	 */
	public boolean handleChunkRequesterAndLogger(PeerController pc) {
		// Initialize the NextRequestHelper using the provided PeerController and PeerHandler
		nextChunkRequestHelper = NextRequestHelper.getNewInstance(pc, this);

		// Obtain the Logger instance from the PeerController
		logger = controller.getLoggerInstance();

		// Return true to indicate successful initialization
		return true;
	}


	/**
	 * Initiates the process of handling a BitField message.
	 * This method starts a new thread for the chunk requester, records the download time,
	 * resets the download size, and sets the flag indicating the start of chunk processing.
	 * 
	 * @author Adithya KNG
	 */
	private void handleBitFieldMessageProcess() {
		// Start a new thread for the chunk requester
		new Thread(nextChunkRequestHelper).start();

		// Record the start time of the download
		timeToDownload = System.currentTimeMillis();

		// Reset the download size
		downloadSize = 0;

		// Synchronize on 'this' to safely update the hasStartedChunkProcessing flag
		synchronized (this) {
			// Set the flag indicating the start of chunk processing
			this.hasStartedChunkProcessing = true;
		}
	}


	/**
	 * processHandshakeMessage
	 *
	 * @param message
	 */
	private void processHandshakeMessage(HandshakeMessage message) {
		peerId = message.getPeerId();
		if (!hasSentHandshake) {
			initiateHandShakeMessage();
			logger.logMessage(
					String.format(Constants.MAKE_CONNECTION_RECEIVER_LOG_MESSAGE,
							controller.getPeerId(), peerId));
			initiateBitFieldMessage();
		} else {
			if (message.getHeader().equals(Constants.HANDSHAKE_HEADER_STRING) &&
					!hasReceivedHandshake) {
						hasReceivedHandshake = true;
				if (hasSentHandshake && !chunkStatus()) {
					new Thread(nextChunkRequestHelper).start();
					timeToDownload = System.currentTimeMillis();
					downloadSize = 0;
					synchronized(this){
						this.hasStartedChunkProcessing = true;
					}
				}
			} else {
				if (Constants.SHOW_OPTIONAL_LOG_MESSAGES) {
					logger.logMessage(
							String.format(Constants.HANDSHAKE_FAILED_LOG_MESSAGE, peerId,
									message.getHeader()));
				}
			}
		}
	}

	/**
	 * Initiates the process of requesting a piece of the file from the neighbor peer.
	 *
	 * @param index The index of the piece to be requested.
	 * @author Adithya KNG
	 */
	private void requestMessage(int index) {
		// Check if the peer is choked by the neighbor, and if so, do not send a request
		if (hasChoked) {
			return;
		}
		// Construct the piece message to be sent
		PeerMessage peerMessage = controller.constructPieceMessage(index);

		// Check if the piece message is successfully constructed
		if (peerMessage == null) {
			return;
		}
		messageHelper.sendMessageWithDelay(peerMessage,2000);
	}


	/**
	 * Processes a Have message received from the neighbor peer.
	 *
	 * @param message The Have message containing the index of a newly available piece.
	 * @author Adithya KNG
	 */
	private void haveMessage(int index, PeerMessage message) {
		try {
			// Log the reception of the Have message
			logHaveMessage(index);

			// Add the Have message to the chunk requester's message queue
			nextChunkRequestHelper.addPeerMessageToQueue(message);

		} catch (Exception e) {
			// Handle exceptions that might occur during the process more gracefully
			handleException(e);
		}
	}

	/**
	 * Logs the reception of a Have message.
	 *
	 * @param index The index of the newly available piece.
	 * @author Adithya KNG
	 */
	private void logHaveMessage(int index) {
		controller.getLoggerInstance().logMessage(
				String.format(Constants.HAVE_LOG_MESSAGE, controller.getPeerId(), peerId, index));
	}

	/**
	 * Handles exceptions that might occur during the processing of a Have message.
	 *
	 * @param e The exception that occurred.
	 * @author Sharan Sai Reddy Konda
	 */
	private void handleException(Exception e) {
		// Log the exception or perform other error handling actions
		handleException(e);
	}


	/**
	 * Processes an Interested message received from the neighbor peer.
	 *
	 * This method is called when the neighbor peer sends an Interested message,
	 * indicating their interest in exchanging pieces with the current peer.
	 *
	 * @param message The Interested message received from the neighbor peer.
	 * @author Bhavan Voram
	 */
	private void interestedMessage() {
		// Log the reception of the Interested message for monitoring purposes
		logger.logMessage(String.format(Constants.INTERESTED_LOG_MESSAGE, controller.getPeerId(), peerId));
	}

	/**
	 * Initiates a handshake by sending a HandshakeMessage to the peer.
	 *
	 * This method is responsible for creating and sending a HandshakeMessage to the peer,
	 * initiating the handshake process. It also updates relevant flags and logs the event.
	 *
	 * @return true if the handshake initiation is successful, false otherwise.
	 * @author Adithya KNG
	 */
	boolean initiateHandShakeMessage() {
		synchronized (this) {
			// Create a HandshakeMessage with the appropriate header and peer ID
			HandshakeMessage message = new HandshakeMessage(Constants.HANDSHAKE_HEADER_STRING, controller.getPeerId());

			try {
				// Send the HandshakeMessage to the peer using the messageHelper
				messageHelper.sendMessage(message);
			} catch (Exception e) {
				// Handle any exceptions that may occur during the message sending process
				handleException(e);
				return false;
			}

			// Update the flag to indicate that the handshake has been sent
			hasSentHandshake = true;

			// Log the handshake initiation event
			logger.logMessage(String.format(Constants.MAKE_CONNECTION_SENDER_LOG_MESSAGE, controller.getPeerId(), peerId));

			// Optionally log the details of the handshake message
			if (Constants.SHOW_OPTIONAL_LOG_MESSAGES) {
				logger.logMessage(String.format(Constants.SENDER_HANDSHAKE_LOG_MESSAGE, controller.getPeerId(), peerId, message.getHeader()));
			}

			// Return true to indicate successful handshake initiation
			return true;
		}
	}


	/**
	 * Initiates the sending of a BitFieldMessage to the peer with a delay.
	 *
	 * This method is responsible for sending a BitFieldMessage to the peer using the messageHelper
	 * with a specified delay. The synchronization ensures thread safety during the message sending process.
	 */
	void initiateBitFieldMessage() {
		synchronized(this){
			// Send the BitFieldMessage to the peer using the messageHelper with a delay of 4000 milliseconds
			messageHelper.sendMessageWithDelay(controller.getPeerMessage(), 4000);
		}
	}

	/**
	 * Initiates the sending of an InterestedMessage to the peer if not choked.
	 *
	 * This method attempts to send the provided InterestedMessage to the peer using
	 * the messageHelper only if the peer is not choked. Any exceptions during
	 * the message sending process are handled.
	 *
	 * @param msg The InterestedMessage to be sent to the peer.
	 * @author Bhavan Voram
	 */
	public void initiateInterestedMessage(PeerMessage msg) {
		try {
			// Check if the peer is not choked before attempting to send the InterestedMessage
			boolean sendMessage = !isNeighborPeerChoking && messageHelper.sendMessage(msg);

			// Optionally, handle the result or take further actions based on sendMessage value
		} catch (Exception e) {
			// Handle any exceptions that may occur during the message sending process
			handleException(e);
		}
	}

	/**
	 * Initiates the sending of a NotInterestedMessage to the peer.
	 *
	 * This method sends a NotInterestedMessage to the peer using the messageHelper,
	 * potentially introducing an error during the message sending process.
	 *
	 * @param message The NotInterestedMessage to be sent to the peer.
	 * @author Adithya KNG
	 */
	public void initiateNotInterestedMessage(PeerMessage msg) {
		// Send the NotInterestedMessage to the peer using the messageHelper, potentially introducing an error
		messageHelper.sendMessageWithError(msg);
	}

	

	/**
	 * Initiates the sending of a Have message to the neighbor peer.
	 *
	 * This method is responsible for initiating the sending of a Have message to the neighbor peer.
	 * It sends the Have message using the messageHelper and handles any potential errors.
	 *
	 * @param message The Have message to be sent to the neighbor peer.
	 * @author Bhavan Voram
	 */
	public void initiateHaveMessage(PeerMessage message) {
		// Send the Have message to the neighbor peer and handle any errors
		messageHelper.sendMessageWithError(message);
	}


	/**
	 * Initiates a Choke message to the neighbor peer if the current peer is not already choked.
	 *
	 * This method is responsible for initiating a Choke message to the neighbor peer
	 * if the current peer is not already choked. It resets download-related metrics,
	 * sets the choke flag, and sends the Choke message to the neighbor peer.
	 *
	 * @param msg The Choke message to be sent to the neighbor peer.
	 * @author Adithya KNG
	 */
	public void initiateChokeMessage(PeerMessage msg) {
		// Check if the current peer is already choked; if yes, do nothing
		if (hasChoked) {
			return;
		}
		// Perform the choking action and send the choke message
		chokeUnchokeAction(true,msg);
	}


	/**
	 * Initiates an Unchoke message to the neighbor peer if the current peer is currently choked.
	 *
	 * This method is responsible for initiating an Unchoke message to the neighbor peer
	 * if the current peer is currently choked. It performs the unchoking action and sends the Unchoke message.
	 *
	 * @param msg The Unchoke message to be sent to the neighbor peer.
	 */
	public void initiateUnchokeMessage(PeerMessage msg) {
		// Check if the current peer is already unchoked; if not, do nothing
		if (!hasChoked) {
			return;
		}
		// Perform the unchoking action and send the Unchoke message
		chokeUnchokeAction(false, msg);
	}


	/**
	 * Performs the choke or unchoke action and sends the corresponding message to the neighbor peer.
	 *
	 * This method is responsible for performing either the choke or unchoke action based on the specified parameter.
	 * It resets download-related metrics, sets the choke flag accordingly, and sends the corresponding message to the neighbor peer.
	 *
	 * @param choke A boolean indicating whether to perform the choke action (true) or unchoke action (false).
	 * @param msg The Choke or Unchoke message to be sent to the neighbor peer.
	 * @author Adithya KNG
	 */
	public void chokeUnchokeAction(Boolean choke, PeerMessage msg) {
		// Reset download-related metrics
		downloadSize = 0;
		timeToDownload = System.currentTimeMillis();

		// Set the choke flag based on the specified parameter
		setChoke(choke);

		// Send the Choke or Unchoke message to the neighbor peer
		messageHelper.sendMessageWithError(msg);
	}

	

	/**
	 * Checks if the peer has received the previous message.
	 *
	 * @return True if the previous message has been received, false otherwise.
	 */
	public boolean hasReceivedPreviousMessage() {
		return hasReceivedPreviousMessage;
	}

	/**
	 * Sets the flag indicating whether the previous message has been received.
	 *
	 * @param isPieceMessageForPreviousMessageReceived True if the previous message is received, false otherwise.
	 */
	public void setHasReceivedPreviousMessage(boolean isPieceMessageForPreviousMessageReceived) {
		this.hasReceivedPreviousMessage = isPieceMessageForPreviousMessageReceived;
	}

	/**
	 * Checks if the peer has received a handshake from the neighbor peer.
	 *
	 * @return True if a handshake has been received, false otherwise.
	 */
	public boolean hasReceivedHandshake() {
		return hasReceivedHandshake;
	}


	/**
	 * Sends a Shutdown message to the neighbor peer.
	 *
	 * This method is responsible for sending a Shutdown message to the neighbor peer,
	 * indicating the intention to shut down the communication.
	 *
	 * @param message The Shutdown message to be sent to the neighbor peer.
	 * @author Adithya KNG
	 */
	public void initiateShutdownMessage(PeerMessage message) {
		// Send the Shutdown message to the neighbor peer using the peer message sender
		messageHelper.sendMessageWithError(message);
	}

	/**
	 * Calculates and returns the download speed in bytes per millisecond.
	 *
	 * This method computes the download speed by dividing the total downloaded size
	 * by the time elapsed since the start of the download.
	 *
	 * @return The download speed in bytes per millisecond.
	 * @author Sharan Sai Reddy Konda
	 */
	public double getDownloadSpeed() {
		return (System.currentTimeMillis() - timeToDownload) != 0 ? ((downloadSize * 1.0) / ((System.currentTimeMillis() - timeToDownload) * 1.0)) : 0;
	}

	/**
	 * Processes a Shutdown message indicating that the peer with the given peerId has completed file download.
	 *
	 * This method is called when a Shutdown message is received, and it notifies the controller
	 * that the peer with the specified peerId has completed the file download process.
	 * 
	 * @author Bhavan Voram
	 */
	public void shutDownMessage() {
		// Notify the controller that the peer with the given peerId has completed file download
		controller.confirmFileDownload(peerId);
	}

	

	public synchronized boolean chunkStatus() {
		return hasStartedChunkProcessing;
	}

	/**
	 * Sends a Request message to the neighbor peer if the current peer is unchoked.
	 *
	 * This method is responsible for sending a Request message to the neighbor peer
	 * if the current peer is unchoked, indicating the interest in obtaining a specific piece of the file.
	 *
	 * @param message The Request message to be sent to the neighbor peer.
	 * @author Sharan Sai Reddy Konda
	 */
	public void initateRequestMessage(PeerMessage msg) {
		// Check if the current peer is unchoked by the neighbor peer
		boolean sendMessage = !isNeighborPeerChoking ? messageHelper.sendMessageWithError(msg) : false;
	}

	public void toggleIsNeighborPeerChoking(boolean value){
		isNeighborPeerChoking = value;
	}

}
