package de.dakror.virtualhub.settings;

import java.io.File;
import java.util.Arrays;

/**
 * @author Dakror
 */
public class CFG
{
	// -- UniVersion -- //
	public static final int VERSION = 0;
	public static final int PHASE = 0;
	
	public static boolean INTERNET;
	
	public static final File DIR = new File(System.clearProperty("user.home") + "/.dakror/VirtualHub");
	
	
	public static void p(Object... p)
	{
		if (p.length == 1) System.out.println(p[0]);
		else System.out.println(Arrays.toString(p));
	}
	
}