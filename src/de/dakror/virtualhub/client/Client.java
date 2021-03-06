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


package de.dakror.virtualhub.client;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Properties;

import javax.swing.JOptionPane;

import de.dakror.universion.UniVersion;
import de.dakror.virtualhub.VirtualHub;
import de.dakror.virtualhub.client.dialog.ChooseCatalogDialog;
import de.dakror.virtualhub.data.Catalog;
import de.dakror.virtualhub.net.NetHandler;
import de.dakror.virtualhub.net.PacketHandler;
import de.dakror.virtualhub.net.packet.Packet;
import de.dakror.virtualhub.net.packet.Packet0Catalogs;
import de.dakror.virtualhub.net.packet.Packet2Eticet;
import de.dakror.virtualhub.net.packet.Packet3Tags;
import de.dakror.virtualhub.net.packet.Packet5Attribute;
import de.dakror.virtualhub.settings.CFG;
import de.dakror.virtualhub.util.Assistant;
import de.dakror.virtualhub.util.ImageMagickAssistant;

/**
 * @author Dakror
 */
public class Client extends Thread implements PacketHandler {
	public static final int VERSION = 2013123115;
	public static final int PHASE = 3;
	
	public static Client currentClient;
	
	Socket socket;
	NetHandler netHandler;
	public ClientFrame frame;
	private Catalog catalog;
	
	public Properties properties;
	
	public static File dir;
	
	public Client() {
		currentClient = this;
		
		dir = new File(CFG.DIR, "Client");
		dir.mkdir();
		
		ImageMagickAssistant.init();
		
		frame = new ClientFrame();
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				disconnect();
			}
		});
		
		Assistant.setJFrameComponentsEnabled(frame, false);
		frame.setVisible(true);
		frame.directoryLoader = new DirectoryLoader();
		frame.synchronizer = new Synchronizer();
		try {
			socket = new Socket(InetAddress.getByName(properties.getProperty("server")), CFG.SERVER_PORT);
		} catch (ConnectException e) {
			JOptionPane.showMessageDialog(frame, "Kann Server unter " + properties.getProperty("server") + " nicht erreichen!", "Server nicht erreichbar!", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Assistant.setJFrameComponentsEnabled(frame, true);
		start();
	}
	
	@Override
	public void run() {
		netHandler = new NetHandler(this, socket);
		netHandler.run();
	}
	
	@Override
	public void parsePacket(byte[] data) {
		switch (Packet.lookupPacket(data[0])) {
			case INVALID: {
				CFG.p("Received invalid packet");
				break;
			}
			case CATALOGS: {
				Packet0Catalogs p = new Packet0Catalogs(data);
				ChooseCatalogDialog.show(frame, p.getCatalogs());
				break;
			}
			case ETICET: {
				Packet2Eticet p = new Packet2Eticet(data);
				frame.setFileEticet(p);
				break;
			}
			case TAGS: {
				Packet3Tags p = new Packet3Tags(data);
				frame.setFileTags(p);
				break;
			}
			case ATTRIBUTE: {
				Packet5Attribute p = new Packet5Attribute(data);
				if (p.getKey().equals("backup.path")) frame.doBackup(p);
				
				break;
			}
			default:
				break;
		}
	}
	
	public void disconnect() {
		if (socket == null) return;
		try {
			socket.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	@Override
	public void sendPacket(Packet p) throws IOException {
		try {
			netHandler.sendPacket(p);
		} catch (SocketException e) {
			JOptionPane.showMessageDialog(frame, "Server unter " + properties.getProperty("server") + " wurde geschlossen!", "Server geschlossen!", JOptionPane.ERROR_MESSAGE);
			System.exit(0);
		}
	}
	
	public Catalog getCatalog() {
		return catalog;
	}
	
	public void setCatalog(Catalog catalog) {
		this.catalog = catalog;
		frame.loadCatalog(catalog);
	}
	
	public static void main(String[] args) {
		VirtualHub.init();
		
		UniVersion.offline = !CFG.INTERNET;
		UniVersion.init(Client.class, VERSION, PHASE);
		
		new Client();
	}
}
