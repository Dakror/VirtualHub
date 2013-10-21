package de.dakror.virtualhub.client;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
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
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;

import com.jtattoo.plaf.AbstractLookAndFeel;
import com.jtattoo.plaf.BaseTreeUI;
import com.jtattoo.plaf.ColorHelper;

import de.dakror.universion.UniVersion;
import de.dakror.virtualhub.data.Catalog;
import de.dakror.virtualhub.net.packet.Packet1Catalog;
import de.dakror.virtualhub.settings.CFG;
import de.dakror.virtualhub.util.Assistant;
import de.dakror.virtualhub.util.WrapLayout;

/**
 * @author Dakror
 */
public class ClientFrame extends JFrame
{
	private static final long serialVersionUID = 1L;
	
	public JScrollPane catalogWrap;
	
	FileTree catalog;
	FileViewPanel fileView;
	JPanel fileInfo;
	JScrollPane fileViewWrap;
	
	DirectoryLoader directoryLoader;
	Synchronizer synchronizer;
	
	Color borderColor = ColorHelper.brighter(AbstractLookAndFeel.getTheme().getFrameColor(), 50);
	
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
				properties.store(new FileOutputStream(new File(Client.dir, "settings.properties")), "VirtualHub Client Einstellungen\r\n\r\n  server = Die IP des Servers, auf dem die VirtualHub Server Software l\u00e4uft\r\n");
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
		JMenuBar menu = new JMenuBar();
		JMenu main = new JMenu("Aktionen");
		menu.add(main);
		
		setJMenuBar(menu);
	}
	
	public void initComponents()
	{
		JTabbedPane tabs = new JTabbedPane();
		tabs.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, borderColor));
		initTree();
		
		catalogWrap = new JScrollPane(catalog, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		catalogWrap.setBorder(null);
		
		tabs.addTab("Katalog", catalogWrap);
		tabs.addTab("Kategorien", new JLabel("Not implemented yet", JLabel.CENTER));
		
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
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("ROOT");
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
		generalPopupMenu.add(new JMenuItem(new AbstractAction("Quelle hinzuf\u00fcgen")
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
					
					if (Client.currentClient.catalog.sources.contains(source))
					{
						JOptionPane.showMessageDialog(ClientFrame.this, "Dieser Ordner ist bereits eine Quelle in diesem Katalog!", "Bereits vorhanden!", JOptionPane.ERROR_MESSAGE);
						actionPerformed(e);
						return;
					}
					else
					{
						Client.currentClient.catalog.sources.add(source);
						DefaultTreeModel dtm = (DefaultTreeModel) catalog.getModel();
						DefaultMutableTreeNode root = (DefaultMutableTreeNode) dtm.getRoot();
						
						DefaultMutableTreeNode dmtn = new DefaultMutableTreeNode(source.getPath().replace("\\", "/"));
						dtm.insertNodeInto(dmtn, root, root.getChildCount());
						loadSubTree(dmtn);
						
						try
						{
							Client.currentClient.sendPacket(new Packet1Catalog(Client.currentClient.catalog));
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
				DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) catalog.getSelectionPath().getLastPathComponent();
				DefaultTreeModel dtm = (DefaultTreeModel) catalog.getModel();
				
				Client.currentClient.catalog.sources.remove(new File(dmtn.getUserObject().toString()));
				
				try
				{
					Client.currentClient.sendPacket(new Packet1Catalog(Client.currentClient.catalog));
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
						DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) catalog.getSelectionPath().getLastPathComponent();
						if (((DefaultMutableTreeNode) dmtn.getParent()).getUserObject().equals("ROOT"))
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
				DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
				if (((DefaultMutableTreeNode) dmtn.getChildAt(0)).getUserObject().equals("CONTENT")) loadSubTree(dmtn);
			}
			
			@Override
			public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException
			{}
		});
	}
	
	public JPanel initView()
	{
		GridBagLayout gbl = new GridBagLayout();
		JPanel viewSuper = new JPanel(gbl);
		
		JPanel settings = new JPanel();
		settings.add(new JLabel("Not implemented yet"));
		settings.setPreferredSize(new Dimension(0, 26));
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
				
				File parent = new File(Assistant.getNodePath((DefaultMutableTreeNode) catalog.getSelectionPath().getLastPathComponent()));
				int count = Assistant.getFileCountWithSamePrefix(parent, "Neuer Ordner");
				
				File folder = new File(parent, "Neuer Ordner" + (count > 0 ? " (" + (count + 1) + ")" : ""));
				folder.mkdir();
				
				DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) catalog.getSelectionPath().getLastPathComponent();
				
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
		
		fileInfo = new JPanel();
		fileInfo.setPreferredSize(new Dimension(0, 80));
		addGridBagLayoutComponent(viewSuper, gbl, fileInfo, 0, 2, 1, 1, 1, 0);
		
		return viewSuper;
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
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) dtm.getRoot();
		
		String notFound = "";
		String sep = ",\r\n ";
		for (File file : Client.currentClient.catalog.sources)
		{
			if (!file.exists())
			{
				notFound += file.getPath().replace("\\", "/") + sep;
				continue;
			}
			DefaultMutableTreeNode dmtn = new DefaultMutableTreeNode(file.getPath().replace("\\", "/"));
			dtm.insertNodeInto(dmtn, root, root.getChildCount());
			loadSubTree(dmtn);
		}
		
		if (notFound.length() > 0)
		{
			notFound = notFound.substring(0, notFound.length() - sep.length());
			JOptionPane.showMessageDialog(this, "Die folgenden Quellordner konnten nicht gefunden werden:\r\n" + notFound, "Quellordner nicht gefunden!", JOptionPane.ERROR_MESSAGE);
		}
		
		dtm.reload();
	}
	
	public void loadSubTree(DefaultMutableTreeNode folder)
	{
		File f = new File(Assistant.getNodePath(folder));
		
		if (!f.exists()) return;
		
		folder.removeAllChildren();
		
		for (File file : f.listFiles())
		{
			if (file.isDirectory() && !file.isHidden())
			{
				DefaultMutableTreeNode dmtn = new DefaultMutableTreeNode(file.getName());
				if (Assistant.hasSubDirectories(file)) dmtn.add(new DefaultMutableTreeNode("CONTENT"));
				folder.add(dmtn);
			}
		}
		
		((DefaultTreeModel) catalog.getModel()).reload(folder);
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
	
	public File getSelectedTreeFile()
	{
		if (catalog.getSelectionPath() == null) return null;
		return new File(Assistant.getNodePath((DefaultMutableTreeNode) catalog.getSelectionPath().getLastPathComponent()));
	}
}
