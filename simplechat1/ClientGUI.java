import java.io.IOException;

import common.ChatIF;

import ocsf.client.ObservableClient;
import client.ChatClient;

/**
 *
 * @author caleb
 */
public class ClientGUI extends javax.swing.JFrame implements ChatIF {

	private static final long serialVersionUID = 1L;
	private javax.swing.JButton btnBlock;
    private javax.swing.JButton btnChannel;
    private javax.swing.JButton btnSend;
    private javax.swing.JButton btnStatus;
    private javax.swing.JComboBox<String> cbStatus;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblUserName;
    private javax.swing.JTextArea txtDisplay;
    private javax.swing.JTextField txtEditable;
	final public static int DEFAULT_PORT = 5555;
	ChatClient client;

	public ClientGUI(String host, int port, String loginid) {
		
		initLookAndFeel();
        initComponents(loginid);
        
        try {
			ObservableClient oc = new ObservableClient(host, port);
			client = new ChatClient(host, port, this, loginid, oc);
		} catch (IOException exception) {
			System.out.println("Error: Can't setup connection!"
					+ " Terminating client.");
			System.exit(1);
		}
        this.setVisible(true);
    }
	
	private void initLookAndFeel() {
		/* Set the Nimbus look and feel */
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ClientGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ClientGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ClientGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ClientGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
	}

	@Override
	public void display(String message) {
		txtDisplay.append(message+"\n");
		txtDisplay.setCaretPosition(txtDisplay.getDocument().getLength()); // scroll to bottom of new text
	}

    private void initComponents(String loginid) {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtDisplay = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        lblUserName = new javax.swing.JLabel();
        cbStatus = new javax.swing.JComboBox<String>();
        txtEditable = new javax.swing.JTextField();
        btnSend = new javax.swing.JButton();
        btnBlock = new javax.swing.JButton();
        btnChannel = new javax.swing.JButton();
        btnStatus = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBorder(new javax.swing.border.MatteBorder(null));

        txtDisplay.setColumns(20);
        txtDisplay.setRows(5);
        jScrollPane1.setViewportView(txtDisplay);

        jLabel1.setText("Logged in as:");

        lblUserName.setText(loginid);

        cbStatus.setModel(new javax.swing.DefaultComboBoxModel<String>(new String[] { "AVAILABLE", "NOT AVAILABLE" }));
        cbStatus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbStatusActionPerformed(evt);
            }
        });

        txtEditable.setText("");

        btnSend.setText("Send");
        btnSend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSendActionPerformed(evt);
            }
        });

        btnBlock.setText("Block");
        btnBlock.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBlockActionPerformed(evt);
            }
        });

        btnChannel.setText("Join Channel");
        btnChannel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnChannelActionPerformed(evt);
            }
        });

        btnStatus.setText("Status");
        btnStatus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStatusActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addComponent(jLabel1)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(lblUserName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(cbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addComponent(txtEditable, javax.swing.GroupLayout.PREFERRED_SIZE, 347, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(btnSend, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel1Layout.createSequentialGroup()
                            .addComponent(btnChannel)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(btnBlock)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(btnStatus))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(lblUserName)
                    .addComponent(cbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSend)
                    .addComponent(txtEditable))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnBlock)
                    .addComponent(btnChannel)
                    .addComponent(btnStatus))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }

    private void accept() {
		
	}
    
	private void send(String message) {
		client.handleMessageFromClientUI(message);
		txtEditable.setText("");
	}

    private void btnSendActionPerformed(java.awt.event.ActionEvent evt) {                                        
        send(txtEditable.getText());
    }                                       

    private void cbStatusActionPerformed(java.awt.event.ActionEvent evt) {                                   
    	if(cbStatus.getSelectedIndex() == 0) {
    		send("#available");
    	} else {
    		send("#notavailable");
    	}
    }                                         

    private void btnChannelActionPerformed(java.awt.event.ActionEvent evt) {                                           
    	send("#channel " + txtEditable.getText());
    }                                          

    private void btnBlockActionPerformed(java.awt.event.ActionEvent evt) {                                         
    	send("#block " + txtEditable.getText());
    }                                        

    private void btnStatusActionPerformed(java.awt.event.ActionEvent evt) {                                          
    	send("#status " + txtEditable.getText());
    }                                         

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        
        String host = "";
		String loginid = "";
		int port = 0; // The port number
		
		try {
			loginid = args[0];
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("ERROR - No login ID specified. Connection aborted");
			System.exit(0);
		}

		try {
			host = args[1];
		} catch (ArrayIndexOutOfBoundsException e) {
			host = "localhost";
		}
		
		try {
			port = Integer.parseInt(args[2]);
		} catch (ArrayIndexOutOfBoundsException e) {
			port = DEFAULT_PORT;
		}
		ClientGUI chat = new ClientGUI(host, port, loginid);
		chat.accept(); // Wait for console data
    }
}
