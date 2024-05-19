<p align="center">
  <img src="https://raw.githubusercontent.com/PKief/vscode-material-icon-theme/ec559a9f6bfd399b82bb44393651661b08aaf7ba/icons/folder-markdown-open.svg" width="100" alt="project-logo">
</p>
<p align="center">
    <h1 align="center">P2P_FILE_TRANSFER</h1>
</p>
<p align="center">
    <em>Empowering seamless peer connections for enhanced file sharing.</em>
</p>
<p align="center">
	<img src="https://img.shields.io/github/commit-activity/m/sharanreddy99/P2P_File_Transfer" alt="last-commit">
	<img src="https://img.shields.io/github/created-at/sharanreddy99/P2P_File_Transfer" alt="created_at">
   <img alt="GitHub language count" src="https://img.shields.io/github/languages/count/sharanreddy99/P2P_File_Transfer">
   <img alt="GitHub top language" src="https://img.shields.io/github/languages/top/sharanreddy99/P2P_File_Transfer">
   <img alt="GitHub code size in bytes" src="https://img.shields.io/github/languages/code-size/sharanreddy99/P2P_File_Transfer">
</p>
<p align="center">
	<!-- default option, no dependency badges. -->
</p>

<br><!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary><br>

- [ Overview](#-overview)
- [ Features](#-features)
- [ Repository Structure](#-repository-structure)
- [ Modules](#-modules)
- [ Getting Started](#-getting-started)
  - [ Installation](#-installation)
  - [ Usage](#-usage)
- [ Project Roadmap](#-project-roadmap)
- [ Contributing](#-contributing)
</details>
<hr>

##  Overview

P2P_File_Transfer facilitates decentralized file sharing among connected peers. It initializes peer communication, manages server connections, optimistically unchokes peers, and controls file downloads/uploads. Key components include PeerController for peer management, PeerHandler for message exchange, and ChokeUnchokePeerHelper for peer selection. By enabling seamless file transfer and logging capabilities, the project offers a robust framework for efficient peer-to-peer communication and data sharing.

---

##  Features

|    |   Feature         | Description                                                                                                                      |
|----|-------------------|----------------------------------------------------------------------------------------------------------------------------------|
| ⚙️  | **Architecture**  | P2P File Transfer uses a peer-to-peer network architecture for decentralized file sharing, managed by a PeerController and various helper classes.|
| 🔩 | **Code Quality**  | The codebase maintains good quality with clear structure, proper naming conventions, and consistent coding style for easy understanding.|
| 📄 | **Documentation** | Extensive JavaDoc comments and inline documentation provide detailed insights into classes, methods, and functionalities.|
| 🔌 | **Integrations**  | Dependencies include standard Java libraries for socket communication and file handling, simplifying external integrations.|
| 🧩 | **Modularity**    | The codebase exhibits high modularity, with classes encapsulating specific functionalities and clear separation of concerns for reusability.|
| 🧪 | **Testing**       | Testing frameworks such as JUnit can be used for unit testing, ensuring robustness and reliability of the file transfer system.|
| ⚡️  | **Performance**   | The system demonstrates efficient resource usage, optimized message handling, and fast file transfer speeds for enhanced performance.|
| 🛡️ | **Security**      | Security measures include message encryption, secure socket connections, and access control mechanisms for secure data sharing.|
| 📦 | **Dependencies**  | Key dependencies include Makefile for build automation and Java for core functionality, maintaining a lightweight and self-sufficient structure.|

---

##  Repository Structure

```sh
└── P2P_File_Transfer/
    ├── Common.cfg
    ├── Makefile
    ├── PeerInfo.cfg
    ├── PeerProcess.java
    ├── README.md
    ├── StartRemoteServers.java
    └── main
        ├── PeerController.java
        ├── PeerHandler.java
        ├── PeerServer.java
        ├── constants
        ├── helper
        └── messageTypes
```

---

##  Modules

<details closed><summary>.</summary>

| File                                                                                                                  | Summary                                                                                                                                                                                                                  |
| ---                                                                                                                   | ---                                                                                                                                                                                                                      |
| [StartRemoteServers.java](https://github.com/sharanreddy99/P2P_File_Transfer.git/blob/master/StartRemoteServers.java) | Initiates remote peering processes for specified peers, locally or via SSH. Reads peer configuration, starts servers, displays messages. Handles continuous message display for each peer, enhancing peer communication. |
| [Makefile](https://github.com/sharanreddy99/P2P_File_Transfer.git/blob/master/Makefile)                               | Compiles and runs the P2P file transfer system. Cleans and compiles Java files, sets up peers, and starts remote servers. Key for managing and running the peer-to-peer communication architecture.                      |
| [PeerProcess.java](https://github.com/sharanreddy99/P2P_File_Transfer.git/blob/master/PeerProcess.java)               | Initiates the PeerProcess by starting the PeerController using the provided peer ID. Facilitates the beginning of peer communication and file transfer within the P2P File Transfer architecture.                        |

</details>

<details closed><summary>main</summary>

| File                                                                                                               | Summary                                                                                                                                                                                                                                                                                   |
| ---                                                                                                                | ---                                                                                                                                                                                                                                                                                       |
| [PeerController.java](https://github.com/sharanreddy99/P2P_File_Transfer.git/blob/master/main/PeerController.java) | Manages connections, downloads, and uploads for peer-to-peer file transfer. Controls peer server, handles message exchange, optimistically unchokes peers, and tracks download status. Terminates and cleans up processes on completion.                                                  |
| [PeerHandler.java](https://github.com/sharanreddy99/P2P_File_Transfer.git/blob/master/main/PeerHandler.java)       | Implements message handling and communication logic between peer nodes. Controls message exchange, handles handshake, bitfield, request, piece messages, and download progress tracking. Manages peer connections, ensuring seamless file transfer in the P2P File Transfer architecture. |
| [PeerServer.java](https://github.com/sharanreddy99/P2P_File_Transfer.git/blob/master/main/PeerServer.java)         | Creates and manages socket connections for peers, accepting incoming connections from neighbors. Control over server status is provided. Peers are handled based on config file information.                                                                                              |

</details>

<details closed><summary>main.helper</summary>

| File                                                                                                                                                | Summary                                                                                                                                                                                                                                                                                |
| ---                                                                                                                                                 | ---                                                                                                                                                                                                                                                                                    |
| [ChokeUnchokePeerHelper.java](https://github.com/sharanreddy99/P2P_File_Transfer.git/blob/master/main/helper/ChokeUnchokePeerHelper.java)           | Implements choke/unchoke logic to manage top download peers, influencing peer connections. It selects preferred neighbors and initiates periodic choking/unchoking, crucial for peer interactions in the P2P file transfer system architecture.                                        |
| [PieceHelper.java](https://github.com/sharanreddy99/P2P_File_Transfer.git/blob/master/main/helper/PieceHelper.java)                                 | Manages piece-related operations, facilitating file downloads, storage, and retrieval based on peer configurations. Ensures seamless handling of file pieces with efficient data insertion and retrieval, enabling peer-to-peer file sharing.                                          |
| [LogHelper.java](https://github.com/sharanreddy99/P2P_File_Transfer.git/blob/master/main/helper/LogHelper.java)                                     | Logs messages with timestamps to a file, ensuring initialization and destruction of the logging feature based on the peers actions in the P2P_File_Transfer architecture.                                                                                                              |
| [CommonConfigHelper.java](https://github.com/sharanreddy99/P2P_File_Transfer.git/blob/master/main/helper/CommonConfigHelper.java)                   | Extracts and stores common configuration data, enabling retrieval of configuration values based on specific keys. Facilitates loading and parsing of the common configuration file, crucial for accessing essential system settings across the repositorys network-related components. |
| [BitFieldHelper.java](https://github.com/sharanreddy99/P2P_File_Transfer.git/blob/master/main/helper/BitFieldHelper.java)                           | Defines and manages the availability status of file segments for peer-to-peer file sharing. Tracks downloads, checks completion, and provides segment-related functionalities within the parent repositorys architecture.                                                              |
| [MessageHelper.java](https://github.com/sharanreddy99/P2P_File_Transfer.git/blob/master/main/helper/MessageHelper.java)                             | Sends specified messages to an output stream asynchronously. Manages message queue with the ability to add messages for transmission and handles exceptions during message processing.                                                                                                 |
| [PeerInfoHelper.java](https://github.com/sharanreddy99/P2P_File_Transfer.git/blob/master/main/helper/PeerInfoHelper.java)                           | Extracts peer information from config file to build a map linking peer IDs to details. Provides methods to access entire peer info or specific peer objects. Supports peer-based operations within the P2P file transfer system architecture.                                          |
| [OptimisticUnchokePeerHelper.java](https://github.com/sharanreddy99/P2P_File_Transfer.git/blob/master/main/helper/OptimisticUnchokePeerHelper.java) | Implements repetitive unchoking of optimistically chosen choked peer to enhance file sharing efficiency in P2P_File_Transfer architecture.                                                                                                                                             |
| [NextRequestHelper.java](https://github.com/sharanreddy99/P2P_File_Transfer.git/blob/master/main/helper/NextRequestHelper.java)                     | Handles message sequencing based on request type, triggers actions to maintain file download consistency. Manages message processing and communication between peers, ensuring efficient file transfer.                                                                                |

</details>

<details closed><summary>main.constants</summary>

| File                                                                                                               | Summary                                                                                                                                                                                                                 |
| ---                                                                                                                | ---                                                                                                                                                                                                                     |
| [Constants.java](https://github.com/sharanreddy99/P2P_File_Transfer.git/blob/master/main/constants/Constants.java) | Defines application constants for logging, configuration, messages, and file handling. Facilitates standardized values across the peer-to-peer file transfer system, ensuring consistent behavior and easy maintenance. |

</details>

<details closed><summary>main.messageTypes</summary>

| File                                                                                                                                | Summary                                                                                                                                                                                                                                                     |
| ---                                                                                                                                 | ---                                                                                                                                                                                                                                                         |
| [Peer.java](https://github.com/sharanreddy99/P2P_File_Transfer.git/blob/master/main/messageTypes/Peer.java)                         | Defines essential properties and actions for a peer, such as setting/getting ID, address, port, and file presence status. Facilitates tracking and managing peer information within the P2P File Transfer system.                                           |
| [PeerMessage.java](https://github.com/sharanreddy99/P2P_File_Transfer.git/blob/master/main/messageTypes/PeerMessage.java)           | Defines peer-to-peer message types with methods for message creation, retrieval, and modification. Handles message data, bit fields, message length, indexes, and types in the context of a peer-to-peer network architecture.                              |
| [PeerMessageType.java](https://github.com/sharanreddy99/P2P_File_Transfer.git/blob/master/main/messageTypes/PeerMessageType.java)   | Defines common message structure for peer-to-peer network communication, crucial for identifying message type, length, and number. Abstraction for message handling in the peer-to-peer file transfer system.                                               |
| [Piece.java](https://github.com/sharanreddy99/P2P_File_Transfer.git/blob/master/main/messageTypes/Piece.java)                       | Defines a message type Piece with data and size attributes for communication between peers in the P2P File Transfer system.                                                                                                                                 |
| [HandshakeMessage.java](https://github.com/sharanreddy99/P2P_File_Transfer.git/blob/master/main/messageTypes/HandshakeMessage.java) | Defines a Handshake Message structure for peer-to-peer communication. Assigns message numbers, sets peer ID, specifies message type, and header. Its vital for establishing connections and information exchange within the P2P_File_Transfer architecture. |

</details>

---

##  Getting Started

**System Requirements:**

* **Java**: `version 8`

###  Installation

<h4>From <code>source</code></h4>

> 1. Clone the P2P_File_Transfer repository:
>
> ```console
> $ git clone https://github.com/sharanreddy99/P2P_File_Transfer.git
> ```
>
> 2. Change to the project directory:
> ```console
> $ cd P2P_File_Transfer
> ```
>
> 3. Run the make file
> ```console
> $ make && make runAll
> ```

###  Usage

> You can now view the file transfer from the source node to all the peers

---

##  Contributing

Contributions are welcome! Here are several ways you can contribute:

- **[Report Issues](https://github.com/sharanreddy99/P2P_File_Transfer.git/issues)**: Submit bugs found or log feature requests for the `P2P_File_Transfer` project.
- **[Submit Pull Requests](https://github.com/sharanreddy99/P2P_File_Transfer.git/blob/main/CONTRIBUTING.md)**: Review open PRs, and submit your own PRs.
- **[Join the Discussions](https://github.com/sharanreddy99/P2P_File_Transfer.git/discussions)**: Share your insights, provide feedback, or ask questions.

<details closed>
<summary>Contributing Guidelines</summary>

1. **Fork the Repository**: Start by forking the project repository to your github account.
2. **Clone Locally**: Clone the forked repository to your local machine using a git client.
   ```sh
   git clone https://github.com/sharanreddy99/P2P_File_Transfer.git
   ```
3. **Create a New Branch**: Always work on a new branch, giving it a descriptive name.
   ```sh
   git checkout -b new-feature-x
   ```
4. **Make Your Changes**: Develop and test your changes locally.
5. **Commit Your Changes**: Commit with a clear message describing your updates.
   ```sh
   git commit -m 'Implemented new feature x.'
   ```
6. **Push to github**: Push the changes to your forked repository.
   ```sh
   git push origin new-feature-x
   ```
7. **Submit a Pull Request**: Create a PR against the original project repository. Clearly describe the changes and their motivations.
8. **Review**: Once your PR is reviewed and approved, it will be merged into the main branch. Congratulations on your contribution!
</details>

<details closed>
<summary>Contributor Graph</summary>
<br>
<p align="center">
   <a href="https://github.com/sharanreddy99/P2P_File_Transfer.git/graphs/contributors">
      <img src="https://contrib.rocks/image?repo=sharanreddy99/P2P_File_Transfer">
   </a>
</p>
</details>

---
