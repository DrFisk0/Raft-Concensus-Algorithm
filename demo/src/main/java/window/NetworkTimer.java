package window;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

//Class made for network class allowing it to keep track of time for the simulation
//and to start and stop the timer at will while keeping track of time.
public class NetworkTimer {
    private Integer totalTime;
    private Network network; //Network the timer is connected to
    private Timer timer; //The actual object that keeps track of time and measures it
    private ArrayList<Integer> alarms = new ArrayList<>(); //Alarms for when a message needs to be send

    public NetworkTimer(Network network) {
        this.totalTime = 0;
        this.network = network;
    }

    public void addAlarm(int alarmTime) {
        if (alarms.contains(alarmTime)) { return; } //Return if we already have an alarm set for the time
        alarms.add(alarmTime);
        if (alarms.size() > 1) { alarms.sort(null); } 
    }

    public void startTimer() {
        timer = new Timer();
        timer.schedule(new simulateTime(network, alarms, this), network.getTickRate(), network.getTickRate()); //Makes a timer that runs simulateTime every tickrate after waiting a tickrate
    }

    public void addToTime() {
        totalTime++;
    }

    public Integer getTotalTime() {
        return totalTime; 
    }

    public void stopTimer() {
        timer.cancel();
    }
}

class simulateTime extends TimerTask {
    private Network network;
    private ArrayList<Integer> alarms;
    private NetworkTimer netTimer;

    public simulateTime(Network network, ArrayList<Integer> alarms, NetworkTimer netTimer) {
        this.network = network;
        this.alarms = alarms;
        this.netTimer = netTimer;
    }

    public void run() {
        netTimer.addToTime(); //The current run makes it so that sim time is 10ms for every 1s real time.
        if (!alarms.isEmpty()) {
            if (network.getTotalTime() >= alarms.get(0)) { //If we've reached or passed the alarm of next message to be send
                network.ping(alarms.remove(0)); //Ping the network when we reached an alarm
            } 
        }
    }
}