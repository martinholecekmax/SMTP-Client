package client;

import java.io.IOException;
import java.util.Scanner;

/**
 * This class handles commands sended to the SMTP Server
 * 
 * @author Martin Holecek
 * 
 */
public class DataProcessing {
	private Session session;
	private Scanner scanner;

	/**
	 * Constructor
	 * 
	 * @param session which handles sockets and data streams
	 * @param scanner takes user input
	 */
	public DataProcessing(Session session, Scanner scanner) {
		this.session = session;
		this.scanner = scanner;
	}

	/**
	 * Connection establishment check
	 * 
	 * @return true if connection is established and server can receive messages. 
	 * Otherwise return false.
	 * 
	 * @throws IOException if socket or data streams are unavailable 
	 */
	public boolean checkServerConnection() throws IOException {
		// Connection established
		String input = session.read();

		// Check if server response is OK
		if (!input.startsWith("2")) {
			//System.out.println("\nCan't establish connection, database unavailable.");
			System.out.println("\n" + input);
			return false;
		} else {
			System.out.println("\n" + input);
			return true;
		}
	}

	/**
	 * Send HELO Command to the SMTP Server
	 * 
	 * @throws IOException if socket or data streams are unavailable 
	 */
	public void sendHelo() throws IOException {
		boolean validDomain = true;
		do {
			// Print message to the user console
			System.out.println("\nPlease enter domain name: ");
			System.out.print("--> ");

			// Get Domain name from user input
			String userInput = scanner.nextLine();
			// Send Domain name to the SMTP Server

			session.write("HELO " + userInput);

			// Get Server response
			String serverResponse = session.read();
			System.out.println(serverResponse);

			// Check if server response is OK
			if(serverResponse.startsWith("2")) {
				validDomain = false;
			} else if (serverResponse.startsWith("503")) {
				// HELO Command already initialised
				validDomain = false;
			}
		}
		while (validDomain);
	}

	/**
	 * Send MAIL Command to the SMTP Server
	 * 
	 * @throws IOException if socket or data streams are unavailable 
	 */
	public void sendMailFrom() throws IOException {
		boolean validMail = true;
		do {
			// Print message to the user console
			System.out.println("\nWho do you want to send mail from: ");
			System.out.print("--> ");

			// Get Sender from user input
			String sender = scanner.nextLine();

			// Send Sender to the SMTP Server
			session.write("MAIL FROM:<" + sender + ">");

			// Get Server response
			String serverResponse = session.read();
			System.out.println(serverResponse);

			// Check if server response is OK
			if(serverResponse.startsWith("2")) {
				validMail = false;
			} else if (serverResponse.startsWith("503")) {
				// MAIL Command already sended
				validMail = false;
			}
		} while (validMail);
	}

	/**
	 * Send RCPT TO Command to the SMTP Server
	 * 
	 * @throws IOException if socket or data streams are unavailable 
	 */
	public void sendRecipient() throws IOException {
		boolean choice = true;
		do {
			// Print message to the user console
			System.out.println("\nWho do you want to sent it to: ");
			System.out.print("--> ");

			// Get Recipient from user input
			String recipient = scanner.nextLine();

			// Send Recipient to the SMTP Server
			session.write("RCPT TO:<" + recipient + ">");

			// Get Server response
			String serverResponse = session.read();
			System.out.println(serverResponse);

			// Check if server response is OK
			if(serverResponse.startsWith("2")) {
				// Check if user wants more recipients
				if (userChoice("Do you want add another? [y/n]: ")) {
					choice = true;
				} else {
					choice = false;
				}
			}
		} while (choice);
	}

	/**
	 *  Send DATA Command and content of the mail to the SMTP Server
	 * 
	 * @throws IOException if socket or data streams are unavailable 
	 */
	public void sendData() throws IOException {
		String line;
		String serverResponse = "";

		// Send DATA Command to the server
		session.write("DATA");

		// Server Response to the data command
		String response = session.read();

		do {
			if (response.startsWith("3")) {
				// Get subject from user input
				String data = getSubject();

				// Print message to the user console
				System.out.println("\nWhat's the message, type $SEND on a new line to verify or type $LONG to test length of the data message.");
				System.out.print("--> ");

				// Get line from user
				line = scanner.nextLine();

				// User must enter $SEND to send message
				while(!line.equalsIgnoreCase("$SEND")) {
					if (line.equals(".")) {
						// If user type dot then append extra dot
						line = line + ".";
					} else if (line.equalsIgnoreCase("$LONG")) {
						/*
						 *  If user enters $LONG then 1000 A's will be 
						 *  appended to the line. This is for testing
						 *  of the line too long at the server side.
						 */
						for (int i = 0; i < 1000; i++) {
							line += "A";
						}
					}

					// add <CRLF> to each line
					data += line + "\r\n";

					// Line indentation
					System.out.print("--> ");

					// Get next line
					line = scanner.nextLine();
				}

				// Append message terminator <CRLF>.<CRLF>
				data += ".\r\n";

				// Send email message to the server
				session.write(data);

				// Get Server response
				serverResponse = session.read();

				// Check if server response is OK
				if(!serverResponse.startsWith("2")){
					// Print Server response to the user console
					System.out.println(serverResponse);

					// Send DATA Command to the server
					session.write("DATA");
					
					// Server Response to the data command
					response = session.read();
					
					// Ask user to enter message again
					System.out.println("\nPlease enter message again:");
					
					// Clear line and data
					line = "";
					data = "";
				} else {
					// Print Server response to the user console
					System.out.println(serverResponse);
				}
			} else {
				// Send DATA Command to the server
				session.write("DATA");
				
				// Server Response to the data command
				response = session.read();

				// Print server response
				System.out.println(response);
			}
		} while(!serverResponse.startsWith("2"));
	}

