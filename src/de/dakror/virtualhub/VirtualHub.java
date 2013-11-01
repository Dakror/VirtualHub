package de.dakror.virtualhub;

import java.util.Properties;

import javax.swing.UIManager;

import com.jtattoo.plaf.acryl.AcrylLookAndFeel;

import de.dakror.universion.UniVersion;
import de.dakror.virtualhub.settings.CFG;
import de.dakror.virtualhub.util.Assistant;


/**
 * @author Dakror
 */
public class VirtualHub
{
	public static void init()
	{
		CFG.INTERNET = Assistant.isInternetReachable();
		
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
	}
}
