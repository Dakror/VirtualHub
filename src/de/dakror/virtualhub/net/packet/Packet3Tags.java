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
	String catalog;
	Tags tags;
	
	public Packet3Tags(File f, String catalog, Tags tags)
	{
		super(3);
		
		file = f;
		this.catalog = catalog;
		this.tags = tags;
	}
	
	public Packet3Tags(byte[] data)
	{
		super(3);
		
		try
		{
			JSONArray arr = new JSONArray(readData(data));
			file = new File(arr.getString(0));
			catalog = arr.getString(1);
			if (arr.length() > 2) tags = new Tags(arr.getJSONArray(2));
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
	
	public String getCatalog()
	{
		return catalog;
	}
	
	@Override
	protected byte[] getPacketData()
	{
		JSONArray arr = new JSONArray();
		arr.put(file.getPath().replace("\\", "/"));
		arr.put(catalog);
		if (tags != null) arr.put(tags.getTags());
		return arr.toString().getBytes();
	}
}
