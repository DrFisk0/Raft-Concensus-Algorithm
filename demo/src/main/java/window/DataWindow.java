package window;


import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;


public class DataWindow {
    private App app;
    private Pane pane;
    private Timer timer;
    private Text timerText;
    private Map<Integer, Text> nodeDataLogs;
    private Map<Integer, Text> nodeDataStatus;
    
    DataWindow(App app, Pane pane) {
        this.app = app;
        this.pane = pane;
        timer = new Timer();
        timerText = new Text("Time: 0");
        nodeDataStatus = new HashMap<>();
        nodeDataLogs = new HashMap<>();
    }

    public void drawNodeData() {

        Button startStopButton = new Button("Start/Stop animation");
        startStopButton.setOnAction(new EventHandler<ActionEvent>() {
            Boolean pressed = false;
            @Override
            public void handle(ActionEvent event) {
                if (!pressed) {
                    app.startSim();
                    pressed = true;
                } else {
                    app.stopSim();
                    pressed = false;
                }
            }
        });
        startStopButton.setTranslateX(640 - 130); //Button.getWidth returns 0 so need to hard code it
        pane.getChildren().add(startStopButton);

        TextField textField = new TextField("Enter command here");
        textField.setTranslateX(640 - 130);
        textField.setTranslateY(30);
        pane.getChildren().add(textField);
        
        Button commandButton = new Button("Send command!");
        commandButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                app.sendCommand(textField.getCharacters().toString());
            }
        });
        commandButton.setTranslateX(640 - 115);
        commandButton.setTranslateY(60);
        pane.getChildren().add(commandButton);


        for (Integer i = 1; i <= 5; i++) {
            drawNodeID(i);
            drawNodeStatus(i);
            drawLogs(i);
            drawButtons(i);
        }
        
        timerText.setX(10);
        timerText.setY(15);
        pane.getChildren().add(timerText);

        TimerTask timerTask = new TimerTask() {
            public void run() {
                timerText.setText("Time: " + String.valueOf(app.getSimTime()));   
                for (Integer i = 1; i <= 5; i++) {
                    updateStatusText(i, app.getNodeState(i));
                    updateLogs(i);
                }
            }
        };

        timer.scheduleAtFixedRate(timerTask, 100l, 100l);
    }

    private void drawLogs(Integer i) {
        Text tempText = new Text("Log: Empty");
        tempText.setX(10);
        tempText.setY(getNodePosY(i) + 15);
        nodeDataLogs.put(i, tempText);
        pane.getChildren().add(tempText);
    }

    private void updateLogs(Integer id) {
        Text text = nodeDataLogs.get(id);
        Log log = app.getNodeLog(id);
        String logString = new String("Log: ");

        if (log.getSize() != 0) {
            for (int i = log.getSize(); i > 0 && log.getSize() - i < 2; i--) {
                LogEntry logEntry = log.getEntryAt(i - 1);
                logString += "Entry: " + i + "[Term: " + logEntry.getTerm() + ", Time: " + logEntry.getTime() + ", Command: " + logEntry.getCommand() + ", Commited: " + logEntry.isCommited() + "]\n        ";
            }   
        } else {
            logString = logString + "Empty";
        }

        text.setText(logString);
    }

    private void drawButtons(Integer i) {
        Button onButton = new Button("Turn on node " + i);
        onButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                app.turnOnNode(i);
                //updateStatusText(i, "Follower");
        }});
        onButton.setTranslateX(10);
        onButton.setTranslateY(getNodePosY(i) + 40);
        pane.getChildren().add(onButton);

        Button offButton = new Button("Turn off node " + i);
        offButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                app.turnOffNode(i);
                //updateStatusText(i,"Offline");
        }});
        offButton.setTranslateX(110);
        offButton.setTranslateY(getNodePosY(i) + 40);
        pane.getChildren().add(offButton);

        Button electionButton = new Button("Start election!");
        electionButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                app.startElection(i);
        }});
        electionButton.setTranslateX(210);
        electionButton.setTranslateY(getNodePosY(i) + 40);
        pane.getChildren().add(electionButton);
    }

    private void updateStatusText(Integer id, NodeState state) {
        Text tempText = nodeDataStatus.get(id);
        StringBuilder string = new StringBuilder("Status: ");

        switch (state) {
            case FOLLOWER:
                string.append("Follower");
                break;
            case LEADER:
                string.append("Leader");
                break;
            case CANDIDATE:
                string.append("Candidate");
                break;
            case OFFLINE:
                string.append("Offline");
                break;
            default:
                string.append("Error");
                break;
        }
        tempText.setText(string.toString());
    }

    private void drawNodeStatus(Integer id) {
        Text tempText = new Text("Status: Follower");
        tempText.setX(60);
        tempText.setY(getNodePosY(id));
        nodeDataStatus.put(id, tempText);
        pane.getChildren().add(tempText);
    }
    
    private Integer getNodePosY(Integer id) {
        return 35 + (92 * (id - 1));
    }

    private void drawNodeID(Integer nodeID) {
        Text tempText = new Text("Node: " + nodeID);
        tempText.setX(10); //The only diff between the node data is the y-value
        tempText.setY(getNodePosY(nodeID));
        pane.getChildren().add(tempText);
    }
}
