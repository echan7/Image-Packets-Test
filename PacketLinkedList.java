/**
 * A Single-linked linkedlist with a "dumb" header node (no data in the node),
 * but without a tail node. It implements ListADT&lt;E&gt; and returns
 * PacketLinkedListIterator when requiring a iterator.
 * 
 * @author honghui
 */
public class PacketLinkedList<E> implements ListADT<E> {
	// TODO: add your fields here
	private Listnode<E> head;
	private int numItems;

	/**
	 * Constructs a empty PacketLinkedList
	 */
	public PacketLinkedList() {
		// TODO
		head = new Listnode<E>(null);
		numItems = 0;
	}

	@Override
	public void add(E item) {
		// TODO
		if(item == null){
			throw new IllegalArgumentException();
		}
		Listnode<E> newNode = new Listnode<E>(item);
		Listnode<E> curr = head;
		while(curr.getNext() != null){
			curr = curr.getNext();
		}
		curr.setNext(newNode);
		numItems ++;
	}

	@Override
	public void add(int pos, E item) {
		// TODO
		if(pos < 0 || pos > numItems){
			throw new IndexOutOfBoundsException();
		}
		if(item == null){
			throw new IllegalArgumentException();
		}
		Listnode<E> newNode = new Listnode<E>(item);
		Listnode<E> curr = head;
		if(pos == numItems){
			add(item);
			return;
		}
		
		for(int i=0; i<pos; i++){
			curr = curr.getNext();
		}
		newNode.setNext(curr.getNext());
		curr.setNext(newNode);
		numItems++;
	}

	@Override
	public boolean contains(E item) {
		// TODO
		Listnode<E> curr = head;
		while(curr.getNext() != null){
			if(curr.getNext().getData().equals(item)){
				return true;
			}
			curr = curr.getNext();
		}
		return false;
	}

	@Override
	public E get(int pos) {
		// TODO
		if(pos < 0 || pos >= numItems){
			throw new IndexOutOfBoundsException();
		}
		
		Listnode<E> curr = head.getNext();
		for(int i=0; i<pos;i++){
			curr = curr.getNext();
		}
		return curr.getData();
	}

	@Override
	public boolean isEmpty() {
		// TODO
		if(numItems == 0){
			return true;
		}
		return false;
	}

	@Override
	public E remove(int pos) {
		// TODO
		if(pos<0 || pos >=numItems){
			throw new IndexOutOfBoundsException();
		}
		
		Listnode<E> curr = head;
		for(int i =0; i < pos;i++){
			curr = curr.getNext();
		}
		E data = curr.getNext().getData();
		
		curr.setNext(curr.getNext().getNext());
		numItems--;
		return data;
	}

	@Override
	public int size() {
		// TODO
		return numItems;
	}

	@Override
	public PacketLinkedListIterator<E> iterator() {
		// TODO
		return new PacketLinkedListIterator<E>(head.getNext());
	}

}
