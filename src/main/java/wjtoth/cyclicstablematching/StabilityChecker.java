package wjtoth.cyclicstablematching;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

public class StabilityChecker {

	private PreferenceSystem preferenceSystem;
	private final CrossProduct<int[]> crossProduct;

	private boolean hasStableMatch;

	private boolean loud;

	// For PerfectMatching checking
	private CrossProduct<Integer> blockers;

	private PerfectMatching[] matchings;

	public StabilityChecker(int numberOfAgents, int numberOfGroups) {
		ArrayList<int[]> permutations = Permutations.permutations(numberOfAgents);
		crossProduct = new CrossProduct<int[]>(permutations, numberOfGroups);
		preferenceSystem = new PreferenceSystem(numberOfGroups, numberOfAgents);
		hasStableMatch = false;
		ArrayList<Integer> agents = new ArrayList<Integer>();
		for (int i = 0; i < numberOfAgents; ++i) {
			agents.add(i);
		}
		blockers = new CrossProduct<Integer>(agents, numberOfGroups);
		Set<PerfectMatching> matchingSet = new TreeSet<PerfectMatching>();
		while (crossProduct.hasNext()) {
			PerfectMatching perfectMatching = new PerfectMatching(numberOfGroups, numberOfAgents);
			perfectMatching.setMatchingFromPermutations(crossProduct.next());
			matchingSet.add(perfectMatching);
		}
		matchings = new PerfectMatching[matchingSet.size()];
		matchingSet.toArray(matchings);
		loud = false;
	}

	public void setPreferenceSystem(PreferenceSystem preferenceSystem) {
		this.preferenceSystem = preferenceSystem;
		hasStableMatch = false;
	}

	public boolean hasStableMatch() {
		hasStableMatch = false;
		loud = false;
		sufficientChecks();
		if (!hasStableMatch && preferenceSystem.size() % (preferenceSystem.getNumberOfAgents() * preferenceSystem.getNumberOfGroups()) == 0) {
			attemptStableMatch();
		}
		if (loud) {
			System.out.println("Has Stable: " + hasStableMatch);
		}
		return hasStableMatch;
	}

	private void sufficientChecks() {
		checkFirstChoiceCycle();
		if (!hasStableMatch) {
			checkFirstChoiceNineCycle();
		}
		if(hasStableMatch) {
			System.out.println("SUFFIFICIENT");
		}
		/*
		 * These will never happen by starting symmetry if(!hasStableMatch) {
		 * checkAllSameDifferent(); }
		 */
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

	@SuppressWarnings("unused")
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
		if (negativeOneChoices = true) {
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
		for (PerfectMatching perfectMatching : matchings) {
			if (loud) {
				System.out.println(perfectMatching);
			}
			if (isComplete(perfectMatching) && isStable(perfectMatching)) {
				hasStableMatch = true;
				break;
			}

		}
	}

	private boolean isComplete(PerfectMatching perfectMatching) {
		for (int[] match : perfectMatching.getMatching()) {
			if (!isAcceptable(match)) {
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

	private boolean isAcceptable(int[] match) {
		for (int i = 0; i < match.length; ++i) {
			int agentIndex = match[i];
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
			if (isBlocking(blocker, perfectMatching)) {
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
			retval = retval && preferenceSystem.getAgents(i).get(agentIndex).prefers(blockPartner, matchPartner);
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
}
