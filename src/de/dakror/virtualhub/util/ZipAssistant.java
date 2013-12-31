package de.dakror.virtualhub.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Dakror
 */
public class ZipAssistant
{
	public static final byte[] BUFFER = new byte[0xFFFF];
	
	public static File unzip(File zip, File dest)
	{
		
		try
		{
			ZipFile zipFile = new ZipFile(zip);
			for (ZipEntry entry : Collections.list(zipFile.entries()))
			{
				extractEntry(zipFile, entry, dest.getPath().replace("\\", "/"));
			}
			zipFile.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return zip;
	}
	
	public static void extractEntry(ZipFile zipFile, ZipEntry entry, String destDir) throws IOException
	{
		File file = new File(destDir + "/" + entry.getName().replace("\\", "/"));
		if (entry.isDirectory()) file.mkdirs();
		else
		{
			file.getParentFile().mkdirs();
			InputStream is = null;
			OutputStream os = null;
			try
			{
				is = zipFile.getInputStream(entry);
				os = new FileOutputStream(file);
				for (int len; (len = is.read(BUFFER)) != -1;)
				{
					os.write(BUFFER, 0, len);
				}
			}
			finally
			{
				if (os != null) os.close();
				if (is != null) is.close();
			}
			file.setExecutable(true, false);
			file.setWritable(true, false);
		}
	}
}
