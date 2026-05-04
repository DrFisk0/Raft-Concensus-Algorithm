package window;

public class LogEntry {
    private Integer term;       //The term the entry was made in
    private Integer time;       //The time the entry was created
    private String command;     //The command the entry recived. For elections this will just be an empty string
    private Boolean commited;   //If the entry is commited

    public LogEntry(int term, int time, String command) {
        this.term = term;
        this.command = command;
        this.time = time;
        this.commited = false;
    }

    public Boolean isCommited() { return commited; }

    public Integer getTerm() { return term; }

    public void commitLogEntry() { commited = true; }

    public String getCommand() { return command; }

    public int getTime() { return time; }

    public Boolean sameEntry(LogEntry logEntry) {
        Boolean same = true;

        if (this.term != logEntry.term) {
            same = false;
        }

        if (this.time != logEntry.time) {
            same = false;
        }        

        if (!(this.command.equals(logEntry.command))) {
            same = false;
        }

        return same;
    }

    public String printLogEntry() {
        return "[Term: " + term + ", Time: " + time + ", Command: " + command + ", Commited: " + commited + "]";
    }
}
