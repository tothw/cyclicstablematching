package wjtoth.cyclicstablematching;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class PerfectMatching implements Comparable<PerfectMatching>{
	
	private final int NUMBER_OF_GROUPS;
	private final int NUMBER_OF_AGENTS;
	
	//Store matching
	ArrayList<int[]> matching;
	private String hash;

	public PerfectMatching(int numberOfGroups, int numberOfAgents) {
		NUMBER_OF_GROUPS = numberOfGroups;
		NUMBER_OF_AGENTS = numberOfAgents;
		matching = new ArrayList<int[]>(numberOfGroups);
	}

	public void setMatchingFromPermutations(ArrayList<int[]> permutationProduct) {
		for (int i = 0; i < NUMBER_OF_AGENTS; ++i) {
			int[] match = new int[NUMBER_OF_GROUPS];
			for (int j = 0; j < NUMBER_OF_GROUPS; ++j) {
				match[j] = permutationProduct.get(j)[i];
			}
			try {
				matching.set(i, match);
			} catch (IndexOutOfBoundsException e) {
				matching.add(i, match);
			}
		}
		sortAndHash();
	}

	private void sortAndHash() {
		Collections.sort(matching, new Comparator<int[]>() {
			@Override
			public int compare(int[] o1, int[] o2) {
				if (o1.length != o2.length) {
					return o1.length - o2.length;
				}
				for (int i = 0; i < o1.length; ++i) {
					if (o1[i] != o2[i]) {
						return o1[i] - o2[i];
					}
				}
				return 0;
			}
		});
		StringBuffer sb = new StringBuffer();
		for (int[] match : matching) {
			for (int agent : match) {
				sb.append(agent);
			}
		}
		hash = sb.toString();
	}

	public void setMatching(ArrayList<int[]> matching) {
		this.matching = matching;
		sortAndHash();
	}
	
	public int getPartner(int group, int agent) {
		int partner = -1;
		for(int[] match : matching) {
			if(match[group] == agent) {
				partner = match[(group+1)%match.length];
			}
		}
		return partner;
	}

	public ArrayList<int[]> getMatching() {
		return matching;
	}

	public String toString() {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("---------\n");
		for(int i = 0; i< matching.size(); ++i) {
			int[] orderedMatch = matching.get(i);
			stringBuffer.append("(");
			for(int j = 0; j< orderedMatch.length; ++j) {
				stringBuffer.append(orderedMatch[j] + " ");
			}
			stringBuffer.append(")\n");
		}
		stringBuffer.append("---------");
		return stringBuffer.toString();
	}

	public int getNUMBER_OF_GROUPS() {
		return NUMBER_OF_GROUPS;
	}

	public int getNUMBER_OF_AGENTS() {
		return NUMBER_OF_AGENTS;
	}

	public String getHash() {
		return hash;
	}

	public int compareTo(PerfectMatching perfectMatching) {
		return hash.compareTo(perfectMatching.getHash());
	}

	@Override
	public boolean equals(Object obj) {
		if(obj.getClass() != getClass()) {
			return false;
		}
		PerfectMatching perfectMatching = (PerfectMatching)obj;
		return hash.equals(perfectMatching.getHash());
	}

	@Override
	public int hashCode() {
		if(hash == null) {
			return 0;
		}
		return hash.hashCode();
	}

	public boolean validate() {
		for(int[] match : matching) {
			for(int i = 0; i < match.length; ++i) {
				boolean isMatched = match[i] >= 0;
				boolean nextIsMatched = match[(i+1) % match.length] >=0;
				if((isMatched && !nextIsMatched) || (!isMatched && nextIsMatched)) {
					return false;
				}
			}
		}
		return true;
	}

	public PerfectMatching extend(int[] permutation) {
		if(permutation.length != NUMBER_OF_AGENTS) {
			System.out.println("Cannot extend with given permutation: " + Arrays.toString(permutation));
			return null;
		}
		ArrayList<int[]> extendedMatching = new ArrayList<int[]>();
		int k = 0;
		for(int[] match : matching) {
			int[] extendedMatch = new int[match.length + 1];
			for(int i = 0; i < match.length; ++i) {
				extendedMatch[i] = match[i];
			}
			extendedMatch[match.length] = permutation[k];
			++k;
		}
		PerfectMatching retval = new PerfectMatching(getNUMBER_OF_GROUPS() + 1, getNUMBER_OF_AGENTS());
		retval.setMatching(extendedMatching);
		return retval;
	}
}
