package ascii;

/**
 * UTF-8 and ASCII Converter
 * 
 * @author Martin Holecek
 *
 */
public class ConvertToASCII {
	
	/**
	 * This static method converts string text to 7-bits ASCII and 
	 * when a symbol is greater than standard ASCII (for example
	 * Extended ASCII) then convert that symbol to UTF-8
	 * 
	 * @param input is a string text
	 * @return byte array of the 7-bits ASCII
	 */
	public static byte[] getAsciiBytesUTF(String input) {
		final StringBuilder out = new StringBuilder();
		for (int i = 0; i < input.length(); i++) {
			final char ch = input.charAt(i);
			if (ch <= 127) {
				out.append(ch);
			}
			else {
				out.append("\\u").append(String.format("%04x", (int)ch));
			}
		}
		return out.toString().getBytes();
	}

	/**
	 * This static method converts string to 7-bits ASCII (0-127)
	 * @param input is a string text
	 * @return byte array converted to the 7-bits ASCII
	 */
	public static byte[] getAsciiBytes(String input) {
		char[] character = input.toCharArray();
		byte[] ascii = new byte[character.length];
		for (int asciiValue = 0; asciiValue < character.length; asciiValue++) {
			ascii[asciiValue] = (byte)(character[asciiValue] & 0x007F);
		}
		return ascii;
	}
}
