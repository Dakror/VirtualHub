package de.dakror.virtualhub;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.UIManager;

import com.jtattoo.plaf.acryl.AcrylLookAndFeel;

import de.dakror.universion.UniVersion;
import de.dakror.virtualhub.client.Client;
import de.dakror.virtualhub.server.Server;
import de.dakror.virtualhub.settings.CFG;
import de.dakror.virtualhub.util.Assistant;


/**
 * @author Dakror
 */
public class VirtualHub
{
	public static void main(String[] args)
	{
		CFG.INTERNET = Assistant.isInternetReachable();
		
		try
		{
			UIManager.put("ProgressBar.cycleTime", new Integer(6000));
			UIManager.put("Tree.collapsedIcon", new ImageIcon(ImageIO.read(VirtualHub.class.getResourceAsStream("/com/jtattoo/plaf/icons/RightArrowInverse.gif"))));
			UIManager.put("Tree.expandedIcon", new ImageIcon(ImageIO.read(VirtualHub.class.getResourceAsStream("/com/jtattoo/plaf/icons/DownArrowInverse.gif"))));
			Properties props = new Properties();
			props.put("logoString", "");
			AcrylLookAndFeel.setCurrentTheme(props);
			UIManager.setLookAndFeel(new AcrylLookAndFeel());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		UniVersion.offline = !CFG.INTERNET;
		UniVersion.init(VirtualHub.class, CFG.VERSION, CFG.PHASE);
		
		// Reporter.init(new File(CFG.DIR, "Logs"));
		
		if (args.length == 0) try
		{
			new Client(InetAddress.getLocalHost());
		}
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}
		else if (args[0].equals("-s")) new Server();
	}
}
