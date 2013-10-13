package de.dakror.virtualhub.client;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import com.jtattoo.plaf.BaseTreeUI;
import com.jtattoo.plaf.acryl.AcrylBorderFactory;

import de.dakror.universion.UniVersion;
import de.dakror.virtualhub.settings.CFG;

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
		setVisible(true);
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
		JMenu main = new JMenu("VirtualHub");
		menu.add(main);
		
		setJMenuBar(menu);
	}
	
	public void initComponents()
	{
		JPanel contentPane = new JPanel(new BorderLayout(0, 0));
		// contentPane.setBorder(AcrylBorderFactory.getInstance().getScrollPaneBorder());
		
		JTabbedPane tabs = new JTabbedPane();
		tabs.setBorder(AcrylBorderFactory.getInstance().getScrollPaneBorder());
		
		initCatalogTree();
		
		JScrollPane catalogWrap = new JScrollPane(catalog, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		tabs.addTab("Katalog", catalogWrap);
		tabs.addTab("Kategorien", new JPanel());
		tabs.setPreferredSize(new Dimension(270, 670));
		
		contentPane.add(tabs, BorderLayout.WEST);
		
		JPanel viewSuper = new JPanel();
		viewSuper.setBorder(AcrylBorderFactory.getInstance().getScrollPaneBorder());
		viewSuper.setPreferredSize(new Dimension(801, 670));
		contentPane.add(viewSuper, BorderLayout.EAST);
		
		setContentPane(contentPane);
	}
	
	public void initCatalogTree()
	{
		catalog = new JTree();
		catalog.setShowsRootHandles(true);
		catalog.setRootVisible(false);
		catalog.setCellRenderer(new CatalogTreeCellRenderer());
		if (catalog.getUI() instanceof BaseTreeUI)
		{
			BaseTreeUI ui = (BaseTreeUI) catalog.getUI();
			ui.paintHorizontalLine = false;
			ui.paintVerticalLine = false;
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
			return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		}
	}
}
