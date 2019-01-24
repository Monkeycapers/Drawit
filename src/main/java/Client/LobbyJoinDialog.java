package Client;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class LobbyJoinDialog extends JDialog {

	private JPanel contentPane;
	private JTextField passwordTextField;
	private JTextField nickTextField;
	JLabel lblConnect;
	JScrollPane userScrollPane;
	JList userList;
	JButton btnJoin;
	JLabel lblNick;
	JLabel lblPassword;
	JScrollPane lobbyInfoScrollPane;
	JLabel lblLobbyInfo;

	LobbyEntry lobbyEntry;

	DrawClient client;

	/**
	 * Create the frame.
	 */
	public LobbyJoinDialog(LobbyEntry lobbyEntry, DrawClient client) {

		this.lobbyEntry = lobbyEntry;
		this.client = client;

		//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		setBounds(100, 100, 292, 389);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		lblConnect = new JLabel("Connect:");
		lblConnect.setHorizontalAlignment(SwingConstants.CENTER);
		lblConnect.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblConnect.setBounds(10, 11, 256, 14);
		contentPane.add(lblConnect);
		
		userScrollPane = new JScrollPane();
		userScrollPane.setViewportBorder(null);
		userScrollPane.setBounds(10, 86, 256, 158);
		contentPane.add(userScrollPane);
		
		userList = new JList();
		userScrollPane.setViewportView(userList);
		
		btnJoin = new JButton("Connect");
		btnJoin.setBounds(177, 317, 89, 23);
		btnJoin.addActionListener(l -> {
			client.sendJoinMessage(lobbyEntry.id, nickTextField.getText(), passwordTextField.getText());
			passwordTextField.setText("");
		});
		contentPane.add(btnJoin);
		
		passwordTextField = new JTextField();
		passwordTextField.setBounds(103, 286, 163, 20);
		contentPane.add(passwordTextField);
		passwordTextField.setColumns(10);
		passwordTextField.setEnabled(lobbyEntry.isPrivate);
		
		nickTextField = new JTextField();
		nickTextField.setColumns(10);
		nickTextField.setBounds(103, 255, 163, 20);
		contentPane.add(nickTextField);
		
		lblNick = new JLabel("Nick: ");
		lblNick.setBounds(10, 258, 83, 14);
		contentPane.add(lblNick);
		
		lblPassword = new JLabel("Password: ");
		lblPassword.setBounds(10, 289, 83, 14);
		contentPane.add(lblPassword);
		
		lobbyInfoScrollPane = new JScrollPane();
		lobbyInfoScrollPane.setViewportBorder(new EmptyBorder(0, 0, 0, 0));
		lobbyInfoScrollPane.setBounds(10, 36, 256, 39);
		contentPane.add(lobbyInfoScrollPane);
		
		lblLobbyInfo = new JLabel(lobbyEntry.toString());
		lobbyInfoScrollPane.setViewportView(lblLobbyInfo);

		setModal(true);
		client.sendLobbyInfoMessage(lobbyEntry.id);
	}

	public void refresh (LobbyEntry lobbyEntry) {
		lblLobbyInfo.setText(lobbyEntry.toString());
		this.lobbyEntry = lobbyEntry;
		passwordTextField.setEnabled(lobbyEntry.isPrivate);
	}

//	public void doShow() {
//		//JDialog dialog = new JDialog(this, "" , Dialog.ModalityType.APPLICATION_MODAL);
//
//		JDialog dialog = new JDialog(this, Dialog.ModalityType.APPLICATION_MODAL);
//
//		dialog.setBounds(350, 350, 200, 200);
//		dialog.setVisible(true);
//	}
}
