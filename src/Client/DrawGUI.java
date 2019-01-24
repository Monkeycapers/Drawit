package Client;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicBorders;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Evan on 7/1/2017.
 */
public class DrawGUI {

    JFrame jFrame;
    DrawCanvas canvas;

    Thread GUIThread;
    boolean isRunning;

    ArrayList<Integer> pointsY;
    ArrayList<Integer> pointsX;

    ArrayList<int[]> pointsYStore;
    ArrayList<int[]> pointsXStore;
    ArrayList<Color> colorStore;
    ArrayList<Integer> strokeStore;

    Color selectedColor;

    //Canvas size in pixels
    final int sizeX = 1000;
    final int sizeY = 800;

    final int MAX_STROKE_SIZE = 10;

    ReentrantLock lock = new ReentrantLock();

    int strokeSize;

    boolean acceptingNewLinePaths = true;

    boolean useLineToMode = true;

    boolean lineToModeIsFixed = false;

    Point pointFrom;

    boolean isShiftDown;
    boolean isTabDown;
    boolean scoreBoardVisible;

    DrawClient client;

    //True: Drawing
    //False: Guessing
    boolean stateMode;

    JPanel rightPanel;
    JTextPane chatTextArea;
    JTextField chatTextField;
    JScrollPane chatTextAreaScroll;
    JPanel buttonToolBar;
    JButton lineToolButton;
    JButton freeDrawButton;
    JButton undoButton;
    JButton resetButton;
    JButton increaseButton;
    JButton decreaseButton;
    JLabel colorPickerButton;

    CardLayout cardLayout;
    JPanel cardPanel;
    JPanel scorePanel;
    final String CANVAS_PANEL = "Canvas";
    final String SCOREBOARD_PANEL = "Scoreboard";
    final Color BACK_COLOR = Color.WHITE;
    JTable scoreBoardTable;
    JScrollPane scoreScrollPane;
    DefaultTableModel model;

    JColorChooser colorChooser;

    JMenuBar menuBar;
    JMenu fileMenu;
    JMenu lobbyMenu;

    JMenuItem settingsItem;
    JMenuItem exitItem;
    JMenuItem leaveLobbyItem;
    JMenuItem joinCreateLobbyItem;

    Timer countDownTimer;

    int countDownTime;

    String title;

    List<UserEntry> userEntryStore;

    public DrawGUI (DrawClient client) {
        //canvasMap = new int[sizeX][sizeY];
        //points = new ArrayList<>();
        this.client = client;
        pointsX = new ArrayList<>();
        pointsY = new ArrayList<>();
        pointsYStore = new ArrayList<>();
        pointsXStore = new ArrayList<>();
        colorStore = new ArrayList<>();
        strokeStore = new ArrayList<>();
        selectedColor = Color.black;
        strokeSize = 10;
        scoreBoardVisible = false;
        isTabDown = false;
        jFrame = new JFrame();
        jFrame.setLayout(new BorderLayout());
        jFrame.setSize(sizeX + 300 + 50, sizeY);
        jFrame.setResizable(false);
        jFrame.setBackground(BACK_COLOR);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(BACK_COLOR);

        canvas = new DrawCanvas();
        canvas.setSize(sizeX, sizeY);
        canvas.setBackground(BACK_COLOR);

        model = new DefaultTableModel(new Object[]{"ID", "Name", "Score"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        userEntryStore = new ArrayList<>();

        scoreBoardTable = new JTable(model);
        scoreBoardTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        scoreBoardTable.setRowSelectionAllowed(true);
        scoreBoardTable.setAutoCreateRowSorter(true);

        scoreBoardTable.setSize(sizeX, sizeY);

        scoreBoardTable.getSelectionModel().addListSelectionListener(l-> {
            System.out.println("Selected row: " + scoreBoardTable.getSelectedRow());
            jFrame.requestFocus();
        });
        scoreBoardTable.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                jFrame.requestFocus();
            }

            @Override
            public void keyPressed(KeyEvent e) {
                jFrame.requestFocus();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                jFrame.requestFocus();
            }
        });
        scoreBoardTable.getColumnModel().getColumn(0).setMaxWidth(30);
        scoreBoardTable.getColumnModel().getColumn(2).setMaxWidth(150);
        scoreBoardTable.setBackground(BACK_COLOR);

//        scoreBoardTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
//                KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "none");
//        scoreBoardTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
//                KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK), "none");

        scoreScrollPane = new JScrollPane();

