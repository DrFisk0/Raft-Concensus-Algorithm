package window;

import java.util.LinkedList;
import java.util.Queue;

enum NodeState {
    FOLLOWER, OFFLINE, LEADER, CANDIDATE
}

public class Node implements Runnable {
    private Integer ID; //Nodes personal ID, must be uniqe from all other nodes.
    private Integer leader; //The ID of the current Leader node. Can be itself.
    private Queue<Message> unproccesed_messages; //Queue is used as to force a FIFO order of proccesing messages
    private NodeState state; //The state of the node, e.g. Follower, Leader
    private Integer timeout; //How long the node needs to wait with no message from the current leader before it starts an election
    private Integer term; //The current election term the node is on.
    private Log log; //The log of the node documenting what has happend
    private Network network; //The network the node is a part of
    private Boolean wantElection;

    public Node(int ID, Network network) {
        this.ID = ID;
        this.wantElection = false;
        this.term = 0;
        this.leader = -2; //minus 1 means that no leader has been elected yet
        this.state = NodeState.FOLLOWER;
        this.network = network;
        this.log = new Log();
        this.timeout = updateTimeout();
        this.unproccesed_messages = new LinkedList<Message>();
    }

    public void turnOff() {
        if (state != NodeState.OFFLINE) {
            System.out.println("Node " + ID + " turning off");
            changeState(NodeState.OFFLINE);
        } else {
            System.out.println("Node " + ID + " already off");
        }
    }

    public void turnOn() {
        if (state == NodeState.OFFLINE) {
        System.out.println("Node " + ID + " turning on");
        changeState(NodeState.FOLLOWER);
        } else {
            System.out.println("Node " + ID + " already on");
        }
    }

    public synchronized Log getLog() {
        return log;
    }

    public synchronized Boolean reciveMessage(Message newMessage) {
        if (state != NodeState.OFFLINE) { return unproccesed_messages.offer(newMessage); } //Offer returns true if message was delivered correctly and false otherwise
        return false; //Should the node be offline then we do not get the message
    }

    private synchronized Boolean hasMessage() { return !unproccesed_messages.isEmpty(); }

    private synchronized Message getMessage() { return unproccesed_messages.poll(); }

    public int getNodeID() { return ID; }

    private int updateTimeout() {
        return network.getRandNum(150, 300);
    }
    private void sendElectionMessage(int reciverID) {
        Message tempMessage = new Message(ID, reciverID, MessageType.REQUESTVOTE, log);
        network.sendMessage(tempMessage);
        //System.out.println("Node " + ID + " has send a vote request to node " + reciverID);
    }

    private void sendVoteYay(int reciverID) {
        Message tempMessage = new Message(ID, reciverID, MessageType.YAYVOTE, log);
        network.sendMessage(tempMessage);
        //System.out.println("Node " + ID + " has voted for node " + reciverID);
    }

    public synchronized void makeWantElection() {
        wantElection = true;
    }

    private synchronized Boolean getWantElection() {
        return wantElection;
    }

    public void startElection() {
        if (!shouldAct()) { return; }
        synchronized (this) {
        wantElection = false;
        }
        updateTimeout();
        System.out.println("Node " + ID + " has started an election");
        changeState(NodeState.CANDIDATE);
        Integer votes = 1; //Start at 1 so that the node votes for itself
        Boolean electionOnGoing = true;
        term++;
        log.addToLog(term, network.getTotalTime(), "Node " + ID + " started and won election"); //Add election to the log
        for (Node node : network.getNodes()) {
            if (node.ID != ID) {
                sendElectionMessage(node.ID);
            }
        }
        int requiredVotes = (network.getNodes().size() / 2) + 1; //Gets the required votes which equals half of the nodes rounded up
        int electionStart = network.getTotalTime();
        while (electionOnGoing) {
            //System.out.println("In the loop");
            if (hasMessage()) {
                System.out.println("Node " + ID + " got a message");
                Message currMessage = getMessage();
                if (currMessage.getType() == MessageType.YAYVOTE) {
                    votes++;
                    System.out.println("Node " + ID + " got a yes");
                } else if (currMessage.getType() == MessageType.ELECTIONSUCCES) { //Election lost :( 
                electionOnGoing = false;
                updateLeader(currMessage);
                } else if (currMessage.getTerm() > term) { //Election is out dated and there is another node which is on a higher term than us
                    electionOnGoing = false;
                    sendVoteYay(currMessage.getSenderID());
                }
            }
            if (votes >= requiredVotes) { // Election was a succsess!
            electionOnGoing = false;
            leader = ID;
            changeState(NodeState.LEADER);
            sendElectionSuccesMessage();
            commitLastEntry();
            System.out.println("Node " + ID + " won the election");
            } else if (waitedTooLong(electionStart, timeout)) {
                //System.out.println("Node " + ID + " has started a second election");
                electionOnGoing = false;
                startElection();
            }
        }
    }

    public int getLeader() { return leader; } 

    private void sendElectionSuccesMessage() {
        for (Node node : network.getNodes()) {
            if (ID != node.ID) {
                Message message = new Message(ID, node.ID, MessageType.ELECTIONSUCCES, log);
                network.sendMessage(message);
            }
        }
    }

    private void updateLeader(Message message) {
        leader = message.getSenderID();
        updateLog(message.getLog());;
        commitLastEntry();
        System.out.println("Node " + ID + " sees node " + leader + " as leader");
    }

