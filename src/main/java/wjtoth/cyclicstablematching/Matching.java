package wjtoth.cyclicstablematching;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class Matching implements Comparable<Matching>{
	
	//number of genders
	private final int NUMBER_OF_GROUPS;
	//number of agents per gender
	private final int NUMBER_OF_AGENTS;
	
	//Store matching
	ArrayList<int[]> matching;
	//string which represents matching
	private String hash;

	//number of non-empty matches
	private int size;

	public Matching(int numberOfGroups, int numberOfAgents) {
		NUMBER_OF_GROUPS = numberOfGroups;
		NUMBER_OF_AGENTS = numberOfAgents;
		matching = new ArrayList<int[]>(numberOfGroups);
		size = -1;
	}

	/**
	 * Converts list of permutations to a matching
	 * @param permutationProduct list of size NUMBER_OF_GROUPS where each
	 *  int[] is a permutation of [0,..., NUMBER_OF_AGENTS - 1]
	 */
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

	/**
	 * Arranges matching tuples in lex order, and
	 * Computes hash string
	 */
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

	/**
	 * sets matching from matching
	 * @param matching NUMBER_OF_AGENTS tuples of sizes NUMBER_OF_GROUPS
	 * int[]'s should have disjoint intersection
	 */
	public void setMatching(ArrayList<int[]> matching) {
		this.matching = matching;
		sortAndHash();
	}
	
	/**
	 * returns index of partner that agent in group has preference over
	 * @param group index of gender agent belongs to
	 * @param agent index of agent
	 * @return
	 */
	public int getPartner(int group, int agent) {
		int partner = -1;
		for(int[] match : matching) {
			if(match[group] == agent) {
				partner = match[(group+1)%match.length];
			}
		}
		return partner;
	}

	/**
	 * 
	 * @return list of tuples representing matching
	 */
	public ArrayList<int[]> getMatching() {
		return matching;
	}

	/**
	 *  Pretty prints matching
	 */
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

	/**
	 * Standard Getters
	 */
	public int getNUMBER_OF_GROUPS() {
		return NUMBER_OF_GROUPS;
	}

	public int getNUMBER_OF_AGENTS() {
		return NUMBER_OF_AGENTS;
	}

	public String getHash() {
		return hash;
	}

	/**
	 * Matchings are compared lexicographically via hash strings,
	 * hence the need to sort.
	 */
	public int compareTo(Matching perfectMatching) {
		return hash.compareTo(perfectMatching.getHash());
	}

	@Override
	public boolean equals(Object obj) {
		if(obj.getClass() != getClass()) {
			return false;
		}
		Matching perfectMatching = (Matching)obj;
		return hash.equals(perfectMatching.getHash());
	}

	@Override
	public int hashCode() {
		if(hash == null) {
			return 0;
		}
		return hash.hashCode();
	}

	/**
	 * @return true iff matching is proper
	 */
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

	/**
	 * 
	 * @param permutation a NUMBER_OF_AGENTS size permutation
	 * @return matching with additional gender taken by appending permutation
	 */
	public Matching extend(int[] permutation) {
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
			extendedMatching.add(extendedMatch);
			++k;
		}
		Matching retval = new Matching(getNUMBER_OF_GROUPS() + 1, getNUMBER_OF_AGENTS());
		retval.setMatching(extendedMatching);
		return retval;
	}
	
	/**
	 * 
	 * @param group index of gender
	 * @param agent index of agent in gender
	 * @return true iff agent has a partner in this matching
	 */
	public boolean isMatchedInGroup(int group, int agent) {
		for(int[] match : matching) {
			if(match[group] == agent) {
				if(match[(group + 1) % NUMBER_OF_GROUPS] != -1) {
					return true;
				}else{
					return false;
				}
			}
		}
		return false;
	}

	public int size() {
		if(size == -1) {
			int count = 0;
			for (int[] match : matching) {
				boolean nonNegative = true;
				for (int i = 0; i < match.length; ++i) {
					if (match[i] == -1) {
						nonNegative = false;
						break;
					}
				}
				if (nonNegative) {
					++count;
				}
			}
			size = count;
		}
		return size;
	}

	public boolean inMatching(int groupIndex, int agentIndex) {
		for(int[] match : matching) {
			if(match[groupIndex] == agentIndex) {
				return true;
			}
		}
		return false;
	}
}
