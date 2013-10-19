package de.dakror.virtualhub.client;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
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
	
	// -- DnD -- //
	public FileButton dragged;
	public Point mouse;
	public DefaultMutableTreeNode targetNode;
	public boolean copy;
	
	public JScrollPane catalogWrap;
	
	JTree catalog;
	JPanel fileView, fileInfo;
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
		catalog = new JTree(dtm);
		catalog.setShowsRootHandles(true);
		catalog.setRootVisible(false);
		catalog.setCellRenderer(new CatalogTreeCellRenderer());
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
						addFolderSourceTree(dmtn);
						
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
				if (((DefaultMutableTreeNode) dmtn.getChildAt(0)).getUserObject().equals("CONTENT"))
				{
					dmtn.removeAllChildren();
					addFolderSourceTree(dmtn);
				}
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
		
		fileView = new JPanel(new WrapLayout(FlowLayout.LEFT, 5, 5));
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
			addFolderSourceTree(dmtn);
		}
		
		if (notFound.length() > 0)
		{
			notFound = notFound.substring(0, notFound.length() - sep.length());
			JOptionPane.showMessageDialog(this, "Die folgenden Quellordner konnten nicht gefunden werden:\r\n" + notFound, "Quellordner nicht gefunden!", JOptionPane.ERROR_MESSAGE);
		}
		
		dtm.reload();
	}
	
	public void addFolderSourceTree(DefaultMutableTreeNode folder)
	{
		File f = new File(Assistant.getNodePath(folder));
		
		for (File file : f.listFiles())
		{
			if (file.isDirectory() && !file.isHidden())
			{
				DefaultMutableTreeNode dmtn = new DefaultMutableTreeNode(file.getName());
				if (Assistant.hasSubDirectories(file)) dmtn.add(new DefaultMutableTreeNode("CONTENT"));
				folder.add(dmtn);
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
	
	public void moveOrCopySelectedFiles()
	{
		DefaultTreeModel dtm = (DefaultTreeModel) catalog.getModel();
		File[] selected = getSelectedFiles();
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode) catalog.getSelectionPath().getLastPathComponent();
		
		new FileMover(this, copy, new File(Assistant.getNodePath(targetNode)), selected);
		
		for (int i = 0; i < parent.getChildCount(); i++)
		{
			for (File f : selected)
				if (f.getName().equals(((DefaultMutableTreeNode) parent.getChildAt(i)).getUserObject())) parent.remove(i);
		}
		
		ArrayList<DefaultMutableTreeNode> targetChildren = new ArrayList<DefaultMutableTreeNode>();
		for (int i = 0; i < targetNode.getChildCount(); i++)
			targetChildren.add((DefaultMutableTreeNode) targetNode.getChildAt(i));
		
		
		for (File f : selected)
		{
			boolean exists = false;
			for (int i = 0; i < targetNode.getChildCount(); i++)
			{
				if (f.getName().equals(((DefaultMutableTreeNode) targetNode.getChildAt(i)).getUserObject()))
				{
					exists = true;
					break;
				}
			}
			
			if (!exists) targetChildren.add(new DefaultMutableTreeNode(f.getName()));
		}
		
		targetNode.removeAllChildren();
		Collections.sort(targetChildren, new Comparator<DefaultMutableTreeNode>()
		{
			@Override
			public int compare(DefaultMutableTreeNode o1, DefaultMutableTreeNode o2)
			{
				return o1.getUserObject().toString().toLowerCase().compareTo(o2.getUserObject().toString().toLowerCase());
			}
		});
		
		for (DefaultMutableTreeNode dmtn : targetChildren)
		{
			targetNode.add(dmtn);
		}
		
		dtm.reload(parent);
		dtm.reload(targetNode);
		
		directoryLoader.fireUpdate();
	}
	
	public File getSelectedTreeFile()
	{
		if (catalog.getSelectionPath() == null) return null;
		return new File(Assistant.getNodePath((DefaultMutableTreeNode) catalog.getSelectionPath().getLastPathComponent()));
	}
	
	class CatalogTreeCellRenderer extends DefaultTreeCellRenderer
	{
		private static final long serialVersionUID = 1L;
		
		public CatalogTreeCellRenderer()
		{
			setLeafIcon(CFG.FOLDER);
			setOpenIcon(CFG.FOLDER);
			setClosedIcon(CFG.FOLDER);
		}
		
		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
		{
			JLabel tce = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			
			if (row < 0 || row >= tree.getRowCount()) return tce;
			
			DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode) catalog.getPathForRow(row).getLastPathComponent();
			
			if (dragged != null)
			{
				int mouseRow = (mouse.y + catalogWrap.getVerticalScrollBar().getValue() - tce.getPreferredSize().height * 2) / tce.getPreferredSize().height;
				if (mouseRow == row)
				{
					if (!setFrameCursor(dmtn))
					{
						targetNode = dmtn;
					}
					
					selected = true;
					tce.setForeground(Color.white);
					
					if (!leaf && mouse.x < 20 * dmtn.getLevel() && mouse.x > 20 * dmtn.getLevel() - 20) tree.expandRow(row);
					
					this.hasFocus = true;
				}
			}
			return tce;
		}
	}
	
	private boolean setFrameCursor(DefaultMutableTreeNode node)
	{
		File nodeFile = new File(Assistant.getNodePath(node));
		
		File[] selected = getSelectedFiles();
		boolean sameFile = false;
		
		for (File f : selected)
		{
			if (nodeFile.getPath().replace("\\", "/").startsWith(f.getPath().replace("\\", "/"))) sameFile = true;
			if (nodeFile.equals(f.getParentFile())) sameFile = true;
		}
		
		Cursor c = copy ? sameFile ? DragSource.DefaultCopyNoDrop : DragSource.DefaultCopyDrop : sameFile ? DragSource.DefaultMoveNoDrop : DragSource.DefaultMoveDrop;
		
		ClientFrame.this.setCursor(c);
		
		return sameFile;
	}
}
