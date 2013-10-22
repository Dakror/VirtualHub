package de.dakror.virtualhub.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.Image;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.image.BufferedImage;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

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
	
	public static String getFileExtension(File f)
	{
		if (!f.isFile()) return "";
		if (f.getName().split(".").length == 1) return "";
		return f.getPath().substring(f.getPath().lastIndexOf(".") + 1).toLowerCase();
	}
	
	public static String wrap(String in, int len)
	{
		String result = "";
		for (int i = 0; i < in.length(); i++)
		{
			if (i % len == 0 && i > 0) result += "<br>";
			result += in.charAt(i);
		}
		return result;
	}
	
	public static String shortenFileName(File f, int len, int l)
	{
		String s = f.getName();
		String[] lines = wrap(s, len).split("<br>");
		if (lines.length <= l)
		{
			String wrapped = wrap(s, len);
			if (f.isDirectory()) return wrapped;
			if (wrapped.lastIndexOf(".") == -1) return wrapped;
			return wrapped.substring(0, wrapped.lastIndexOf(".") + 1) + getFileExtension(f);
		}
		String ext = getFileExtension(f);
		String result = "";
		for (int i = 0; i < l - 1; i++)
			result += lines[i] + "<br>";
		String lastline = lines[l - 1];
		result += "..." + lastline.substring(3, lastline.length() - 3) + ((ext.length() > 0) ? ("." + ext) : "");
		return result;
	}
	
	public static int getFileCountWithSamePrefix(File folder, String prefix)
	{
		int count = 0;
		
		for (File f : folder.listFiles())
		{
			if (f.isHidden()) continue;
			
			if (f.getName().startsWith(prefix)) count++;
		}
		
		return count;
	}
	
	public static boolean deleteDirectory(File dir)
	{
		for (File f : dir.listFiles())
		{
			boolean fd = false;
			
			if (f.isDirectory()) fd = deleteDirectory(f);
			else fd = f.delete();
			
			if (!fd) return false;
		}
		return dir.delete();
	}
	
	public static void setCorrectDragCursor(DragSourceDragEvent dsde)
	{
		int action = dsde.getDropAction();
		if (action == DnDConstants.ACTION_COPY) dsde.getDragSourceContext().setCursor(DragSource.DefaultCopyDrop);
		else
		{
			if (action == DnDConstants.ACTION_MOVE) dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
			else dsde.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
		}
	}
	
	public static void sortNodeChildren(DefaultMutableTreeNode dmtn)
	{
		ArrayList<DefaultMutableTreeNode> nodes = new ArrayList<DefaultMutableTreeNode>();
		for (int i = 0; i < dmtn.getChildCount(); i++)
			nodes.add((DefaultMutableTreeNode) dmtn.getChildAt(i));
		
		Collections.sort(nodes, new Comparator<DefaultMutableTreeNode>()
		{
			
			@Override
			public int compare(DefaultMutableTreeNode o1, DefaultMutableTreeNode o2)
			{
				return o1.getUserObject().toString().toLowerCase().compareTo(o2.getUserObject().toString().toLowerCase());
			}
		});
		
		dmtn.removeAllChildren();
		for (DefaultMutableTreeNode node : nodes)
			dmtn.add(node);
	}
	
	public static int getFileCount(File folder)
	{
		int i = 0;
		for (File f : folder.listFiles())
		{
			if (f.isHidden()) continue;
			if (f.isDirectory()) i += getFileCount(f);
			else i++;
		}
		
		return i;
	}
	
	public static BufferedImage toBufferedImage(Image img)
	{
		BufferedImage image = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		image.getGraphics().drawImage(img, 0, 0, null);
		
		return image;
	}
	
	public static boolean containsNode(DefaultMutableTreeNode parent, DefaultMutableTreeNode child)
	{
		for (int i = 0; i < parent.getChildCount(); i++)
			if (((DefaultMutableTreeNode) parent.getChildAt(i)).getUserObject().toString().equals(child.getUserObject().toString())) return true;
		
		return false;
	}
	
	public static int getIndexOfComponent(Container container, Component c)
	{
		int index = -1;
		for (int i = 0; i < container.getComponentCount(); i++)
		{
			if (container.getComponent(i).equals(c))
			{
				index = i;
				break;
			}
		}
		
		return index;
	}
}
