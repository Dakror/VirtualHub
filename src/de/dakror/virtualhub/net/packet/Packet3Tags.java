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
			file = new File(arr.getString(0));
			if (arr.length() > 1) tags = new Tags(arr.getJSONArray(1));
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
		arr.put(file.getPath().replace("\\", "/"));
		if (tags != null) arr.put(tags.getTags());
		return arr.toString().getBytes();
	}
}
