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
 

package de.dakror.virtualhub.net.packet;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * @author Dakror
 */
public class Packet0Catalogs extends Packet {
	JSONArray catalogs;
	
	public Packet0Catalogs(JSONArray catalogs) {
		super(0);
		this.catalogs = catalogs;
	}
	
	
	public Packet0Catalogs(byte[] data) {
		super(0);
		try {
			catalogs = new JSONArray(new String(readData(data)));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public JSONArray getCatalogs() {
		return catalogs;
	}
	
	@Override
	protected byte[] getPacketData() {
		return catalogs.toString().getBytes();
	}
}
