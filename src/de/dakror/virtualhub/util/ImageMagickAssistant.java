package de.dakror.virtualhub.util;

import java.awt.Dimension;
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
			if (Assistant.getFileExtension(f).equals("xcf")) return null; // xcf conversion pretty bad on IM
			
			String filePath = f.getPath().replace("\\", "/");
			filePath = filePath.substring(0, filePath.indexOf(".") > -1 ? filePath.lastIndexOf(".") : filePath.length()) + "CACHECACHE.png";
			
			ArrayList<String> cmds = new ArrayList<String>();
			for (String s : getAdditionalParameters())
				cmds.add(s);
			
			cmds.add(dir.getPath().replace("\\", "/") + "/windows/convert.exe");
			cmds.add("-layers");
			cmds.add("merge");
			cmds.add("-thumbnail");
			cmds.add(CFG.PREVIEWSIZE.width + "x" + CFG.PREVIEWSIZE.height + ">");
			cmds.add(f.getPath().replace("\\", "/"));
			cmds.add(filePath);
			
			Process process = new ProcessBuilder(cmds).start();
			process.waitFor();
			
			File dest = new File(filePath);
			if (!dest.exists()) return null;
			
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
		if (JTattooUtilities.isMac()) return new String[] { "/bin/bash", "-c", "export", "MAGICK_HOME=\"" + dir.getPath().replace("\\", "/") + "\";", "export", "PATH=\"$MAGICK_HOME/bin:$PATH\";", "export", "DYLD_LIBRARY_PATH=\"$MAGICK_HOME/\"" };
		return new String[] {};
	}
	
	public static Dimension getSize(File f)
	{
		if (JTattooUtilities.isWindows())
		{
			try
			{
				if (Assistant.getFileExtension(f).equals("xcf")) return null; // xcf conversion pretty bad on IM
				
				String filePath = f.getPath().replace("\\", "/");
				filePath = filePath.substring(0, filePath.indexOf(".") > -1 ? filePath.lastIndexOf(".") : filePath.length()) + "CACHECACHE.png";
				Process process = new ProcessBuilder(dir.getPath().replace("\\", "/") + "/windows/convert.exe", "-layers", "merge", f.getPath().replace("\\", "/"), filePath).start();
				process.waitFor();
				
				File dest = new File(filePath);
				if (!dest.exists()) return null;
				
				BufferedImage image = ImageIO.read(dest);
				
				dest.delete();
				
				return new Dimension(image.getWidth(), image.getHeight());
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		return null;
	}
}
