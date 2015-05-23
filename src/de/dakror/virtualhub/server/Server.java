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
 

package de.dakror.virtualhub.server;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import de.dakror.universion.UniVersion;
import de.dakror.virtualhub.VirtualHub;
import de.dakror.virtualhub.net.NetHandler;
import de.dakror.virtualhub.settings.CFG;
import de.dakror.virtualhub.util.Assistant;

/**
 * @author Dakror
 */
public class Server extends Thread {
	public static final int VERSION = 2013123115;
	public static final int PHASE = 3;
	
	public static Server currentServer;
	
	public ServerFrame frame;
	public JSONArray catalogs;
	public JSONObject settings;
	
	public static File dir;
	
	HashMap<Socket, NetHandler> clients = new HashMap<Socket, NetHandler>();
	
	ServerSocket socket;
	
	public Server() {
		currentServer = this;
		
		dir = new File(CFG.DIR, "Server");
		dir.mkdir();
		
		frame = new ServerFrame();
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				shutdown();
			}
		});
		
		setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				frame.log("ERROR: " + sw.toString());
			}
		});
		
		try {
			socket = new ServerSocket(CFG.SERVER_PORT, 0, InetAddress.getLocalHost());
			frame.log("Starte Server unter " + socket.getInetAddress().getHostAddress() + ":" + socket.getLocalPort());
		} catch (BindException e) {
			frame.log("Es läuft bereits ein Server auf diesem Port!");
			shutdown();
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		DBManager.init();
		frame.log("Datenbank initialisiert");
		
		start();
	}
	
	@Override
	public void run() {
		while (!socket.isClosed()) {
			Socket s = null;
			try {
				s = socket.accept();
				handleConnection(s);
			} catch (IOException e) {}
		}
	}
	
	public void handleConnection(Socket s) {
		frame.log("Client verbunden: " + Assistant.getSocketAddress(s));
		NetHandler netHandler = new NetHandler(null, s);
		clients.put(s, netHandler);
		
		netHandler.start();
	}
	
	public void removeClient(Socket s, String msg) {
		clients.remove(s);
		frame.log("Client getrennt: " + Assistant.getSocketAddress(s) + ((!msg.equals("Verbindung getrennt")) ? " (" + msg + ")" : ""));
	}
	
	public void shutdown() {
		try {
			
			if (socket != null) socket.close();
			frame.save();
			frame.log("Server geschlossen\r\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		VirtualHub.init();
		
		UniVersion.offline = !CFG.INTERNET;
		UniVersion.init(Server.class, VERSION, PHASE);
		
		new Server();
	}
}
