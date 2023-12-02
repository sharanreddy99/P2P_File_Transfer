default:
	javac StartRemoteServers.java

runAll:
	java StartRemoteServers

runCodebase:
	rm -rf peer_1002
	rm -rf peer_1003
	rm -rf peer_1004
	rm -rf peer_1005
	rm -rf peer_1006
	rm -rf log*
	javac PeerProcess.java

runPeer:
	java PeerProcess ${peerid}