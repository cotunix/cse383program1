/*
   Kyle Richardson
   CSE383 - Fall 2015
   9/2/2015

   Server.java

   Simple UDP server that waits for a client to send messages, and then echoes the messages back to all other clients.
   Modified from code provided by Dr. Scott Campbell

 */

package richarkc;
import java.net.*;
import java.io.*;
import java.util.*;

public class Server {
	DatagramSocket sock;
	DatagramPacket pkt;
	int port;
	Log log;
	ArrayList<SocketAddress> clientList; 

	// start server
	public static void main(String a[]) {
		int port = 0;
		try {
			port = Integer.parseInt(a[0]);
		} catch (Exception err) {
			System.err.println("Could not parse arguemnt");
			System.exit(-1);
		}

		try {
			new Server(port).Main();
		} catch (IOException err) {
			System.err.println("Could not start server - probably port in use");
			System.exit(-1);
		}
	}

	//constructor - opens socket
	public Server(int port) throws IOException {
		this.port = port;
		sock = new DatagramSocket(port);
		log = new Log("server.log");
	}

	/*
	Waits for packet from clients
	*/
	public void Main() {
		System.out.println("Server Starting");	
		log.log("Server Starting");
		clientList = new ArrayList<SocketAddress>();

		//loop forever
		while(true) {
			try {

				log.log("Waiting for packet");
				//get new message
				byte b[] = new byte[1024];
				DatagramPacket pkt= new DatagramPacket(b,b.length);
				sock.receive(pkt);

				ByteArrayInputStream bis= new ByteArrayInputStream(b);
				DataInputStream dis = new DataInputStream(bis);
				String newmsg = dis.readUTF();
				//Decipher message using protocol	
				try{
					readProtocol(newmsg, dis.readUTF(), pkt.getSocketAddress());
				}
				catch (IllegalArgumentException e) {
					//if the client provides an incorrect data type or the incorrect amount of arguments this will catch it
					log.log("Invalid command from: " + pkt.getSocketAddress());
					sendMessage("MESSAGE", "Invalid command, try again", pkt.getSocketAddress());
				}


			} catch (Exception err) {
				log.log("Error " + err);
			}
		}
	}
	/*
	readProtocol takes in the messages from the client and translates it into valid statement in the following protocol:
	HELLO: Adds a client to the connected list and allows for them to receive echoed messsages, server sends back "HELLO-RESPONSE"
	MESSAGE: Receives a message from the client and echoes it to all connected clients
	GOODBYE: Removes a client from the connected list, server sends back "GOODBYE-RESPONSE"
	*/	
	public void readProtocol(String newmsg, String msg2, SocketAddress add) throws IOException {
		
		if (newmsg.startsWith("HELLO")) {
			if (!(clientList.contains(add))) {
				clientList.add(add);
				sendMessage("HELLO-RESPONSE","", add);
				log.log("Client connected: " + add);
				}
									
			}
				
			else if (newmsg.startsWith("MESSAGE")) {
				if (msg2.length() > 500) {
					log.log("Message from " + add + " too long, declined");
					sendMessage("MESSAGE", "ERROR: Messages are limited to 500 characters", add);
				}
				else {
					log.log("Received message from: " + add + " " + msg2);
					sendAll(msg2);
				}
			}

				
			else if (newmsg.startsWith("GOODBYE")){
				sendMessage("GOODBYE-RESPONSE","", add);
				clientList.remove(add);
				log.log("Client disconnected: " + add);
			}
				
			else {
				log.log("Invalid command from " + add + ": " + msg2);
				sendMessage("MESSAGE", "Invalid command, try again", add);
			}

	}

	/*
	sendMessage send a message to one client, typically used for "HELLO-RESPONSE" and "GOODBYE-RESPONSE" and for errors
	*/

	public void sendMessage(String msg, String msg2, SocketAddress add) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		dos.writeUTF(msg);
		dos.writeUTF(msg2);
		byte data[] = bos.toByteArray();
		DatagramPacket sendResponse = new DatagramPacket(data,data.length,add);
		sock.send(sendResponse);
	}

	/*
	sendAll will send a message to all connected clients
	*/

	public void sendAll(String msg) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		dos.writeUTF("MESSAGE");
		dos.writeUTF(msg);
		byte data[] = bos.toByteArray();
		//loop to send to all clients in clientList
		for (SocketAddress i : clientList) {
			DatagramPacket sendResponse = new DatagramPacket(data, data.length, i);	
			sock.send(sendResponse);
			log.log("Sent message to " + i.toString() + " " + msg);
		}	

	}

}

