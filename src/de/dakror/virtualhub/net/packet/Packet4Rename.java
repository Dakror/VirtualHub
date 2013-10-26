package de.dakror.virtualhub.net.packet;

import java.io.File;

/**
 * @author Dakror
 */
public class Packet4Rename extends Packet
{
	File o, n;
	
	public Packet4Rename(File o, File n)
	{
		super(4);
		
		this.o = o;
		this.n = n;
	}
	
	public Packet4Rename(byte[] data)
	{
		super(4);
		
		String[] parts = readData(data).split("\\[");
		o = new File(parts[0]);
		n = new File(parts[1]);
	}
	
	public File getOldFile()
	{
		return o;
	}
	
	public File getNewFile()
	{
		return n;
	}
	
	@Override
	protected byte[] getPacketData()
	{
		return (o.getPath().replace("\\", "/") + "[" + n.getPath().replace("\\", "/")).getBytes();
	}
	
}
