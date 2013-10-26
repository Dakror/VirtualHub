package de.dakror.virtualhub.net.packet;

import java.io.File;

/**
 * @author Dakror
 */
public class Packet3Tag extends Packet
{
	File file;
	boolean add;
	String tag;
	
	public Packet3Tag(File f, boolean add, String tag)
	{
		super(3);
		
		file = f;
		this.add = add;
		this.tag = tag;
	}
	
	public File getFile()
	{
		return file;
	}
	
	public boolean isAdd()
	{
		return add;
	}
	
	public String getTag()
	{
		return tag;
	}
	
	@Override
	protected byte[] getPacketData()
	{
		return (file.getPath().replace("\\", "/") + "[" + Boolean.toString(add) + "[" + tag).getBytes();
	}
}
