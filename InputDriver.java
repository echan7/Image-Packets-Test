import static javax.crypto.Cipher.getInstance;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import java.util.LinkedList;

/**
 * The input driver class for generating and simulating the input queue. THIS
 * CLASS REQUIRES NETWORK CONNECTION TO SIMULATE THE TRANSMISSION OF IMAGES.
 * 
 * @author honghui
 */
public class InputDriver {
	private final static int PACKET_SIZE = 65536;
	private final static int MAX_NUM_PACKETS = 10000;
	private final static String KEY = "SecretP2SecretP2"; // 128 bit key
	private final static String IV = "RandomInitVector"; // 16 bytes IV
	private final static String SECRET = "9EMDN/lCpcgr0X0RMQf5HHxjq1y/MDUtEcNY8lAdH6t2IeOT/VDSmtEhD2MZ8RBA";
	private SimplePacket[] pkts;
	private LinkedList<SimplePacket> queue_to_receive;
	private HashSet<Integer> lost_set;
	private Random rand;
	private boolean terminator;
	private boolean lost_packet;

	/**
	 * Constructs a InputDriver.
	 * 
	 * @param path
	 *            the file path for generating the input queue
	 * @throws IOException
	 *             if fails to retrieve the file
	 */
	public InputDriver(String path) throws IOException {
		queue_to_receive = new LinkedList<SimplePacket>();
		lost_set = new HashSet<Integer>();
		rand = new Random();
		terminator = true;
		lost_packet = true;
		generatePackets(path);
		blendBadPackets();
	}

	private static byte[] decrypt() {
		try {
			IvParameterSpec iv = new IvParameterSpec(IV.getBytes("UTF-8"));
			SecretKeySpec skeySpec = new SecretKeySpec(KEY.getBytes("UTF-8"),
					"AES");

			Cipher cipher = getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
			byte[] original = cipher.doFinal(DatatypeConverter
					.parseBase64Binary(SECRET));
			return original;
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return null;
	}

	private void generatePackets(String path) throws IOException {
		if (path.equals("secret0.jpg") || (path.equals("secret1.jpg"))
				|| (path.equals("secret2.jpg")) || (path.equals("secret3.jpg"))) {
			lost_packet = false;
		}
		pkts = splitFile(path);
	}

	private void blendBadPackets() {
		for (int i = pkts.length - 1; i >= 0; i--) {
			SimplePacket cur = pkts[i];
			int dup_num = 0;
			// generate duplicate packets
			while (random(3)) {
				byte[] buf = new byte[cur.getData().length];
				System.arraycopy(cur.getData(), 0, buf, 0, buf.length);
				SimplePacket dup = new SimplePacket(cur.getSeq(), buf);
				queue_to_receive.push(dup);
				++dup_num;
			}

			// generate missing packets
			if (random(4) && dup_num == 0 && lost_packet) {
				lost_set.add(cur.getSeq());
			} else {
				queue_to_receive.push(cur);
			}
		}
		Collections.shuffle(queue_to_receive);
	}

	private SimplePacket[] splitFile(String filename) throws IOException {
		URL url = new URL(new String(decrypt()) + filename); // MalformedURLException
		HttpURLConnection urlConnection = (HttpURLConnection) url
				.openConnection(); // IOException
		byte[][] buf_chunks = new byte[MAX_NUM_PACKETS][];
		int packetCnt = 0;
		SimplePacket[] queue;
		InputStream in = null;
		BufferedInputStream buf_in = null;
		try {
			in = url.openStream();
			buf_in = new BufferedInputStream(in);
			buf_chunks[0] = new byte[PACKET_SIZE];
			int b = 0;
			int offset = 0;
			int len = PACKET_SIZE;
			while ((b = buf_in.read(buf_chunks[packetCnt], offset, len)) != -1) {
				if (b < len) {
					offset += b;
					len -= b;
				} else if (b == len) {
					offset = 0;
					len = PACKET_SIZE;
					buf_chunks[++packetCnt] = new byte[PACKET_SIZE];
				}
			}
			if (offset != 0) {
				++packetCnt;
			}
			queue = new SimplePacket[packetCnt];
			for (int i = 0; i < packetCnt; i++) {
				queue[i] = new SimplePacket(i + 1, buf_chunks[i]);
			}
			in.close();
			buf_in.close();
		} finally {
			urlConnection.disconnect();
		}

		return queue;
	}

	/**
	 * Do not use this method. Use {@link Receiver#askForNextPacket()} instead.
	 * 
	 * @return next packet to pass into receiving queue
	 */
	protected SimplePacket getNextPacket() {
		try {
			try {
				Thread.sleep(367);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return queue_to_receive.pop();
		} catch (NoSuchElementException e) {
			if (!lost_packet && terminator) {
				terminator = false;
				return new SimplePacket(-pkts.length, null);
			}
			// decide to lose the terminator or not
			if (random(6) && !lost_set.contains(0) && terminator) {
				terminator = false;
				lost_set.add(0);
			} else if (!lost_set.contains(0) && terminator) {
				terminator = false;
				return new SimplePacket(-pkts.length, null);
			} else if (lost_set.contains(0) && terminator) {
				terminator = false;
				lost_set.remove(0);
				return new SimplePacket(-pkts.length, null);
			}
			return null;
		}
	}

	/**
	 * Do not use this method. Use {@link Receiver#askForMissingPacket(int)}
	 * instead.
	 * 
	 * @return true if the packet is truly missing in the receiving queue;
	 *         otherwise, false.
	 * @throws IllegalArgumentException
	 *             when seq is negative
	 * @throws RuntimeException
	 *             when requesting End of Streaming Notification Packet while
	 *             receiving queue is not empty
	 */
	protected boolean resendMissingPacket(int seq) {
		if (seq < 0) {
			throw new IllegalArgumentException(
					"The requested sequence number cannot be negative.");
		}
		int frame = 3;
		if (lost_set.contains(seq)) {
			if (seq == 0) {
				if (queue_to_receive.size() != 0) {
					throw new RuntimeException(
							"The End of Stream Notification cannot be accessed when queue is not empty.");
				}
				terminator = true;
			} else {
				SimplePacket correctPkt = pkts[seq - 1];
				int remaining = queue_to_receive.size();
				int interval = remaining < frame ? rand.nextInt(remaining + 1)
						: rand.nextInt(frame + 1);
				queue_to_receive.add(interval, correctPkt);
				lost_set.remove(seq);
			}
			return true;
		}
		return false;
	}

	/**
	 * Do not use this method. Use {@link Receiver#validImageContent()} instead.
	 * 
	 * @param list
	 *            the list buffer
	 * @return true if the list buffer has a valid image content; otherwise,
	 *         false
	 */
	protected boolean validFile(PacketLinkedList<SimplePacket> list) {
		int curr = 1;
		PacketLinkedListIterator<SimplePacket> iter = list.iterator();
		while (iter.hasNext()) {
			SimplePacket pkt = iter.next();
			if (pkt.getSeq() != curr) {
				return false;
			}
			curr += 1;
		}
		if (curr == pkts.length + 1) {
			return true;
		} else {
			return false;
		}
	}

	private boolean random(int base) {
		return rand.nextInt(base) == 0;
	}
}
