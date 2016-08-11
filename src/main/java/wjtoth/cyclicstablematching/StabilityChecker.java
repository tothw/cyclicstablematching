package wjtoth.cyclicstablematching;

import java.util.*;
import java.util.stream.Collectors;

public class StabilityChecker {

	//PreferenceSystem to be checked for stable matchimg
	private PreferenceSystem preferenceSystem;

	//flag for existence of stable matching
	private boolean hasStableMatch;

	//set true for additional printing of behaviour
	private boolean loud;

	// For matching checking, iterates over blocking tuples
	private CrossProduct<Integer> blockers;

	//matching to use in search of stable matching
	private ArrayList<Matching[]> matchings;

	//caches last stable match in hopes that next preference system
	//to check will also be stable under this matching
	//TODO investigate how often this is even useful
	private Matching lastSuccessfulMatching = null;

	/**
	 *  Standard constructor
	 * @param numberOfAgents 
	 * @param numberOfGroups
	 */
	public StabilityChecker(int numberOfAgents, int numberOfGroups) {
		preferenceSystem = new PreferenceSystem(numberOfGroups, numberOfAgents);
		hasStableMatch = false;
		ArrayList<Integer> agents = new ArrayList<Integer>();
		for (int i = 0; i < numberOfAgents; ++i) {
			agents.add(i);
		}
		blockers = new CrossProduct<Integer>(agents, numberOfGroups);
		matchings = new ArrayList<Matching[]>();
		buildMatchings(numberOfAgents, numberOfGroups);
		loud = false;
	}
	
	/**
	 * Builds all matchings with numberOfGroups genders and numberOfAgents agents
	 * in each gender
	 * @param numberOfAgents
	 * @param numberOfGroups
	 */
	private void buildMatchings(int numberOfAgents, int numberOfGroups) {
		//Obtain all permutations of subsets of [0, ..., numberOfAgents-1]
		List<int[]> permutations = new ArrayList<>();
		for (PermutationArray permutationArray : Permutations.permutationsOfAllSubsets(numberOfAgents)) {
			permutations.add(permutationArray.getArray());
		}
		System.out.println("Have permutations");
		//Split permutations by length
		ArrayList<List<int[]>> permutationsSplitByLength = splitByLength(permutations, numberOfAgents);
		System.out.println("Split permutations");
		ArrayList<Matching> matchingSet = new ArrayList<Matching>();
		//iterate over lengths and compute all matchings for said length
		for (List<int[]> permutationsOfALength : permutationsSplitByLength) {
			System.out.println("Processing: " + permutationsOfALength.size() + " permutations");
			List<Matching> uniqueMatchings = getMatchings(permutationsOfALength, numberOfAgents, numberOfGroups)
					.stream().distinct().collect(Collectors.toList());
			matchingSet.addAll(uniqueMatchings);
			Matching[] matchingsArray = new Matching[matchingSet.size()];
			matchingSet.toArray(matchingsArray);
			matchings.add(matchingsArray);
			matchingSet.clear();
		}
		//Matchings now is a list of lists of matchings of a given length
		//length means number of tuples in matching
		System.out.println("Done Processing Permutations");
	}

	/**
	 * Constructs all possible matchings formed by cross products
	 * of the permutations passed in
	 * @param permutations
	 * @param numberOfAgents
	 * @param numberOfGroups
	 * @return
	 */
	private List<Matching> getMatchings(List<int[]> permutations, int numberOfAgents, int numberOfGroups) {
		CrossProduct<int[]> crossProduct = new CrossProduct<int[]>(permutations, 2);
		List<Matching> matchingSet = new LinkedList<>();
		//Start by matching pairs
		while (crossProduct.hasNext()) {
			ArrayList<int[]> match = crossProduct.next();
			Matching perfectMatching = new Matching(2, numberOfAgents);
			perfectMatching.setMatchingFromPermutations(match);
			if (perfectMatching.validate()) {
				matchingSet.add(perfectMatching);
			}
		}
		//then iteratively extend pairs until be have numberOfGroups-tuples
		//this is done to reduce symmetry and compute faster than doing all tuples at once
		for (int i = 2; i < numberOfGroups; ++i) {
			matchingSet = extendMatchingsByPermutations(permutations, matchingSet);
		}
		return matchingSet.stream().distinct().collect(Collectors.toList());
	}

	/**
	 * for each permutation, for each matching, add mathing extended by permutation
	 * to output list
	 * @param permutations
	 * @param perfectMatchings
	 * @return
	 */
	private List<Matching> extendMatchingsByPermutations(List<int[]> permutations,
			List<Matching> perfectMatchings) {
		Set<Matching> retval = new TreeSet<>();
		for (Matching perfectMatching : perfectMatchings) {
			for (int[] permutation : permutations) {
				Matching extendedMatching = perfectMatching.extend(permutation);
				if (extendedMatching.validate()) {
					retval.add(perfectMatching.extend(permutation));
				}
			}
		}
		return retval.stream().distinct().collect(Collectors.toList());
	}

