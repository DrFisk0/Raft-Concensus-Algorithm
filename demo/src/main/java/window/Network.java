package window;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javafx.application.Platform;


public class Network {
    private App app; //The app the network will be visualised on
    private Set<Node> nodes; //The nodes in the network
    private ClientNode clientNode;
    private NetworkTimer netTimer; //The speed at which the simulation runs
    private Map<Integer, Set<Message>> messagesInFlight; //Map that holds the messages that are begin send, MessageID is the key with the Message(s) being the value
    private Integer timerTickRate; //The rate of which timers tick in the two timer classes
    private Boolean running; //Bool for if the simulation is running

    public Network(App app) {
        this.app = app;
        this.running = false; 
        this.timerTickRate = 100;
        this.nodes = createNodes();
        this.netTimer = new NetworkTimer(this);
        this.messagesInFlight = new HashMap<Integer, Set<Message>>();
        this.clientNode = createClientNode();
    }

    private ClientNode createClientNode() { //Makes the client node for the network
        ClientNode clientNode = new ClientNode(this);
        Thread thread = new Thread(clientNode);
        thread.start();
        return clientNode;
    }

    public ClientNode getClientNode() {
        return clientNode;
    }

    private Node getLeader() {
        Set<Integer> responses = new HashSet<>();

        for (Node node : nodes) { //For loop to see if all the nodes agree on who the leader is
            if (node.getState() != NodeState.OFFLINE) { //We need to check only online nodes as an offline node could have out of date info
                responses.add(node.getLeader());
            }
        }

        if (responses.size() == 1 && !(responses.contains(-2))) { //If there only is one element and it is not -2 we know there is a concensus on who the leader is
            Iterator<Integer> tempIterator = responses.iterator();
            return getNode(tempIterator.next());
        } else {
            //System.out.println("Couldn't get concensus on leader");
            return null;
        }
    }

    public void startSim() {
        System.out.println("Starting simulation");
        if (!running) {
            running = true;
            netTimer.startTimer();
            app.startAllCircles();
        }
        //app.createCircle(totalTime, timerTickRate, 0);

    }

    public void stopSim() {
        System.out.println("Stopping simulation");
        netTimer.stopTimer();
        running = false;
        app.stopAllCircles();
    }

    public int getTickRate() { return timerTickRate; }

    private Set<Node> createNodes() { //Creates and starts the nodes in the network
        Set<Node> newNodes = new HashSet<Node>();
        for (int i = 1; i <= 5; i++) {
            Node tempNode = new Node(i, this);
            newNodes.add(tempNode);
            Thread tempThread = new Thread(tempNode);
            tempThread.start();
        }
        return newNodes;
    }

    public Boolean isRunning() {
        return running;
    }

    public void sendMessage(Message message, Integer delay) {
        proccesMessage(message, delay);
        Platform.runLater(() -> { //Needs to run on javafx and this takes care of that
            app.drawMessageCircle(message.getSenderID(), message.getReciverID(), Double.valueOf(delay - getTotalTime()));
        }
    );

    }

    public synchronized void sendMessage(Message message) {
        if (message.getType() != MessageType.CLIENT) { //If message is not send by client handle it normally
            sendMessage(message, getTotalTime() + getDelay());
        } else { //The message is a client message
            Node leader = getLeader();
            if (leader != null) { //Check to see if concensus on leader has been reached
                message.setReciverID(leader.getNodeID());
                sendMessage(message, getTotalTime() + getDelay());
            } else {
                System.out.println("Couldn't get concensus on leader");   
            }   
        }
    }

    private synchronized void proccesMessage(Message message, int delay) {
        Set<Message> newSet = new HashSet<>();
        
        newSet.add(message);
            if (messagesInFlight.containsKey(delay)) {
                for (Message currMessage : messagesInFlight.remove(delay)) {
                    newSet.add(currMessage);
                }
            }
            messagesInFlight.put(delay, newSet);
            netTimer.addAlarm(delay);
            //System.out.println("Send message");
    }

    public void ping(int alarmTime) { //Ping the network for when message(s) needs to be send
        for (Message message : messagesInFlight.remove(alarmTime)) {
            deliverMessage(message);
        }
    }

    private void deliverMessage(Message message) { //Gives the message to the node it was send to
        int reciverID = message.getReciverID();
        Node tempNode = getNode(reciverID);
        tempNode.reciveMessage(message);
    }

    public int getRandNum(int min, int max) {
        return (int)(Math.random() * (max - min) + min);
    }

    private int getDelay() {
        return getRandNum(5, 30);
    }

    public void exit() {
        System.out.println("Exiting simulation");
        System.exit(0);
    }

    public void startElection(Integer id) {
        Node tempNode = getNode(id);
        tempNode.makeWantElection();
    }

    public Log getNodeLog(Integer id) {
        return getNode(id).getLog();
    }

    public synchronized Node getNode(int targetNodeID) {
        //Nodes have id's from 0-4
        Node returnNode = null;
        for (Node node : nodes) {
            if (node.getNodeID() == targetNodeID) {
                returnNode = node;
            }
        }
        return returnNode;
    }

    public Set<Node> getNodes() {
        return nodes;
    }

    public void printNodeLogs() {
        for (Node node : nodes) {
            node.printlog();
        }
    }

    public synchronized Integer getTotalTime() {
        return netTimer.getTotalTime();
    }


    public void turnOffNode(Integer id) {
        Node tempNode = getNode(id);
        tempNode.turnOff();
    }

    public void turnOnNode(Integer id) {
        Node tempNode = getNode(id);
        tempNode.turnOn();
    }

    public void hasConcensus() {
        Boolean concensus = true;
        Log comparisonLog = null;
        for (Node node : nodes) {
            if (node.getState() != NodeState.OFFLINE) {
                if (comparisonLog != null) {
                    if (!node.getLog().sameLog(comparisonLog)) {
                        concensus = false;
                    }
                } else {
                    comparisonLog = node.getLog().copyLog();
                }
            }
        }
        if (concensus) {
            System.out.println("Concensus is reached");
        } else {
            System.out.println("Concensus is NOT reached");
        }
    }
}
