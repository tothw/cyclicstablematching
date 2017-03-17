package wjtoth.cyclicstablematching;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import wjtoth.cyclicstablematching.checks.Check;
import wjtoth.cyclicstablematching.checks.CheckAlmostInductive;
import wjtoth.cyclicstablematching.checks.CheckCycle;
import wjtoth.cyclicstablematching.checks.CheckFixing;
import wjtoth.cyclicstablematching.checks.CheckGenderStability;
import wjtoth.cyclicstablematching.checks.CheckInductive;
import wjtoth.cyclicstablematching.checks.CheckInitialChoices;

public class StabilityChecker {

	// matching to use in search of stable matching
	private Matching[] matchings;

	// matchings by type: the four cases
	List<List<Matching>> matchingsByType;

	// matchings of one gender to anther;
	// used in some sufficient checks
	private Matching[] oneGenderMatchings;

	Check[] quickChecks;
	Check[] longChecks;
	
	public boolean case1,case2,case3,case4,case5;

	/**
	 * Standard constructor
	 * 
	 * @param numberOfAgents
	 * @param numberOfGroups
	 */
	public StabilityChecker(int numberOfGroups, int numberOfAgents, boolean track) {
		ArrayList<Integer> agents = new ArrayList<Integer>();
		for (int i = 0; i < numberOfAgents; ++i) {
			agents.add(i);
		}
		matchings = buildMatchings(numberOfAgents, numberOfGroups);
		sortMatchingsByType();
		oneGenderMatchings = buildMatchings(numberOfAgents, 2);

		quickChecks = new Check[] { new CheckInitialChoices(track), new CheckCycle(track) };
		longChecks = new Check[] { new CheckInductive(matchings, track),
				new CheckGenderStability(oneGenderMatchings, track), new CheckAlmostInductive(matchings, track),
				new CheckFixing(matchings, track) };
		
		case1 = false;
		case2 = false;
		case3 = false;
		case4 = false;
		case5 = false;
	}

	private void sortMatchingsByType() {
		matchingsByType = new ArrayList<List<Matching>>();
		for (int i = 0; i < 5; ++i) {
			matchingsByType.add(new LinkedList<Matching>());
		}
		for (Matching matching : matchings) {
			sortByType(matching);
		}

	}

	private void sortByType(Matching matching) {
		int partnerOfM0 = matching.getPartner(0, 0);
		if (partnerOfM0 == 0) {
			// can be case 1, 3, or 4
			int partnerOfW0 = matching.getPartner(1, 0);
			if (partnerOfW0 == 0) {
				// is case 1
				matchingsByType.get(0).add(matching);
			}
			if (partnerOfW0 == 1) {
				// can be case 3 or 4
				int partnerOfD0 = matching.getPartner(2, 0);
				if (partnerOfD0 == 1) {
					// is case 3
					matchingsByType.get(2).add(matching);
				}
				if (partnerOfD0 == 2) {
					// can be case 4 provided m1 gets their first choice
					matchingsByType.get(3).add(matching);
				}
			}
			if(partnerOfW0 == 2) {
				//can be case 5
				int partnerOfD0 = matching.getPartner(2, 0);
				if(partnerOfD0 == 2 || partnerOfD0 == 1) {
					//still need to verify partner of d1 at PS evaluation time
					matchingsByType.get(4).add(matching);
				}
			}
		}
		if (partnerOfM0 == 1) {
			// can be case 2
			int partnerOfW1 = matching.getPartner(1, 1);
			if (partnerOfW1 == 0) {
				// is case 2
				matchingsByType.get(1).add(matching);
			}
		}

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
		if(quickChecks[1].check(preferenceSystem)) {
			//quickChecks[1] checks for 111 triples
			//return true;
		}
		if(preferenceSystem.isComplete()) {
			return completeCheck(preferenceSystem);
		}
		return false;
	}

	//complete checking procedure for n=4
	private boolean completeCheck(PreferenceSystem preferenceSystem) {
		CheckInductive check = (CheckInductive)longChecks[0]; //inductive check
		for(Matching matching : matchingsByType.get(2)) {
			if(check.checkImpl(matching, preferenceSystem)) {
				case3 = true;
				return true;
			}
		}
		for(Matching matching : matchingsByType.get(3)) {
			if(matching.getPartner(0, 1) != preferenceSystem.preferences[0][1][0]) {
				continue;
			}
			if(check.checkImpl(matching, preferenceSystem)) {
				case4 = true;
				return true;
			}
		}
		for(Matching matching : matchingsByType.get(4)) {
			if(matching.getPartner(2, 1) != preferenceSystem.preferences[2][1][0]) {
				continue;
			}
			if(check.checkImpl(matching, preferenceSystem)) {
				case5 = true;
				return true;
			}
		}
		
		for(int i = 1; i<preferenceSystem.numberOfAgents; ++i) {
			if(preferenceSystem.preferences[1][i][0] != 0) {
				return true;
			}
		}
		for(Matching matching : matchingsByType.get(1)) {
			if(check.checkImpl(matching, preferenceSystem)) {
				case2 = true;
				return true;
			}
		}
		for(Matching matching : matchingsByType.get(0)) {
			if(check.checkImpl(matching, preferenceSystem)) {
				case1 = true;
				return true;
			}
		}
		return false;
	}

	private boolean quickChecks(PreferenceSystem preferenceSystem) {
		for (Check check : quickChecks) {
			if (check.check(preferenceSystem)) {
				return true;
			}
		}
		return false;
	}

	private boolean slowChecks(PreferenceSystem preferenceSystem) {
		for (Check check : longChecks) {
			if (check.check(preferenceSystem)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * verifies a complete preference system on 3 genders
	 * 
	 * @param preferenceSystem
	 */
	public void verify(PreferenceSystem preferenceSystem) {
		boolean stableMatchingFound = false;
		Matching stableMatching = null;
		for (Matching matching : matchings) {
			boolean isBlocked = false;
			for (int a = 0; a < preferenceSystem.numberOfAgents && !isBlocked; ++a) {
				int partnerOfA = matching.getPartner(0, a);
				int rankOfPartnerOfA = preferenceSystem.ranks[0][a][partnerOfA];
				for (int i = 0; i < rankOfPartnerOfA && !isBlocked; ++i) {
					int b = preferenceSystem.preferences[0][a][i];
					int partnerOfB = matching.getPartner(1, b);
					int rankOfPartnerOfB = preferenceSystem.ranks[1][b][partnerOfB];
					for (int j = 0; j < rankOfPartnerOfB && !isBlocked; ++j) {
						int c = preferenceSystem.preferences[2][b][j];
						int partnerOfC = matching.getPartner(2, c);
						if (preferenceSystem.prefers(2, c, a, partnerOfC)) {
							isBlocked = true;
						}
					}
				}
			}
			if (!isBlocked) {
				System.out.println("Stable Matching found:");
				System.out.println(matching);
				stableMatching = matching;
				System.out.println("For System: ");
				System.out.println(preferenceSystem);
				stableMatchingFound = true;
				break;
			}
		}
		if (!stableMatchingFound) {
			System.out.println("NO Stable Matching found");
			System.out.println("For System: ");
			System.out.println(preferenceSystem);
		}
		System.out.println("Testing Inductive");

		CheckInductive check = new CheckInductive(matchings, false);
		System.out.println(check.check(preferenceSystem));

		System.out.println("Particular Inductive Test");
		System.out.println(stableMatching);
		System.out.println(check.checkImpl(stableMatching, preferenceSystem));

	}

}