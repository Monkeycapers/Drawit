package Server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Evan on 6/27/2017.
 */
public class GUI implements Runnable {

    JFrame jFrame;
    Canvas canvas;

    Thread GUIThread;
    boolean isRunning;

    ArrayList<Integer> pointsY;
    ArrayList<Integer> pointsX;

    ArrayList<int[]> pointsYStore;
    ArrayList<int[]> pointsXStore;

    //Canvas size in pixels
    final int sizeX = 1500;
    final int sizeY = 800;

    ReentrantLock lock = new ReentrantLock();

    int strokeSize = 10;

    boolean acceptingNewLinePaths = true;

    boolean useLineToMode = true;

    boolean lineToModeIsFixed = false;

    Point pointFrom;

    boolean isShiftDown;

    public GUI () {
        //canvasMap = new int[sizeX][sizeY];
        //points = new ArrayList<>();
        pointsX = new ArrayList<>();
        pointsY = new ArrayList<>();
        pointsYStore = new ArrayList<>();
        pointsXStore = new ArrayList<>();
        jFrame = new JFrame();
        jFrame.setLayout(new BorderLayout());
        jFrame.setSize(sizeX, sizeY);
        canvas = new Canvas();
        canvas.setSize(sizeX, sizeY);
        jFrame.add(canvas, BorderLayout.WEST);




        jFrame.setVisible(true);
        canvas.createBufferStrategy(2);
        canvas.addMouseListener(new mousePress());
        canvas.addMouseMotionListener(new mouseMotion());
        canvas.addKeyListener(new keyPress());
        //jFrame.addKeyListener(new keyPress());
        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    public void start() {
        //isRunning = true;
        GUIThread = new Thread(this);
        GUIThread.start();
    }


    public void run() {
        if (!isRunning) {
            isRunning = true;
            while (isRunning) {
                try {
                    Thread.sleep(10);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                lock.lock();
                draw();
                lock.unlock();
            }
        }
    }

    public void draw() {
        try {
            BufferStrategy bufferStrategy = canvas.getBufferStrategy();
            Graphics g = bufferStrategy.getDrawGraphics();
            Graphics2D g2 = (Graphics2D)g;
            g2.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            g2.setColor(Color.black);
            g2.setStroke(new BasicStroke(strokeSize));
//        for (int x = 0; x < canvasMap.length; x ++) {
//            for (int y = 0; y < canvasMap[x].length; y++) {
//                //Convert int to color
//                if (canvasMap[x][y] == 1) {
//                    g.drawRect(x, y, 1, 1);
//                }
//            }
//        }

            int[] pointsXarr = new int[pointsX.size()];
            int[] pointsYarr = new int[pointsY.size()];

            for (int i = 0; i < pointsX.size(); i++) {
                pointsXarr[i] = pointsX.get(i);
                pointsYarr[i] = pointsY.get(i);
            }

            g2.drawPolyline(pointsXarr, pointsYarr, pointsX.size());
            for (int i = 0; i < pointsXStore.size(); i++) {
                g2.drawPolyline(pointsXStore.get(i), pointsYStore.get(i), pointsXStore.get(i).length);
            }

            //g2.setColor(Color.red);
            //g2.setStroke(new BasicStroke(1));

            g2.setColor(Color.gray);
            if (lineToModeIsFixed) {
                if (canvas.getMousePosition() != null) {
                    g2.drawLine((int)pointFrom.getX(), (int)pointFrom.getY(), (int)canvas.getMousePosition().getX(), (int)canvas.getMousePosition().getY());
                }
                if (pointsXStore.size() >= 1 && canvas.getMousePosition() != null) {
                    //Todo: dot product of the two lines find the angle for a arc

                    //Todo: Replace with arctan

                    int dY = (int)canvas.getMousePosition().getY() - (int)pointFrom.getY();
                    int dX = (int)canvas.getMousePosition().getX() - (int)pointFrom.getX();

                    int dY2 = (int)pointsYStore.get(pointsYStore.size() - 1)[1] - (int)pointsYStore.get(pointsYStore.size() - 1)[0];
                    int dX2 = (int)pointsXStore.get(pointsXStore.size() - 1)[1] - (int)pointsXStore.get(pointsXStore.size() - 1)[0];

                    int dotProduct = (dY * dY2) + (dX * dX2);

                    double magA = Math.sqrt((dY * dY) + (dX * dX));
                    double magB = Math.sqrt((dY2 * dY2) + (dX2 * dX2));

                    double angle = Math.acos(dotProduct / (magA * magB));
                    //double angle = Math.atan(magB / magA);

                    //System.out.println("Angle: " + angle * (180 / Math.PI));

                    //jFrame.setTitle("Angle: " + angle * (180 / Math.PI) + " (~ " + Math.round((angle * (180 / Math.PI))) + ")");
                    jFrame.setTitle("Angle: ~= " + Math.round((angle * (180 / Math.PI))));
                }


            }

            g2.setColor(Color.red);
            g2.setStroke(new BasicStroke(1));



            for (int i = 0; i < pointsX.size(); i++) {

                g2.drawRect(pointsXarr[i], pointsYarr[i], 1, 1);
            }

            bufferStrategy.show();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void reset() {
        pointsX = new ArrayList<>();
        pointsY = new ArrayList<>();
        pointsXStore = new ArrayList<>();
        pointsYStore = new ArrayList<>();
    }

    private class keyPress implements KeyListener {
        @Override
        public void keyPressed(KeyEvent e) {
            //System.out.println(e.getKeyCode() == KeyEvent.VK_SHIFT);
            if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                isShiftDown = true;
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                isShiftDown = false;
            }
        }

        @Override
        public void keyTyped(KeyEvent e) {
            //System.out.println("test");
            if (e.getKeyChar() == 'r' || e.getKeyChar() == 'R') {
                Runnable task = () -> {
                    lock.lock();
                    reset();
                    lock.unlock();

                };
                new Thread(task).start();
            }
            else if (e.getKeyChar() == '+') {
                strokeSize += 1;
            }
            else if (e.getKeyChar() == '-') {
                strokeSize -= 1;
            }
            else if (e.getKeyChar() == 'z' ) {
                if (pointsXStore.size() == 0) return;
                Runnable task = () -> {
                    lock.lock();
                    pointsXStore.remove(pointsXStore.size() - 1);
                    pointsYStore.remove(pointsYStore.size() - 1);

                    lock.unlock();

                };
                new Thread(task).start();
            }
            else if (e.getKeyChar() == 'd') {
                //Dump
                for (int[] x: pointsXStore) {
                    for (int xx: x) {
                        System.out.println(xx);
                    }
                    System.out.println("---");
                }
                System.out.println("Y:");
                for (int[] y: pointsYStore) {
                    for (int yy: y) {
                        System.out.println(yy);
                    }
                    System.out.println("---");
                }
            }

        }

    }

    private class mousePress implements MouseListener {
        @Override
        public void mouseClicked(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }

        @Override
        public void mousePressed(MouseEvent e) {

            if (e.getButton() == MouseEvent.BUTTON3 && lineToModeIsFixed) {
                //Cancel
                lineToModeIsFixed = false;
            }
            if (useLineToMode && e.getButton() == MouseEvent.BUTTON1) {
                if (lineToModeIsFixed) {
                    //Add the line to the list'
                    pointsXStore.add(new int[] {(int)pointFrom.getX(), e.getX()});
                    pointsYStore.add(new int[] {(int)pointFrom.getY(), e.getY()});
                    pointFrom = e.getPoint();
                }
                else {
                    pointFrom = new Point(e.getX(), e.getY());
                    lineToModeIsFixed = true;
                }
                //lineToModeIsFixed = !lineToModeIsFixed;
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            //On mouse release, clear PointsX and PointsY
            if (useLineToMode) {
                return;
            }
            Runnable task = () -> {
                lock.lock();

                int[] pointsXarr = new int[pointsX.size()];
                int[] pointsYarr = new int[pointsY.size()];

                for (int i = 0; i < pointsX.size(); i++) {
                    pointsXarr[i] = pointsX.get(i);
                    pointsYarr[i] = pointsY.get(i);
                }

                pointsXStore.add(pointsXarr);
                pointsYStore.add(pointsYarr);



                pointsX = new ArrayList<>();
                pointsY = new ArrayList<>();
                lock.unlock();
            };
            new Thread(task).start();
        }

    }

    private class mouseMotion implements MouseMotionListener {
        @Override
        public void mouseDragged(MouseEvent e) {
            //System.out.println("DRAGGED: (" + e.getX() + "," + e.getY() );
            //canvasMap[e.getX()][e.getY()] = 1;
           // points.add(new Point(e.getX(), e.getY()));
            if (acceptingNewLinePaths && !useLineToMode) {
                Runnable task = () -> {
                    acceptingNewLinePaths = false;
                    lock.lock();
                    pointsX.add(e.getX());
                    pointsY.add(e.getY());
                    lock.unlock();
                    try {
                        Thread.sleep(10);
                    }
                    catch (Exception ee) {
                        ee.printStackTrace();
                    }
                    acceptingNewLinePaths = true;
                };
                new Thread(task).start();
            }
        }
        @Override
        public void mouseMoved(MouseEvent e) {

        }
    }

    public static void main(String[] args) {
        GUI gui = new GUI();
        gui.start();
    }

}
