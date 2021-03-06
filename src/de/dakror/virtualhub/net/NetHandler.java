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


package de.dakror.virtualhub.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import org.json.JSONException;

import de.dakror.virtualhub.data.Eticet;
import de.dakror.virtualhub.data.Tags;
import de.dakror.virtualhub.net.packet.Packet;
import de.dakror.virtualhub.net.packet.Packet.PacketTypes;
import de.dakror.virtualhub.net.packet.Packet0Catalogs;
import de.dakror.virtualhub.net.packet.Packet1Catalog;
import de.dakror.virtualhub.net.packet.Packet2Eticet;
import de.dakror.virtualhub.net.packet.Packet3Tags;
import de.dakror.virtualhub.net.packet.Packet4Rename;
import de.dakror.virtualhub.net.packet.Packet5Attribute;
import de.dakror.virtualhub.server.DBManager;
import de.dakror.virtualhub.server.Server;
import de.dakror.virtualhub.util.Assistant;

/**
 * Both Client- and Serversided
 * 
 * @author Dakror
 */
public class NetHandler extends Thread implements PacketHandler {
	Socket socket;
	PacketHandler handler;
	
	public DataInputStream dis;
	public DataOutputStream dos;
	
	public NetHandler(PacketHandler handler, Socket socket) {
		this.socket = socket;
		this.handler = handler;
	}
	
	@Override
	public void run() {
		try {
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
			
			if (handler == null) sendPacket(new Packet0Catalogs(Server.currentServer.catalogs));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		while (!socket.isClosed()) {
			try {
				int length = dis.readInt();
				byte[] data = new byte[length];
				dis.readFully(data, 0, length);
				
				if (handler != null) handler.parsePacket(data);
				else parsePacket(data);
			} catch (SocketException e) {
				try {
					dis.close();
					dos.close();
					if (isServerSided()) Server.currentServer.removeClient(socket, "Verbindung verloren");
					return;
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			} catch (EOFException e) {
				try {
					dis.close();
					dos.close();
					if (isServerSided()) Server.currentServer.removeClient(socket, "Verbindung getrennt");
					return;
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Only called on Server side
	 */
	@Override
	public void parsePacket(byte[] data) {
		PacketTypes t = Packet.lookupPacket(data[0]);
		Server.currentServer.frame.plog("< " + Assistant.getSocketAddress(socket) + " : " + t.name());
		switch (t) {
			case INVALID: {
				Server.currentServer.frame.plog("Empfing ungültiges Paket");
				break;
			}
			case CATALOGS: {
				Packet0Catalogs p = new Packet0Catalogs(data);
				Server.currentServer.catalogs = p.getCatalogs();
				Server.currentServer.frame.plog("Kataloge geändert von: " + Assistant.getSocketAddress(socket));
				break;
			}
			case CATALOG: {
				Packet1Catalog p = new Packet1Catalog(data);
				for (int i = 0; i < Server.currentServer.catalogs.length(); i++) {
					try {
						if (Server.currentServer.catalogs.getJSONObject(i).getString("name").equals(p.getCatalog().getName())) {
							Server.currentServer.catalogs.put(i, p.getCatalog().getJSONObject());
							Server.currentServer.frame.plog("Katalog " + p.getCatalog().getName() + " geändert von: " + Assistant.getSocketAddress(socket));
							break;
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				break;
			}
			case ETICET: {
				Packet2Eticet p = new Packet2Eticet(data);
				Eticet result = DBManager.eticet(p.getFile(), p.getCatalog(), p.getEticet());
				if (result != null) {
					try {
						sendPacket(new Packet2Eticet(p.getFile(), p.getCatalog(), result));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				break;
			}
			case TAGS: {
				Packet3Tags p = new Packet3Tags(data);
				Tags tags = DBManager.tags(p.getFile(), p.getCatalog(), p.getTags());
				if (tags != null) {
					try {
						sendPacket(new Packet3Tags(p.getFile(), p.getCatalog(), tags));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				break;
			}
			case RENAME: {
				Packet4Rename p = new Packet4Rename(data);
				DBManager.rename(p);
				break;
			}
			case ATTRIBUTE: {
				Packet5Attribute p = new Packet5Attribute(data);
				if (p.getKey().equals("backup.path")) {
					try {
						sendPacket(new Packet5Attribute("backup.path", Server.currentServer.settings.has("backup.path") ? Server.currentServer.settings.getString("backup.path") : ""));
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else if (p.getKey().equals("web.tagdata")) {
					try {
						sendPacket(new Packet5Attribute("web.tagdata", DBManager.tagdata(p.getValue()).toString()));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				break;
			}
			default:
				break;
		}
	}
	
	@Override
	public void sendPacket(Packet p) throws IOException {
		if (isServerSided()) Server.currentServer.frame.plog("> " + Assistant.getSocketAddress(socket) + " : " + p.getType().name());
		dos.write(p.getData());
	}
	
	public boolean isServerSided() {
		return Server.currentServer != null;
	}
}
