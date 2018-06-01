/**
 * The CacheImage structure which includes an image name and it corresponding
 * PacketLinkedList.
 *
 * @author honghui
 */

public class CacheImage<E> {
	private String imageName;
	private PacketLinkedList<E> list;

	/**
	 * Constructs a CacheImage with an image name and it corresponding
	 * PacketLinkedList.
	 * 
	 * @param imageName
	 *            the image name
	 * @param list
	 *            it corresponding PacketLinkedList
	 */
	public CacheImage(String imageName, PacketLinkedList<E> list) {
		this.imageName = imageName;
		this.list = list;
	}

	/**
	 * Returns the image name of the CacheImage
	 * 
	 * @return the image name of the CacheImage
	 */
	public String getImageName() {
		return imageName;
	}

	
	/**
	 * Returns the PacketLinkedList of the CacheImage
	 * 
	 * @return the PacketLinkedList of the CacheImage
	 */
	public PacketLinkedList<E> getPacketLinkedList() {
		return list;
	}
}
