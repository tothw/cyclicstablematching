package wjtoth.cyclicstablematching;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class CrossProduct<T> {
	
	private ArrayList<ArrayList<T>> sets;
	private int dimension;
	private int[] indices;
	private boolean hasNext;


	public CrossProduct(ArrayList<T> set, int dimension) {
		ArrayList<ArrayList<T>> sets = new ArrayList<>(dimension);
		for(int i = 0; i<dimension; ++i) {
			sets.add(set);
		}
		initialize(sets);
	}

	public CrossProduct(ArrayList<ArrayList<T>> sets) {
		initialize(sets);
	}

	private void initialize(ArrayList<ArrayList<T>> sets) {
		this.sets = sets;
		this.dimension = sets.size();
		indices = new int[dimension];
		hasNext = true;
	}
	
	public boolean hasNext() {
		return hasNext;
	}
	
	public ArrayList<T> next() {
		ArrayList<T> retval = new ArrayList<T>();
		for(int i = 0; i<dimension; ++i) {
			retval.add(sets.get(i).get(indices[i]));
		}
		incrementIndices();
		return retval;
	}
	
	public void incrementIndices() {
		int index = indices.length-1;
		boolean successFlag = false;
		while(index >= 0) {
			ArrayList<T> set = sets.get(index);
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
