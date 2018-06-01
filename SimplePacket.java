/**
 * The Packet structure which includes a sequence number, a checksum
 * (true/false), and the delivered data.
 *
 * @author honghui
 */
public class SimplePacket {
	private int seq;
	private byte[] data;

	/**
	 * Constructs a SimplePacket with sequence number, checksum validation, and
	 * the data.
	 * 
	 * @param seq
	 *            the sequence number starting from 1
	 * @param buf
	 *            the delivered data in the packet
	 */
	public SimplePacket(int seq, byte[] buf) {
		this.seq = seq;
		data = buf;
	}

	/**
	 * Returns the sequence number of the packet
	 * 
	 * @return the sequence number of the packet
	 */
	public int getSeq() {
		return seq;
	}

	/**
	 * Returns the bytes delivered by the packet
	 * 
	 * @return the bytes delivered by the packet
	 */
	public byte[] getData() {
		return data;
	}
}
