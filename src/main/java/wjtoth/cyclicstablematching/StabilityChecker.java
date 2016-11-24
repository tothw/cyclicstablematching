package wjtoth.cyclicstablematching;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class StabilityChecker {

	// For matching checking, iterates over blocking tuples
	private CrossProduct<Integer> blockers;

	// matching to use in search of stable matching
	private ArrayList<Matching[]> matchings;

	// matchings of one gender to anther;
	// used in some sufficient checks
	private ArrayList<Matching[]> oneGenderMatchings;

	/**
	 * Standard constructor
	 * 
	 * @param numberOfAgents
	 * @param numberOfGroups
	 */
	public StabilityChecker(int numberOfGroups, int numberOfAgents) {
		ArrayList<Integer> agents = new ArrayList<Integer>();
		for (int i = 0; i < numberOfAgents; ++i) {
			agents.add(i);
		}
		blockers = new CrossProduct<Integer>(agents, numberOfGroups);
		matchings = new ArrayList<Matching[]>();
		matchings = buildMatchings(numberOfAgents, numberOfGroups);
		oneGenderMatchings = buildMatchings(numberOfAgents, 2);
	}

	/**
	 * Builds all matchings with numberOfGroups genders and numberOfAgents
	 * agents in each gender
	 * 
	 * @param numberOfAgents
	 * @param numberOfGroups
	 */
	private ArrayList<Matching[]> buildMatchings(int numberOfAgents, int numberOfGroups) {
		ArrayList<Matching[]> matchings = new ArrayList<>();
		// Obtain all permutations of subsets of [0, ..., numberOfAgents-1]
		List<int[]> permutations = new ArrayList<>();
		for (PermutationArray permutationArray : Permutations.permutationsOfAllSubsets(numberOfAgents)) {
			permutations.add(permutationArray.getArray());
		}
		System.out.println("Have permutations");
		// Split permutations by length
		ArrayList<List<int[]>> permutationsSplitByLength = splitByLength(permutations, numberOfAgents);
		System.out.println("Split permutations");
		ArrayList<Matching> matchingSet = new ArrayList<Matching>();
		// iterate over lengths and compute all matchings for said length
		for (List<int[]> permutationsOfALength : permutationsSplitByLength) {
			System.out.println("Processing: " + permutationsOfALength.size() + " permutations");
			Set<Matching> uniqueMatchings = getMatchings(permutationsOfALength, numberOfAgents, numberOfGroups);
			matchingSet.addAll(uniqueMatchings);
			Matching[] matchingsArray = new Matching[matchingSet.size()];
			matchingSet.toArray(matchingsArray);
			matchings.add(matchingsArray);
			matchingSet.clear();
		}
		// Matchings now is a list of lists of matchings of a given length
		// length means number of tuples in matching
		System.out.println("Done Processing Permutations");
		return matchings;
	}

	/**
	 * Constructs all possible matchings formed by cross products of the
	 * permutations passed in
	 * 
	 * @param permutations
	 * @param numberOfAgents
	 * @param numberOfGroups
	 * @return
	 */
	private Set<Matching> getMatchings(List<int[]> permutations, int numberOfAgents, int numberOfGroups) {
		CrossProduct<int[]> crossProduct = new CrossProduct<int[]>(permutations, 2);
		Set<Matching> matchingSet = new TreeSet<>();
		// Start by matching pairs
		while (crossProduct.hasNext()) {
			ArrayList<int[]> match = crossProduct.next();
			Matching perfectMatching = new Matching(2, numberOfAgents);
			perfectMatching.setMatchingFromPermutations(match);
			if (perfectMatching.validate()) {
				matchingSet.add(perfectMatching);
			}
		}
		// then iteratively extend pairs until be have numberOfGroups-tuples
		// this is done to reduce symmetry and compute faster than doing all
		// tuples at once
		for (int i = 2; i < numberOfGroups; ++i) {
			matchingSet = extendMatchingsByPermutations(permutations, matchingSet);
		}
		return matchingSet;
	}

	/**
	 * for each permutation, for each matching, add mathing extended by
	 * permutation to output list
	 * 
	 * @param permutations
	 * @param perfectMatchings
	 * @return
	 */
	private Set<Matching> extendMatchingsByPermutations(List<int[]> permutations, Set<Matching> perfectMatchings) {
		Set<Matching> retval = new TreeSet<>();
		for (Matching perfectMatching : perfectMatchings) {
			for (int[] permutation : permutations) {
				Matching extendedMatching = perfectMatching.extend(permutation);
				if (extendedMatching.validate()) {
					retval.add(perfectMatching.extend(permutation));
				}
			}
		}
		return retval;
	}

	/**
	 * Splits list of permutations into list of lists of permutations separated
	 * by length of permutation
	 * 
	 * @param permutations
	 * @param numberOfAgents
	 * @return
	 */
	private ArrayList<List<int[]>> splitByLength(List<int[]> permutations, int numberOfAgents) {
		ArrayList<List<int[]>> retval = new ArrayList<List<int[]>>(numberOfAgents);
		for (int i = 0; i < numberOfAgents; ++i) {
			retval.add(new ArrayList<int[]>());
		}
		for (int[] permutation : permutations) {
			int length = 0;
			for (int i = 0; i < numberOfAgents; ++i) {
				if (permutation[i] > -1) {
					++length;
				}
			}
			retval.get(length - 1).add(permutation);
		}
		return retval;
	}

	public boolean isStable(PreferenceSystem preferenceSystem) {
		boolean checkInductive = checkInductive(preferenceSystem);
		if (checkInductive) {
			return true;
		}
		boolean checkGenderStability = checkGenderStability(preferenceSystem);
		if (checkGenderStability) {
			return true;
		}
		return false;
	}

	private boolean checkInductive(PreferenceSystem preferenceSystem) {
		for (int size = 0; size < matchings.size(); ++size) {
			Matching[] matchingsOfASize = matchings.get(size);
			for (Matching matching : matchingsOfASize) {
				boolean stableFlag = checkInductive(matching, preferenceSystem);
				if (stableFlag) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean checkInductive(Matching matching, PreferenceSystem preferenceSystem) {
		boolean isValid = validateMatching(matching, preferenceSystem);
		if (isValid) {
			// compute internal blocking triples
			boolean hasInternalBlocks = checkInternalBlocks(matching, preferenceSystem);
			if(!hasInternalBlocks) {
				return false;
			}
			// compute potential blocks against matching
			List<List<Integer>> potentialBlocks = computePotentialBlocks(matching, preferenceSystem);
			//check lemma 4.3.7
			boolean lemma4_3_7 = checkLemma4_3_7(potentialBlocks, matching, preferenceSystem);
			if(lemma4_3_7) {
				return true;
			}
		} else {
			// fixing lemma
		}
		return false;
	}

	private boolean checkLemma4_3_7(List<List<Integer>> potentialBlocks, Matching matching, PreferenceSystem preferenceSystem) {
		for(int group = 0; group < preferenceSystem.numberOfGroups; ++group) {
			List<Integer> groupBlocks = potentialBlocks.get(group);
			for(int v : groupBlocks) {
				if(!matching.isMatchedInGroup(group, v)) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean checkInternalBlocks(Matching matching, PreferenceSystem preferenceSystem) {
		for (int a = 0; a < preferenceSystem.numberOfAgents; ++a) {
			if (matching.isMatchedInGroup(0, a)) {
				int partnerOfA = matching.getPartner(0, a);
				for (int i = 0; i < preferenceSystem.ranks[0][a][partnerOfA]; ++i) {
					int b = preferenceSystem.preferences[0][a][i];
					if (matching.isMatchedInGroup(1, b)) {
						int partnerOfB = matching.getPartner(1, b);
						for (int j = 0; j < preferenceSystem.ranks[1][b][partnerOfB]; ++j) {
							int c = preferenceSystem.preferences[1][b][j];
							if(matching.isMatchedInGroup(2, c)) {
								int partnerOfC = matching.getPartner(2, c);
								if(preferenceSystem.prefers(2,c,a,partnerOfC)) {
									return false;
								}
							}
						}
					}
				}
			}
		}
		return true;
	}

	private boolean validateMatching(Matching matching, PreferenceSystem preferenceSystem) {
		for (int[] triple : matching.getMatching()) {
			for (int i = 0; i < 2; ++i) {
				int v = triple[i];
				int matchV = triple[(i + 1) % 3];
				if (!preferenceSystem.isAcceptable(i, v, matchV)) {
					return false;
				}
			}
		}
		return true;
	}

	List<List<Integer>> computePotentialBlocks(Matching matching, PreferenceSystem preferenceSystem) {
		List<List<Integer>> potentialBlocks = new ArrayList<List<Integer>>(3);
		for(int i = 0; i<3; ++i) {
			List<Integer> groupBlocks = new LinkedList<Integer>();
			for(int v = 0; v < preferenceSystem.numberOfAgents; ++v) {
				if(matching.isMatchedInGroup((i-1 + 3)%3, v)) {
					int partnerOfV = matching.getPartner((i-1+3)%3, v);
					for(int j = 0; j<preferenceSystem.ranks[(i-1+3)%3][v][partnerOfV]; ++j) {
						int u = preferenceSystem.preferences[(i-1+3)%3][v][j];
						groupBlocks.add(u);
					}
				}
			}
			potentialBlocks.add(i, groupBlocks);
		}
		return potentialBlocks;
	}
	
	private boolean checkGenderStability(PreferenceSystem preferenceSystem) {
		return false;
	}
}