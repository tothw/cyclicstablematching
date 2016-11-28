package wjtoth.cyclicstablematching;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class Matching implements Comparable<Matching> {

	// number of genders
	private final int NUMBER_OF_GROUPS;
	// number of agents per gender
	private final int NUMBER_OF_AGENTS;

	// Store matching
	ArrayList<int[]> matching;
	// string which represents matching
	private String hash;

	// number of non-empty matches
	private int size;

	public Matching(int numberOfGroups, int numberOfAgents) {
		NUMBER_OF_GROUPS = numberOfGroups;
		NUMBER_OF_AGENTS = numberOfAgents;
		matching = new ArrayList<int[]>(numberOfGroups);
		size = -1;
	}

	/**
	 * Converts list of permutations to a matching
	 * 
	 * @param permutationProduct
	 *            list of size NUMBER_OF_GROUPS where each int[] is a
	 *            permutation of [0,..., NUMBER_OF_AGENTS - 1]
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
	 * Arranges matching tuples in lex order, and Computes hash string
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
	 * 
	 * @param matching
	 *            NUMBER_OF_AGENTS tuples of sizes NUMBER_OF_GROUPS int[]'s
	 *            should have disjoint intersection
	 */
	public void setMatching(ArrayList<int[]> matching) {
		this.matching = matching;
		sortAndHash();
	}

	/**
	 * returns index of partner that agent in group has preference over
	 * 
	 * @param group
	 *            index of gender agent belongs to
	 * @param agent
	 *            index of agent
	 * @return
	 */
	public int getPartner(int group, int agent) {
		int partner = -1;
		for (int[] match : matching) {
			if (match[group] == agent) {
				partner = match[(group + 1) % match.length];
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
	 * Pretty prints matching
	 */
	public String toString() {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("---------\n");
		for (int i = 0; i < matching.size(); ++i) {
			int[] orderedMatch = matching.get(i);
			stringBuffer.append("(");
			for (int j = 0; j < orderedMatch.length; ++j) {
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
	 * Matchings are compared lexicographically via hash strings, hence the need
	 * to sort.
	 */
	public int compareTo(Matching perfectMatching) {
		return hash.compareTo(perfectMatching.getHash());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj.getClass() != getClass()) {
			return false;
		}
		Matching perfectMatching = (Matching) obj;
		return hash.equals(perfectMatching.getHash());
	}

	@Override
	public int hashCode() {
		if (hash == null) {
			return 0;
		}
		return hash.hashCode();
	}

	/**
	 * @return true iff matching is proper
	 */
	public boolean validate() {
		for (int[] match : matching) {
			for (int i = 0; i < match.length; ++i) {
				boolean isMatched = match[i] >= 0;
				boolean nextIsMatched = match[(i + 1) % match.length] >= 0;
				if ((isMatched && !nextIsMatched) || (!isMatched && nextIsMatched)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 
	 * @param permutation
	 *            a NUMBER_OF_AGENTS size permutation
	 * @return matching with additional gender taken by appending permutation
	 */
	public Matching extend(int[] permutation) {
		if (permutation.length != NUMBER_OF_AGENTS) {
			System.out.println("Cannot extend with given permutation: " + Arrays.toString(permutation));
			return null;
		}
		ArrayList<int[]> extendedMatching = new ArrayList<int[]>();
		int k = 0;
		for (int[] match : matching) {
			int[] extendedMatch = new int[match.length + 1];
			for (int i = 0; i < match.length; ++i) {
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
	 * @param group
	 *            index of gender
	 * @param agent
	 *            index of agent in gender
	 * @return true iff agent has a partner in this matching
	 */
	public boolean isMatchedInGroup(int group, int agent) {
		for (int[] match : matching) {
			if (match[group] == agent) {
				if (match[(group + 1) % NUMBER_OF_GROUPS] != -1) {
					return true;
				} else {
					return false;
				}
			}
		}
		return false;
	}

	public int size() {
		if (size == -1) {
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

	public boolean isInternallyBlocked(PreferenceSystem preferenceSystem) {
		for (int a = 0; a < preferenceSystem.numberOfAgents; ++a) {
			if (isMatchedInGroup(0, a)) {
				int partnerOfA = getPartner(0, a);
				for (int i = 0; i < preferenceSystem.ranks[0][a][partnerOfA]; ++i) {
					int b = preferenceSystem.preferences[0][a][i];
					if (isMatchedInGroup(1, b)) {
						int partnerOfB = getPartner(1, b);
						for (int j = 0; j < preferenceSystem.ranks[1][b][partnerOfB]; ++j) {
							int c = preferenceSystem.preferences[1][b][j];
							if (isMatchedInGroup(2, c)) {
								int partnerOfC = getPartner(2, c);
								if (preferenceSystem.prefers(2, c, a, partnerOfC)) {
									return true;
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

	public List<List<Integer>> firstOrderDissatisfied(PreferenceSystem preferenceSystem) {
		int n = preferenceSystem.numberOfGroups;
		List<List<Integer>> potentialBlocks = new ArrayList<List<Integer>>(n);

		for (int i = 0; i < n; ++i) {
			List<Integer> groupBlocks = new LinkedList<Integer>();
			for (int v = 0; v < preferenceSystem.numberOfAgents; ++v) {
				if (isMatchedInGroup((i - 1 + n) % n, v)) {
					int partnerOfV = getPartner((i - 1 + n) % n, v);
					for (int j = 0; j < preferenceSystem.ranks[(i - 1 + n) % n][v][partnerOfV]; ++j) {
						int u = preferenceSystem.preferences[(i - 1 + n) % n][v][j];
						groupBlocks.add(u);
					}
				}
			}
			potentialBlocks.add(i, groupBlocks);
		}
		return potentialBlocks;
	}

	public Matching append(int[] tuple) {
		matching.add(tuple);
		sortAndHash();
		return this;
	}

	public Matching validSubmatching(PreferenceSystem preferenceSystem) {
		return validSubmatching(preferenceSystem, 0);
	}

	public Matching validSubmatching(PreferenceSystem preferenceSystem, int group) {
		int n = preferenceSystem.numberOfGroups;
		Matching retval = new Matching(NUMBER_OF_GROUPS, NUMBER_OF_AGENTS);
		for (int[] tuple : matching) {
			boolean validFlag = true;
			for (int i = 0; i < tuple.length; ++i) {
				int agent = tuple[i];
				int partner = tuple[(i + 1) % tuple.length];
				if (partner == -1
						|| preferenceSystem.ranks[(i + group) % n][agent][partner] >= preferenceSystem.numberOfAgents) {
					validFlag = false;
				}
			}
			if (validFlag) {
				retval.append(tuple);
			}
		}

		return retval;
	}

	public boolean isInternallyBlocked(PreferenceSystem preferenceSystem, int group, int[] triple) {
		for (int a = 0; a < preferenceSystem.numberOfAgents; ++a) {
			if(group == 0 && triple[group] == a) {
				continue;
			}
			if (isMatchedInGroup(0, a,triple)) {
				int partnerOfA = getPartner(0, a,triple);
				for (int i = 0; i < preferenceSystem.ranks[0][a][partnerOfA]; ++i) {
					int b = preferenceSystem.preferences[0][a][i];
					if(group == 1 && triple[group] == b) {
						continue;
					}
					if (isMatchedInGroup(1, b,triple)) {
						int partnerOfB = getPartner(1, b,triple);
						for (int j = 0; j < preferenceSystem.ranks[1][b][partnerOfB]; ++j) {
							int c = preferenceSystem.preferences[1][b][j];
							if(group == 2 && triple[group] == c) {
								continue;
							}
							if (isMatchedInGroup(2, c,triple)) {
								int partnerOfC = getPartner(2, c,triple);
								if (preferenceSystem.prefers(2, c, a, partnerOfC)) {
									return true;
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

	private int getPartner(int group, int agent, int[] triple) {
		if(triple[group] == agent) {
			return triple[(group+1)%triple.length];
		}
		return getPartner(group, agent);
	}

	public boolean isMatchedInGroup(int group, int agent, int[] triple) {
		if(triple[group] == agent) {
			return true;
		}
		return isMatchedInGroup(group,agent);
	}

	public List<List<Integer>> firstOrderDissatisfied(PreferenceSystem preferenceSystem, int group, int[] triple) {
		int n = preferenceSystem.numberOfGroups;
		List<List<Integer>> potentialBlocks = new ArrayList<List<Integer>>(n);

		for (int i = 0; i < n; ++i) {
			List<Integer> groupBlocks = new LinkedList<Integer>();
			for (int v = 0; v < preferenceSystem.numberOfAgents; ++v) {
				if (isMatchedInGroup((i - 1 + n) % n, v,triple)) {
					int partnerOfV = getPartner((i - 1 + n) % n, v,triple);
					for (int j = 0; j < preferenceSystem.ranks[(i - 1 + n) % n][v][partnerOfV]; ++j) {
						int u = preferenceSystem.preferences[(i - 1 + n) % n][v][j];
						if(u != preferenceSystem.numberOfAgents) {
							groupBlocks.add(u);
						}
					}
				}
			}
			potentialBlocks.add(i, groupBlocks);
		}
		return potentialBlocks;
	}
}
