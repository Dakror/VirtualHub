package de.dakror.virtualhub.net.packet;

import java.io.File;

import de.dakror.virtualhub.data.Eticet;

/**
 * @author Dakror
 */
public class Packet2Eticet extends Packet
{
	File file;
	Eticet eticet;
	
	public Packet2Eticet(File f, Eticet eticet)
	{
		super(2);
		file = f;
		this.eticet = eticet;
	}
	
	public Packet2Eticet(byte[] data)
	{
		super(2);
		String str = readData(data);
		file = new File(str.substring(0, str.lastIndexOf(":")));
		eticet = Eticet.values()[Integer.parseInt(str.substring(str.lastIndexOf(":") + 1))];
	}
	
	public File getFile()
	{
		return file;
	}
	
	public Eticet getEticet()
	{
		return eticet;
	}
	
	@Override
	protected byte[] getPacketData()
	{
		return (file.getPath().replace("\\", "/") + ":" + eticet.ordinal()).getBytes();
	}
}
