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
