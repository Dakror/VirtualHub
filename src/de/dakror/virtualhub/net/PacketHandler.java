package de.dakror.virtualhub.net;

import java.io.IOException;

import de.dakror.virtualhub.net.packet.Packet;

/**
 * @author Dakror
 */
public interface PacketHandler
{
	public void parsePacket(byte[] data);
	
	public void sendPacket(Packet p) throws IOException;
}
