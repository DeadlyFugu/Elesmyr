/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.sekien.elesmyr.msgsys;

import com.esotericsoftware.minlog.Log;
import net.sekien.elesmyr.system.Main;
import net.sekien.hbt.TagNotFoundException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created with IntelliJ IDEA. User: matt Date: 1/04/13 Time: 9:25 AM To change this template use File | Settings | File
 * Templates.
 */
public class Client {
private Connection connection;
private boolean running = true;

public void sendUDP(Message msg) {
	try {
		connection.sendUDP(msg);
	} catch (Exception e) {
		if (running) Main.handleError(e);
	}
}

public void sendTCP(Message msg) {
	try {
		connection.sendTCP(msg);
	} catch (Exception e) {
		if (running) Main.handleError(e);
	}
}

public void received(Message msg) {
	msg.addConnection(connection);
	MessageSystem.receiveClient(msg);
}

public void start() {
	running = true;
}

public void connect(int i, InetAddress address, int port, int port2) throws IOException {
	Socket socket = new Socket(address, port);
	Log.info("client", "Client connected to "+socket.toString());
	connection = new Connection(-1, socket);
	new Thread() {
		public void run() {
			this.setName("[client] listener");
			while (running) {
				try {
					received(connection.readMsg());
				} catch (TagNotFoundException e) {
					Log.error("client", "Badly formed message received.");
					e.printStackTrace();
				} catch (NullPointerException npe) {
					//Gets thrown when client is stopped
					running = false;
				} catch (SocketException e) {
					//Gets thrown when client is stopped
					running = false;
				} catch (Exception e) {
					if (running) {
						Main.handleCrash(e);
						System.exit(1);
					}
				}
			}
		}
	}.start();
}

public void stop() {
	Log.info("client", "Client stopped");
	running = false;
}

public void close() throws IOException {
	connection.close();
}

public boolean isConnected() {
	return running && connection.isConnected();
}
}
