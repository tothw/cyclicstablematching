package wjtoth.cyclicstablematching;

import java.util.ArrayList;
import java.util.Arrays;

public class Permutations {

	public static ArrayList<int[]> permutations(int n) {
		return permutations(array(n));
	}

	public static ArrayList<int[]> permutationsOfAllSubsets(int n) {
		return permutationsOfAllSubsets(array(n));
	}

	public static int[] array(int n) {
		int[] retval = new int[n];
		for(int i = 0; i<n;++i) {
			retval[i] = i;
		}
		return retval;
	}

	public static ArrayList<int[]> permutationsOfAllSubsets(int[] array) {
		int subsets = (int)Math.pow(2, array.length);
		int[] indices = new int[array.length];

		ArrayList<int[]> retval = new ArrayList<>();

		for(int k = 1; k<subsets; ++k) {
			int[] subset = new int[array.length];
			for(int i = 0; i<indices.length;++i) {
				int include = (k >> i) % 2;
				if(include == 1) {
					subset[i] = array[i];
				} else{
					subset[i] = -1;
				}
			}
			retval.addAll(permutations(subset));
		}

		return retval;
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
