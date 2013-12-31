package de.dakror.virtualhub.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;

import com.jtattoo.plaf.AbstractLookAndFeel;
import com.jtattoo.plaf.BaseTreeUI;
import com.jtattoo.plaf.ColorHelper;

import de.dakror.universion.UniVersion;
import de.dakror.virtualhub.client.file.EticetableTreeNode;
import de.dakror.virtualhub.client.file.FileButton;
import de.dakror.virtualhub.client.file.FileMover;
import de.dakror.virtualhub.client.file.FileTree;
import de.dakror.virtualhub.client.file.FileTreeCellRenderer;
import de.dakror.virtualhub.client.file.FileViewPanel;
import de.dakror.virtualhub.client.tags.TagsTree;
import de.dakror.virtualhub.client.tags.TagsTreeCellRender;
import de.dakror.virtualhub.data.Catalog;
import de.dakror.virtualhub.data.Eticet;
import de.dakror.virtualhub.net.packet.Packet1Catalog;
import de.dakror.virtualhub.net.packet.Packet2Eticet;
import de.dakror.virtualhub.net.packet.Packet3Tags;
import de.dakror.virtualhub.net.packet.Packet4Rename;
import de.dakror.virtualhub.net.packet.Packet5Attribute;
import de.dakror.virtualhub.settings.CFG;
import de.dakror.virtualhub.util.Assistant;
import de.dakror.virtualhub.util.JHintTextField;
import de.dakror.virtualhub.util.WrapLayout;

/**
 * @author Dakror
 */
public class ClientFrame extends JFrame
{
	private static final long serialVersionUID = 1L;
	
	public FileTree catalog;
	TagsTree tags;
	FileViewPanel fileView;
	JPanel fileInfo;
	JScrollPane fileViewWrap;
	
	// -- file Info components -- //
	JTextField fileInfoName;
	JLabel fileInfoType, fileInfoDetails, fileInfoSize;
	
	public DirectoryLoader directoryLoader;
	Synchronizer synchronizer;
	
	Color borderColor = ColorHelper.brighter(AbstractLookAndFeel.getTheme().getFrameColor(), 50);
	
	List<EticetableTreeNode> lastAddedTreeNodes = new ArrayList<EticetableTreeNode>();
	
	public ClientFrame()
	{
		super("VirtualHub Client (" + UniVersion.prettyVersion() + ")");
		setSize(1080, 675);
		setMinimumSize(new Dimension(1080, 675));
		try
		{
			setIconImage(ImageIO.read(getClass().getResource("/img/icon.png")));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		init();
		
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	public void init()
	{
		initFiles();
		initMenu();
		initComponents();
	}
	
	public void initFiles()
	{
		try
		{
			if (!new File(Client.dir, "settings.properties").exists())
			{
				// properties file
				Properties properties = new Properties();
				properties.put("server", InetAddress.getLocalHost().getHostAddress());
				Client.currentClient.properties = properties;
				properties.store(new FileOutputStream(new File(Client.dir, "settings.properties")), "VirtualHub Client Einstellungen\r\n\r\n  server = Die IP des Servers, auf dem die VirtualHub Server Software läuft\r\n");
			}
			else
			{
				Client.currentClient.properties = new Properties();
				Client.currentClient.properties.load(new FileInputStream(new File(Client.dir, "settings.properties")));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void initMenu()
	{
		JMenuBar menubar = new JMenuBar();
		JMenu menu = new JMenu("Aktionen");
		menu.add(new JMenuItem(new AbstractAction("Katalog-Backup erstellen", new ImageIcon(getClass().getResource("/img/backup.png")))
		{
			private static final long serialVersionUID = 1L;
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (JOptionPane.showConfirmDialog(ClientFrame.this, "Sind Sie sicher, dass Sie ein Backupdes Katalogs erstellen wollen?\nDieser Vorgang kann einige Zeit in Anspruch nehmen.", "Backup starten?", JOptionPane.YES_NO_CANCEL_OPTION) == JOptionPane.YES_OPTION)
				{
					try
					{
						Client.currentClient.sendPacket(new Packet5Attribute("backup.path", ""));
					}
					catch (IOException e1)
					{
						e1.printStackTrace();
					}
				}
			}
		}));
		menubar.add(menu);
		
		setJMenuBar(menubar);
	}
	
	public void initComponents()
	{
		JTabbedPane tabs = new JTabbedPane();
		tabs.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, borderColor));
		
		initTree();
		
		JScrollPane catalogWrap = new JScrollPane(catalog, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		catalogWrap.setBorder(null);
		
		tabs.addTab("Katalog", catalogWrap);
		
		initTags();
		
		JScrollPane tagsWrap = new JScrollPane(tags, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		tagsWrap.setBorder(null);
		
		tabs.addTab("Schlüsselwörter", tagsWrap);
		
		tabs.setMinimumSize(new Dimension(1, 670));
		tabs.setPreferredSize(new Dimension(270, 670));
		
		JPanel viewSuper = initView();
		
		viewSuper.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, borderColor));
		viewSuper.setPreferredSize(new Dimension(801, 670));
		viewSuper.setMinimumSize(new Dimension(1, 670));
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabs, viewSuper);
		splitPane.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, borderColor));
		splitPane.setDividerLocation(270);
		splitPane.setOneTouchExpandable(true);
		
