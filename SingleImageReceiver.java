import java.io.IOException;
import java.util.Iterator;

/**
 * This class simulates a receiver application for a single image by maintaining
 * an image buffer, which is a linked list of packets of the transmitted image
 * file. It collects packets from our InputDriver and reconstructs the image
 * file using a PacketLinkedList&lt;SimplePacket&gt; for the image buffer.
 *
 * @author honghui
 */
public class SingleImageReceiver {
	private InputDriver input;
	private PacketLinkedList<SimplePacket> list;

	/**
	 * Constructs a Receiver to obtain the image file transmitted.
	 * 
	 * @param file
	 *            the filename you want to receive
	 * 
	 * @throws IOException
	 *             if fails to retrieve the file
	 */
	public SingleImageReceiver(String file) throws IOException {
		input = new InputDriver(file);
		list = new PacketLinkedList<SimplePacket>();
	}

	/**
	 * Returns the PacketLinkedList buffer in the receiver
	 * 
	 * @return the PacketLinkedList object
	 */
	public PacketLinkedList<SimplePacket> getListBuffer() {
		return list;
	}

	/**
	 * Asks for retransmitting the packet with a sequence number. The requested
	 * packet will arrive later by using {@link #askForNextPacket()}. Notice
	 * that ONLY missing packet will be retransmitted. Pass seq=0 if the missing
	 * packet is the "End of Streaming Notification" packet.
	 * 
	 * @param seq
	 *            the sequence number of the requested missing packet
	 * @return true if the requested packet is added in the receiving queue;
	 *         otherwise, false
	 */
	public boolean askForMissingPacket(int seq) {
		return input.resendMissingPacket(seq);
	}

	/**
	 * Returns true if the maintained list buffer has a valid image content.
	 * Notice that when it returns false.
	 * 
	 * @return true if the maintained list buffer has a valid image content;
	 *         otherwise, false
	 */
	public boolean validImageContent() {
		return input.validFile(list);
	}

	/**
	 * Returns the next packet.
	 * 
	 * @return the next SimplePacket object; returns null if no more packet to
	 *         receive
	 */
	public SimplePacket askForNextPacket() {
		return input.getNextPacket();
	}

	/**
	 * Outputs the formatted content in the PacketLinkedList buffer. See course
	 * webpage for the formatting detail.
	 * 
	 * @param list
	 *            the PacketLinkedList buffer
	 */
	public void displayList(PacketLinkedList<SimplePacket> list) {
		// TODO
		for(SimplePacket e : list){
			if(!e.equals(list.get(list.size()-1))){
				System.out.print(e.getSeq()+", ");
			}else{
				System.out.print(e.getSeq()+"\n");
			}
		}
	}

	/**
	 * Reconstructs the file by arranging the {@link PacketLinkedList} in
	 * correct order. It uses {@link #askForNextPacket()} to get packets until
	 * no more packet to receive. It eliminates the duplicate packets and asks
	 * for retransmitting for end-of-stream notification or missing packets.
	 */
	public void reconstructFile() {
		// TODO
		SimplePacket packetToAdd;
		System.out.print("Asking for packets using loop \n");
		do{
			// request for a new packet everytime loop runs till no more are left
			// assign the packet to the variable declared earlier
			 packetToAdd = askForNextPacket();
			 //add that packet to the simplePacketList
			 if(packetToAdd!=null){
			 list.add(packetToAdd);
			 }
			 System.out.print(packetToAdd.getSeq()+", ");
		}while(packetToAdd.getSeq()>0 && null!=packetToAdd); /* stops when end of 
		streaming notification is added or when no more packets are left to rcv */
		System.out.println("\nDone Asking for Packets. Now Checking if EON was added");
		
		// for understanding only please delete if clause after program done
		if(askForMissingPacket(0)){
			System.out.print("EON was not added. Go in loop to ask for missing packets\n");
		}else{
			System.out.print("EON was added. Skip loop because there is no need to ask for missing packets\n");
		}
		// please delete if clause after done
		while(askForMissingPacket(0)){
			
			packetToAdd=askForNextPacket();
			
			if(packetToAdd!=null){
				list.add(packetToAdd);
			}
		}
		// since we know the last packet must be the endOfNotification packet
		// we know packetToAdd has last been assigned the EON packet
		int oriNumPackets=packetToAdd.getSeq()*-1;
		//run a loop to check if all sequences are present in loop
		for(int i =1; i<=oriNumPackets; i++){
			while(askForMissingPacket(i)){
				packetToAdd=askForNextPacket();
				System.out.println("Asking for missing packet for seq:" + i);
				if(packetToAdd!=null){
					list.add(packetToAdd);
					
				}
			}
		}
		System.out.println("\nDone adding missing packets. Add packets according to sequences");
		PacketLinkedList<SimplePacket>sortedList = new PacketLinkedList<SimplePacket>();
		//create a new PacketLinkedList and add Packets according to sequences.
		boolean added= false;
		for(int i =1; i<=oriNumPackets; i++){
			added=false;
			for(int j=0; j<list.size()&&!added; j++){
				SimplePacket PacketForChecking = list.get(j);
				System.out.print("Is seq "+ PacketForChecking.getSeq()+" == "+i+" ? ");
			 if(PacketForChecking.getSeq()==i){	
				 System.out.println("\nSequence "+ i+" added");
				 sortedList.add(PacketForChecking);
				 added=true;
			 }
			}
		}
		
		list=sortedList;	

		 displayList(list); //uncomment during debugging only
		}
		
		
	
		
	}

