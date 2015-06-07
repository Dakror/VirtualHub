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

import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;


/**
 * @author Dakror
 */
public class Tags {
	ArrayList<String> tags = new ArrayList<String>();
	
	public Tags(JSONArray arr) throws JSONException {
		for (int i = 0; i < arr.length(); i++)
			tags.add(arr.getString(i));
	}
	
	public Tags(String... t) {
		tags.addAll(Arrays.asList(t));
	}
	
	public boolean contains(String tag) {
		return tags.contains(tag);
	}
	
	public boolean add(String tag) {
		if (!contains(tag)) {
			tags.add(tag);
			return true;
		}
		
		return false;
	}
	
	public boolean remove(String tag) {
		if (contains(tag)) {
			tags.remove(tag);
			return true;
		}
		
		return false;
	}
	
	public String[] getTags() {
		return tags.toArray(new String[] {});
	}
	
	public String serialize() {
		return tags.toString().replace("[", "").replace("]", "");
	}
	
	@Override
	public String toString() {
		return tags.toString();
	}
}
