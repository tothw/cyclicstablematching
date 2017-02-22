package wjtoth.cyclicstablematching;

import java.util.ArrayList;
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

	// matchings of one gender to anther;
	// used in some sufficient checks
	private Matching[] oneGenderMatchings;

	Check[] quickChecks;
	Check[] longChecks;

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

		quickChecks = new Check[] { new CheckInitialChoices(), new CheckCycle() };
		longChecks = new Check[] { new CheckInductive(matchings), new CheckGenderStability(oneGenderMatchings),
				new CheckAlmostInductive(matchings), new CheckFixing(matchings) };
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
		if (quickChecks(preferenceSystem)) {
			return true;
		}
		if (preferenceSystem.length >= 3 && slowChecks(preferenceSystem)) {
			return true;
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

}