default:
	find . -type f -path "./*" -name "*.class" -delete
	javac PeerProcess.java && javac StartRemoteServers.java
	rm -rf peer_1002
	rm -rf peer_1003
	rm -rf peer_1004
	rm -rf peer_1005
	rm -rf peer_1006
	rm -rf peer_1007
	rm -rf peer_1008
	rm -rf peer_1009
	rm -rf log*

runAll:
	java StartRemoteServers

runPeer:
	java PeerProcess ${peerid}