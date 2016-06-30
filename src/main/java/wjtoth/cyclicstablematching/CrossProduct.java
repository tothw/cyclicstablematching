package wjtoth.cyclicstablematching;

import java.util.ArrayList;

public class CrossProduct<T> {
	
	private ArrayList<T> set;
	private int dimension;

	private int[] indices;
	private boolean hasNext;
	
	public CrossProduct(ArrayList<T> set, int dimension) {
		this.set = set;
		this.dimension = dimension;
		indices = new int[dimension];
		hasNext = true;
	}
	
	public boolean hasNext() {
		return hasNext;
	}
	
	public ArrayList<T> next() {
		ArrayList<T> retval = new ArrayList<T>();
		for(int i = 0; i<dimension; ++i) {
			retval.add(set.get(indices[i]));
		}
		incrementIndices();
		return retval;
	}
	
	public void incrementIndices() {
		int index = indices.length-1;
		boolean successFlag = false;
		while(index >= 0) {
			indices[index] = (indices[index] + 1) % set.size();
			if(indices[index] == 0) {
				--index;
			}else{
				successFlag = true;
				break;
			}
		}
		if(!successFlag) {
			hasNext = false;
		}
	}
	
	public void reset() {
		for(int i = 0; i<indices.length; ++i) {
			indices[i] = 0;
		}
		hasNext = true;
	}
	
	public int getDimension() {
		return dimension;
	}
	
	public int getLength() {
		return indices.length;
	}
}