        scoreScrollPane.setSize(sizeX, sizeY);
        scoreScrollPane.setBackground(BACK_COLOR);

        scoreScrollPane.setViewportView(scoreBoardTable);

        scorePanel = new JPanel();
        scorePanel.setLayout(null);
        scorePanel.setSize(sizeX, sizeY);
        scorePanel.setBackground(BACK_COLOR);
        scorePanel.add(scoreScrollPane);
        cardPanel.setSize(sizeX, sizeY);
        cardPanel.setBackground(BACK_COLOR);
        cardPanel.add(canvas, CANVAS_PANEL);
        cardPanel.add(scorePanel, SCOREBOARD_PANEL);
        //cardPanel.addKeyListener(new keyPress());
        //cardLayout.show(cardPanel, SCOREBOARD_PANEL);
        //jFrame.add(canvas);

        menuBar = new JMenuBar();

        fileMenu = new JMenu("File");

        settingsItem = new JMenuItem("Settings");
        settingsItem.addActionListener(l -> {
            System.out.println("Settings menu item pressed");
            //Todo

        });
        fileMenu.add(settingsItem);

        exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(l -> {
            System.out.println("Exit menu item pressed");
            System.exit(0);
        });
        fileMenu.add(exitItem);

        menuBar.add(fileMenu);

        lobbyMenu = new JMenu("Lobby");

        joinCreateLobbyItem = new JMenuItem("Lobbies");
        joinCreateLobbyItem.addActionListener(l -> {
            System.out.println("Lobbies menu item pressed");
            client.showLobbyBrowser();
        });
        lobbyMenu.add(joinCreateLobbyItem);

        leaveLobbyItem = new JMenuItem("Leave");
        leaveLobbyItem.addActionListener(l -> {
            System.out.println("Leave lobby pressed");
            client.sendLeaveMessage();
        });
        lobbyMenu.add(leaveLobbyItem);
        menuBar.add(lobbyMenu);

        jFrame.setJMenuBar(menuBar);

        countDownTimer = new Timer(1000, new TimerAction());

        buttonToolBar = new JPanel();
        buttonToolBar.setBackground(BACK_COLOR);
        buttonToolBar.setLayout(new BoxLayout(buttonToolBar, BoxLayout.PAGE_AXIS));
        lineToolButton = new JButton();
        lineToolButton.setIcon(new ImageIcon("resource/LINE_THUMB.jpg"));
        undoButton = new JButton();
        undoButton.setIcon(new ImageIcon("resource/UNDO_THUMB.jpg"));
        resetButton = new JButton();
        resetButton.setIcon(new ImageIcon("resource/RESET_THUMB.jpg"));
        increaseButton = new JButton();
        increaseButton.setIcon(new ImageIcon("resource/PLUS_THUMB.jpg"));
        decreaseButton = new JButton();
        decreaseButton.setIcon(new ImageIcon("resource/MINUS_THUMB.jpg"));
//        colorPickerButton = new JButton () {
//            @Override
//            public void paint(Graphics g) {
//                super.paint(g);
//                g.setColor(selectedColor);
//                g.drawRect(0, 0, 50, 50);
//            }
//        };
        colorChooser = new JColorChooser(selectedColor);
        colorPickerButton = new JLabel();
        colorPickerButton.setBackground(selectedColor);
        colorPickerButton.setOpaque(true);

