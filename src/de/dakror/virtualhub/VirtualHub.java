package de.dakror.virtualhub;

import de.dakror.universion.UniVersion;
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
		
		UniVersion.offline = !CFG.INTERNET;
		UniVersion.init(VirtualHub.class, CFG.VERSION, CFG.PHASE);
		
		// Reporter.init(new File(CFG.DIR, "Logs"));
	}
}
