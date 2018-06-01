/**
 * This exception should be thrown whenever the file failed in retrieveImage
 * 
 * @author honghui
 */

public class InvalidImageException extends Exception {
	/**
	 * Constructs a InvalidImageException with a message
	 * 
	 * @param filename
	 *            the name of the failed file
	 */
	public InvalidImageException(String filename) {
		super(filename + " is not valid for this program");
	}
}
