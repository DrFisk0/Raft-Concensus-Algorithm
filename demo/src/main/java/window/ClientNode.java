package window;

import java.util.LinkedList;
import java.util.Queue;

public class ClientNode implements Runnable {
    private Network network; //Network the client node is part of
    private Queue<Message> unproccesed_messages; //Queue is used as to force a FIFO order of proccesing messages
    private Queue<Message> unsend_messages; //Queue is used as to force a FIFO order of proccesing messages
    private Boolean ready;
    private Integer leader;

    public ClientNode(Network network) {
        this.leader = -1;
        this.ready = true;
        this.network = network;
        this.unproccesed_messages = new LinkedList<Message>();
        this.unsend_messages = new LinkedList<Message>();        
    }

    public Boolean reciveMessage(Message message) {
        return unproccesed_messages.offer(message);
    }

    private Message getMessage() {
        return unproccesed_messages.poll();
    }

    private synchronized Boolean hasMessage() {
        return !unproccesed_messages.isEmpty();
    }

    private Boolean addMessage(Message message) {
        return unsend_messages.offer(message);
    }

    public void getCommandText(String command) {
        Message message = new Message(-1, leader, MessageType.CLIENT, command);
        addMessage(message);
    }

    public void proccesMessage(Message message) {
        if (message.getType() == MessageType.ELECTIONSUCCES) {
            leader = message.getSenderID();
        } else if (message.getType() == MessageType.COMMIT) {
            ready = true;
            unsend_messages.remove();
        }
    }

    private Boolean waitedTooLong(Integer startWait, Integer deadline) {
        return network.getTotalTime() >= deadline + startWait;
    }

    public void run() {
        //network.startSim();
        while (true) {
            if (hasMessage()) {
                Message message = getMessage();
                proccesMessage(message);
            }
            if (ready && leader != -1 && !unsend_messages.isEmpty()) {
                network.sendMessage(unsend_messages.peek());
                ready = false;
                Integer waitStart = network.getTotalTime();
                while (!ready) {
                    if (hasMessage()) {
                        Message message = getMessage();
                        proccesMessage(message);
                    }
                    if (waitedTooLong(waitStart, 100)) {
                        waitStart = network.getTotalTime();
                        network.sendMessage(unsend_messages.peek());
                    }
                }
            }   
        }
    }
}
