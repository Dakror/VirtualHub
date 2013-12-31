package de.dakror.virtualhub.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import com.jtattoo.plaf.JTattooUtilities;

import de.dakror.virtualhub.client.Client;
import de.dakror.virtualhub.settings.CFG;

/**
 * @author Dakror
 */
public class ImageMagickAssistant
{
	public static File dir = new File(Client.dir, "ImageMagick");
	
	public static void init()
	{
		if (dir.mkdir())
		{
			File tmpFile = new File(dir, "tmp.zip");
			try
			{
				Assistant.copyInputStream(ImageMagickAssistant.class.getResourceAsStream("/ImageMagick.zip"), new FileOutputStream(tmpFile));
				ZipAssistant.unzip(tmpFile, dir);
				tmpFile.delete();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public static BufferedImage getThumbnail(File f)
	{
		try
		{
			if (f.isDirectory()) return null;
			if (Assistant.getFileExtension(f).equals("xcf")) return null; // xcf conversion pretty bad on IM
			
			String filePath = f.getPath().replace("\\", "/");
			filePath = filePath.substring(0, filePath.indexOf(".") > -1 ? filePath.lastIndexOf(".") : filePath.length()) + "CACHECACHE.png";
			
			ArrayList<String> cmds = new ArrayList<String>();
			for (String s : getAdditionalParameters())
				cmds.add(s);
			
			String exec = "";
			if (JTattooUtilities.isWindows()) exec = "/windows/convert.exe";
			if (JTattooUtilities.isMac()) exec = "/mac/convert.sh";
			
			String cmd = "\"" + dir.getPath().replace("\\", "/") + exec + "\" \"" + f.getPath().replace("\\", "/") + "\" -layers merge -thumbnail " + CFG.PREVIEWSIZE.width + "x" + CFG.PREVIEWSIZE.height + " \"" + filePath + "\"";
			
			if (JTattooUtilities.isMac()) cmds.add("export MAGICK_HOME=\"" + dir.getPath().replace("\\", "/") + "/mac\"; export PATH=\"$MAGICK_HOME:$PATH\"; export DYLD_LIBRARY_PATH=\"$MAGICK_HOME/\"; " + cmd);
			else cmds.add(cmd);
			
			Process process = new ProcessBuilder(cmds.toArray(new String[] {})).start();
			process.waitFor();
			
			File dest = new File(filePath);
			if (!dest.exists())
			{
				CFG.p("dest failed");
				return null;
			}
			
			BufferedImage image = ImageIO.read(dest);
			
			dest.delete();
			
			return image;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static String[] getAdditionalParameters()
	{
		if (JTattooUtilities.isMac()) return new String[] { "/bin/sh", "-c" };
		return new String[] {};
	}
}
