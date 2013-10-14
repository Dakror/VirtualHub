package de.dakror.virtualhub.util;

import java.awt.Component;
import java.awt.Container;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;

/**
 * @author Dakror
 */
public class Assistant
{
	public static boolean isInternetReachable()
	{
		try
		{
			return InetAddress.getByName("dakror.de").isReachable(60000);
		}
		catch (Exception e)
		{
			return false;
		}
	}
	
	public static void setFileContent(File f, String s)
	{
		f.getParentFile().mkdirs();
		try
		{
			OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(f));
			osw.write(s);
			osw.close();
		}
		catch (Exception e)
		{}
	}
	
	public static String getURLContent(URL u)
	{
		String res = "", line = "";
		try
		{
			BufferedReader br = new BufferedReader(new InputStreamReader(u.openStream()));
			while ((line = br.readLine()) != null)
				res += line;
			br.close();
		}
		catch (IOException e)
		{
			return null;
		}
		return res;
	}
	
	public static String getFileContent(File f)
	{
		String res = "", line = "";
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(f));
			while ((line = br.readLine()) != null)
				res += line;
			br.close();
		}
		catch (IOException e)
		{
			return null;
		}
		return res;
	}
	
	public static String getFolderChecksum(File folder)
	{
		if (!folder.exists()) return null;
		String[] files = folder.list();
		Arrays.sort(files);
		String f = Arrays.toString(files) + getFolderSize(folder);
		return MD5(f.getBytes());
	}
	
	public static String MD5(byte[] b)
	{
		MessageDigest md = null;
		try
		{
			md = MessageDigest.getInstance("MD5");
		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		return HexBin.encode(md.digest(b));
	}
	
	public static long getFolderSize(File directory)
	{
		long length = 0;
		for (File file : directory.listFiles())
		{
			if (file.isFile()) length += file.length();
			else length += getFolderSize(file);
		}
		return length;
	}
	
	public static void deleteFolder(File folder)
	{
		for (File f : folder.listFiles())
		{
			if (f.isDirectory()) deleteFolder(f);
			f.delete();
		}
	}
	
	public static String formatBinarySize(long size, int digits)
	{
		final String[] levels = { "", "K", "M", "G", "T" };
		for (int i = levels.length - 1; i > -1; i--)
			if (size > (long) Math.pow(1024, i))
			{
				DecimalFormat df = new DecimalFormat();
				df.setMaximumFractionDigits(digits);
				df.setMinimumFractionDigits(digits);
				return df.format(size / Math.pow(1024, i)) + levels[i] + "B";
			}
		return null;
	}
	
	public static void copyInputStream(InputStream in, OutputStream out) throws IOException
	{
		byte[] buffer = new byte[1024];
		int len = in.read(buffer);
		while (len >= 0)
		{
			out.write(buffer, 0, len);
			len = in.read(buffer);
		}
		in.close();
		out.close();
	}
	
	public static String getSocketAddress(Socket s)
	{
		return s.getInetAddress().getHostAddress() + ":" + s.getPort();
	}
	
	public static void setJFrameComponentsEnabled(JFrame frame, boolean enabled)
	{
		setContainerComponentsEnabled(frame.getContentPane(), enabled);
		if (frame.getJMenuBar() != null) setContainerComponentsEnabled(frame.getJMenuBar(), enabled);
		
		frame.repaint();
	}
	
	private static void setContainerComponentsEnabled(Container c, boolean enabled)
	{
		for (Component component : c.getComponents())
		{
			if (component instanceof Container) setContainerComponentsEnabled((Container) component, enabled);
			component.setEnabled(enabled);
		}
		
		c.repaint();
	}
	
	public static boolean hasSubDirectories(File dir)
	{
		for (File f : dir.listFiles())
			if (f.isDirectory()) return true;
		return false;
	}
	
	public static String getNodePath(DefaultMutableTreeNode node)
	{
		String res = "";
		TreeNode[] path = node.getPath();
		for (int i = 1; i < path.length; i++)
		{
			DefaultMutableTreeNode tn = (DefaultMutableTreeNode) path[i];
			res += tn.getUserObject() + "/";
		}
		if (res.length() == 0) return "";
		return res.substring(0, res.length() - 1);
	}
}
