package window;

enum MessageType {
    REQUESTVOTE, YAYVOTE, READY, COMMIT, HEARTBEAT, ELECTIONSUCCES, LEADERCONFIRM, CLIENT, GETREADY
}

public class Message {
    private Integer senderID;   //The id of the node who send the message
    private Integer reciverID;  //The id of the node who is to recive the message
    private MessageType type;   //The type of the message being send
    private Log log;            //The log given to the message
    private String command;     //The command in the message, only used by client node

    public Message(Integer senderID, MessageType type, String command) { //Client node uses this constructor
        this.senderID = senderID;
        this.reciverID = null;
        this.type = type;
        this.log = null;
        this.command = command;
    }

    public Message(Integer senderID, Integer reciverID, MessageType type, Log log) { //Network nodes use this constructor
        this.senderID = senderID;
        this.reciverID = reciverID;
        this.type = type;
        this.log = log;
        this.command = "";
    }

    public void setReciverID(Integer reciverID) { this.reciverID = reciverID; }

    public Integer getReciverID() { return reciverID; }

    public MessageType getType() { return type; }

    public Log getLog() { return log; }

    public Integer getSenderID() { return senderID; }

    public Integer getTerm() { return log.getLast().getTerm(); }

    public String getCommand() { return command; }

    public void printMessage() {
        System.out.println("Sender: " + senderID);
        System.out.println("Reciver: " + reciverID);
        System.out.println("Type: " + type);
        System.out.println("Text: " + command);
        log.printLog();
    }
}
