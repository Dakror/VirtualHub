package de.dakror.virtualhub.net.packet;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * @author Dakror
 */
public abstract class Packet
{
	public static enum PacketTypes
	{
		INVALID,
		CATALOGS,
		CATALOG,
		ETICET,
		TAGS,
		
		;
		public int getID()
		{
			return ordinal() - 1;
		}
	}
	
	public byte packetID;
	
	public Packet(int packetID)
	{
		this.packetID = (byte) packetID;
	}
	
	protected abstract byte[] getPacketData();
	
	public byte[] getData()
	{
		byte[] strData = getPacketData();
		
		ByteBuffer bb = ByteBuffer.allocate(strData.length + 5);
		bb.putInt(strData.length + 1);
		bb.put(packetID);
		bb.put(strData);
		
		return bb.array();
	}
	
	public static String readData(byte[] data)
	{
		return new String(Arrays.copyOfRange(data, 1, data.length)).trim();
	}
	
	public PacketTypes getType()
	{
		return Packet.lookupPacket(packetID);
	}
	
	public static PacketTypes lookupPacket(int id)
	{
		for (PacketTypes pt : PacketTypes.values())
		{
			if (pt.getID() == id) return pt;
		}
		
		return PacketTypes.INVALID;
	}
}