        colorPickerButton.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!stateMode) return;
                Runnable task = () -> {
                    selectedColor = JColorChooser.showDialog(jFrame, "Pick a draw color", selectedColor);
                    colorPickerButton.setBackground(selectedColor);
                    client.sendDrawColorMessage(selectedColor, strokeSize);
                };
                new Thread(task).start();
            }
            @Override
            public void mousePressed(MouseEvent e) {
            }
            @Override
            public void mouseReleased(MouseEvent e) {
            }
            @Override
            public void mouseEntered(MouseEvent e) {
            }
            @Override
            public void mouseExited(MouseEvent e) {
            }
        });

        //lineToolButton.setBorder(BorderFactory.createEmptyBorder())

        Border line = new LineBorder(Color.GREEN);
        Border margin = new EmptyBorder(5, 15, 5, 15);
        Border compound = new CompoundBorder(line, margin);
        lineToolButton.setBorder(compound);

        lineToolButton.setContentAreaFilled(false);
        lineToolButton.setFocusPainted(false);
        //colorPickerButton.setContentAreaFilled(false);
        //colorPickerButton.setFocusPainted(false);

        lineToolButton.addActionListener(l -> {
            useLineToMode = !useLineToMode;
            updateButtonBorders();
        });
        undoButton.addActionListener(l -> {
            if (stateMode) {
                undo();
                client.sendUndoMessage();
            }
        });
        resetButton.addActionListener(l -> {
            if (stateMode) {
                reset();
                client.sendResetMessage();
            }
        });

        freeDrawButton = new JButton();
        freeDrawButton.setIcon(new ImageIcon("resource/FREEDRAW_THUMB.jpg"));
        //freeDrawButton.setBorder(BorderFactory.createEmptyBorder());
        freeDrawButton.setContentAreaFilled(false);
        freeDrawButton.setFocusPainted(false);

        increaseButton.setContentAreaFilled(false);
        decreaseButton.setContentAreaFilled(false);
        increaseButton.setFocusPainted(false);
        decreaseButton.setFocusPainted(false);

        line = new LineBorder(Color.BLACK);
        margin = new EmptyBorder(5, 15, 5, 15);
        compound = new CompoundBorder(line, margin);
        freeDrawButton.setBorder(compound);
        undoButton.setBorder(compound);
        resetButton.setBorder(compound);
        increaseButton.setBorder(compound);
        decreaseButton.setBorder(compound);
        //colorPickerButton.setBorder(compound);

        undoButton.setContentAreaFilled(false);
        undoButton.setFocusPainted(false);
        resetButton.setContentAreaFilled(false);
        resetButton.setFocusPainted(false);


        freeDrawButton.addActionListener(l -> {
            useLineToMode = !useLineToMode;
            updateButtonBorders();
        });

        increaseButton.addActionListener(l -> {
            if (strokeSize < MAX_STROKE_SIZE) {
                strokeSize ++;
                client.sendDrawColorMessage(selectedColor, strokeSize);
            }
        });
        decreaseButton.addActionListener(l -> {
            if (strokeSize > 0) {
                strokeSize --;
                client.sendDrawColorMessage(selectedColor, strokeSize);
            }
        });

        Dimension d = new Dimension(50, 50);

        lineToolButton.setMaximumSize(d);
        freeDrawButton.setMaximumSize(d);
        lineToolButton.setPreferredSize(d);
        freeDrawButton.setPreferredSize(d);
        undoButton.setPreferredSize(d);
        undoButton.setMaximumSize(d);
        resetButton.setPreferredSize(d);
        resetButton.setMaximumSize(d);
        colorPickerButton.setPreferredSize(d);
        colorPickerButton.setMaximumSize(d);
        increaseButton.setPreferredSize(d);
        increaseButton.setMaximumSize(d);
        decreaseButton.setPreferredSize(d);
        decreaseButton.setMaximumSize(d);

        buttonToolBar.add(lineToolButton, Component.CENTER_ALIGNMENT);
        buttonToolBar.add(freeDrawButton, Component.CENTER_ALIGNMENT);
        buttonToolBar.add(undoButton, Component.CENTER_ALIGNMENT);
        buttonToolBar.add(resetButton, Component.CENTER_ALIGNMENT);

        buttonToolBar.add(increaseButton, Component.CENTER_ALIGNMENT);
        buttonToolBar.add(decreaseButton, Component.CENTER_ALIGNMENT);

        buttonToolBar.add(colorPickerButton, Component.CENTER_ALIGNMENT);

        buttonToolBar.setPreferredSize(new Dimension(50, sizeY));

        buttonToolBar.setVisible(true);

        jFrame.add(buttonToolBar, BorderLayout.WEST);

        rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(BACK_COLOR);

        rightPanel.setPreferredSize(new Dimension(300, sizeY));

        chatTextArea = new JTextPane();
        chatTextArea.setBackground(BACK_COLOR);
        chatTextArea.setEditable(false);

        //chatTextArea.setLineWrap(true);

        //chatTextArea.setEnabled(false);

        Style style = chatTextArea.addStyle("default", null);
        StyleConstants.setForeground(style, Color.BLACK);

        chatTextAreaScroll = new JScrollPane(chatTextArea);
        //chatTextAreaScroll.add(chatTextArea);
        chatTextAreaScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        chatTextAreaScroll.setPreferredSize(new Dimension(200, sizeY - 70));

        rightPanel.add(chatTextAreaScroll, BorderLayout.NORTH);
        chatTextField = new JTextField("");
        chatTextField.addActionListener(e -> {
            client.sendChatMessage(chatTextField.getText());
            chatTextField.setText("");
        });

        rightPanel.add(chatTextField, BorderLayout.SOUTH);

        rightPanel.setVisible(true);

        jFrame.add(rightPanel, BorderLayout.EAST);


        jFrame.setVisible(true);
        //canvas.createBufferStrategy(2);
        canvas.addMouseListener(new mousePress());
        canvas.addMouseMotionListener(new mouseMotion());
        jFrame.addKeyListener(new keyPress());
        jFrame.setFocusTraversalKeysEnabled(false);

        canvas.setVisible(true);
        canvas.setEnabled(true);
        //jFrame.addKeyListener(new keyPress());
        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        stateMode = false;

        jFrame.add(cardPanel);
        jFrame.setFocusable(true);


        //SwingUtilities.invokeLater(client::showLobbyBrowser);

    }

    public void start() {
        //isRunning = true;
//        GUIThread = new Thread(this);
//        GUIThread.start();

    }

    void updateButtonBorders () {
        Border line = new LineBorder(Color.GREEN);
        Border margin = new EmptyBorder(5, 15, 5, 15);
        Border compound = new CompoundBorder(line, margin);
        line = new LineBorder(Color.BLACK);
        Border compound2 = new CompoundBorder(line, margin);
        if (useLineToMode) {
            lineToolButton.setBorder(compound);
            freeDrawButton.setBorder(compound2);
        }
        else {
            lineToolButton.setBorder(compound2);
            freeDrawButton.setBorder(compound);
        }
    }

