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
