package wjtoth.cyclicstablematching;

import java.util.*;
import java.util.stream.Collectors;

public class StabilityChecker {

	private PreferenceSystem preferenceSystem;

	private boolean hasStableMatch;

	private boolean loud;

	// For PerfectMatching checking
	private CrossProduct<Integer> blockers;

	private ArrayList<PerfectMatching[]> matchings;

	private PerfectMatching lastSuccessfulMatching = null;

	public StabilityChecker(int numberOfAgents, int numberOfGroups) {
		preferenceSystem = new PreferenceSystem(numberOfGroups, numberOfAgents);
		hasStableMatch = false;
		ArrayList<Integer> agents = new ArrayList<Integer>();
		for (int i = 0; i < numberOfAgents; ++i) {
			agents.add(i);
		}
		blockers = new CrossProduct<Integer>(agents, numberOfGroups);
		matchings = new ArrayList<PerfectMatching[]>();
		buildMatchings(numberOfAgents, numberOfGroups);
		loud = false;
	}

	private void buildMatchings(int numberOfAgents, int numberOfGroups) {
		List<int[]> permutations = new ArrayList<>();
		for (PermutationArray permutationArray : Permutations.permutationsOfAllSubsets(numberOfAgents)) {
			permutations.add(permutationArray.getArray());
		}
		System.out.println("Have permutations");
		ArrayList<List<int[]>> permutationsSplitByLength = splitByLength(permutations, numberOfAgents);
		System.out.println("Split permutations");
		ArrayList<PerfectMatching> matchingSet = new ArrayList<PerfectMatching>();
		for (List<int[]> permutationsOfALength : permutationsSplitByLength) {
			System.out.println("Processing: " + permutationsOfALength.size() + " permutations");
			List<PerfectMatching> uniqueMatchings = getMatchings(permutationsOfALength, numberOfAgents, numberOfGroups)
					.stream().distinct().collect(Collectors.toList());
			matchingSet.addAll(uniqueMatchings);
			PerfectMatching[] matchingsArray = new PerfectMatching[matchingSet.size()];
			matchingSet.toArray(matchingsArray);
			matchings.add(matchingsArray);
			matchingSet.clear();
		}
		System.out.println("Done Processing Permutations");
	}

	private List<PerfectMatching> getMatchings(List<int[]> permutations, int numberOfAgents, int numberOfGroups) {
		CrossProduct<int[]> crossProduct = new CrossProduct<int[]>(permutations, 2);
		List<PerfectMatching> matchingSet = new LinkedList<>();
		while (crossProduct.hasNext()) {
			ArrayList<int[]> match = crossProduct.next();
			PerfectMatching perfectMatching = new PerfectMatching(2, numberOfAgents);
			perfectMatching.setMatchingFromPermutations(match);
			if (perfectMatching.validate()) {
				matchingSet.add(perfectMatching);
			}
		}
		for (int i = 2; i < numberOfGroups; ++i) {
			matchingSet = extendMatchingsByPermutations(permutations, matchingSet);
		}

		return matchingSet.stream().distinct().collect(Collectors.toList());
	}

	private List<PerfectMatching> extendMatchingsByPermutations(List<int[]> permutations,
			List<PerfectMatching> perfectMatchings) {
		Set<PerfectMatching> retval = new TreeSet<>();
		for (PerfectMatching perfectMatching : perfectMatchings) {
			for (int[] permutation : permutations) {
				PerfectMatching extendedMatching = perfectMatching.extend(permutation);
				if (extendedMatching.validate()) {
					retval.add(perfectMatching.extend(permutation));
				}
			}
		}
		return retval.stream().distinct().collect(Collectors.toList());
	}

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

	public void setPreferenceSystem(PreferenceSystem preferenceSystem) {
		this.preferenceSystem = preferenceSystem;
		hasStableMatch = false;
	}

	public boolean hasStableMatch() {
		hasStableMatch = false;
		loud = false;
		// Generalized by new Stable Match check (Claim 1)
		if (sufficientChecks()) {
			return hasStableMatch;
		}
		if (!hasStableMatch) {
			attemptStableMatch();
		}
		if (loud) {
			System.out.println("Has Stable: " + hasStableMatch);
		}
		return hasStableMatch;
	}

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
	public void attemptStableMatch() {
		if (lastSuccessfulMatching != null) {
			if (isComplete(lastSuccessfulMatching) && isStable(lastSuccessfulMatching)) {
				hasStableMatch = true;
				return;
			}
		}
		int size = 0;
		int maxCardinality = preferenceSystem.getMaxCardinality();
		boolean validMatchingFlag = true;
		for (PerfectMatching[] matchingsOfASize : matchings) {
			if(validMatchingFlag == false) {
				break;
			}
			++size;
			if (size < maxCardinality || (maxCardinality == 1 && size > 1)) {
				continue;
			}
			validMatchingFlag = false;
			for (PerfectMatching perfectMatching : matchingsOfASize) {
				if (loud) {
					System.out.println(perfectMatching);
				}
				if (isComplete(perfectMatching)) {
					validMatchingFlag = true;
					if (isStable(perfectMatching)) {

						hasStableMatch = true;
						lastSuccessfulMatching = perfectMatching;
						return;
					}
				}
			}
		}
	}

	private boolean isComplete(PerfectMatching perfectMatching) {
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

	private boolean isVerified(int[] match, PerfectMatching perfectMatching) {
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

	public boolean isStable(PerfectMatching perfectMatching) {
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

	private boolean isBlocking(ArrayList<Integer> blocker, PerfectMatching perfectMatching) {
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

	public boolean loudHasStableMatch() {
		loud = true;
		System.out.println("Loud: " + loud);
		sufficientChecks();
		if (!hasStableMatch) {
			attemptStableMatch();
		}
		return hasStableMatch;
	}
	
	public boolean checkAllPossible() {
		for (PerfectMatching[] matchingsOfASize : matchings) {
			for (PerfectMatching perfectMatching : matchingsOfASize) {
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
