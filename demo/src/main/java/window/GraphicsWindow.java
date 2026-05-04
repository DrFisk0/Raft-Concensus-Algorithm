package window;

import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;

import javafx.animation.TranslateTransition;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.util.Pair;

public class GraphicsWindow {
    private App app;
    private Pane pane;
    private Map<Integer, Circle> nodeCircles;
    private Map<Integer, Pair<Integer,Integer>> networkNodesCords;
    private Pair<Integer, Integer> clientNodePos;

    GraphicsWindow(App app, Pane pane) {
        this.app = app;
        this.pane = pane;
        this.clientNodePos = new Pair<Integer,Integer>(40, 40);
        this.nodeCircles = new HashMap<>();
        networkNodesCords = new HashMap<>();
        networkNodesCords.put(1, new Pair<>(320, 48)); //Top node1 cords
        networkNodesCords.put(2, new Pair<>(512, 192)); //Middle right node2 cords
        networkNodesCords.put(3, new Pair<>(448, 384)); //Bottom right node3 cords
        networkNodesCords.put(4, new Pair<>(192, 384)); //Bottom left node4 cords
        networkNodesCords.put(5, new Pair<>(128, 192)); //Middle left node5 cords
        
    }

    private Integer getClientNodePosX() {
        return clientNodePos.getKey();
    }

    private Integer getClientNodePosY() {
        return clientNodePos.getValue();
    }

    private Integer getNetNodePosX(Integer id) {
        return networkNodesCords.get(id).getKey();
    }

    private Integer getNetNodePosY(Integer id) {
        return networkNodesCords.get(id).getValue();
    }

    public void drawMessageCircle(Integer senderID, Integer reciverID, Double travelTime) {
        if (senderID > 0) {
            drawNetNodeMessageCircle(senderID, reciverID, travelTime);
        } else {
            drawClientNodeMessageCircle(reciverID, travelTime);
        }
    }

    private void drawClientNodeMessageCircle(Integer reciverID, Double travelTime) {
        Circle testCircle = new Circle();
        testCircle.setCenterX(getClientNodePosX());
        testCircle.setCenterY(getClientNodePosY());
        testCircle.setRadius(15);
        testCircle.setFill(Color.BLUE);

        travelTime = travelTime / 10;

        System.out.println("Client Node sending message to node " + reciverID + " with delay " + travelTime + "s");

        TranslateTransition tt = new TranslateTransition(Duration.seconds(travelTime), testCircle);

        Integer startPosX = getClientNodePosX();
        Integer startPosY = getClientNodePosY();
        Integer endPosX = getNetNodePosX(reciverID);
        Integer endPosY = getNetNodePosY(reciverID);

        tt.setToX(endPosX - startPosX);
        tt.setToY(endPosY - startPosY);

        app.addMovingBall(tt);

        pane.getChildren().add(testCircle);

        tt.setOnFinished(e -> {
            removeCircle(testCircle);
        });
    }

    private void drawNetNodeMessageCircle(Integer senderID, Integer reciverID, Double travelTime) {
        Circle testCircle = new Circle();
        testCircle.setCenterX(getNetNodePosX(senderID));
        testCircle.setCenterY(getNetNodePosY(senderID));
        testCircle.setRadius(15);
        testCircle.setFill(Color.BLUE);

        travelTime = travelTime / 10;

        System.out.println("Node " + senderID + " sending message to node " + reciverID + " with delay " + travelTime + "s");

        TranslateTransition tt = new TranslateTransition(Duration.seconds(travelTime), testCircle);

        Integer startPosX = getNetNodePosX(senderID);
        Integer startPosY = getNetNodePosY(senderID);
        Integer endPosX = getNetNodePosX(reciverID);
        Integer endPosY = getNetNodePosY(reciverID);

        tt.setToX(endPosX - startPosX);
        tt.setToY(endPosY - startPosY);

        app.addMovingBall(tt);

        pane.getChildren().add(testCircle);

        tt.setOnFinished(e -> {
            removeCircle(testCircle);
        });
    }

    private void removeCircle(Circle circle) {
        pane.getChildren().remove(circle);
    }

    public void drawNodeCircles() {
        drawClientNodeCircle();
        for (Integer i = 1; i <=5; i++) {
            drawNetworkCircle(i);
        }
    }

    private void drawClientNodeCircle() {
        Circle circle = new Circle();
        circle.setCenterX(getClientNodePosX());
        circle.setCenterY(getClientNodePosY());
        circle.setRadius(30);
        circle.setFill(Color.BLACK);

        Text text = new Text("Client Node");
        text.setX(getClientNodePosX() - 33);
        text.setY(getClientNodePosY() + 40);
    
        pane.getChildren().addAll(circle, text);
    }

    public void fillCircleGray(Integer id) {
        Circle tempCircle = nodeCircles.get(id);
        tempCircle.setFill(Color.GRAY);
    }

    public void fillCircleGreen(Integer id) {
        Circle tempCircle = nodeCircles.get(id);
        tempCircle.setFill(Color.GREEN);
    }

    private void drawNetworkCircle(Integer id) {
        Circle circle = new Circle();
        circle.setCenterX(getNetNodePosX(id));
        circle.setCenterY(getNetNodePosY(id));
        circle.setRadius(30);
        circle.setFill(Color.GREEN);
        nodeCircles.put(id, circle);

        Text text = new Text("Node: " + id);
        text.setX(getNetNodePosX(id) - 19);
        text.setY(getNetNodePosY(id) + 40);

        pane.getChildren().addAll(circle, text);
    }
}
