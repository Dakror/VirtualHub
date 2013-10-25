package de.dakror.virtualhub.data;

import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;


/**
 * @author Dakror
 */
public class Tags
{
	ArrayList<String> tags = new ArrayList<String>();
	
	public Tags(JSONArray arr) throws JSONException
	{
		for (int i = 0; i < arr.length(); i++)
			tags.add(arr.getString(i));
	}
	
	public Tags(String... t)
	{
		tags.addAll(Arrays.asList(t));
	}
	
	public boolean contains(String tag)
	{
		return tags.contains(tag);
	}
	
	public void add(String tag)
	{
		if (!contains(tag)) tags.add(tag);
	}
	
	public String[] getTags()
	{
		return tags.toArray(new String[] {});
	}
}
