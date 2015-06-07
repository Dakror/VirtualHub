/*******************************************************************************
 * Copyright 2015 Maximilian Stark | Dakror <mail@dakror.de>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/


package de.dakror.virtualhub.data;

import java.io.File;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Dakror
 */
public class Catalog {
	String name;
	public ArrayList<File> sources = new ArrayList<File>();
	public ArrayList<String> tags = new ArrayList<String>();
	
	public Catalog(JSONObject o) {
		try {
			name = o.getString("name");
			
			JSONArray sources = o.getJSONArray("sources");
			for (int i = 0; i < sources.length(); i++)
				this.sources.add(new File(sources.getString(i)));
			
			JSONArray tags = o.getJSONArray("tags");
			for (int i = 0; i < tags.length(); i++)
				this.tags.add(tags.getString(i));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public JSONObject getJSONObject() {
		try {
			JSONObject o = new JSONObject();
			o.put("name", name);
			JSONArray sources = new JSONArray();
			for (File f : this.sources)
				sources.put(f.getPath().replace("\\", "/"));
			o.put("sources", sources);
			
			o.put("tags", tags.toArray(new String[] {}));
			
			return o;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
}
