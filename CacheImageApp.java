import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

//import com.sun.corba.se.spi.orbutil.fsm.Input; // ???????

/**
 * This is the main class that simulates an image viewer application with a
 * cache. For each image, it checks whether the image is in the cache. If not,
 * it created a SingleImageReceiver object to build the image and then store it
 * in the cache. After getting the PacketLinkedList of the cache, we can simply
 * send it to the ImageDriver to open it.
 * 
 * @author honghui
 */
public class CacheImageApp {

	// Used to open an image from a SimplePacketList.
	// Keep it as field for performance to avoid having to
	// reconstruct the driver for each image.
	private ImageDriver img;

	// Used to store a list of PacketLists for completed images.
	// This means that we do not have to rebuild the PacketLinkedList for
	// images that we have already processed.
	private PacketLinkedList<CacheImage<SimplePacket>> cachePacketLinkedList;

	/**
	 * Constructs a CacheImageApp to build and return completed image linked
	 * lists for image files that have been transmitted. You need to initialize
	 * private members of this class.
	 */
	public CacheImageApp() {
		img = new ImageDriver();
		// TODO
		// initialize linkedList with a header node
		cachePacketLinkedList = new PacketLinkedList<CacheImage<SimplePacket>>();
	}

	/**
	 * Returns the CacheImage LinkedList, so that it may be tested independently
	 * of the rest of the program.
	 * 
	 * @return the CachePacket LinkedList
	 */
	public PacketLinkedList<CacheImage<SimplePacket>> getCachePacketLinkedList() {
		return cachePacketLinkedList;
	}

	/**
	 * Receive a image file. If the image is in the cache, it simply gets its
	 * packetLinkedList from the cache.
	 * 
	 * Otherwise, it creates a SingleImageReceiver object to receive the image
	 * and then stores it in the cache.
	 * 
	 * @param filename
	 *            the filename you want to receive
	 * @throws IOException
	 *             if the constructor of SingleImageReceiver fails and throw
	 *             IOException
	 * @throws InvalidImageException
	 *             if the image is not in the cache and the reconstructed list
	 *             is null
	 * 
	 * @return the packet list for the specific image file.
	 */
	public PacketLinkedList<SimplePacket> retrieveImage(String filename)
			throws IOException, InvalidImageException {
	
		// TODO
		// Find existing packet list if we have it in the cache
		boolean packetExist = false;
		CacheImage<SimplePacket> cacheImageCheck;
		PacketLinkedList<SimplePacket>simplePacket = null;
		
		for(int i=0; i<cachePacketLinkedList.size();i++ ){
			cacheImageCheck= cachePacketLinkedList.get(i);
			

			if(cacheImageCheck.getImageName().equals(filename)){
				packetExist = true;
				simplePacket = cacheImageCheck.getPacketLinkedList();
			}
		}

		// If no PacketLinkedList found in the cache, must build one
		if(packetExist){
			return simplePacket;
		}else{
		// build new image cache 
					//create a new img Receiver and pass in filename
					SingleImageReceiver imgRcv= new SingleImageReceiver(filename);
					//reconstruct the img received
					imgRcv.reconstructFile();
					// declare a new  PacketLinkList for storing simple image Packets
					simplePacket=imgRcv.getListBuffer();
					
					//declare a variable that will hold the packet intended to be added
					CacheImage<SimplePacket>newPacket = new CacheImage<SimplePacket>(filename, simplePacket);
					
				// If the reconstructed list is null, throw InvalidImageException.
				if( simplePacket==null){
						throw new InvalidImageException(filename);
				}else{
					
				// Otherwise add it to the cache for the next time.
					cachePacketLinkedList.add(newPacket);;
				
				}
		}
		// Return the newly build packet list	
		return simplePacket;
	
		
		
	}

	/**
	 * Opens the image file by sending the packet list for the image to the
	 * ImageDriver.
	 * 
	 * @param packetLinkedList
	 *            packet list for the image
	 */
	public void openImage(PacketLinkedList<SimplePacket> packetLinkedList) {
		try {
			img.openImage(packetLinkedList);
		} catch (Exception e) {
			System.out.println("Unable to open image packet list");
			e.printStackTrace();
		}
	}

	/**
	 * Initiates a CacheImageApp to build and open images.
	 * 
	 * @param args
	 *            command line arguments
	 */
	public static void main(String[] args) {
		CacheImageApp app = new CacheImageApp();
		Scanner sc = new Scanner(System.in);
		String filename = "";
		while (!filename.equals("0")) {
			System.out.print("Enter 0 to quit or image filename: ");
			filename = sc.nextLine().trim();
			if (filename.equals("0")) {
				System.out.println("Program End.");
			} else if (filename.isEmpty()) {
				System.out.println("The input is empty. Please input a vaild vaule.");
			} else {
				try {
					System.out.println("Retrieve Image = " + filename);
					PacketLinkedList<SimplePacket> packetList = app
							.retrieveImage(filename);
					app.openImage(packetList);
				} catch (FileNotFoundException e) {
					System.out.println("Sorry, " + filename
							+ " can not be retrieved.");
				} catch (Exception e) {
					System.out.println("Sorry, something unexpected happened.");
					// e.printStackTrace();  // uncomment during debugging only
				}
			}
		}
		sc.close();
	}
}