    private void proccesMessage(Message message) {
        updateTimeout(); //Get new timeout
        switch (message.getType()) {
            case REQUESTVOTE:
                if (higherTerm(message)) { //We don't want to vote for someone who is at a lower or equal term than us
                    term = message.getTerm();
                    sendVoteYay(message.getSenderID());
                    changeState(NodeState.FOLLOWER);
                }
                break;
            case ELECTIONSUCCES:
                updateLeader(message);
                break;
            case GETREADY:
                log.addToLog(message.getTerm(), message.getLog().getLast().getTime(), message.getLog().getLast().getCommand()); //Message doesn't carry a command but carries a log that does
                sendReadyMessage(message.getSenderID());
                break;
            case COMMIT:
                commitLastEntry();
                System.out.println("Node " + ID + " has commited last entry");
                //printlog();
                break;
            case HEARTBEAT:
                if (higherTerm(message)) {
                    term = message.getTerm();
                    leader = message.getSenderID();
                    updateLog(message.getLog());
                    changeState(NodeState.FOLLOWER);
                }
                break;
            case CLIENT:
                //Only leader should get here    
                log.addToLog(term, network.getTotalTime(), message.getCommand());
                sendGetReadyMessage();
                break;
            case READY:
                //Late ready message, just ignore it
                break;
            case YAYVOTE:
                //Late yayvote message, just ignore
                break;
            default:
                System.out.println("Node " + ID + " got an unknown message");
                message.printMessage();
                break;
        }
    }

    public synchronized void printlog() {
        System.out.println("Node " + ID + " printing log");
        log.printLog();
    }

    private void commitLastEntry() {
        if (log.getLast().isCommited()) {
            System.out.println("Node " + ID + "'s last entry was already commited");
        } else {
            log.getLast().commitLogEntry();
        }
        //System.out.println("Node " + ID + " has commited");
    }

    private void sendReadyMessage(Integer reciverID) {
        Message message = new Message(ID, reciverID, MessageType.READY, log);
        network.sendMessage(message);
    }

    private void sendGetReadyMessage() {
        LogEntry logEntry = log.getLast();
        Log tempLog = new Log();
        tempLog.addToLog(logEntry.getTerm(), logEntry.getTime(), logEntry.getCommand());

        for (Node node : network.getNodes()) {
            if (ID != node.ID) {
                Message tempmessage = new Message(ID, node.ID, MessageType.GETREADY, tempLog);
                network.sendMessage(tempmessage);
            }
        }

        System.out.println("Node " + ID + " send get ready messages and is waiting");

        Integer nodesReady = 1; //Vote for itself
        Integer requiredReady = (network.getNodes().size() / 2) + 1; //Only works with uneven number of nodes.
        Boolean interupted = false;

        while (nodesReady <= requiredReady && !interupted) {
            if (hasMessage()) {
                Message response = getMessage();
                if (response.getType() == MessageType.READY) {
                    nodesReady++;
                } else {
                    interupted = true;
                    proccesMessage(response);
                }
            }
        }
        if (nodesReady >= requiredReady) { //Half or more of all nodes are ready to commit
            sendCommit();
            commitLastEntry();
            System.out.println("Node " + ID + " send out commit messages");
        } 
    }

    private void sendCommit() {
        for (Node node : network.getNodes()) {
            if (ID != node.ID) {
                Message message = new Message(ID, node.ID, MessageType.COMMIT, log);
                network.sendMessage(message);
            }
        }
    }

    private Boolean waitedTooLong(Integer startWait, Integer deadline) {
        return network.getTotalTime() >= deadline + startWait;
    }

    private synchronized void updateLog(Log log) {
        System.out.println("Node " + ID + " updating their log");
        this.log = log.copyLog();
    }

    public int getTerm() { return term; }

    public synchronized void changeState(NodeState newNodeState) { state = newNodeState; }

    public synchronized NodeState getState() { return state; }

    private Boolean higherTerm(Message message) {
        return term < message.getTerm();
    }

    private synchronized Boolean shouldAct() {
        //System.out.println("Network running = " + network.isRunning());
        return network.isRunning() && !(state == NodeState.OFFLINE);
    }

    private void sendHeartbeatMessage() {
        System.out.println("Node " + ID + " sending heartbeat with term " + term);
        for (Node node : network.getNodes()) {
            if (ID != node.ID) {
                Message message = new Message(ID, node.ID, MessageType.HEARTBEAT, log);
                network.sendMessage(message);
            }
        }
    }

    private void followerLogic(Integer waitStart) {
        while (!waitedTooLong(waitStart, timeout) && !getWantElection()) {        
            if (hasMessage() && shouldAct()) {
                Message currMessage = getMessage();
                //System.out.println("Node " + ID + " got a message");
                proccesMessage(currMessage);
                waitStart = network.getTotalTime();
            }
        }
        //System.out.println("Node " + ID + " has waited too long");
        if (shouldAct()) { 
            synchronized (this) {
                wantElection = true;
            }
        }
    }

    private void leaderLogic(Integer waitStart) {
        int heartBeatTime = timeout / 5; 
        while (!waitedTooLong(heartBeatTime, waitStart) && state == NodeState.LEADER) {
            if (hasMessage() && shouldAct()) { //Check that we are still online
                Message currMessage = getMessage();
                proccesMessage(currMessage);
            }
        }
        if (shouldAct() && state == NodeState.LEADER) { sendHeartbeatMessage(); } //Need to make sure we're still online
    }

    public void setTimeout(Integer newTimeout) {
        timeout = newTimeout;
    }

    public void run() {
        System.out.println("Node " + ID + " running");
        while (true) {
            if (getWantElection()) {
                startElection();
            }
            if (shouldAct()) { //Check if node is online and that the sim is running
                int waitStart = network.getTotalTime();
                if (state == NodeState.LEADER) { //Special logic for leader
                    leaderLogic(waitStart);
                } else { //Follower logic
                    followerLogic(waitStart);
                }
            }
        }
    }
}