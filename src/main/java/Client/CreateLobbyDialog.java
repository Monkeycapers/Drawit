package Client;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JCheckBox;
import javax.swing.SwingConstants;
import java.awt.Font;

public class CreateLobbyDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	public JTextField lobbyNameTextField;
	public JTextField usePasswordTextField;
	public JCheckBox chckbxUsePassword;
	public JTextField nickTextField;
	public JLabel lblNick;
	public JLabel lblNewLabel;

	DrawClient client;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			CreateLobbyDialog dialog = new CreateLobbyDialog(null);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public CreateLobbyDialog(DrawClient client) {
		setResizable(false);
		setBounds(100, 100, 266, 216);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		{
			lobbyNameTextField = new JTextField();
			lobbyNameTextField.setBounds(111, 77, 129, 20);
			contentPanel.add(lobbyNameTextField);
			lobbyNameTextField.setColumns(10);

		}
		{
			usePasswordTextField = new JTextField();
			usePasswordTextField.setEnabled(false);
			usePasswordTextField.setBounds(111, 108, 129, 20);
			contentPanel.add(usePasswordTextField);
			usePasswordTextField.setColumns(10);
		}
		{
			JLabel lblName = new JLabel("Lobby Name");
			lblName.setBounds(10, 80, 91, 14);
			contentPanel.add(lblName);
		}

		chckbxUsePassword = new JCheckBox("Use Password");
		chckbxUsePassword.setHorizontalAlignment(SwingConstants.LEFT);
		chckbxUsePassword.setBounds(6, 107, 99, 23);
		chckbxUsePassword.addActionListener( l -> usePasswordTextField.setEnabled(chckbxUsePassword.isSelected()));
		contentPanel.add(chckbxUsePassword);

		nickTextField = new JTextField();
		nickTextField.setBounds(111, 46, 129, 20);
		contentPanel.add(nickTextField);
		nickTextField.setColumns(10);

		lblNick = new JLabel("Nick");
		lblNick.setBounds(10, 49, 91, 14);
		contentPanel.add(lblNick);

		lblNewLabel = new JLabel("Create lobby:");
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lblNewLabel.setBounds(10, 11, 230, 24);
		contentPanel.add(lblNewLabel);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(l -> {
					if (chckbxUsePassword.isSelected()) {
						client.sendCreateMessage(nickTextField.getText(), lobbyNameTextField.getText(), usePasswordTextField.getText());
						usePasswordTextField.setText("");
					}
					else {
						client.sendCreateMessage(nickTextField.getText(), lobbyNameTextField.getText());
					}

				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		setModal(true);
	}
}
