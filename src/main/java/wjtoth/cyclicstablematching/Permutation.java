package wjtoth.cyclicstablematching;

import java.util.ArrayList;

public class Permutation {

	public static ArrayList<int[]> permutations(int n) {
		int[] array = new int[n];
		for(int i = 0; i<n; ++i) {
			array[i] = i;
		}
		return permutations(array);
	}
	
	public static ArrayList<int[]> permutations(int[] array) {
	    ArrayList<int[]> retval = new ArrayList<int[]>();
	    if (array.length == 1) {
	        retval.add(array);
	    } else if (array.length > 1) {
	        int lastIndex = array.length - 1;
	        int last = array[lastIndex];
	        int[] rest = new int[array.length-1];
	        for(int i = 0; i<rest.length; ++i) {
	        	rest[i] = array[i];
	        }
	        retval = merge(permutations(rest), last);
	    }
	    return retval;
	}
	
	public static ArrayList<int[]> merge (ArrayList<int[]> list, int addend) {
		ArrayList<int[]> retval = new ArrayList<int[]>();
		for(int[] prev : list) {
			for(int i = 0; i<prev.length+1; ++i) {
				int[] next = new int[prev.length+1];
				for(int j = 0; j<i; ++j) {
					next[j] = prev[j];
				}
				next[i] = addend;
				for(int j = i+1; j<next.length; ++j) {
					next[j] = prev[j-1];
				}
				retval.add(next);
			}
		}
		return retval;
	}
}