	/**
	 * Splits list of permutations into list of lists of permutations separated 
	 * by length of permutation
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

	/**
	 * Set preference system to be checked for stable matching
	 * @param preferenceSystem
	 */
	public void setPreferenceSystem(PreferenceSystem preferenceSystem) {
		this.preferenceSystem = preferenceSystem;
		hasStableMatch = false;
	}

	// true iff preference system has 
	// stable matching once extended
	public boolean hasStableMatch() {
		hasStableMatch = false;
		loud = false;
		// Generalized by new Stable Match check (Claim 1)
		// But still faster (linear) to run these checks first
		if (sufficientChecks()) {
			return hasStableMatch;
		}
		if (!hasStableMatch) {
			//attempt to find a stable matching via (Claim 1)
			attemptStableMatch();
		}
		if (loud) {
			System.out.println("Has Stable: " + hasStableMatch);
		}
		return hasStableMatch;
	}

	/**
	 * Checks 1st choice 3-cycle and 9-cycle
	 * and 1st choice all same and 1st choice all different
	 * @return true iff sufficient condition for extensions to have
	 * stable matching is satisfied
	 */
	private boolean sufficientChecks() {
		checkFirstChoiceCycle();
		if (!hasStableMatch) {
			checkFirstChoiceNineCycle();
		}
		checkAllSameDifferent();
		return hasStableMatch;
	}

	private void checkFirstChoiceCycle() {
		for (Agent agent : preferenceSystem.getAgents(0)) {
			checkFirstChoiceCycle(agent);
			if (hasStableMatch) {
				break;
			}
		}
	}

	private void checkFirstChoiceCycle(Agent agent) {
		int agentFirstChoice = agent.getFirstChoice();
		int partnerIndex = agentFirstChoice;
		for (int i = 1; i < preferenceSystem.getGroups().size(); ++i) {
			if (partnerIndex == -1) {
				return;
			}
			partnerIndex = preferenceSystem.getAgents(i).get(partnerIndex).getFirstChoice();
		}
		if (agent.getIndex() == partnerIndex) {
			hasStableMatch = true;
		}
	}

	private void checkFirstChoiceNineCycle() {
		for (Agent agent : preferenceSystem.getAgents(0)) {
			checkFirstChoiceNineCycle(agent);
			if (hasStableMatch) {
				break;
			}
		}
	}

	private void checkFirstChoiceNineCycle(Agent agent) {
		int agentFirstChoice = agent.getFirstChoice();
		int partnerIndex = agentFirstChoice;
		for (int i = 1; i < 3 * preferenceSystem.getGroups().size(); ++i) {
			if (partnerIndex == -1) {
				return;
			}
			partnerIndex = preferenceSystem.getAgents(i % 3).get(partnerIndex).getFirstChoice();
		}
		if (agent.getIndex() == partnerIndex) {
			hasStableMatch = true;
		}
	}

	private void checkAllSameDifferent() {
		for (Group group : preferenceSystem.getGroups()) {
			checkAllSameDifferent(group);
		}
	}

	private void checkAllSameDifferent(Group group) {
		int[] firstChoices = new int[group.getGroupSize()];
		for (int i = 0; i < firstChoices.length; ++i) {
			firstChoices[i] = group.getAgents().get(i).getFirstChoice();
		}
		// check all -1
		boolean negativeOneChoices = true;
		for (int i = 0; i < firstChoices.length; ++i) {
			negativeOneChoices = negativeOneChoices && firstChoices[i] == -1;
		}
		if (negativeOneChoices == true) {
			return;
		}
		if (firstChoices.length == 1) {
			if (firstChoices[0] != -1) {
				hasStableMatch = true;
			}
			return;
		}
		Arrays.sort(firstChoices);
		if (loud) {
			System.out.println("First Choices: " + Arrays.toString(firstChoices));
		}
		if (firstChoices[0] == 0 && firstChoices[firstChoices.length - 1] == firstChoices.length - 1) {
			hasStableMatch = true;
			if (loud) {
				System.out.println("All Different");
			}
			return;
		}
		int choice = firstChoices[0];
		if (firstChoices[firstChoices.length - 1] == choice) {
			hasStableMatch = true;
			if (loud) {
				System.out.println("All Same");
			}
		}
	}

