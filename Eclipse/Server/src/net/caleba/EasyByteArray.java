package net.caleba;

public class EasyByteArray {
	
	private int capacity;
	private int size;
	private byte[] data;
	
	public EasyByteArray() {
		capacity = 0;
		size = 0;
		data = new byte[capacity];
	}
	
	public EasyByteArray(byte[] start) {
		capacity = start.length;
		size = start.length;
		data = start;
	}
	
	public EasyByteArray(int initialCapacity) {
		capacity = initialCapacity;
		size = 0;
		data = new byte[initialCapacity];
	}
	
	public void add(byte[] addition) {
		if((size + addition.length) > capacity) {
			capacity = size + addition.length;
			byte[] newArray = new byte[capacity];
			for(int i=0; i<size; ++i) {
				newArray[i] = data[i];
			}
			data = newArray;
		}
		for(int i=0; i<addition.length; ++i) {
			data[i+size] = addition[i];
		}
		size += addition.length;
	}
	
	public void add(byte addition) {
		if(size == capacity) {
			++capacity;
			byte[] newArray = new byte[capacity];
			for(int i=0; i<size; ++i) {
				newArray[i] = data[i];
			}
			data = newArray;
		}
		data[size] = addition;
		++size;
	}
	
	public byte[] toArray() {
		if(size < capacity) {
			byte[] newArray = new byte[size];
			for(int i=0; i<size; ++i) {
				newArray[i] = data[i];
			}
		}
		return data;
	}
	
	public EasyByteArray removeBefore(byte[] id) {
		for(int i=0; i<=size+id.length; ++i) {
			boolean spot = true;
			for(int j=0; j<id.length; ++j) {
				if(data[i+j] != id[j]) {
					spot = false;
					break;
				}
			}
			if(spot) {
				byte[] newArray = new byte[capacity];
				for(int j=i+id.length; j<size; ++j) {
					newArray[j-i-id.length] = data[j];
				}
				data = newArray;
				size -= i + id.length;
				break;
			}
		}
		return this;
	}
	
	public void set(int position, byte newValue) {
		if(position >= size || position < 0) {
			throw new ArrayIndexOutOfBoundsException();
		}
		data[position] = newValue;
	}
	
}