		setContentPane(splitPane);
	}
	
	public void initTree()
	{
		EticetableTreeNode root = new EticetableTreeNode("ROOT");
		DefaultTreeModel dtm = new DefaultTreeModel(root);
		catalog = new FileTree(dtm);
		catalog.setExpandsSelectedPaths(true);
		catalog.setShowsRootHandles(true);
		catalog.setRootVisible(false);
		catalog.setCellRenderer(new FileTreeCellRenderer(this));
		if (catalog.getUI() instanceof BaseTreeUI)
		{
			BaseTreeUI ui = (BaseTreeUI) catalog.getUI();
			ui.paintHorizontalLine = false;
			ui.paintVerticalLine = false;
		}
		
		final JPopupMenu generalPopupMenu = new JPopupMenu();
		generalPopupMenu.add(new JMenuItem(new AbstractAction("Quelle hinzufügen")
		{
			private static final long serialVersionUID = 1L;
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				JFileChooser jfc = new JFileChooser(System.getProperty("user.home"));
				jfc.setMultiSelectionEnabled(false);
				jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				jfc.setFileHidingEnabled(false);
				
				if (jfc.showOpenDialog(ClientFrame.this) == JFileChooser.APPROVE_OPTION)
				{
					File source = jfc.getSelectedFile();
					
					if (Client.currentClient.getCatalog().sources.contains(source))
					{
						JOptionPane.showMessageDialog(ClientFrame.this, "Dieser Ordner ist bereits eine Quelle in diesem Katalog!", "Bereits vorhanden!", JOptionPane.ERROR_MESSAGE);
						actionPerformed(e);
						return;
					}
					else
					{
						Client.currentClient.getCatalog().sources.add(source);
						DefaultTreeModel dtm = (DefaultTreeModel) catalog.getModel();
						EticetableTreeNode root = (EticetableTreeNode) dtm.getRoot();
						
						EticetableTreeNode dmtn = new EticetableTreeNode(source.getPath().replace("\\", "/"));
						dtm.insertNodeInto(dmtn, root, root.getChildCount());
						loadSubTree(dmtn);
						
						try
						{
							Client.currentClient.sendPacket(new Packet1Catalog(Client.currentClient.getCatalog()));
						}
						catch (IOException e1)
						{
							e1.printStackTrace();
						}
						
						dtm.reload();
					}
				}
			}
		}));
		final JPopupMenu sourcePopupMenu = new JPopupMenu();
		sourcePopupMenu.add(new JMenuItem(new AbstractAction("Quelle entfernen")
		{
			private static final long serialVersionUID = 1L;
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				EticetableTreeNode dmtn = (EticetableTreeNode) catalog.getSelectionPath().getLastPathComponent();
				DefaultTreeModel dtm = (DefaultTreeModel) catalog.getModel();
				
				Client.currentClient.getCatalog().sources.remove(new File(dmtn.getUserObject().toString()));
				
				try
				{
					Client.currentClient.sendPacket(new Packet1Catalog(Client.currentClient.getCatalog()));
				}
				catch (IOException e1)
				{
					e1.printStackTrace();
				}
				
				dtm.removeNodeFromParent(dmtn);
				
				dtm.reload();
				
				directoryLoader.fireUpdate();
			}
		}));
		
		catalog.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseReleased(MouseEvent e)
			{
				if (e.getButton() == MouseEvent.BUTTON3)
				{
					int row = catalog.getRowForLocation(e.getX(), e.getY());
					if (row != -1)
					{
						catalog.setSelectionRow(row);
						EticetableTreeNode dmtn = (EticetableTreeNode) catalog.getSelectionPath().getLastPathComponent();
						if (((EticetableTreeNode) dmtn.getParent()).getUserObject().equals("ROOT"))
						{
							sourcePopupMenu.show(e.getComponent(), e.getX(), e.getY());
						}
					}
					else generalPopupMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
		catalog.addTreeWillExpandListener(new TreeWillExpandListener()
		{
			@Override
			public void treeWillExpand(TreeExpansionEvent e) throws ExpandVetoException
			{
				EticetableTreeNode dmtn = (EticetableTreeNode) e.getPath().getLastPathComponent();
				if (((EticetableTreeNode) dmtn.getChildAt(0)).getUserObject().equals("CONTENT")) loadSubTree(dmtn);
			}
			
			@Override
			public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException
			{}
		});
	}
	
	public void initTags()
	{
		tags = new TagsTree(null);
		tags.setExpandsSelectedPaths(true);
		tags.setShowsRootHandles(false);
		tags.setCellRenderer(new TagsTreeCellRender());
		tags.setToggleClickCount(0);
		if (tags.getUI() instanceof BaseTreeUI)
		{
			BaseTreeUI ui = (BaseTreeUI) tags.getUI();
			ui.paintHorizontalLine = false;
			ui.paintVerticalLine = false;
		}
		final JPopupMenu generalPopupMenu = new JPopupMenu();
		generalPopupMenu.add(new JMenuItem(new AbstractAction("Schlüsselwort hinzufügen")
		{
			private static final long serialVersionUID = 1L;
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				String name = JOptionPane.showInputDialog(ClientFrame.this, "Bitte geben Sie den Namen des neuen Schlüsselwortes ein.", "Schlüsselwort hinzufügen", JOptionPane.PLAIN_MESSAGE);
				
				if (name == null || name.length() == 0) return;
				
				if (!name.matches("^[a-zA-Z0-9öäüÖÄÜß ]*$"))
				{
					JOptionPane.showMessageDialog(ClientFrame.this, "Schlüsselwörter dürfen nur Buchstaben,\nZahlen und Leerzeichen enthalten!", "Ungültiges Schlüsselwort", JOptionPane.ERROR_MESSAGE);
					actionPerformed(e);
					return;
				}
				
				if (Client.currentClient.getCatalog().tags.contains(name))
				{
					JOptionPane.showMessageDialog(ClientFrame.this, "Es existiert bereits ein Schlüsselwort mit diesem Namen!", "Schlüsselwort bereits vorhanden!", JOptionPane.ERROR_MESSAGE);
					actionPerformed(e);
					return;
				}
				
				Client.currentClient.getCatalog().tags.add(name);
				updateTags();
				try
				{
					Client.currentClient.sendPacket(new Packet1Catalog(Client.currentClient.getCatalog()));
				}
				catch (IOException e1)
				{
					e1.printStackTrace();
				}
			}
		}));
		final JPopupMenu tagsPopupMenu = new JPopupMenu();
		tagsPopupMenu.add(new JMenuItem(new AbstractAction("Schlüsselwort entfernen")
		{
			private static final long serialVersionUID = 1L;
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) tags.getSelectionPath().getLastPathComponent();
				
				Client.currentClient.getCatalog().tags.remove(dmtn.getUserObject());
				
				try
				{
					Client.currentClient.sendPacket(new Packet1Catalog(Client.currentClient.getCatalog()));
				}
				catch (IOException e1)
				{
					e1.printStackTrace();
				}
				
				dmtn.removeFromParent();
				
				((DefaultTreeModel) tags.getModel()).reload();
			}
		}));
		
		tags.addFocusListener(new FocusListener()
		{
			@Override
			public void focusLost(FocusEvent e)
			{
				if (!generalPopupMenu.isShowing() && !tagsPopupMenu.isShowing()) tags.setSelectionRow(-1);
			}
			
			@Override
			public void focusGained(FocusEvent e)
			{}
		});
		
		tags.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseReleased(MouseEvent e)
			{
				if (e.getButton() == MouseEvent.BUTTON1)
				{
					int row = tags.getRowForLocation(e.getX(), e.getY());
					if (row > 0) showTagFiles(((DefaultMutableTreeNode) tags.getPathForRow(row).getLastPathComponent()).getUserObject().toString());
					else
					{
						showTagFiles(null);
						tags.setSelectionRow(-1);
					}
				}
				else if (e.getButton() == MouseEvent.BUTTON3)
				{
					int row = tags.getRowForLocation(e.getX(), e.getY());
					if (row > 0)
					{
						tags.setSelectionRow(row);
						
						tagsPopupMenu.show(e.getComponent(), e.getX(), e.getY());
					}
					else generalPopupMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
	}
	
	public JPanel initView()
	{
		GridBagLayout gbl = new GridBagLayout();
		JPanel viewSuper = new JPanel(gbl);
		
		JPanel settings = new JPanel(/* new FlowLayout(FlowLayout.LEFT, 0, 0) */new BorderLayout());
		settings.add(new JLabel());
		settings.setPreferredSize(new Dimension(0, 26));
		final JHintTextField search = new JHintTextField("Suche im aktuellen Verzeichnis");
		search.setBorder(BorderFactory.createEmptyBorder());
		// search.setPreferredSize(new Dimension(200, 26));
		search.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyReleased(KeyEvent e)
			{
				showSearchFiles(search.getText());
			}
		});
		settings.add(search, BorderLayout.CENTER);
		
		addGridBagLayoutComponent(viewSuper, gbl, settings, 0, 0, 1, 1, 1, 0);
		
		int gap = 8;
		
		fileView = new FileViewPanel(new WrapLayout(FlowLayout.LEFT, gap, gap));
		
		final JPopupMenu popup = new JPopupMenu();
		popup.add(new JMenuItem(new AbstractAction("Neuer Ordner", CFG.FOLDER)
		{
			private static final long serialVersionUID = 1L;
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (catalog.getSelectionPath() == null) return;
				
				DefaultTreeModel dtm = (DefaultTreeModel) catalog.getModel();
				
				File parent = new File(Assistant.getNodePath((EticetableTreeNode) catalog.getSelectionPath().getLastPathComponent()));
				int count = Assistant.getFileCountWithSamePrefix(parent, "Neuer Ordner");
				
				File folder = new File(parent, "Neuer Ordner" + (count > 0 ? " (" + (count + 1) + ")" : ""));
				folder.mkdir();
				
				EticetableTreeNode dmtn = (EticetableTreeNode) catalog.getSelectionPath().getLastPathComponent();
				
				boolean expanded = catalog.isExpanded(catalog.getSelectionPath());
				
				dmtn.removeAllChildren();
				dtm.reload(dmtn);
				loadSubTree(dmtn);
				
				if (expanded) catalog.expandPath(catalog.getSelectionPath());
				
				directoryLoader.fireUpdate();
			}
		}));
		fileView.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseReleased(MouseEvent e)
			{
				if (e.getButton() == MouseEvent.BUTTON3 && catalog.getSelectionPath() != null) popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
		
		fileViewWrap = new JScrollPane(fileView, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		fileViewWrap.setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, borderColor));
		fileViewWrap.setPreferredSize(new Dimension(1, 1));
		fileViewWrap.getVerticalScrollBar().setUnitIncrement(50);
		fileView.setSize(new Dimension(fileViewWrap.getSize().width, 0));
		
		addGridBagLayoutComponent(viewSuper, gbl, fileViewWrap, 0, 1, 1, 1, 1, 1);
		
		initInfo();
		addGridBagLayoutComponent(viewSuper, gbl, fileInfo, 0, 2, 1, 1, 1, 0);
		
		return viewSuper;
	}
	
	public void initInfo()
	{
		GridBagLayout gbl = new GridBagLayout();
		fileInfo = new JPanel(gbl);
		
		fileInfo.setBorder(BorderFactory.createCompoundBorder(fileInfo.getBorder(), BorderFactory.createEmptyBorder(10, 10, 10, 10)));
		fileInfo.setPreferredSize(new Dimension(0, 80));
		
		fileInfoName = new JTextField("");
		fileInfoName.setOpaque(false);
		final Border oldBorder = fileInfoName.getBorder();
		fileInfoName.setBorder(null);
		fileInfoName.setForeground(Color.darkGray);
		fileInfoName.setFont(fileInfoName.getFont().deriveFont(18f));
		fileInfoName.setEditable(false);
		fileInfoName.addFocusListener(new FocusListener()
		{
			
			@Override
			public void focusLost(FocusEvent e)
			{
				fileInfoName.setEditable(false);
				fileInfoName.setBorder(null);
				fileInfoName.setForeground(Color.darkGray);
				fileInfoName.getCaret().setVisible(false);
			}
			
			@Override
			public void focusGained(FocusEvent e)
			{
				if (getSelectedFiles().length != 1) return;
				
				fileInfoName.setEditable(true);
				fileInfoName.setBorder(oldBorder);
				fileInfoName.getCaret().setVisible(true);
				fileInfoName.setForeground(Color.black);
			}
		});
		fileInfoName.addKeyListener(new KeyAdapter()
		{
			@Override
			public void keyPressed(KeyEvent e)
			{
				if (!fileInfoName.isEditable() || getSelectedFiles().length == 0) return;
				
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
				{
					String newName = fileInfoName.getText();
					
					File f = getSelectedFiles()[0];
					File newFile = new File(f.getParentFile(), newName);
					boolean dir = f.isDirectory();
					if (f.renameTo(newFile))
					{
						if (dir) loadSubTree((EticetableTreeNode) catalog.getSelectionPath().getLastPathComponent());
						
						directoryLoader.fireUpdate();
						
						try
						{
							Client.currentClient.sendPacket(new Packet4Rename(f, newFile));
						}
						catch (IOException e1)
						{
							e1.printStackTrace();
						}
						
						// -- unfocus -- //
						fileInfoName.setEditable(false);
						fileInfoName.setBorder(null);
						fileInfoName.setForeground(Color.darkGray);
						fileInfoName.getCaret().setVisible(false);
					}
					else JOptionPane.showMessageDialog(ClientFrame.this, (dir ? "Das Verzeichnis" : "Die Datei") + " konnte nicht umbenannt werden!", "Fehler!", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		
		addGridBagLayoutComponent(fileInfo, gbl, fileInfoName, 0, 0, 3, 2, 0, 1);
		
		fileInfoType = new JLabel();
		fileInfoType.setForeground(Color.decode("#1E395B"));
		fileInfoType.setFont(fileInfoType.getFont().deriveFont(14f));
		addGridBagLayoutComponent(fileInfo, gbl, fileInfoType, 0, 2, 1, 1, 1, 1);
		
		fileInfoDetails = new JLabel();
		fileInfoDetails.setForeground(Color.decode("#1E395B"));
		fileInfoDetails.setFont(fileInfoDetails.getFont().deriveFont(14f));
		addGridBagLayoutComponent(fileInfo, gbl, fileInfoDetails, 1, 2, 1, 1, 1, 1);
		
		fileInfoSize = new JLabel();
		fileInfoSize.setForeground(Color.decode("#1E395B"));
		fileInfoSize.setFont(fileInfoSize.getFont().deriveFont(14f));
		addGridBagLayoutComponent(fileInfo, gbl, fileInfoSize, 2, 2, 1, 1, 1, 1);
	}
	
	public void showSearchFiles(String search)
	{
		if (!directoryLoader.synced) return;
		
		for (Component c : fileView.getComponents())
			c.setVisible(true);
		
		if (search != null && search.length() > 0)
		{
			for (Component c : fileView.getComponents())
			{
				FileButton fb = (FileButton) c;
				c.setVisible(fb.file.getName().toLowerCase().contains(search.toLowerCase()));
			}
		}
	}
	
	public void showTagFiles(String tag)
	{
		if (!directoryLoader.synced) return;
		
		for (Component c : fileView.getComponents())
			c.setVisible(true);
		
		if (tag != null && tag.length() > 0)
		{
			for (Component c : fileView.getComponents())
			{
				FileButton fb = (FileButton) c;
				c.setVisible(fb.tags.contains(tag));
			}
		}
	}
	
	public void addGridBagLayoutComponent(Container parent, GridBagLayout gbl, Component c, int x, int y, int width, int height, double wx, double wy)
	{
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.gridwidth = width;
		gbc.gridheight = height;
		gbc.weightx = wx;
		gbc.weighty = wy;
		gbl.setConstraints(c, gbc);
		parent.add(c);
	}
	
	public void loadCatalog(Catalog catalog)
	{
		DefaultTreeModel dtm = (DefaultTreeModel) this.catalog.getModel();
		EticetableTreeNode root = (EticetableTreeNode) dtm.getRoot();
		
		String notFound = "";
		String sep = ",\r\n ";
		for (File file : Client.currentClient.getCatalog().sources)
		{
			if (!file.exists())
			{
				notFound += file.getPath().replace("\\", "/") + sep;
				continue;
			}
			EticetableTreeNode dmtn = new EticetableTreeNode(file.getPath().replace("\\", "/"));
			dtm.insertNodeInto(dmtn, root, root.getChildCount());
			loadSubTree(dmtn);
		}
		
		if (notFound.length() > 0)
		{
			notFound = notFound.substring(0, notFound.length() - sep.length());
			JOptionPane.showMessageDialog(this, "Die folgenden Quellordner konnten nicht gefunden werden:\r\n" + notFound, "Quellordner nicht gefunden!", JOptionPane.ERROR_MESSAGE);
		}
		
		dtm.reload();
		
		DefaultMutableTreeNode root1 = new DefaultMutableTreeNode(Client.currentClient.getCatalog().getName());
		DefaultTreeModel dtm1 = new DefaultTreeModel(root1);
		tags.setModel(dtm1);
		
		updateTags();
	}
	
	public void loadSubTree(EticetableTreeNode folder)
	{
		File f = new File(Assistant.getNodePath(folder));
		
		if (!f.exists()) return;
		
		folder.removeAllChildren();
		
		for (File file : f.listFiles())
		{
			if (file.isDirectory() && !file.isHidden())
			{
				EticetableTreeNode dmtn = new EticetableTreeNode(file.getName());
				if (Assistant.hasSubDirectories(file)) dmtn.add(new EticetableTreeNode("CONTENT"));
				
				folder.add(dmtn);
				lastAddedTreeNodes.add(dmtn);
				try
				{
					Client.currentClient.sendPacket(new Packet2Eticet(file, Client.currentClient.getCatalog().getName(), Eticet.NULL));
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		((DefaultTreeModel) catalog.getModel()).reload(folder);
	}
	
	public void updateTags()
	{
		DefaultTreeModel dtm = (DefaultTreeModel) tags.getModel();
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) tags.getModel().getRoot();
		root.removeAllChildren();
		
		Collections.sort(Client.currentClient.getCatalog().tags);
		for (String t : Client.currentClient.getCatalog().tags)
		{
			root.add(new DefaultMutableTreeNode(t));
		}
		
		dtm.reload();
	}
	
	public void setFileInfo(final FileButton f)
	{
		if (f == null)
		{
			fileInfoName.setText("");
			fileInfoType.setText("");
			fileInfoSize.setText("");
			fileInfoDetails.setText("");
		}
		else
		{
			File[] sel = getSelectedFiles();
			if (sel.length == 1)
			{
				fileInfoName.setText(f.file.getName());
				fileInfoType.setText(FileSystemView.getFileSystemView().getSystemTypeDescription(f.file));
				if (!f.file.isDirectory()) fileInfoSize.setText("Größe: " + Assistant.formatBinarySize(f.file.length(), 2));
				else fileInfoSize.setText("");
				
				new Thread()
				{
					@Override
					public void run()
					{
						Dimension size = null;// TODO ImageMagickAssistant.getSize(f.file);
						if (size != null) fileInfoDetails.setText("Abmessungen: " + size.width + " x " + size.height);
						else
						{
							fileInfoDetails.setText("");
							
							if (f.file.isDirectory())
							{
								int fl = 0, dir = 0;
								
								for (File file : f.file.listFiles())
								{
									if (f.file.isHidden()) continue;
									
									if (file.isDirectory()) dir++;
									else fl++;
								}
								
								fileInfoDetails.setText((fl > 0 ? fl + " Datei" + (fl > 1 ? "en" : "") : "") + (fl > 0 && dir > 0 ? " und " : "") + (dir > 0 ? dir + " Unterordner" : ""));
							}
						}
					}
				}.start();
			}
			else
			{
				setFileInfo(null);
				if (sel.length > 0) fileInfoName.setText(sel.length + " Elemente ausgewählt");
			}
		}
	}
	
	@Override
	public void setTitle(String s)
	{
		super.setTitle("VirtualHub Client (" + UniVersion.prettyVersion() + ") " + s);
	}
	
	public File[] getSelectedFiles()
	{
		ArrayList<File> files = new ArrayList<File>();
		for (Component c : fileView.getComponents())
		{
			if (c instanceof FileButton)
			{
				FileButton fb = (FileButton) c;
				if (fb.isSelected()) files.add(fb.file);
			}
		}
		
		return files.toArray(new File[] {});
	}
	
	public FileButton[] getSelectedFileButtons()
	{
		ArrayList<FileButton> files = new ArrayList<FileButton>();
		for (Component c : fileView.getComponents())
		{
			if (c instanceof FileButton)
			{
				FileButton fb = (FileButton) c;
				if (fb.isSelected()) files.add(fb);
			}
		}
		
		return files.toArray(new FileButton[] {});
	}
	
	public int getFileIndex(File f)
	{
		for (int i = 0; i < fileView.getComponentCount(); i++)
		{
			if (((FileButton) fileView.getComponent(i)).file.equals(f)) return i;
		}
		
		return -1;
	}
	
	public File getSelectedTreeFile()
	{
		if (catalog.getSelectionPath() == null) return null;
		return new File(Assistant.getNodePath((EticetableTreeNode) catalog.getSelectionPath().getLastPathComponent()));
	}
	
	public void setFileEticet(Packet2Eticet packet)
	{
		if (fileView.getComponentCount() > 0)
		{
			for (Component c : fileView.getComponents())
			{
				if (c instanceof FileButton)
				{
					FileButton fb = (FileButton) c;
					if (fb.file.equals(packet.getFile()))
					{
						fb.setEticet(packet.getEticet());
						fb.repaint();
						
						break;
					}
				}
			}
		}
		
		EticetableTreeNode found = null;
		try
		{
			for (EticetableTreeNode etn : lastAddedTreeNodes)
			{
				if (new File(Assistant.getNodePath(etn)).equals(packet.getFile()))
				{
					found = etn;
				}
			}
		}
		catch (ConcurrentModificationException e)
		{
			try
			{
				Thread.sleep(10);
			}
			catch (InterruptedException e1)
			{
				e1.printStackTrace();
			}
			setFileEticet(packet);
		}
		
		if (found != null)
		{
			found.setEticet(packet.getEticet());
			catalog.repaint();
			lastAddedTreeNodes.remove(found);
		}
		
		if (catalog.getSelectionPath() != null)
		{
			EticetableTreeNode parent = (EticetableTreeNode) catalog.getSelectionPath().getLastPathComponent();
			for (int i = 0; i < parent.getChildCount(); i++)
			{
				EticetableTreeNode etn = (EticetableTreeNode) parent.getChildAt(i);
				if (etn.getUserObject().equals(packet.getFile().getName()))
				{
					etn.setEticet(packet.getEticet());
					catalog.repaint();
					break;
				}
			}
		}
	}
	
	public void setFileTags(Packet3Tags packet)
	{
		if (fileView.getComponentCount() > 0)
		{
			for (Component c : fileView.getComponents())
			{
				if (c instanceof FileButton)
				{
					FileButton fb = (FileButton) c;
					if (fb.file.equals(packet.getFile()))
					{
						fb.tags = packet.getTags();
						break;
					}
				}
			}
		}
	}
	
	public void doBackup(Packet5Attribute packet)
	{
		if (packet.getValue().length() == 0) JOptionPane.showMessageDialog(this, "Es wurde noch kein Backupverzeichnis konfiguiert!\nBitte stellen Sie ein solches in der VirtualHub Server Software ein,\n indem Sie unter Aktionen -> Backup-Einstellungen einen Pfad festlegen.\nVersuchen Sie daraufhin erneut, ein Backup zu erstellen.", "Backupverzeichnis nicht konfiguriert!", JOptionPane.ERROR_MESSAGE);
		else new FileMover(this, false, true, new File(packet.getValue() + "/" + Client.currentClient.getCatalog().getName() + "-Backup " + new SimpleDateFormat("dd.MM.yy HH-mm").format(new Date())), Client.currentClient.getCatalog().sources.toArray(new File[] {}));
	}
}
