package de.dakror.virtualhub.net.packet;

/**
 * @author Dakror
 */
public class Packet5Attribute extends Packet {
	String key, value;
	
	public Packet5Attribute(String key, Object value) {
		super(5);
		this.key = key;
		this.value = value.toString();
	}
	
	public Packet5Attribute(byte[] data) {
		super(5);
		String[] parts = readData(data).split("\\[");
		key = parts[0];
		if (parts.length > 1) value = parts[1];
		else value = "";
	}
	
	public String getKey() {
		return key;
	}
	
	public String getValue() {
		return value;
	}
	
	@Override
	protected byte[] getPacketData() {
		return (key + "[" + value).getBytes();
	}
}
