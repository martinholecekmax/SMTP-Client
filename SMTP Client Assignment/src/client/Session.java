package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import ascii.ConvertToASCII;

/**
 * This Class creates Session between 
 * server and client.
 * 
 * @author Martin Holecek
 *
 */
public class Session {

	public Socket socket = null;
	private DataInputStream input = null;
	private DataOutputStream output = null;

	/**
	 * Constructor
	 * 
	 * @param socket connection between server and client
	 * @throws IOException if the DataStream is not available
	 */
	public Session(Socket socket) throws IOException {
		this.socket = socket;
		input = new DataInputStream(socket.getInputStream());
		output = new DataOutputStream(socket.getOutputStream());
	}

	/**
	 * Close data stream and socket channel
	 */
	public void close() {
		try {
			input.close();
			output.close();
			socket.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Sent string message over the network (7-bits ASCII)
	 * @param msg is a message in string format
	 * @throws IOException if the DataStream is not available
	 */
	public void write(String msg) throws IOException {
		byte[] message = ConvertToASCII.getAsciiBytes(msg);
		output.writeInt(message.length);
		output.flush();
		output.write(message);
		output.flush();
	}

	/**
	 * Read incoming ASCII text and convert that to the
	 * string text
	 * @return text in string format
	 * @throws IOException if the DataStream is not available
	 */
	public String read() throws IOException {
			int length = input.readInt();  
			byte[] message = null;
			String data;
			if(length>0) {
				message = new byte[length];
				input.readFully(message, 0, message.length); // read the message
				data = new String(message, "UTF-8");
			} else {
				data = "";
			}
			return data;
	}
}