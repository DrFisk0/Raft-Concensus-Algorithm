package window;

import java.util.ArrayList;

//Log class, The only way to modify a log is to add and commit.
//Note that there is no way to delete commited data, this is intentional
public class Log {
    private ArrayList<LogEntry> entries; //A list of log entries

    public Log() {
        this.entries = new ArrayList<>();
    }

    public LogEntry getLast() {
        if (entries.size() > 0) { return entries.get(entries.size()-1); }
        return null;
    }

    private void append(Integer term, Integer time, String command) {
        //System.out.println("Appending; Term: " + term + ", Time: " + time + ", Command: [" + command + "]");
        LogEntry new_entry = new LogEntry(term, time, command);
        entries.add(new_entry);
    }

    //Adds to the log and removes the last entry if it isn't commited aka. there isn't a concensus on it
    public void addToLog(int term, int time, String command) {
        if (entries.size() == 0) {
            append(term, time, command);
        } else if (entries.get(entries.size()-1).isCommited()) {
            append(term, time, command);   
        } else {
            entries.remove(entries.size() - 1); //Remove latest entry that isn't commited
            append(term, time, command);
        }
    }

    public LogEntry getEntryAt(Integer pos) {
        return entries.get(pos);
    }

    //Checks to see if two logs have the same entries
    public Boolean sameLog(Log comparisonLog) {
        if (entries.size() != comparisonLog.entries.size()) { return false; }

        for (int i = 0; i < entries.size(); i++) {
            LogEntry firstLogEntry = entries.get(i);
            LogEntry secondLogEntry = comparisonLog.entries.get(i);
            if (!firstLogEntry.sameEntry(secondLogEntry)) {
                return false;
            }
        }
        return true;
    }

    //Makes a deep copy of the given log and returns it
    public Log copyLog() {
        Log returnLog = new Log();
        for (LogEntry logEntry : entries) {
            returnLog.append(logEntry.getTerm(), logEntry.getTime(), logEntry.getCommand());
            if (logEntry.isCommited()) { returnLog.commitLastEntry(); }
        }
        return returnLog;
    }

    public void commitLastEntry() {
        entries.get(entries.size()-1).commitLogEntry();
    }

    public Integer getSize() {
        return entries.size();
    }

    public void printLog() {
        if (entries.size() != 0) {
            StringBuilder logString = new StringBuilder("Log: ");
            for (int i = 0; i < entries.size(); i++) {
                logString.append("Entry " + i + " " + entries.get(i).printLogEntry() + " ");
            }
            System.out.println(logString);
        } else {
            System.out.println("Log: Empty");
        }
    }
}
