/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package net.sekien.elesmyr.msgsys;

import net.sekien.hbt.*;

import java.io.IOException;
import java.net.Socket;

/**
 * Created with IntelliJ IDEA. User: matt Date: 1/04/13 Time: 9:23 AM To change this template use File | Settings | File
 * Templates.
 */
public class Connection {
private int id;
private Socket socket = null;
private HBTOutputStream out;
private HBTInputStream in;
private boolean fastlinked = false;

public Connection(int id, Socket clientSocket) throws IOException {
	this.socket = clientSocket;
	out = new HBTOutputStream(socket.getOutputStream(), false);
	in = new HBTInputStream(socket.getInputStream(), false);
	this.id = id;
}

public int getID() {
	return id;
}

public boolean isConnected() {
	return socket.isConnected();
}

public void close() throws IOException {
	in.close();
	out.close();
	socket.close();
}

void sendTCP(Message msg) throws IOException {
	HBTCompound msgc = new HBTCompound("");
	msgc.addTag(new HBTString("t", msg.getTarget()));
	msgc.addTag(new HBTString("n", msg.getName()));
	HBTCompound payload = new HBTCompound("d");
	payload.getData().addAll(msg.getData().getData());
	msgc.addTag(payload);
	if (msg.getSender()!=null) {
		msgc.addTag(new HBTString("s", msg.getSender()));
	}
	out.write(msgc);
}

void sendUDP(Message msg) throws IOException {
	sendTCP(msg);
}

Message readMsg() throws IOException, TagNotFoundException {
	HBTCompound msgc = (HBTCompound) in.read();
	Message msg = new Message(((HBTString) msgc.getTag("t")).getData()+"."+
			                          ((HBTString) msgc.getTag("n")).getData(),
			                         (HBTCompound) msgc.getTag("d"));
	if (msgc.hasTag("s")) {
		msg.setSender(msgc.getString("s", null));
	}
	return msg;
}

void setFastlinked() {
	fastlinked = true;
}

boolean getFastlinked() {
	return fastlinked;
}

@Override
public String toString() {
	return socket.getInetAddress().getCanonicalHostName();
}
}
