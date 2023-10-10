default:
	find . -type f -path "./*" -name "*.class" -delete
	javac PeerProcess.java && javac StartRemoteServers.java
	
runAll:
	java StartRemoteServers

runPeer:
	java PeerProcess ${peerid}