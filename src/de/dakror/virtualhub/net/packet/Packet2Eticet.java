package de.dakror.virtualhub.net.packet;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONException;

import de.dakror.virtualhub.data.Eticet;

/**
 * @author Dakror
 */
public class Packet2Eticet extends Packet {
	File file;
	Eticet eticet;
	String catalog;
	
	public Packet2Eticet(File f, String catalog, Eticet eticet) {
		super(2);
		file = f;
		this.catalog = catalog;
		this.eticet = eticet;
	}
	
	public Packet2Eticet(byte[] data) {
		super(2);
		
		try {
			JSONArray arr = new JSONArray(readData(data));
			file = new File(arr.getString(0));
			catalog = arr.getString(1);
			eticet = Eticet.values()[arr.getInt(2)];
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	
	public File getFile() {
		return file;
	}
	
	public Eticet getEticet() {
		return eticet;
	}
	
	public String getCatalog() {
		return catalog;
	}
	
	@Override
	protected byte[] getPacketData() {
		JSONArray arr = new JSONArray();
		arr.put(file.getPath().replace("\\", "/"));
		arr.put(catalog);
		arr.put(eticet.ordinal());
		
		return arr.toString().getBytes();
	}
}
