package de.dakror.virtualhub.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;

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
		if (JTattooUtilities.isWindows())
		{
			try
			{
				if (Assistant.getFileExtension(f).equals("xcf")) return null; // xcf conversion pretty bad on IM
				
				String filePath = f.getPath().replace("\\", "/");
				filePath = filePath.substring(0, filePath.indexOf(".") > -1 ? filePath.lastIndexOf(".") : filePath.length()) + "CACHECACHE.png";
				Process process = new ProcessBuilder(dir.getPath().replace("\\", "/") + "/windows/convert.exe", "-layers", "merge", "-thumbnail", CFG.PREVIEWSIZE.width * 2 + "x" + CFG.PREVIEWSIZE.height * 2 + ">", f.getPath().replace("\\", "/"), filePath).start();
				process.waitFor();
				CFG.p(filePath);
				
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
		}
		
		return null;
	}
}
