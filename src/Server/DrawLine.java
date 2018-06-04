package Server;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Evan on 7/1/2017.
 */
public class DrawLine {

    List<DrawPoint> points;

    private Color lineColor;

    private int strokeSize;

    public DrawLine() {
        points = new ArrayList<>();
        lineColor = Color.white;
    }

    public DrawLine(Color c, int strokeSize) {
        points = new ArrayList<>();
        lineColor = c;
        this.strokeSize = strokeSize;
    }

    public DrawLine(Color c, List<DrawPoint> points, int strokeSize) {
        this.points = points;
        lineColor = c;
        this.strokeSize = strokeSize;
    }

    public Color getLineColor() {
        return lineColor;
    }

    public void setLineColor(Color c) {
        lineColor = c;
    }

    public int getStrokeSize() {
        return strokeSize;
    }

    public void setStrokeSize(int strokeSize) {
        this.strokeSize = strokeSize;
    }

    @Override
    public String toString() {
        if (points.isEmpty()) return "Empty line";
        return "Points from " + points.get(0) + " to " + points.get(points.size()) + " , amount: " + points.size() + ", Color: " + lineColor + " , Stroke Size: " + strokeSize + ".";
    }


    public int[] getPointsX() {
        int[] pointsX = new int[points.size()];
        for (int i = 0; i < points.size(); i++) {
            pointsX[i] = points.get(i).getX();
        }
        return pointsX;
    }

    public int[] getPointsY() {
        int[] pointsY = new int[points.size()];
        for (int i = 0; i < points.size(); i++) {
            pointsY[i] = points.get(i).getY();
        }
        return pointsY;
    }


}
