package wjtoth.cyclicstablematching;

import java.util.ArrayList;
import java.util.Arrays;

public class StabilityChecker {

	private PreferenceSystem preferenceSystem;
	private final CrossProduct<int[]> crossProduct;
	
	private boolean hasStableMatch;
	
	private boolean loud;
	
	//For PerfectMatching checking
	private CrossProduct<Integer> blockers;
	
	public StabilityChecker(int numberOfAgents, int numberOfGroups) {
		ArrayList<int[]> permutations = Permutations.permutations(numberOfAgents);
		crossProduct = new CrossProduct<int[]>(permutations, numberOfGroups);
		preferenceSystem = new PreferenceSystem(numberOfGroups, numberOfAgents);
		hasStableMatch = false;
		ArrayList<Integer> agents = new ArrayList<Integer>();
		for(int i = 0; i<numberOfAgents; ++i) {
			agents.add(i);
		}
		blockers = new CrossProduct<Integer>(agents, numberOfGroups);
	}
	
	public void setPreferenceSystem(PreferenceSystem preferenceSystem) {
		this.preferenceSystem = preferenceSystem;
		hasStableMatch = false;
	}
	
	public boolean hasStableMatch() {
		loud = false;
		sufficientChecks();
		if(!hasStableMatch) {
			attemptStableMatch(crossProduct);
		}
		return hasStableMatch;
	}

	private void sufficientChecks() {
		checkFirstChoiceCycle();
		if(!hasStableMatch) {
			checkFirstChoiceNineCycle();
		}

		/* These will never happen by starting symmetry
		 * if(!hasStableMatch) {
			checkAllSameDifferent();
		}*/
	}
	
	private void checkFirstChoiceCycle() {
		for(Agent agent : preferenceSystem.getAgents(0)) {
			checkFirstChoiceCycle(agent);
			if(hasStableMatch) {
				break;
			}
		}
	}
	
	private void checkFirstChoiceCycle(Agent agent) {
		int agentFirstChoice = agent.getFirstChoice();
		int partnerIndex = agentFirstChoice;
		for(int i = 1; i<preferenceSystem.getGroups().size(); ++i) {
			if(partnerIndex == -1) {
				return;
			}
			partnerIndex = preferenceSystem.getAgents(i).get(partnerIndex).getFirstChoice();
		}
		if(agent.getIndex() == partnerIndex) {
			hasStableMatch = true;
		}
	}

	private void checkFirstChoiceNineCycle() {
		for(Agent agent : preferenceSystem.getAgents(0)) {
			checkFirstChoiceNineCycle(agent);
			if(hasStableMatch) {
				break;
			}
		}
	}

	private void checkFirstChoiceNineCycle(Agent agent) {
		int agentFirstChoice = agent.getFirstChoice();
		int partnerIndex = agentFirstChoice;
		for(int i = 1; i<3*preferenceSystem.getGroups().size(); ++i) {
			if(partnerIndex == -1) {
				return;
			}
			partnerIndex = preferenceSystem.getAgents(i%3).get(partnerIndex).getFirstChoice();
		}
		if(agent.getIndex() == partnerIndex) {
			hasStableMatch = true;
		}
	}
	
	@SuppressWarnings("unused")
	private void checkAllSameDifferent() {
		for(Group group : preferenceSystem.getGroups()) {
			checkAllSameDifferent(group);
		}
	}
	
	private void checkAllSameDifferent(Group group) {
		int[] firstChoices  = new int[group.getGroupSize()];
		for(int i = 0; i<firstChoices.length; ++i) {
			firstChoices[i] = group.getAgents().get(i).getFirstChoice();
		}
		//check all -1
		boolean negativeOneChoices = true;
		for(int i = 0; i<firstChoices.length; ++i) {
			negativeOneChoices = negativeOneChoices && firstChoices[i] == -1;
		}
		if(negativeOneChoices = true) {
			return;
		}
		if(firstChoices.length == 1) {
			if(firstChoices[0] != -1){
				hasStableMatch = true;
			}
			return;
		}
		Arrays.sort(firstChoices);
		System.out.println("First Choices: " + Arrays.toString(firstChoices));
		if(firstChoices[0] == 0 && firstChoices[firstChoices.length-1] == firstChoices.length-1) {
			hasStableMatch = true;
			System.out.println("All Different");
			return;
		}
		int choice = firstChoices[0];
		if(firstChoices[firstChoices.length - 1] == choice) {
			hasStableMatch = true;
			System.out.println("All Same");
		}
	}
	
	//assumes all groups have same size!
	public void attemptStableMatch(CrossProduct<int[]> permutationProductIterator) {
		permutationProductIterator.reset();
		PerfectMatching perfectMatching = new PerfectMatching(permutationProductIterator.getDimension(),permutationProductIterator.getLength());
		while(permutationProductIterator.hasNext()) {
			ArrayList<int[]> permutationProduct = permutationProductIterator.next();
			perfectMatching.setMatching(permutationProduct);
			if (isStable(perfectMatching)) {
				hasStableMatch = true;
				break;
			}
			
		}
		if(loud)
			System.out.println(perfectMatching.toString());
	}
	
	private boolean isStable(PerfectMatching perfectMatching) {
		blockers.reset();
		while(blockers.hasNext()){
			ArrayList<Integer> blocker = blockers.next();
			if(isBlocking(blocker, perfectMatching)) {
				return false;
			}
		}
		return true;
	}
	
	private boolean isBlocking(ArrayList<Integer> blocker, PerfectMatching perfectMatching) {
		boolean retval = true;
		for(int i = 0; i<blocker.size(); ++i) {
			int agentIndex = blocker.get(i);
			int blockPartner = blocker.get((i+1)%blocker.size());
			int matchPartner = perfectMatching.getPartner(i, agentIndex);
			retval = retval && !preferenceSystem.getAgents(i).get(agentIndex).prefers(blockPartner, matchPartner);
		}
		return retval;
	}
	
	public boolean loudHasStableMatch() {
		loud = true;
		System.out.println("Loud: " + loud);
		sufficientChecks();
		if(!hasStableMatch) {
			attemptStableMatch(crossProduct);
		}
		return hasStableMatch;
	}
}
