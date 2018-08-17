package client;

import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class sends and receives messages from the SMTP Server
 * 
 * @author Martin Holecek
 * 
 */
public class ClientHandler implements Runnable{
	private boolean sendLoop;
	private boolean testLoop;
	private DataProcessing process;
	private boolean mainLoop;
	private Logger logger;

	/**
	 * Constructor
	 * 
	 * @param session sends and receives messages from the SMTP Server
	 * @param scanner reads string from user
	 * @param logger is used to log any errors to the file or console
	 */
	public ClientHandler(Session session, Logger logger, Scanner scanner) {
		this.logger = logger;
		process = new DataProcessing(session, scanner);
		sendLoop = true;
		testLoop = true;
		mainLoop = true;
	}

	/**
	 * override method which will send messages to the SMTP Server
	 */
	public void run() {
		try {
			// Connection Establishment
			if(!process.checkServerConnection()) {
				process.sendQuit();
				return;
			}

			// Main program Loop
			while (mainLoop) {
				// Swich between MODES which user selected
				switch (process.selectMode()) {
				case SENDMODE:
					// Send Reset Command
					process.sendReset();
					
					// Send Helo Command
					process.sendHelo();

					// Send Message Loop
					while (sendLoop) {
						try {	
							// Send MAIL FROM Command
							process.sendMailFrom();

							// Send RCPT TO Command
							process.sendRecipient();

							// Send DATA Command and content of the mail
							process.sendData();

							// Check if user want to send more messages
							sendLoop = process.userChoice("\nDo you want to send new email? [y/n]:");

						} catch (IOException e) {
							// If data stream connection fail the program will be terminated
							logger.log(Level.SEVERE, "Server does not responding", e);
							System.out.println("\nServer does not responding. Program has been Terminated ...");
							return;
						}
					}
					sendLoop = true;
					break;
				case TESTMODE:
					// Test Mode Loop
					while (testLoop) {
						testLoop = process.startTestMode();
					}
					testLoop = true;
					break;
				case QUIT:
					mainLoop = false;
					break;
				default:
					break;
				}
			}

			// Close connection
			process.sendQuit();

		} catch (IOException e) {
			logger.log(Level.SEVERE, "Input Error", e);
			System.out.println("Program has been Terminated ...");
		}
	}
}