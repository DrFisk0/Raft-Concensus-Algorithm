package window;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

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

    private void sendMessage(Message message) {
        network.sendMessage(message);
        ready = false;
    }

    private void getStartStopCommand(String command) {
        switch (command) {
            case "start sim":
                network.startSim();
                break;
            case "stop sim":
                network.stopSim();
                break;
            default:
                badCommand(command);
                break;
        }
    }

    private void getTurnOnOffCommand(String command) {
        Character lastChar = command.charAt(command.length() - 1); //Gets the last character in the command
        Boolean lastCharIsDigit = Character.isDigit(lastChar); //Checks if it's a number
        String commandStart = command.substring(0, command.length() - 1); //Gets the string except for the last character

        if (commandStart.equals("turn off node ") && lastCharIsDigit) {
            network.turnOffNode(Character.getNumericValue(lastChar));
        } else if (commandStart.equals("turn on node ") && lastCharIsDigit) {
            network.turnOnNode(Character.getNumericValue(lastChar));
        } else {
            badCommand(command);
        }
    }

    private void getExitCommand(String command) {
        if (command.equals("exit")) {
            network.exit();
        }
    }

    private void badCommand(String command) {
        System.out.println("Got bad command");
        System.out.println("[" + command + "]");
    }

    private void getPrintCommand(String command) {
        if (command.equals("print all logs")) {
            System.out.println("Printing all logs");
            //network.printNodeLogs();
        }
    }

    private void proccesCommand(String command) { //Checks the first letter of the command
        command = command.toLowerCase();
        switch (command.charAt(0)) {
            case 's':
                getStartStopCommand(command);
                break;
            case 't':
                getTurnOnOffCommand(command);
                break;
            case 'c':
                getCommandText(command);
                break;
            case 'p':
                getPrintCommand(command);
                break;
            case 'e':
                getExitCommand(command);
                break;
            case 'h':
                if (command.equals("help")) {
                    printHelpText();
                } else if (command.equals("has concensus")) {
                    network.hasConcensus();
                } else {
                    badCommand(command);
                }
                break;
            default:
                badCommand(command);
                break;
        }
    }

    private void printHelpText() {
        System.out.println("For stopping the simulation, type: [stop sim]");
        System.out.println("For starting the simulation, type: [start sim]");
        System.out.println("To turn off an node, type [turn off node <Node ID>] where Node ID is replaced by the desired nodes id");
        System.out.println("To turn on an node, type [turn on node <Node ID>] where Node ID is replaced by the desired nodes id");
        System.out.println("When sending a command, type [com: <command>] where command is replaced by the desired command");
        System.out.println("Please note that command is just a string so every string given is accepted");
        System.out.println("To exit the program, type [exit]");
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
                    if (waitedTooLong(waitStart, 250)) {
                        waitStart = network.getTotalTime();
                        network.sendMessage(unsend_messages.peek());
                    }
                }
            }   
        }
    }
}
