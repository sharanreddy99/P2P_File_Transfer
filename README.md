# CNT5106C Project

Group member: Yefan Tao, Zhang Dong, Zhuobiao Qiao

Java implementation of a P2P file sharing software similar to BitTorrent. BitTorrent is a popular P2P protocol for file distribution. Among its interesting features, you are asked to implement the choking-unchoking mechanism which is one of the most important features of BitTorrent.

## How to use?

1. Change directory to `src`, run command to compile the program
```shell
javac -d . ProcessStarter.java
javac PeerProcess.java
```
2. Add directory and config file for each peer under the `src` file. For example:
```
src
├── 1001
│   └── thefile
├── 1002
├── 1003
├── 1004
├── 1005
├── 1006
│   └── thefile
├── 1007
├── 1008
├── 1009
├── PeerInfo.cfg
└── common.cfg
```

3. Run command `java ProcessStarter` and wait until the program to successfully complete and stop.

4. You can find the sample log file under `log` folder under `src`.

```
[Sat, 12 Dec 2020 05:24:12 PM]: Peer [peer_ID 1001] Peer [1001] : exchanging bitfiled 0
[Sat, 12 Dec 2020 05:24:16 PM]: Peer [peer_ID 1001] Handshake Message received and processed correctly.
[Sat, 12 Dec 2020 05:24:16 PM]: Peer [peer_ID 1001] Peer 1001 is connected from Peer 1002.
```
```
[Sat, 12 Dec 2020 05:24:21 PM]: Peer [peer_ID 1001] Peer [1001] has the preferred neighbors [1004 , 1003 , 1002 , ]
[Sat, 12 Dec 2020 05:24:22 PM]: Peer [peer_ID 1001] Peer [1001] recieved the 'interested' message from [1002]
```
```
[Sat, 12 Dec 2020 05:24:29 PM]: Peer [peer_ID 1001] Peer [1001] recieved the 'have' message from [1003] for the piece13
[Sat, 12 Dec 2020 05:24:30 PM]: Peer [peer_ID 1001] Peer [1001] is choked by [1004]
[Sat, 12 Dec 2020 05:24:30 PM]: Peer [peer_ID 1001] Peer [1001] is unchoked by [1004]
```
```
[Sat, 12 Dec 2020 05:25:36 PM]: Peer [peer_ID 1001] Peer [1001] has downloaded the complete file.
[Sat, 12 Dec 2020 05:25:46 PM]: Peer [peer_ID 1001] Peer [1001] has the optimistically unchoked neighbor [1009]
```