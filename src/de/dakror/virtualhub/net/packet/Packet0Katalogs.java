package de.dakror.virtualhub.net.packet;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * @author Dakror
 */
public class Packet0Katalogs extends Packet
{
	JSONArray katalogs;
	
	public Packet0Katalogs(JSONArray katalogs)
	{
		super(0);
		this.katalogs = katalogs;
	}
	
	
	public Packet0Katalogs(byte[] data)
	{
		super(0);
		try
		{
			katalogs = new JSONArray(new String(readData(data)));
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}
	
	public JSONArray getKatalogs()
	{
		return katalogs;
	}
	
	@Override
	protected byte[] getPacketData()
	{
		return katalogs.toString().getBytes();
	}
}
