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


    GraphicsWindow(App app, Pane pane) {
        this.app = app;
        this.pane = pane;
        this.nodeCircles = new HashMap<>();
        networkNodesCords = new HashMap<>();
        networkNodesCords.put(1, new Pair<>(320, 48)); //Top node1 cords
        networkNodesCords.put(2, new Pair<>(512, 192)); //Middle right node2 cords
        networkNodesCords.put(3, new Pair<>(448, 384)); //Bottom right node3 cords
        networkNodesCords.put(4, new Pair<>(192, 384)); //Bottom left node4 cords
        networkNodesCords.put(5, new Pair<>(128, 192)); //Middle left node5 cords
    }

    private Integer getNodePosX(Integer id) {
        return networkNodesCords.get(id).getKey();
    }

    private Integer getNodePosY(Integer id) {
        return networkNodesCords.get(id).getValue();
    }

    public void drawMessageCircle(Integer senderID, Integer reciverID, Double travelTime) {
        Circle testCircle = new Circle();
        testCircle.setCenterX(getNodePosX(senderID));
        testCircle.setCenterY(getNodePosY(senderID));
        testCircle.setRadius(15);
        testCircle.setFill(Color.BLUE);

        travelTime = travelTime / 10;

        System.out.println("Node " + senderID + " sending message to node " + reciverID + " with delay " + travelTime + "s");

        TranslateTransition tt = new TranslateTransition(Duration.seconds(travelTime), testCircle);

        Integer startPosX = getNodePosX(senderID);
        Integer startPosY = getNodePosY(senderID);
        Integer endPosX = getNodePosX(reciverID);
        Integer endPosY = getNodePosY(reciverID);

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

    public void drawNetworkCircles() {
        for (Integer i = 1; i <=5; i++) {
            drawNetworkCircle(i);
        }
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
        circle.setCenterX(getNodePosX(id));
        circle.setCenterY(getNodePosY(id));
        circle.setRadius(30);
        circle.setFill(Color.GREEN);
        nodeCircles.put(id, circle);

        Text text = new Text("Node: " + id);
        text.setX(getNodePosX(id) - 19);
        text.setY(getNodePosY(id) + 40);

        pane.getChildren().addAll(circle, text);
    }
}
