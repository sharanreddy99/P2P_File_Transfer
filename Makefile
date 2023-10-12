default:
	find . -type f -path "./*" -name "*.class" -delete
	javac PeerProcess.java && javac StartRemoteServers.java
	rm -f 1002/thefile
	rm -f 1003/thefile
	rm -f 1004/thefile
	rm -f 1005/thefile
	rm -f 1007/thefile
	rm -f 1008/thefile
	rm -f 1009/thefile
	rm -rf log

runAll:
	java StartRemoteServers

runPeer:
	java PeerProcess ${peerid}