package de.dakror.virtualhub.client;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
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

import com.jtattoo.plaf.BaseTreeUI;
import com.jtattoo.plaf.acryl.AcrylBorderFactory;

import de.dakror.universion.UniVersion;
import de.dakror.virtualhub.data.Catalog;
import de.dakror.virtualhub.net.packet.Packet1Catalog;
import de.dakror.virtualhub.settings.CFG;
import de.dakror.virtualhub.util.Assistant;

/**
 * @author Dakror
 */
public class ClientFrame extends JFrame
{
	private static final long serialVersionUID = 1L;
	
	JTree catalog;
	
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
		tabs.setBorder(AcrylBorderFactory.getInstance().getScrollPaneBorder());
		
		initTree();
		
		JScrollPane catalogWrap = new JScrollPane(catalog, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		catalogWrap.setBorder(null);
		
		tabs.addTab("Katalog", catalogWrap);
		tabs.addTab("Kategorien", new JPanel());
		
		tabs.setMinimumSize(new Dimension(1, 670));
		tabs.setPreferredSize(new Dimension(270, 670));
		
		JPanel viewSuper = new JPanel();
		viewSuper.setBorder(AcrylBorderFactory.getInstance().getScrollPaneBorder());
		viewSuper.setPreferredSize(new Dimension(801, 670));
		viewSuper.setMinimumSize(new Dimension(1, 670));
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabs, viewSuper);
		splitPane.setDividerLocation(270);
		splitPane.setOneTouchExpandable(true);
		setContentPane(splitPane);
	}
	
	public void initTree()
	{
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("");
		
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
		
		final JPopupMenu popupMenu = new JPopupMenu();
		popupMenu.add(new JMenuItem(new AbstractAction("Quelle hinzuf\u00fcgen")
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
		catalog.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseReleased(MouseEvent e)
			{
				if (e.isPopupTrigger())
				{
					int row = catalog.getRowForLocation(e.getX(), e.getY());
					if (row != -1)
					{
						catalog.setSelectionRow(row);
					}
					else popupMenu.show(e.getComponent(), e.getX(), e.getY());
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
	
	public void loadCatalog(Catalog catalog)
	{
		// load tree sources
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
			return tce;
		}
	}
}
