package wjtoth.cyclicstablematching;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class TreePreferenceSystem implements PreferenceSystem {

	private PreferenceSystem parentPreferenceSystem;
	private int extensionGroup;
	private int extensionAgent;
	private int appendedAgent;
	private boolean hasStableMatch;
	
	public TreePreferenceSystem(PreferenceSystem parentPreferenceSystem, int extensionGroup, int extensionAgent, int appendedAgent) {
		this.parentPreferenceSystem = parentPreferenceSystem;
		this.extensionGroup = extensionGroup;
		this.extensionAgent = extensionAgent;
		this.appendedAgent = appendedAgent;
		hasStableMatch = false;
	}

	@Override
	public int sumAcceptablePartnerCount() {
		return parentPreferenceSystem.sumAcceptablePartnerCount() + 1;
	}

	@Override
	public PreferenceSystem deepCopy() {
		return new TreePreferenceSystem(parentPreferenceSystem.deepCopy(), extensionGroup, extensionAgent, appendedAgent);
	}

	@Override
	public List<PreferenceSystem> extend() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Integer> filterUnacceptablePartners(Agent agent) {
		Collection<Integer> previousUnacceptablePartners = parentPreferenceSystem.filterUnacceptablePartners(agent);
		if(agent.getIndex() == extensionAgent && agent.getGroupIndex() == extensionGroup) {
			previousUnacceptablePartners.remove(appendedAgent);
		}
		return previousUnacceptablePartners;
	}

	@Override
	public boolean hasStableMatch(CrossProduct<int[]> crossProduct) {
		sufficientChecks();
		if(!hasStableMatch) {
			attemptStableMatch(crossProduct);
		}
		return hasStableMatch;
	}
	
	private void sufficientChecks() {
		checkFirstChoiceCycle();
		/* These will never happen by starting symmetry
		 * if(!hasStableMatch) {
			checkAllSameDifferent();
		}*/
	}
	
	private void checkFirstChoiceCycle() {
		for(Agent agent : groups.get(0).getAgents()) {
			checkFirstChoiceCycle(agent);
			if(hasStableMatch) {
				break;
			}
		}
	}
	
	private void checkFirstChoiceCycle(Agent agent) {
		int agentFirstChoice = agent.getFirstChoice();
		int partnerIndex = agentFirstChoice;
		for(int i = 1; i<groups.size(); ++i) {
			if(partnerIndex == -1) {
				return;
			}
			partnerIndex = groups.get(i).getAgents().get(partnerIndex).getFirstChoice();
		}
		if(agent.getIndex() == partnerIndex) {
			hasStableMatch = true;
		}
	}
	
	@SuppressWarnings("unused")
	private void checkAllSameDifferent() {
		for(Group group : groups) {
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
			negativeOneChoices = negativeOneChoices && (firstChoices[i] == -1);
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
	/* (non-Javadoc)
	 * @see wjtoth.cyclicstablematching.PreferenceSystem#attemptStableMatch(wjtoth.cyclicstablematching.CrossProduct)
	 */
	@Override
	public void attemptStableMatch(CrossProduct<int[]> permutationProductIterator) {
		PerfectMatching perfectMatching = new PerfectMatching(permutationProductIterator.getDimension(),permutationProductIterator.getLength());
		while(permutationProductIterator.hasNext()) {
			ArrayList<int[]> permutationProduct = permutationProductIterator.next();
			perfectMatching.setMatching(permutationProduct);
			if (isStable(perfectMatching)) {
				hasStableMatch = true;
				break;
			}
		}
	}
	
	private boolean isStable(PerfectMatching perfectMatching) {
		parentPreferenceSystem.getBlockers().reset();
		while(getBlockers().hasNext()){
			ArrayList<Integer> blocker = getBlockers().next();
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
			/**
			System.out.println("agentIndex " + agentIndex);
			System.out.println("blockPartner " + blockPartner);
			System.out.println("matchPartner " + matchPartner);
			**/
			
			retval = retval && !getAgent(i,agentIndex).prefers(blockPartner, matchPartner);
		}
		return retval;
	}

	@Override
	public Agent getAgent(int groupIndex, int agentIndex) {
		if(groupIndex != extensionGroup || agentIndex != extensionAgent) {
			return parentPreferenceSystem.getAgent(groupIndex, agentIndex);
		}else{
			Agent newAgent = parentPreferenceSystem.getAgent(groupIndex, agentIndex).deepCopy();
			newAgent.append(appendedAgent);
			return newAgent;
		}
	}
	
	@Override
	public CrossProduct<Integer> getBlockers() {
		return parentPreferenceSystem.getBlockers();
	}
}
