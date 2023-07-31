/**
 * @file FirmwareUpdateUI.java
 *
 */
/* Copyright (C) 2022 by Arjan van Vught mailto:info@gd32-dmx.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.gd32.dmx;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class FirmwareUpdateUI extends JFrame {
	private static final long serialVersionUID = -4796794206089271499L;

	private final int BUFFERSIZE = 1024;
	private final int PORT = 0x2905;
	private DatagramSocket socketReceive = null;

	private JComponent contentPane;
	private JScrollPane scrollPane;
	private JTree tree;
	private DefaultTreeModel model;
	private JMenuItem mntmExit;
	private JLabel lblStatus;

	private TreeMap<Integer, Node> treeMap;
	private JScrollPane scrollPaneTextArea;
	private JTextArea textArea;
	private JMenu mnView;
	private JMenuItem mntmExpandAll;
	private JMenuItem mntmCollapseAll;
	private JMenuItem mntmRefresh;
	private JMenu mnAction;
	private JMenu mnRun;
	private JMenu mnHelp;
	private JMenuItem mntmAbout;
	private JMenuItem mntmClientTFTP;
	private JMenuItem mntmDisplayOnOff;
	private JMenuItem mntmServerTFTP;
	private JMenuItem mntmVersion;
	private JMenu mnWorkflow;
	private JMenuItem mntmFirmwareInstallation;
	private JMenu mnNetwork;
	private JMenuItem mntmSelectInterface;
	
	private static InterfaceAddress interfaceAddress;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					NetworkInterfaces networkInterfaces = new NetworkInterfaces();
					FirmwareUpdateUI.interfaceAddress = networkInterfaces.getInterfaceAddress();
					FirmwareUpdateUI frame = new FirmwareUpdateUI();
					frame.setVisible(true);
					frame.constructTree();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public FirmwareUpdateUI() {
		System.out.println(System.getProperty("os.name"));

		setTitle(interfaceAddress.getAddress());

		createSocket();

		initComponents();
		createEvents();
	}

	private void initComponents() {
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 408, 350);
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		mntmExit = new JMenuItem("Exit");
		mntmExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.ALT_DOWN_MASK));
		mnFile.add(mntmExit);
		
		mnAction = new JMenu("Action");
		menuBar.add(mnAction);
		
		mntmDisplayOnOff = new JMenuItem("Display On/Off");
		mntmDisplayOnOff.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK));
		mnAction.add(mntmDisplayOnOff);
		
		mntmServerTFTP = new JMenuItem("TFTP Server On/Off");
		mntmServerTFTP.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK));
		mnAction.add(mntmServerTFTP);
		
		mntmVersion = new JMenuItem("Version");
		mntmVersion.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
		mnAction.add(mntmVersion);
		
		mnRun = new JMenu("Run");
		menuBar.add(mnRun);
		
		mntmClientTFTP = new JMenuItem("TFTP Client");
		mntmClientTFTP.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.ALT_DOWN_MASK));
		mnRun.add(mntmClientTFTP);
		
		mnView = new JMenu("View");
		menuBar.add(mnView);
		
		mntmExpandAll = new JMenuItem("Expand All");
		mntmExpandAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.ALT_DOWN_MASK));
		mnView.add(mntmExpandAll);
		
		mntmCollapseAll = new JMenuItem("Collapse All");
		mntmCollapseAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.ALT_DOWN_MASK));
		mnView.add(mntmCollapseAll);
		
		mntmRefresh = new JMenuItem("Refresh");
		mntmRefresh.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.ALT_DOWN_MASK));
		mnView.add(mntmRefresh);
		
		mnWorkflow = new JMenu("Workflow");
		menuBar.add(mnWorkflow);
		
		mntmFirmwareInstallation = new JMenuItem("Firmware installation");
		mnWorkflow.add(mntmFirmwareInstallation);
		
		mnNetwork = new JMenu("Network");
		menuBar.add(mnNetwork);
		
		mntmSelectInterface = new JMenuItem("Select Interface");
		mntmSelectInterface.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.ALT_DOWN_MASK));
		mnNetwork.add(mntmSelectInterface);
		
		mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);
		
		mntmAbout = new JMenuItem("About");
		mntmAbout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK));
		mnHelp.add(mntmAbout);

		contentPane = new JPanel();
		contentPane.setToolTipText("Make sure that this application is in the same network as the nodes");

		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		
		tree = new JTree();
		tree.setToolTipText("Press ALT-R for refresh");
		tree.setRootVisible(false);
		tree.setModel(model);
		
		scrollPane.setViewportView(tree);
		
		lblStatus = new JLabel("No Status");
		
		scrollPaneTextArea = new JScrollPane();
		
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
						.addGroup(Alignment.LEADING, gl_contentPane.createSequentialGroup()
							.addContainerGap()
							.addComponent(lblStatus, GroupLayout.DEFAULT_SIZE, 276, Short.MAX_VALUE))
						.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE)
						.addComponent(scrollPaneTextArea, GroupLayout.DEFAULT_SIZE, 282, Short.MAX_VALUE))
					.addGap(10))
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addContainerGap()
					.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 199, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(scrollPaneTextArea, GroupLayout.DEFAULT_SIZE, 57, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblStatus))
		);
		
		textArea = new JTextArea();
		textArea.setEditable(false);
		scrollPaneTextArea.setViewportView(textArea);
		
		contentPane.setLayout(gl_contentPane);
	}

	private void createEvents() {
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doExit();
			}
		});
		
		mntmExpandAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int row = tree.getRowCount() - 1;
				while (row >= 0) {
					tree.expandRow(row);
					row--;
				}
			}
		});
		
		mntmCollapseAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = tree.getRowCount() - 1;
				while (row >= 0) {
					tree.collapseRow(row);
					row--;
				}
			}
		});
		
		mntmRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				constructTree();
			}
		});
		
		mntmAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				About about = new About();
				String buildNumber = FirmwareUpdateUI.class.getPackage().getImplementationVersion();
				about.setBuildNumber(buildNumber);
				about.setVisible(true);
			}
		});
		
		mntmDisplayOnOff.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final TreePath path = tree.getSelectionPath();
				
				if (path != null) {
					if (path.getPathCount() == 2) {
						final DefaultMutableTreeNode treeNnode = (DefaultMutableTreeNode) path.getPathComponent(1);
						final Node node = (Node) treeNnode.getUserObject();
						final String display = requestUDP(node, "?display#");
						
						if (display.contains("On")) {
							int n = JOptionPane.showConfirmDialog(null, "Display is On\nSet display Off? ", node.toString(), JOptionPane.OK_CANCEL_OPTION);
							sendUDP(node, "!display#" + (n == JOptionPane.OK_OPTION ? "0" : "1"));
						} else {
							int n = JOptionPane.showConfirmDialog(null, "Display is Off\nSet display On? ", node.toString(), JOptionPane.OK_CANCEL_OPTION);
							sendUDP(node, "!display#" + (n != JOptionPane.OK_OPTION ? "0" : "1"));
						}
					}
				} else {
					JOptionPane.showMessageDialog(null, "No node selected for display action.");
				}
			}
		});
		
		mntmServerTFTP.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final TreePath path = tree.getSelectionPath();
				
				if (path != null) {
					if (path.getPathCount() == 2) {
						final DefaultMutableTreeNode treeNnode = (DefaultMutableTreeNode) path.getPathComponent(1);
						final Node node = (Node) treeNnode.getUserObject();
						final String tftpServer = requestUDP(node, "?tftp#");
						
						if (tftpServer.contains("On")) {
							int n = JOptionPane.showConfirmDialog(null, "TFTP Server is On\nSet TFTP Server Off? ", node.toString(), JOptionPane.OK_CANCEL_OPTION);
							sendUDP(node, "!tftp#" + (n == JOptionPane.OK_OPTION ? "0" : "1"));
						} else {
							int n = JOptionPane.showConfirmDialog(null, "TFTP Server is Off\nSet TFTP Server On? ", node.toString(), JOptionPane.OK_CANCEL_OPTION);
							sendUDP(node, "!tftp#" + (n != JOptionPane.OK_OPTION ? "0" : "1"));
						}
					}
				} else {
					JOptionPane.showMessageDialog(null, "No node selected for TFTP Server action.");
				}
			}
		});
		
		mntmVersion.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final TreePath path = tree.getSelectionPath();
				
				if (path != null) {
					if (path.getPathCount() == 2) {
						final DefaultMutableTreeNode treeNnode = (DefaultMutableTreeNode) path.getPathComponent(1);
						final Node node = (Node) treeNnode.getUserObject();
						final String version = requestUDP(node, "?version#");
						JOptionPane.showMessageDialog(null, node + "\n" + version);
					}
				} else {
					JOptionPane.showMessageDialog(null, "No node selected for version action.");
				}
			}
		});
		
		mntmClientTFTP.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final TreePath path = tree.getSelectionPath();

				if (path != null) {
					if (path.getPathCount() == 2) {
						final DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) path.getPathComponent(1);
						final Node node = (Node) treeNode.getUserObject();
						try {
							TFTPClient client = new TFTPClient("", InetAddress.getByName(node.getIpAdress()), getInterfaceAddress());
							client.setVisible(true);
						} catch (UnknownHostException e1) {
							e1.printStackTrace();
						}
					}
				} else {
					JOptionPane.showMessageDialog(null, "No node selected for TFTP Client to run.");
				}
			}
		});
		
		mntmFirmwareInstallation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final TreePath path = tree.getSelectionPath();

				if (path != null) {
					if (path.getPathCount() == 2) {
						final DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) path.getPathComponent(1);
						final Node node = (Node) treeNode.getUserObject();
						
						if (!node.getOutputName().equals("Config")) {
							JOptionPane.showMessageDialog(null, "Node selected is not a bootloader TFTP.");
							return;
						}
						
						try {
							FirmwareInstallation firmware = new FirmwareInstallation(node, getFirmwareUpdate(), interfaceAddress);
							firmware.setVisible(true);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				} else {
					JOptionPane.showMessageDialog(null, "No node selected for Workflow Firmware action.");
				}
			}
		});
		
		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				final int x = e.getX();
				final int y = e.getY();

				TreePath path = tree.getPathForLocation(x, y);

				if (path != null) {
					final DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) path.getPathComponent(1);
					final Node node = (Node) treeNode.getUserObject();

					if (isRightClick(e)) {
						final int cnt = path.getPathCount();
						System.out.println("Right ->" + cnt);
						if (cnt == 2) {
							if (node.isBootLoader()) {
								doReboot(node);
							} else {
								doSwitchToBootLoader(node);
							}
						}
					}
				} else {
					System.err.printf("tree.getPathForLocation(%d, %d)=null\n", x, y);
				}
			}
		});
		
		mntmSelectInterface.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doInterfaces();
			}
		});
	}
	
	private InterfaceAddress getInterfaceAddress() {
		return FirmwareUpdateUI.interfaceAddress;
	}
	
	private void doInterfaces() {
		NetworkInterfaces networkInterfaces;
		networkInterfaces = new NetworkInterfaces(this);
		networkInterfaces.Show();
	}
	
	private FirmwareUpdateUI getFirmwareUpdate() {
		return this;
	}
	
	private void doSwitchToBootLoader(Node node) {
		int n = JOptionPane.showConfirmDialog(null, "Switch to bootloader TFTP", node.toString(), JOptionPane.OK_CANCEL_OPTION);
		if (n == JOptionPane.OK_OPTION) {
			sendUDP(node, "!tftp#1");
			String response = requestUDP(node, "?tftp#");
			boolean isEnabled = response.contains("On");
			
			if (isEnabled) {
				lblStatus.setForeground(Color.GREEN);
			} else {
				lblStatus.setForeground(Color.ORANGE);
				response = requestUDP(node, "?tftp#");
				isEnabled = response.contains("On");
				
				if (isEnabled) {
					lblStatus.setForeground(Color.GREEN);
				} else {
					lblStatus.setForeground(Color.RED);
				}
			}
			
			lblStatus.setText(response);
			
			if (isEnabled) {
				sendUDP(node, "?reboot##");
				try {
					String version;
					Graphics g = getGraphics();
					do {
						textArea.append("|");
						update(g);
						Thread.sleep(1000);
						textArea.append("/");
						update(g);
						version = requestUDP(node, "?version#");
						textArea.append("-");
						update(g);
					} while (version.contains("ERROR"));
					textArea.setText("");
					constructTree();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void doReboot(Node node) {
		int n = JOptionPane.showConfirmDialog(null, "Reboot", node.toString(), JOptionPane.OK_CANCEL_OPTION);
		if (n == JOptionPane.OK_OPTION) {
			try {
				sendUDP(node, "?reboot##");
				JOptionPane.showMessageDialog(null, "Reboot message has been sent.");
				String version;
				Graphics g = getGraphics();
				do {
					textArea.append("|");
					update(g);
					Thread.sleep(1000);
					textArea.append("/");
					update(g);
					version = requestUDP(node, "?version#");
					textArea.append("-");
					update(g);
				} while (version.contains("ERROR"));
				textArea.setText("");
				constructTree();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private boolean isRightClick(MouseEvent e) {
		return (e.getButton() == MouseEvent.BUTTON3 || (System.getProperty("os.name").contains("Mac OS X")
				&& (e.getModifiers() & InputEvent.BUTTON1_MASK) != 0
				&& (e.getModifiers() & InputEvent.CTRL_MASK) != 0));
	}
	
	public void setTitle(InetAddress inetAddress) {
		String text = inetAddress.getHostAddress();
		setTitle("Firmware Update Manager " + text);
	}
	
	public void setInterfaceAddress(InterfaceAddress interfaceAddress) {
		FirmwareUpdateUI.interfaceAddress = interfaceAddress;
		createSocket();
	}
	
	public void constructTree() {
		Graphics g = getGraphics();

		lblStatus.setText("Searching ...");
		lblStatus.setForeground(Color.RED);

		update(g);

		DefaultMutableTreeNode root = new MyDefaultMutableTreeNode("Root");
		DefaultMutableTreeNode child = null;

		treeMap = new TreeMap<Integer, Node>();
		
		for (int i = 0; i < 1; i++) {
			try {
				broadcast("?list#");
				while (true) {
					byte[] buffer = new byte[BUFFERSIZE];
					DatagramPacket dpack = new DatagramPacket(buffer, buffer.length);
					socketReceive.receive(dpack);

					textArea.append(dpack.getAddress().toString() + "\n");
					update(g);

					String str = new String(dpack.getData());
					final String data[] = str.split("\n");

					Node node = new Node(data[0]);

					if (node.isValid()) {
						treeMap.put(ByteBuffer.wrap(dpack.getAddress().getAddress()).getInt(), node);
					}
				}
			} catch (SocketTimeoutException e) {
				lblStatus.setText("No replies");
				lblStatus.setForeground(Color.GREEN);
				update(g);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if (!treeMap.isEmpty()) {
			textArea.setText("");

			Set<Map.Entry<Integer, Node>> entries = treeMap.entrySet();

			for (Map.Entry<Integer, Node> entry : entries) {
				final Node node = entry.getValue();
				child = new DefaultMutableTreeNode(node);
				
				if ((node.getDisplayName() != null) && (node.getDisplayName().trim().length() != 0)) {
					child.add(new DefaultMutableTreeNode(node.getDisplayName()));
				}
				
				final String outputName = node.getOutputName();
				
				if (!outputName.equals("Config")) {
					child.add(new DefaultMutableTreeNode(outputName));
				}
				root.add(child);
			}
		} else {
			textArea.setText("No Node's found\n");
		}
		
		if (tree == null) {
			tree = new JTree(new DefaultTreeModel(root));
		} else {
			tree.setModel(new DefaultTreeModel(root));
		}

		update(g);
	}
	
	private void doExit() {
		System.exit(0);
	}

	public void broadcast(String broadcastMessage) {
		byte[] buffer = broadcastMessage.getBytes();
		try {
			final DatagramPacket packet = new DatagramPacket(buffer, buffer.length,
					InetAddress.getByName("255.255.255.255"), PORT);
			try {
				socketReceive.send(packet);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (UnknownHostException u) {
			u.printStackTrace();
		}
	}
	
	public void sendUDP(Node node, String message) {
		System.out.println(node.getIpAdress() + ":" + PORT + " " + message);

		byte[] buffer = message.getBytes();
		try {
			final DatagramPacket packet = new DatagramPacket(buffer, buffer.length,
					InetAddress.getByName(node.getIpAdress()), PORT);
			try {
				socketReceive.send(packet);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (UnknownHostException u) {
			u.printStackTrace();
		}
	}
	
	public String requestUDP(Node node, String request) {
		sendUDP(node, request);
		
		byte[] bufferSendReceive = new byte[BUFFERSIZE];
		DatagramPacket packetReceive = new DatagramPacket(bufferSendReceive, bufferSendReceive.length);
					
		try {
			while (true) {
				socketReceive.receive(packetReceive);
				final String received = new String(packetReceive.getData()).trim();
				System.out.println("Message received [" + received + "]");
				return received;
			}
		} catch (SocketTimeoutException e) {
			System.out.println("Timeout reached!");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return new String("#ERROR - time out");
	}

	private void createSocket() {
		if (socketReceive != null) {
			socketReceive.close();
		}
		try {
			socketReceive = new DatagramSocket(null);
			SocketAddress sockaddr = new InetSocketAddress(interfaceAddress.getAddress(), PORT);
			socketReceive.setBroadcast(true);
			socketReceive.setSoTimeout(1000);
			socketReceive.bind(sockaddr);
		} catch (BindException e) {
			JOptionPane.showMessageDialog(null, "There is already an application running using the UDP port: " + PORT);
			doExit();
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
}
