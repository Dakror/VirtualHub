package de.dakror.virtualhub.net.packet;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONException;

import de.dakror.virtualhub.data.Tags;

/**
 * @author Dakror
 */
public class Packet3Tags extends Packet
{
	public static final String CATALOG = "CATALOG";
	
	File file;
	Tags tags;
	
	public Packet3Tags(File f, Tags tags)
	{
		super(3);
		file = f;
		this.tags = tags;
	}
	
	public Packet3Tags(byte[] data)
	{
		super(3);
		try
		{
			JSONArray arr = new JSONArray(readData(data));
			if (!arr.getString(0).equals(CATALOG)) file = new File(arr.getString(0));
			tags = new Tags(arr.getJSONArray(1));
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		
	}
	
	public File getFile()
	{
		return file;
	}
	
	public Tags getTags()
	{
		return tags;
	}
	
	@Override
	protected byte[] getPacketData()
	{
		JSONArray arr = new JSONArray();
		if (file != null) arr.put(file.getPath().replace("\\", "/"));
		else arr.put("NULL");
		
		arr.put(tags.getTags());
		
		return arr.toString().getBytes();
	}
}
