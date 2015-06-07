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

import org.json.JSONException;
import org.json.JSONObject;

import de.dakror.virtualhub.data.Catalog;

/**
 * @author Dakror
 */
public class Packet1Catalog extends Packet {
	Catalog catalog;
	
	public Packet1Catalog(Catalog catalog) {
		super(1);
		this.catalog = catalog;
	}
	
	public Packet1Catalog(byte[] b) {
		super(1);
		try {
			catalog = new Catalog(new JSONObject(new String(b)));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public Catalog getCatalog() {
		return catalog;
	}
	
	@Override
	protected byte[] getPacketData() {
		return catalog.getJSONObject().toString().getBytes();
	}
	
}
