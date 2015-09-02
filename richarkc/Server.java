/*
   Scott Campbell
   CSE383-f15

   Lab1

   Sample datagram server

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
		log = new Log("UDP Server.log");
	}

	//main working code for server
	/*
	Listens for message from client and sends them the previous message received
	*/
	public void Main() {
		String msg = "first";
		System.out.println("Server Starting");	//only message I will send to std out
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

				log.log(pkt.getSocketAddress() + " Got Packet" );
				ByteArrayInputStream bis= new ByteArrayInputStream(b);
				DataInputStream dis = new DataInputStream(bis);
				String newmsg = dis.readUTF();
				System.out.println("Got a packet from " + pkt.getSocketAddress());
				System.out.println(newmsg);
				
				if (newmsg.startsWith("HELLO")) {
					if (!(clientList.contains(pkt.getSocketAddress()))) {
						clientList.add(pkt.getSocketAddress());
						sendMessage("HELLO-RESPONSE","", pkt.getSocketAddress());
					}
				
						
				}
				
				else if (newmsg.startsWith("MESSAGE")) {
					sendAll(dis.readUTF());
				}

				
				else if (newmsg.startsWith("GOODBYE")){
					sendMessage("GOODBYE-RESPONSE","",pkt.getSocketAddress());
					clientList.remove(pkt.getSocketAddress());
				}
					

			} catch (IOException err) {
				log.log("Error " + err);
			}
		}
	}

	public void sendMessage(String msg, String msg2, SocketAddress add) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		dos.writeUTF(msg);
		dos.writeUTF(msg2);
		byte data[] = bos.toByteArray();
		DatagramPacket sendResponse = new DatagramPacket(data,data.length,add);
		sock.send(sendResponse);
	}

	public void sendAll(String msg) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		dos.writeUTF("MESSAGE");
		dos.writeUTF(msg);
		byte data[] = bos.toByteArray();
		for (SocketAddress i : clientList) {
			DatagramPacket sendResponse = new DatagramPacket(data, data.length, i);	
			sock.send(sendResponse);
		}	

	}

}

