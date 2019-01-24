package Client;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class LobbyBrowser extends JDialog {

	public JPanel contentPane;
	public JTextField txtGuest;
    JPanel topPanel;
    JPanel lobbyBrowserPanel;
    JPanel titleLabelPanel;
    JLabel label;
    JPanel nickPanel;
    JLabel lblNick;
    JScrollPane lobbyScrollPane;
    JTable lobbyTable;
    JPanel buttonPanel;
    JPanel leftButtonPanel;
    JButton btnJoin;
    JButton btnPreview;
    JButton btnDirectConnect;
    JPanel rightButtonPanel;
    JButton btnCreate;

    //DefaultListModel model;
    DefaultTableModel model;
    DrawClient client;

    LobbyJoinDialog joinDialog;
    CreateLobbyDialog createLobbyDialog;

    List<LobbyEntry> lobbyEntryStore;

    /**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LobbyBrowser frame = new LobbyBrowser(null);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}


	public LobbyBrowser(DrawClient client) {

        this.client = client;
        model = new DefaultTableModel(new Object[] {"ID", "Name", "Players", "Public/Private", "Gamemode"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
		initGui();

	}

    /**
     * Create the frame.
     */
	public void initGui() {
	    setModal(true);
        //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 748, 538);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        setContentPane(contentPane);
        contentPane.setLayout(new BorderLayout(0, 0));

        lobbyBrowserPanel = new JPanel();
        contentPane.add(lobbyBrowserPanel, BorderLayout.CENTER);
        lobbyBrowserPanel.setLayout(new BorderLayout(0, 0));

        topPanel = new JPanel();
        lobbyBrowserPanel.add(topPanel, BorderLayout.NORTH);
        topPanel.setLayout(new BorderLayout(0, 0));

        titleLabelPanel = new JPanel();
        topPanel.add(titleLabelPanel, BorderLayout.WEST);

        label = new JLabel("Game Browser: ");
        label.setFont(new Font("Tahoma", Font.PLAIN, 18));
        titleLabelPanel.add(label);

        nickPanel = new JPanel();
        topPanel.add(nickPanel, BorderLayout.EAST);

        lblNick = new JLabel("Nick: ");
        nickPanel.add(lblNick);

        txtGuest = new JTextField();
        txtGuest.setText("Guest");
        nickPanel.add(txtGuest);
        txtGuest.setColumns(10);

        lobbyScrollPane = new JScrollPane();
        lobbyBrowserPanel.add(lobbyScrollPane, BorderLayout.CENTER);

        lobbyTable = new JTable(model);
        lobbyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        //lobbyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        lobbyTable.setRowSelectionAllowed(true);
        lobbyTable.setAutoCreateRowSorter(true);

        // listener
        lobbyTable.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int col = lobbyTable.columnAtPoint(e.getPoint());
                String name = lobbyTable.getColumnName(col);
                System.out.println("Column index selected " + col + " " + name);
            }
        });

        lobbyTable.getColumnModel().getColumn(0).setMaxWidth(30);

        lobbyTable.getColumnModel().getColumn(2).setMaxWidth(70);
        lobbyTable.getColumnModel().getColumn(3).setMaxWidth(100);
        lobbyTable.getColumnModel().getColumn(4).setMaxWidth(200);

        //lobbyList.setLayoutOrientation(JList.VERTICAL);
//        lobbyList.addListSelectionListener(l -> {
//            btnJoin.setEnabled(lobbyList.getSelectedIndex() >= 0);
//            //LobbyEntry lobbyEntry = (LobbyEntry)model.get(lobbyList.getSelectedIndex());
//        });
        lobbyScrollPane.setViewportView(lobbyTable);

        lobbyEntryStore = new ArrayList<>();

        buttonPanel = new JPanel();
        contentPane.add(buttonPanel, BorderLayout.SOUTH);
        buttonPanel.setLayout(new BorderLayout(0, 0));

        leftButtonPanel = new JPanel();
        buttonPanel.add(leftButtonPanel, BorderLayout.WEST);

        btnJoin = new JButton("Join");
        leftButtonPanel.add(btnJoin);
        btnJoin.setHorizontalAlignment(SwingConstants.LEFT);
        btnJoin.addActionListener(l -> {
            if (lobbyTable.getSelectedRow() <= -1) return;
            LobbyEntry lobbyEntry = lobbyEntryStore.get(lobbyTable.getSelectedRow());
            if (lobbyEntry == null) return;
            if (lobbyEntry.isPrivate) {
                showLobbyPreviewDialog(lobbyEntry);
            }
            else {
                client.sendJoinMessage(lobbyEntry.id, txtGuest.getText());
            }
        });

        btnPreview = new JButton("Preview");
        btnPreview.addActionListener(l -> {
            if (lobbyTable.getSelectedRow() <= -1) return;
            LobbyEntry lobbyEntry = lobbyEntryStore.get(lobbyTable.getSelectedRow());
            showLobbyPreviewDialog(lobbyEntry);
        });
        leftButtonPanel.add(btnPreview);

        btnDirectConnect = new JButton("Direct Connect");
        leftButtonPanel.add(btnDirectConnect);
        //buttonPanel.setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{btnJoin}));

        rightButtonPanel = new JPanel();
        buttonPanel.add(rightButtonPanel, BorderLayout.EAST);

        btnCreate = new JButton("Create");
        btnCreate.addActionListener(l -> showCreateLobbyDialog());
        rightButtonPanel.add(btnCreate);
        Runnable task = () -> {
          setVisible(true);
        };
        SwingUtilities.invokeLater(task);

    }

    public void sortBy(int col) {

    }

    public void showLobbyPreviewDialog(LobbyEntry lobbyEntry) {
	    joinDialog = new LobbyJoinDialog(lobbyEntry, client);
	    joinDialog.setVisible(true);
    }

    public void showCreateLobbyDialog() {
	    createLobbyDialog = new CreateLobbyDialog(client);
	    createLobbyDialog.setVisible(true);
    }

    public LobbyEntry getLobbyEntryById(int id) {
//        for (Object o: model.toArray()) {
//            LobbyEntry lobbyEntry = (LobbyEntry)o;
//            if (lobbyEntry.id == id) return lobbyEntry;
//        }
//        return null;
        for (LobbyEntry lobbyEntry: lobbyEntryStore) {
            if (lobbyEntry.id == id) return lobbyEntry;
        }
        return null;
    }

    public void addLobbyEntry(LobbyEntry lobbyEntry) {
        removeLobbyEntry(lobbyEntry);
        //model.addElement(lobbyEntry);
        lobbyEntryStore.add(lobbyEntry);
        model.addRow(lobbyEntry.toStringArray());
    }

    public void removeLobbyEntry(LobbyEntry lobbyEntry) {
        LobbyEntry l = getLobbyEntryById(lobbyEntry.id);
        if (l == null) return;
        model.removeRow(lobbyEntryStore.indexOf(l));
        lobbyEntryStore.remove(l);
    }

    public void hideAll() {
	    setVisible(false);
	    if (createLobbyDialog != null)
	    createLobbyDialog.setVisible(false);
	    if (joinDialog != null)
	    joinDialog.setVisible(false);
    }
}
