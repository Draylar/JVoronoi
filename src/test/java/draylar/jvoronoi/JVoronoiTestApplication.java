package draylar.jvoronoi;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;


public class JVoronoiTestApplication extends Application {

    private final int canvasOffsetX = 200;
    private final int canvasOffsetY = 200;

    @Override
    public void start(Stage stage) throws Exception {
        Canvas canvas = new Canvas(800, 800);
        Canvas highlight = new Canvas(800, 800);

        // Render Voronoi
        GraphicsContext context = canvas.getGraphicsContext2D();
        GraphicsContext highlightContext = highlight.getGraphicsContext2D();

        // display voronoi
        JVoronoi voronoi = new JVoronoi(0, 80);
        for(int x = -200; x < 400; x++) {
            for (int y = -200; y < 400; y++) {
                double eval = voronoi.tesselateWithEdge(x, y, 5);
                context.setFill(Color.color(eval, eval, eval));
                context.fillRect(x + canvasOffsetX, y + canvasOffsetY, 1, 1);
            }
        }

        // render features
        for (Position feature : voronoi.getFeatures()) {
            context.setFill(Color.RED);
            context.fillOval(feature.x + canvasOffsetX, feature.y + canvasOffsetY, 5, 5);
        }

        // Setup UI
        Pane root = new Pane();
        root.getChildren().add(canvas);
        root.getChildren().add(highlight);
        Scene scene = new Scene(root, 800, 800);
        stage.setScene(scene);
        stage.show();

        root.setOnMouseMoved(event -> {
            double x = event.getSceneX() - canvasOffsetX;
            double y = event.getSceneY() - canvasOffsetY;

            Position nearestFeature = voronoi.getNearestFeature(x, y, false);
            Position secondNearestFeature = voronoi.getNearestFeature(x, y, true);

            Platform.runLater(() -> {
                highlightContext.setFill(Color.TRANSPARENT);
                highlightContext.clearRect(0, 0, canvas.getWidth() * 5, canvas.getHeight() * 5);

                // BLUE IS NEAREST
                highlightContext.setFill(Color.BLUE);
                highlightContext.fillOval(nearestFeature.x + 5 + canvasOffsetX, nearestFeature.y + canvasOffsetY, 5, 5);

                // ORANGE IS SECOND NEAREST
                highlightContext.setFill(Color.ORANGE);
                highlightContext.fillOval(secondNearestFeature.x - 5 + canvasOffsetX, secondNearestFeature.y + canvasOffsetY, 5, 5);

                voronoi.getSurroundingFeatures(x, y).forEach(it -> {
                    double distance = Math.sqrt(Math.pow(it.x - x, 2) + Math.pow(it.y - y, 2));
                    highlightContext.fillText(String.format("%.2f", distance), it.x + canvasOffsetX, it.y - 10 + canvasOffsetY);
                });
            });
        });
    }
}