	/**
	 * Get Subject from user input
	 * 
	 * @return subject if user entered any, else return empty string
	 */
	public String getSubject() {
		String subject;
		boolean choice = true;
		do {

			// Print message to the user console
			System.out.println("\nWhat's the subject: ");
			System.out.print("--> ");

			// Get subject from user input
			subject = scanner.nextLine();

			// Check if subject is empty
			if (subject.isEmpty()) {
				// Ask user if he wants send message without subject
				if (userChoice("That was blank. Are you sure? [y/n]: ")) {
					// Append <CRLF> at the end of the subject line
					choice = false;
					subject = "";
				}
			} else {
				subject = "Subject: " + subject + "\r\n";
				choice = false;
			}
		} while(choice);

		// Insert subject into data
		return subject;
	}

	/**
	 * Send QUIT Command to the SMTP Server
	 * 
	 * @throws IOException if socket or data streams are unavailable 
	 */
	public void sendQuit() throws IOException {
		// Send QUIT Command to the SMTP Server
		session.write("QUIT");

		// Check if server response is OK
		if(session.read().startsWith("2")) {
			System.out.println("\nProgram Terminated ...");

			// Close socket and data streams
			session.close();
		}
		System.out.println("Client Terminated");
	}

	/**
	 * User must enter [Y] for yes or [N] for no.
	 * Choice is not case sensitive
	 * 
	 * @param text is string printed to the user
	 * @return true if the answer is yes, and false if answer is no
	 */
	public boolean userChoice(String text) {
		String input;
		boolean choice = true;
		do {
			// Print message to the user console
			System.out.print(text);
			input = scanner.nextLine();

			// Check if user enter Y or N answer
			if (input.equalsIgnoreCase("N")) {
				return false;
			} else if (input.equalsIgnoreCase("Y")) {
				return true;
			}
		} while (choice);

		// This case should never happened
		return false;
	}

	/**
	 * Select mode of the client
	 * MODE 1 = TEST MODE (user can sent command manually to the SMTP server)
	 * MODE 2 = SEND MODE (user receive guided instructions and commands are automatically generated)
	 * QUIT 3 = QUIT PROGRAM
	 * 
	 * @return SelectionState.TESTMODE for TEST MODE,
	 *  SelectionState.SENDMODE for SEND MODE 
	 *  and SelectionState.QUIT for QUIT
	 */
	public SelectionState selectMode() {
		String input;
		boolean choice = true;
		do {
			// Print message to the user console
			System.out.println("\nPlease Select Mode: ");
			System.out.println("\n  TEST MODE ----- 1");
			System.out.println("  SEND MODE ----- 2");
			System.out.println("  QUIT PROGRAM -- 3");
			System.out.print("\nEnter Your Selecion: ");

			// Get string from user
			input = scanner.nextLine();

			// Check if user enter Y or N answer
			if (input.equals("1")) {
				return SelectionState.TESTMODE;
			} else if (input.equals("2")) {
				return SelectionState.SENDMODE;
			} else if (input.equals("3")) {
				return SelectionState.QUIT;
			}
			
			// Invalid input
			System.out.println("\nYou must select value from the list!");
			
		} while (choice);

		// This case should never happened
		return SelectionState.QUIT;
	}

	/**
	 * Start Test Mode which anables user to send 
	 * individual commands
	 * 
	 * @return true for next command, false if user 
	 * want to exit
	 * 
	 * @throws IOException if socket or data streams are unavailable 
	 */
	public boolean startTestMode() throws IOException {
		// Print message to the user console
		System.out.println("\nType [EXIT] to quit TEST MODE ");
		System.out.println("Type [LONG] to send 100 recipients to the server.");
		System.out.println("Please Enter SMTP Command: ");
		System.out.print("--> ");

		// Get Recipient from user input
		String command = scanner.nextLine();
		command = command.trim();

		if (command.equalsIgnoreCase("DATA")) {
			sendData();
		} else if (command.equalsIgnoreCase("EXIT")) {
			return false;
		} else if (command.equalsIgnoreCase("LONG")) {
			// This is used for test of the more then 100 recipients
			for (int i = 0; i < 101; i++) {
				session.write("RCPT TO:<MAX@MAX>");
				if (i == 100) {
					// Print last response to the console
					System.out.println(session.read());
				} else {
					session.read();
				}
			}
		} else {
			// Send Recipient to the SMTP Server
			session.write(command);

			// Get Server response
			String serverResponse = session.read();
			System.out.println(serverResponse);
		}
		return true;
	}

	/**
	 * Send Reset (RSET) Command to the server
	 * 
	 * @throws IOException if socket or data streams are unavailable
	 */
	public void sendReset() throws IOException {
		session.write("RSET");
		session.read();
	}
}