	// assumes all groups have same size!
	/**
	 * Attempts a stable matching of prefernce system
	 */
	public void attemptStableMatch() {
		if (lastSuccessfulMatching != null) {
			if (isComplete(lastSuccessfulMatching) && isStable(lastSuccessfulMatching)) {
				hasStableMatch = true;
				return;
			}
		}
		int size = 0;
		//stores sizes of longest preference list an agent has 
		int maxCardinality = preferenceSystem.getMaxCardinality();
		boolean validMatchingFlag = true;
		for (Matching[] matchingsOfASize : matchings) {
			if(validMatchingFlag == false) {
				//if no matchings of a smaller size then there is
				//no reason to check larger matchings
				break;
			}
			++size;
			//matchings smaller than max cardinality need not be checked
			//were checked earlier on smaller system and extensions will not
			//change their effectiveness
			if (size < maxCardinality || (maxCardinality == 1 && size > 1)) {
				continue;
			}
			validMatchingFlag = false;
			for (Matching perfectMatching : matchingsOfASize) {
				if (loud) {
					System.out.println(perfectMatching);
				}
				//checks if matching satisfies conditions of being a matching
				//that can be used to implicate a stable matching
				if (isComplete(perfectMatching)) {
					validMatchingFlag = true;
					//check for stability
					if (isStable(perfectMatching)) {

						hasStableMatch = true;
						lastSuccessfulMatching = perfectMatching;
						return;
					}
				}
			}
		}
	}

	/**
	 * true iff all matches are acceptable to agents
	 * and all partners preferred by an agent to their partner
	 *  are matched
	 * @param perfectMatching
	 * @return
	 */
	private boolean isComplete(Matching perfectMatching) {
		for (int[] match : perfectMatching.getMatching()) {
			if (!isAcceptable(match) || !isVerified(match, perfectMatching)) {
				if (loud) {
					System.out.println("Not complete");
				}
				return false;
			}
		}
		if (loud) {
			System.out.println("Complete");
		}
		return true;
	}

	/**
	 * true iff all preferred matches to partner are matched
	 * @param match
	 * @param perfectMatching
	 * @return
	 */
	private boolean isVerified(int[] match, Matching perfectMatching) {
		for (int i = 0; i < match.length; ++i) {
			if (match[i] == -1) {
				continue;
			}
			Agent agent = preferenceSystem.getAgents(i).get(match[i]);
			int partner = perfectMatching.getPartner(i, match[i]);
			for (int j = 0; j < preferenceSystem.getNumberOfAgents(); ++j) {
				if (agent.prefers(j, partner)
						&& !perfectMatching.isMatchedInGroup((i + 1) % preferenceSystem.getNumberOfGroups(), j)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * true iff all matches are acceptable to matched agents
	 * @param match
	 * @return
	 */
	private boolean isAcceptable(int[] match) {
		for (int i = 0; i < match.length; ++i) {
			int agentIndex = match[i];
			if (agentIndex == -1) {
				continue;
			}
			int matchPartner = match[(i + 1) % match.length];
			if (preferenceSystem.getAgents(i).get(agentIndex).getUnacceptablePartners().contains(matchPartner)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * true iff no blocking tuples
	 * @param perfectMatching
	 * @return
	 */
	public boolean isStable(Matching perfectMatching) {
		blockers.reset();
		while (blockers.hasNext()) {
			ArrayList<Integer> blocker = blockers.next();
			boolean invalidBlocker = false;
			for (int i = 0; i < preferenceSystem.getNumberOfGroups(); ++i) {
				int blockingAgent = blocker.get(i);
				if (!perfectMatching.isMatchedInGroup(i, blockingAgent)) {
					invalidBlocker = true;
				}
			}
			if (!invalidBlocker && isBlocking(blocker, perfectMatching)) {
				if (loud) {
					System.out.println("Is Blocking");
				}
				return false;
			}
		}
		return true;
	}

	/**
	 * true iff blocker blocks matching
	 * @param blocker
	 * @param perfectMatching
	 * @return
	 */
	private boolean isBlocking(ArrayList<Integer> blocker, Matching perfectMatching) {
		boolean retval = true;
		for (int i = 0; i < blocker.size(); ++i) {
			int agentIndex = blocker.get(i);
			int blockPartner = blocker.get((i + 1) % blocker.size());
			int matchPartner = perfectMatching.getPartner(i, agentIndex);
			if (matchPartner != -1) {
				retval = retval && preferenceSystem.getAgents(i).get(agentIndex).prefers(blockPartner, matchPartner);
			}
		}
		return retval;
	}

	//has stable with more output
	public boolean loudHasStableMatch() {
		loud = true;
		System.out.println("Loud: " + loud);
		sufficientChecks();
		if (!hasStableMatch) {
			attemptStableMatch();
		}
		return hasStableMatch;
	}
	
	/**
	 * Check all possible matchings,
	 * skipping none
	 * @return
	 */
	public boolean checkAllPossible() {
		for (Matching[] matchingsOfASize : matchings) {
			for (Matching perfectMatching : matchingsOfASize) {
				if (isComplete(perfectMatching)) {
					if (isStable(perfectMatching)) {
						System.out.println("Stable Matching:");
						System.out.println(perfectMatching);
						return true ;
					}
				}
			}
		}
		return false;
	}
}
