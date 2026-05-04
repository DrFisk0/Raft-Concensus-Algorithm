package window;

import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene dataScene;
    private static Scene graphicsScene;
    private Pane dataPane;
    private Pane graphicsPane;
    private Network network;
    private Set<TranslateTransition> ballsMoving;
    private GraphicsWindow graphicsWindow;
    private DataWindow dataWindow;

    @Override
    public void start(Stage stage) throws IOException {
        Scene scene = new Scene(loadFXML("primary"), 640, 480);

        ballsMoving = new HashSet<>();
        network = new Network(this);

        Stage graphicsStage = new Stage();
        graphicsPane = new Pane();
        graphicsScene = new Scene(graphicsPane, 640, 480);
        graphicsWindow = new GraphicsWindow(this, graphicsPane);
        graphicsWindow.drawNodeCircles();
        graphicsStage.setTitle("Raft Concensus Algorithm Visualisation");
        graphicsStage.setResizable(false);
        graphicsStage.setScene(graphicsScene);

        Stage dataStage = new Stage();
        dataPane = new Pane();
        dataScene = new Scene(dataPane, 640, 480);
        dataWindow = new DataWindow(this, dataPane);
        dataWindow.drawNodeData();
        dataStage.setScene(dataScene);
        dataStage.setTitle("Raft Concensus Algorithm Data");
        dataStage.setResizable(false);
        dataStage.setScene(dataScene);

        dataStage.show();
        graphicsStage.show();
        
    }

    public void stopAllCircles() {
        for (TranslateTransition tt : ballsMoving) {
            tt.pause();
        }
    }

    public void startAllCircles() {
        for (TranslateTransition tt : ballsMoving) {
            tt.play();
        }
    }

    public void sendCommand(String command) {
        ClientNode clientNode = network.getClientNode();
        clientNode.getCommandText(command);
    }

    public Log getNodeLog(Integer id) {
        return network.getNodeLog(id);
    }

    public void drawMessageCircle(Integer senderID, Integer reciverID, Double travelTime) {
        graphicsWindow.drawMessageCircle(senderID, reciverID, travelTime);
    }

    public void addMovingBall(TranslateTransition tt) {
        ballsMoving.add(tt);
        tt.play();

        if (!network.isRunning()) {
            tt.pause();
        }
    }

    public Integer getSimTime() {
        return network.getTotalTime();
    }

    public void startSim() {
        network.startSim();
    }

    public void stopSim() {
        network.stopSim();
    }

    public NodeState getNodeState(Integer nodeID) {
        return network.getNode(nodeID).getState();
    }

    public void turnOffNode(Integer id) {
        network.turnOffNode(id);
        graphicsWindow.fillCircleGray(id);
    }

    public void turnOnNode(Integer id) {
        network.turnOnNode(id);
        graphicsWindow.fillCircleGreen(id);
    }

    public void startElection(Integer id) {
        network.startElection(id);
    }

    static void setRoot(String fxml) throws IOException {
        dataScene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}