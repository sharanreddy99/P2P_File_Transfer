# Group Name: Sharan Sai Reddy Konda's Group

## Members:

1. Sharan Sai Reddy Konda
2. Aditya KNG
3. Bhavan Voram

## Instructions to Run:

### Remotely

- set the `IS_LOCAL_HOST` to false in the `main/constants/Constants.java`` file.
- Command: `make && make runAll`
- Explanation: This will compile all the necessary files and launch the peers with the configuration specified in the `PeerInfo.cfg` file.

### Locally

- set the `IS_LOCAL_HOST` to true in the `main/constants/Constants.java`` file.
- Command: `make && make runCompile && make runAll`
- Explanation: This will compile all the necessary files and launch the peers with the configuration specified in the `PeerInfo.cfg` file.

## Running our application on remote servers:

This works as follows:

- We run the `StartRemoteServers.java` file which will first copy the project to the servers as each server shares the volume with other linux instances.

- We then compile the code in just one of the instances the project is soft linked in other instances.

- We then run the `PeerProcess.java` in each server to start the peer server and share the information among peers.

## CFG Files

- As indicated in the requirements we have two .CFG files to store information related to the peering application and information related to the peers.

## Establishing connections

- Since the peers are started in the increasing order of their peerIDs, it will be sufficient if each peer sends a connection request or waits for a socket connection from all the peers that have a larger peerID.
