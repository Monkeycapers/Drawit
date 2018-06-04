package Client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Created by Evan on 7/3/2017.
 */
public class DrawLobbyGui {

    DrawClient client;

    JFrame frame;

    JPanel cardPanel;
    JPanel mainPanel;

    JPanel serverBrowserPanel;
    JPanel topPanel;

    JPanel joinPanel;
    JPanel lobbyPanel;

    JList lobbyList;

    DefaultListModel model;

    JScrollPane lobbyListScrollPane;

    JButton joinButton;
    JButton createButton;

    JButton serverListJoinButton;
    JTextField serverListTextField;

    final int SIZE_X = 400;
    final int SIZE_Y = 400;

    final String MAIN_CARD = "Main";
    final String CREATE_CARD = "Create";
    final String JOIN_CARD = "Join";

    //Object[] listData;

    JButton backButton;

    CardLayout cardLayout;

    public DrawLobbyGui(DrawClient client) {

        this.client = client;
        frame = new JFrame();
        frame.setSize(SIZE_X, SIZE_Y);
        //frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        mainPanel = new JPanel(new BorderLayout());

        serverBrowserPanel = new JPanel();
        serverBrowserPanel.setLayout(new BoxLayout(serverBrowserPanel, BoxLayout.PAGE_AXIS));
        JLabel serverBroswerLabel = new JLabel("Server Browser");
        serverBroswerLabel.setFont(new Font("Sans-Serif", Font.BOLD, 20));
        serverBroswerLabel.setBorder(new EmptyBorder(0, 0, 5, 0));
        serverBrowserPanel.add(serverBroswerLabel, Component.LEFT_ALIGNMENT);
        serverBrowserPanel.setPreferredSize(new Dimension(SIZE_X, SIZE_Y - 75));

//        listData = new Object[]{
//                //new LobbyEntry(1, "AServerName", true, 5, 10)
//        };

        model = new DefaultListModel();
        lobbyList = new JList(model);
        lobbyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lobbyList.setLayoutOrientation(JList.VERTICAL);
        lobbyList.setPreferredSize(new Dimension(SIZE_X - 100, SIZE_Y - 75));
        lobbyList.addListSelectionListener(l -> {
            if (lobbyList.getSelectedIndex() <= -1) return;
            LobbyEntry lobbyEntry = (LobbyEntry)model.get(lobbyList.getSelectedIndex());
            serverListTextField.setEnabled(lobbyEntry.isPrivate);
            serverListJoinButton.setEnabled(true);
        });
        lobbyListScrollPane = new JScrollPane(lobbyList);
        lobbyListScrollPane.setPreferredSize(new Dimension(SIZE_X - 100 , SIZE_Y - 75));
        serverBrowserPanel.add(lobbyListScrollPane, Component.LEFT_ALIGNMENT);


        JPanel listButtons = new JPanel();
        listButtons.setLayout(new BorderLayout());
        serverListJoinButton = new JButton("Join");
        serverListJoinButton.setEnabled(false);
        serverListJoinButton.addActionListener(l -> {
            if (lobbyList.getSelectedIndex() <= -1) return;
            LobbyEntry lobbyEntry = (LobbyEntry)model.get(lobbyList.getSelectedIndex());
            client.sendJoinMessage(lobbyEntry.id, serverListTextField.getText());
            serverListTextField.setText("");

        });
        listButtons.add(serverListJoinButton, BorderLayout.EAST);

        serverListTextField = new JTextField();
        serverListTextField.setEnabled(false);
        serverListTextField.setMaximumSize(new Dimension(100, 30));
        serverListTextField.setPreferredSize(new Dimension(100, 30));
        listButtons.add(serverListTextField, BorderLayout.WEST);
        listButtons.setVisible(true);

        serverBrowserPanel.add(listButtons);


        mainPanel.add(serverBrowserPanel, BorderLayout.SOUTH);

        topPanel = new JPanel();
        topPanel.setPreferredSize(new Dimension(SIZE_X, 75));

        joinButton = new JButton("Join Lobby");
        joinButton.addActionListener(l -> {
            cardLayout.show(cardPanel, JOIN_CARD);
        });
        createButton = new JButton("Create Lobby");


        topPanel.add(joinButton);
        topPanel.add(createButton);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        mainPanel.setVisible(true);

        cardPanel.add(mainPanel, MAIN_CARD);

        joinPanel = new JPanel();
        joinPanel.setLayout(new BoxLayout(joinPanel, BoxLayout.PAGE_AXIS));
        joinPanel.setSize(new Dimension(SIZE_X, SIZE_Y));

        //joinPanel.setLayout(null);

        backButton = new JButton("Back");
        backButton.addActionListener(l -> {
            cardLayout.show(cardPanel, MAIN_CARD);
        });
        joinPanel.add(backButton);
        joinPanel.setVisible(true);
        cardPanel.add(joinPanel, JOIN_CARD);

        frame.add(cardPanel);
        frame.setVisible(true);


    }


    public static void main(String[] args) {
        try {
            // Set System L&F
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        new DrawLobbyGui(null);
    }

}
