package wjtoth.cyclicstablematching;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Iterates over all tuples in cross product of passed in lists
 * 
 * @author wjtoth
 *
 * @param <T> Type of sets to take Cross Product
 */
public class CrossProduct<T> {
	
	//lists to be crossed
	private ArrayList<List<T>> sets;
	//number of positions in tuples
	private int dimension;
	//used to track which tuple iterator is at
	private int[] indices;
	//flag true iff there are tuples left to iterate over
	private boolean hasNext;


	/**
	 * Constructs CrossProduct by crossing set with itself dimension times
	 * @param set
	 * @param dimension
	 */
	public CrossProduct(List<T> set, int dimension) {
		ArrayList<List<T>> sets = new ArrayList<>(dimension);
		for(int i = 0; i<dimension; ++i) {
			sets.add(set);
		}
		initialize(sets);
	}

	/**
	 * Constructs CrossProduct by crossing sets against each other
	 * dimension will be sets.size()
	 * @param sets
	 */
	public CrossProduct(ArrayList<List<T>> sets) {
		initialize(sets);
	}

	/**
	 * Initialization of class common to both constructors
	 * @param sets
	 */
	private void initialize(ArrayList<List<T>> sets) {
		this.sets = sets;
		this.dimension = sets.size();
		indices = new int[dimension];
		hasNext = true;
	}
	
	public boolean hasNext() {
		return hasNext;
	}
	
	/**
	 * Computes next tuple to return
	 * by incrementing indices which is a list
	 * of which index in respective set to take
	 * next tuple from
	 * @return
	 */
	public ArrayList<T> next() {
		ArrayList<T> retval = new ArrayList<T>();
		for(int i = 0; i<dimension; ++i) {
			retval.add(sets.get(i).get(indices[i]));
		}
		incrementIndices();
		return retval;
	}
	
	/**
	 * count up indices lexicographically
	 */
	public void incrementIndices() {
		int index = indices.length-1;
		boolean successFlag = false;
		while(index >= 0) {
			List<T> set = sets.get(index);
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
	
	/**
	 * set all indices to 0,
	 * in effect restarting iteration
	 */
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
