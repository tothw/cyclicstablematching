package wjtoth.cyclicstablematching;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class StabilityChecker {

	// matching to use in search of stable matching
	private Matching[] matchings;

	// matchings of one gender to anther;
	// used in some sufficient checks
	private Matching[] oneGenderMatchings;

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
	private Matching[] buildMatchings(int numberOfAgents, int numberOfGroups) {
		// Obtain all permutation of [0, ..., numberOfAgents-1]
		List<int[]> permutations = new ArrayList<>();
		for (PermutationArray permutationArray : Permutations.permutations(numberOfAgents)) {
			permutations.add(permutationArray.getArray());
		}
		System.out.println("Processing: " + permutations.size() + " permutations");
		Set<Matching> uniqueMatchings = getMatchings(permutations, numberOfAgents, numberOfGroups);
		Matching[] matchingsArray = new Matching[uniqueMatchings.size()];
		uniqueMatchings.toArray(matchingsArray);
		// length means number of tuples in matching
		System.out.println("Done Processing Permutations");
		return matchingsArray;
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
	 * for each permutation, for each matching, add matching extended by
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
		for (Matching matching : matchings) {
			boolean stableFlag = checkInductive(matching, preferenceSystem);
			if (stableFlag) {
				return true;
			}
		}
		return false;
	}

	private boolean checkInductive(Matching matching, PreferenceSystem preferenceSystem) {

		Matching subMatching = matching.validSubmatching(preferenceSystem);
		if (subMatching.size() > 0) {
			// compute internal blocking triples
			boolean isInternallyBlocked = subMatching.isInternallyBlocked(preferenceSystem);
			if (isInternallyBlocked) {
				return false;
			}
			// compute potential blocks against matching
			List<List<Integer>> potentialBlocks = subMatching.firstOrderDissatisfied(preferenceSystem);
			// check lemma 4.3.7
			boolean lemma4_3_7 = checkLemma4_3_7(potentialBlocks, subMatching, preferenceSystem);
			if (lemma4_3_7) {
				return true;
			}
			// check lemma 4.3.12
			boolean lemma4_3_12_And_4_3_13 = checkLemma4_3_12_And_4_3_13(potentialBlocks, subMatching,
					preferenceSystem);
			if (lemma4_3_12_And_4_3_13) {
				return true;
			}
		}
		if (subMatching.size() >= preferenceSystem.numberOfAgents - 2
				&& subMatching.size() < preferenceSystem.numberOfAgents) {
			// fixing lemma 4.3.14
			boolean lemma4_3_14 = checkLemma4_3_14(subMatching, preferenceSystem);
			if (lemma4_3_14) {
				return true;
			}
		}

		return false;
	}

	// only works for 3 groups atm
	private boolean checkLemma4_3_14(Matching subMatching, PreferenceSystem preferenceSystem) {
		for (int group = 0; group < preferenceSystem.numberOfGroups; ++group) {
			if (checkLemma4_3_14(subMatching, preferenceSystem, group)) {
				return true;
			}
		}
		return false;
	}

	private boolean checkLemma4_3_14(Matching subMatching, PreferenceSystem preferenceSystem, int group) {
		for (int exceptionAgent = 0; exceptionAgent < preferenceSystem.numberOfAgents; ++exceptionAgent) {
			if (subMatching.isMatchedInGroup(group, exceptionAgent)) {
				continue;
			}
			if (checkLemma4_3_14(subMatching, preferenceSystem, group, exceptionAgent)) {
				return true;
			}
		}
		return false;
	}

	private boolean checkLemma4_3_14(Matching subMatching, PreferenceSystem preferenceSystem, int group,
			int exceptionAgent) {
		int n = preferenceSystem.numberOfGroups;
		for (int exceptionPartner = 0; exceptionPartner < preferenceSystem.numberOfAgents; ++exceptionPartner) {
			if (subMatching.isMatchedInGroup((group + 1) % n, exceptionPartner)
					|| preferenceSystem.ranks[group][exceptionAgent][exceptionPartner] < preferenceSystem.numberOfAgents) {
				continue;
			}
			if (checkLemma4_3_14(subMatching, preferenceSystem, group, exceptionAgent, exceptionPartner)) {
				return true;
			}
		}
		return false;
	}

	private boolean checkLemma4_3_14(Matching subMatching, PreferenceSystem preferenceSystem, int group,
			int exceptionAgent, int exceptionPartner) {
		int n = preferenceSystem.numberOfGroups;
		for (int c = 0; c < preferenceSystem.numberOfAgents; ++c) {
			if (subMatching.isMatchedInGroup((group + 2) % n, c)
					|| preferenceSystem.ranks[(group + 1) % n][exceptionPartner][c] == preferenceSystem.numberOfAgents
					|| preferenceSystem.ranks[(group + 2) % n][c][exceptionAgent] == preferenceSystem.numberOfAgents) {
				continue;
			}
			int[] triple = new int[n];
			triple[group] = exceptionAgent;
			triple[(group + 1) % n] = exceptionPartner;
			triple[(group + 2) % n] = c;
			if (attemptFix(subMatching, preferenceSystem, group, triple)) {
				return true;
			}
		}
		return false;
	}

	private boolean attemptFix(Matching subMatching, PreferenceSystem preferenceSystem, int group, int[] triple) {
		//valid no internal blocks
		if(!subMatching.isInternallyBlocked(preferenceSystem,group,triple)){
			List<List<Integer>>potentialBlocks = subMatching.firstOrderDissatisfied(preferenceSystem, group, triple);
			for (int i = 0; i < preferenceSystem.numberOfGroups; ++i) {
				List<Integer> groupBlocks = potentialBlocks.get(i);
				for (int v : groupBlocks) {
					if (!subMatching.isMatchedInGroup(i, v,triple)) {
						return false;
					}
				}
			}
			//perform fixing
			int newLastGroup = group;
			int newLastAgent = triple[group];
			int newLastChoice = triple[(group+1)%triple.length];
			if(preferenceSystem.fixedLastGroup == preferenceSystem.numberOfGroups
					&& preferenceSystem.fixedLastAgent == preferenceSystem.numberOfAgents
					&& preferenceSystem.fixedLastChoice == preferenceSystem.numberOfAgents) {
				preferenceSystem.fixedLastGroup = newLastGroup;
				preferenceSystem.fixedLastAgent = newLastAgent;
				preferenceSystem.fixedLastChoice = newLastChoice;
			} else {
				if(preferenceSystem.fixedLastGroup == newLastGroup
						&& preferenceSystem.fixedLastAgent == newLastAgent
						&& preferenceSystem.fixedLastChoice != newLastChoice) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean checkLemma4_3_12_And_4_3_13(List<List<Integer>> potentialBlocks, Matching matching,
			PreferenceSystem preferenceSystem) {
		int[] idol = new int[preferenceSystem.numberOfGroups];
		for (int i = 0; i < idol.length; ++i) {
			idol[i] = preferenceSystem.numberOfAgents;
		}
		for (int group = 0; group < preferenceSystem.numberOfGroups; ++group) {
			List<Integer> groupBlocks = potentialBlocks.get(group);
			for (int u : groupBlocks) {
				if (!matching.isMatchedInGroup(group, u)) {
					if (idol[group] != preferenceSystem.numberOfAgents) {
						return false;
					} else {
						idol[group] = u;
					}
				}
			}
		}
		boolean[] isFirstChoiceVerified = new boolean[preferenceSystem.numberOfGroups];
		for (int i = 0; i < isFirstChoiceVerified.length; ++i) {
			if (idol[i] != preferenceSystem.numberOfAgents) {
				isFirstChoiceVerified[i] = verifyFirstChoice(idol[i], i, matching, preferenceSystem);
			}
		}
		// check lemma 4_3_12
		for (int group = 0; group < preferenceSystem.numberOfGroups; ++group) {
			if (isFirstChoiceVerified[group]) {
				boolean noOtherIdols = true;
				for (int i = 0; i != group; i = (i + 1) % 3) {
					if (idol[i] != preferenceSystem.numberOfAgents) {
						noOtherIdols = false;
						break;
					}
				}
				if (noOtherIdols) {
					return true;
				}
			}
		}
		// check lemma 4_3_13
		if (matching.size() >= preferenceSystem.numberOfGroups - 2) {
			for (int group = 0; group < preferenceSystem.numberOfGroups; ++group) {
				if (isFirstChoiceVerified[group] && isFirstChoiceVerified[(group + 1) % 3]) {
					if (idol[(group + 2) % 3] == preferenceSystem.numberOfAgents) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean verifyFirstChoice(int idol, int group, Matching matching, PreferenceSystem preferenceSystem) {
		for (int agent = 0; agent < preferenceSystem.numberOfAgents; ++agent) {
			if (!matching.isMatchedInGroup(group, agent)) {
				int rank = preferenceSystem.ranks[group][agent][idol];
				for (int i = 0; i < rank; ++i) {
					int preferred = preferenceSystem.preferences[group][agent][i];
					if (!matching.isMatchedInGroup((group + 1) % 3, preferred)) {
						return false;
					}
				}
			}
		}
		return true;
	}

	private boolean checkLemma4_3_7(List<List<Integer>> potentialBlocks, Matching matching,
			PreferenceSystem preferenceSystem) {
		for (int group = 0; group < preferenceSystem.numberOfGroups; ++group) {
			List<Integer> groupBlocks = potentialBlocks.get(group);
			for (int v : groupBlocks) {
				if (!matching.isMatchedInGroup(group, v)) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean checkGenderStability(PreferenceSystem preferenceSystem) {
		for(Matching matching : oneGenderMatchings) {
			if(matching.size() != preferenceSystem.numberOfAgents) {
				continue;
			}
			for(int i = 0; i<preferenceSystem.numberOfGroups; ++i) {
				if(checkGenderStability(matching, i, preferenceSystem)) {
					System.out.println("SUCCESS");
					return true;
				}
			}
		}
		return false;
	}

	private boolean checkGenderStability(Matching matching, int group, PreferenceSystem preferenceSystem) {
		boolean[] firstChoices = new boolean[preferenceSystem.numberOfAgents];
		int n = preferenceSystem.numberOfGroups;
		for(int i = 0; i<preferenceSystem.numberOfAgents; ++i) {
			int partner = matching.getPartner(0, i);
			int rank = preferenceSystem.ranks[group][i][partner];
			for(int j = 0; j<rank; ++j) {
				int preferred = preferenceSystem.preferences[group][i][j];
				if(preferred == preferenceSystem.numberOfAgents) {
					continue;
				}
				int newFirst = preferenceSystem.preferences[(group+1)%n][preferred][0];
				if(newFirst == preferenceSystem.numberOfAgents) {
					//potential for a first choice fixing lemma here?
					return false;
				}
				if(firstChoices[newFirst] == false) {
					firstChoices[newFirst] = true;
				} else {
					return false;
				}
			}
		}
		return false;
	}
}