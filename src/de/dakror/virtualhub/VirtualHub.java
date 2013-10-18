package de.dakror.virtualhub;

import java.util.Properties;

import javax.swing.UIManager;

import com.jtattoo.plaf.acryl.AcrylLookAndFeel;

import de.dakror.universion.UniVersion;
import de.dakror.virtualhub.client.Client;
import de.dakror.virtualhub.server.Server;
import de.dakror.virtualhub.settings.CFG;


/**
 * @author Dakror
 */
public class VirtualHub
{
	public static void main(String[] args)
	{
		CFG.INTERNET = false;// Assistant.isInternetReachable();
		
		try
		{
			Properties props = new Properties();
			props.put("logoString", "");
			AcrylLookAndFeel.setCurrentTheme(props);
			UIManager.setLookAndFeel(new AcrylLookAndFeel());
			UIManager.put("ProgressBar.cycleTime", new Integer(6000));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		UniVersion.offline = !CFG.INTERNET;
		UniVersion.init(VirtualHub.class, CFG.VERSION, CFG.PHASE);
		
		// Reporter.init(new File(CFG.DIR, "Logs"));
		
		if (args.length == 0) new Client();
		else if (args[0].equals("-s")) new Server();
	}
}