//    public void addLobbyEntry(LobbyEntry lobbyEntry) {
//
//    }
//
//    public void removeLobbyEntry(LobbyEntry lobbyEntry) {
//
//    }

//    public void run() {
//        if (!isRunning) {
//            isRunning = true;
//            while (isRunning) {
//
//
//                try {
//                    Thread.sleep(10);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                lock.lock();
//                draw();
//
//                lock.unlock();
//            }
//        }
//    }

    private class DrawCanvas extends JPanel {

        public DrawCanvas () {

        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            lock.lock();
            draw(g);
            lock.unlock();
        }
    }

    public void draw(Graphics g) {
        try {
            //BufferStrategy bufferStrategy = canvas.getBufferStrategy();
            //Graphics g = bufferStrategy.getDrawGraphics();
            Graphics2D g2 = (Graphics2D)g;
            g2.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

            g2.setColor(Color.white);
            g2.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

            g2.setStroke(new BasicStroke(strokeSize));

            if (pointsXStore.size() > 0) {

//                System.out.println("-----------");
//                System.out.println("PointsXStore size: " + pointsXStore.size());
                for (int i = 0; i < pointsXStore.size(); i++) {
//                    for (int j = 0; j < pointsXStore.get(i).length; j++) {
//                        System.out.println(pointsXStore.get(i)[j]);
//                        System.out.println(pointsYStore.get(i)[j]);
//                    }
                    g2.setColor(colorStore.get(i));
                    g2.setStroke(new BasicStroke(strokeStore.get(i)));
                    g2.drawPolyline(pointsXStore.get(i), pointsYStore.get(i), pointsXStore.get(i).length);
//                    for (int x = 0; x <= pointsXStore.get(i).length - 1; x ++) {
//                        //g2.fillOval(pointsXStore.get(i)[x], pointsYStore.get(i)[x], 10, 10);
//                    }
                }
                //System.out.println("-----------");
            }

            //g2.setColor(Color.red);
            //g2.setStroke(new BasicStroke(1));

            g2.setColor(selectedColor);

            g2.setStroke(new BasicStroke(strokeSize));
            int[] pointsXarr = new int[pointsX.size()];
            int[] pointsYarr = new int[pointsY.size()];

            for (int i = 0; i < pointsX.size(); i++) {
                pointsXarr[i] = pointsX.get(i);
                pointsYarr[i] = pointsY.get(i);
            }
            g2.drawPolyline(pointsXarr, pointsYarr, pointsX.size());

            g2.setColor(Color.gray);
            if (lineToModeIsFixed) {
                if (canvas.getMousePosition() != null) {
                    g2.drawLine((int)pointFrom.getX(), (int)pointFrom.getY(), (int)canvas.getMousePosition().getX(), (int)canvas.getMousePosition().getY());
                }
                if (pointsXStore.size() >= 1 && canvas.getMousePosition() != null) {
                    //Todo: dot product of the two lines find the angle for a arc

                    //Todo: Replace with arctan

//                    int dY = (int)canvas.getMousePosition().getY() - (int)pointFrom.getY();
//                    int dX = (int)canvas.getMousePosition().getX() - (int)pointFrom.getX();
//
//                    int dY2 = (int)pointsYStore.get(pointsYStore.size() - 1)[1] - (int)pointsYStore.get(pointsYStore.size() - 1)[0];
//                    int dX2 = (int)pointsXStore.get(pointsXStore.size() - 1)[1] - (int)pointsXStore.get(pointsXStore.size() - 1)[0];
//
//                    int dotProduct = (dY * dY2) + (dX * dX2);
//
//                    double magA = Math.sqrt((dY * dY) + (dX * dX));
//                    double magB = Math.sqrt((dY2 * dY2) + (dX2 * dX2));
//
//                    double angle = Math.acos(dotProduct / (magA * magB));
                    //double angle = Math.atan(magB / magA);

                    //System.out.println("Angle: " + angle * (180 / Math.PI));

                    //jFrame.setTitle("Angle: " + angle * (180 / Math.PI) + " (~ " + Math.round((angle * (180 / Math.PI))) + ")");
                    //jFrame.setTitle("Angle: ~= " + Math.round((angle * (180 / Math.PI))));
                }


            }

//            g2.setColor(Color.red);
//            g2.setStroke(new BasicStroke(1));
//
//
//
//            for (int i = 0; i < pointsX.size(); i++) {
//
//                g2.drawRect(pointsXarr[i], pointsYarr[i], 1, 1);
//            }

            //bufferStrategy.show();

           // menuBar.repaint();


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
        colorStore = new ArrayList<>();
        strokeStore = new ArrayList<>();
        canvas.repaint();
    }

    public void startDrawing() {
        stateMode = true;
        reset();
        canvas.repaint();
    }

    public void startGuessing() {
        stateMode = false;
        reset();
        canvas.repaint();
    }

    public void addLine(int[] x, int[] y, Color color, int strokeSize) {

        pointsX = new ArrayList<>();
        pointsY = new ArrayList<>();

        pointsXStore.add(x);
        pointsYStore.add(y);
        colorStore.add(color);
        strokeStore.add(strokeSize);
        canvas.repaint();
    }

    public void addPoint(int x, int y, Color color, int strokeSize) {
        pointsX.add(x);
        pointsY.add(y);
        selectedColor = color;
        this.strokeSize = strokeSize;
        colorPickerButton.setBackground(selectedColor);
        canvas.repaint();
    }

    public void addChatMessage(String text) {
        try {
            Style style = chatTextArea.getStyle("default");
            StyleConstants.setForeground(style, Color.BLACK);
            chatTextArea.getStyledDocument().insertString(chatTextArea.getStyledDocument().getLength(), "\n" + text, style);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        //chatTextArea.setText(chatTextArea.getText() + "\n" + text);
        //chatTextArea.append("\n" + text);
    }

    public void setTitle(String s) {
        this.title = s;
        jFrame.setTitle(s);
    }

    public void setCountDown(int time) {
        countDownTimer.stop();
        countDownTime = time;
        countDownTimer.start();
    }

    public UserEntry getUserEntryById(int id) {
        for (UserEntry userEntry: userEntryStore) {
            if (userEntry.id == id) return userEntry;
        }
        return null;
    }

    public void addUserEntry(UserEntry userEntry) {
        removeUserEntry(userEntry);
        //model.addElement(lobbyEntry);
        userEntryStore.add(userEntry);
        model.addRow(userEntry.toStringArray());
    }

    public void removeUserEntry(UserEntry userEntry) {
        UserEntry l = getUserEntryById(userEntry.id);
        if (l == null) return;
        model.removeRow(userEntryStore.indexOf(l));
        userEntryStore.remove(l);
    }

    private class keyPress implements KeyListener {
        @Override
        public void keyPressed(KeyEvent e) {
            //System.out.println(e.getKeyCode() == KeyEvent.VK_TAB);
            if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                isShiftDown = true;
            }
            else if (e.getKeyCode() == KeyEvent.VK_TAB) {
                if (!isTabDown) {
                    if (scoreBoardVisible) {
                        cardLayout.show(cardPanel, CANVAS_PANEL);
                    }
                    else {
                        cardLayout.show(cardPanel, SCOREBOARD_PANEL);
                    }
                    scoreBoardVisible = !scoreBoardVisible;
                    isTabDown = true;
                }
            }
//            else if (e.getKeyCode() == KeyEvent.VK_TAB) {
//                cardLayout.show(cardPanel, SCOREBOARD_PANEL);
//            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                isShiftDown = false;
            }
            else if (e.getKeyCode() == KeyEvent.VK_TAB) {
                //cardLayout.show(cardPanel, CANVAS_PANEL);
                isTabDown = false;
            }
        }

        @Override
        public void keyTyped(KeyEvent e) {
            //System.out.println("test");
            //System.out.println(e.getKeyChar());
            if ((e.getKeyChar() == 'r' || e.getKeyChar() == 'R') && stateMode) {
                Runnable task = () -> {
                    lock.lock();
                    reset();
                    lock.unlock();

                };
                new Thread(task).start();
                client.sendResetMessage();
            }
            else if (e.getKeyChar() == '+') {
                strokeSize += 1;
            }
            else if (e.getKeyChar() == '-') {
                strokeSize -= 1;
            }
            else if (e.getKeyChar() == 'z' ) {
                if (!stateMode) return;
                undo();
                client.sendUndoMessage();
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

    public void undo() {
        if (pointsXStore.size() == 0) return;
        Runnable task = () -> {
            lock.lock();

            pointsXStore.remove(pointsXStore.size() - 1);
            pointsYStore.remove(pointsYStore.size() - 1);
            colorStore.remove(colorStore.size() - 1);
            strokeStore.remove(strokeStore.size() - 1);

            lock.unlock();

            canvas.repaint();

        };
        new Thread(task).start();
    }

    private class mousePress implements MouseListener {
        @Override
        public void mouseClicked(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {
            jFrame.requestFocus();
        }

        @Override
        public void mouseExited(MouseEvent e) {
            chatTextField.requestFocus();
        }

        @Override
        public void mousePressed(MouseEvent e) {
            //cardPanel.grabFocus();
            if (!stateMode) return;
            if (e.getButton() == MouseEvent.BUTTON3 && lineToModeIsFixed) {
                //Cancel
                lineToModeIsFixed = false;
            }
            if (useLineToMode && e.getButton() == MouseEvent.BUTTON1) {
                if (lineToModeIsFixed) {
                    //Add the line to the list'

                    int[] X = new int[] {(int)pointFrom.getX(), e.getX()};
                    int[] Y = new int[] {(int)pointFrom.getY(), e.getY()};

                    pointsXStore.add(X);
                    pointsYStore.add(Y);
                    colorStore.add(selectedColor);
                    strokeStore.add(strokeSize);


                    client.sendLine(X, Y, selectedColor, strokeSize);

                    pointFrom = e.getPoint();
                }
                else {
                    pointFrom = new Point(e.getX(), e.getY());
                    lineToModeIsFixed = true;
                }
                //lineToModeIsFixed = !lineToModeIsFixed;
            }
            canvas.repaint();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (!stateMode) return;
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
                colorStore.add(selectedColor);
                strokeStore.add(strokeSize);

                client.sendLine(pointsXarr, pointsYarr, selectedColor, strokeSize);

                pointsX = new ArrayList<>();
                pointsY = new ArrayList<>();
                lock.unlock();
            };
            new Thread(task).start();
            canvas.repaint();
        }

    }

    private class mouseMotion implements MouseMotionListener {
        @Override
        public void mouseDragged(MouseEvent e) {
            //System.out.println("DRAGGED: (" + e.getX() + "," + e.getY() );
            //canvasMap[e.getX()][e.getY()] = 1;
            // points.add(new Point(e.getX(), e.getY()));
            if (acceptingNewLinePaths && !useLineToMode && stateMode) {
                Runnable task = () -> {
                    acceptingNewLinePaths = false;
                    lock.lock();
                    pointsX.add(e.getX());
                    pointsY.add(e.getY());
                    lock.unlock();

                    client.sendPoint(e.getX(), e.getY());

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
            canvas.repaint();
        }
        @Override
        public void mouseMoved(MouseEvent e) {

            canvas.repaint();
        }
    }

    private class TimerAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            countDownTime--;
            jFrame.setTitle(title + " | Time remaining: " + countDownTime + " Seconds" );
            if (countDownTime <= 0) {
                countDownTimer.stop();
            }
        }
    }
}
