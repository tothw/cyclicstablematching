package wjtoth.cyclicstablematching;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class PreferenceSystem {

	private ArrayList<Group> groups;
	private int[] groupSizes;
	
	private boolean hasStableMatch;
	private PerfectMatching stableMatching;
	
	//Form PerfectMatching checking
	CrossProduct<Integer> blockers;
	
	public PreferenceSystem(int[] groupSizes) {
		int n = groupSizes.length;
		groups = new ArrayList<Group>(n);
		for(int i = 0; i<groupSizes.length; ++i) {
			Group group = new Group(groupSizes[i], groupSizes[(i+1)%n], i);
			groups.add(group);			
		}
		this.groupSizes = groupSizes;
		hasStableMatch = false;
		ArrayList<Integer> agents = new ArrayList<Integer>();
		for(int i = 0; i<groupSizes[0]; ++i) {
			agents.add(i);
		}
		blockers = new CrossProduct<Integer>(agents, groups.size());
	}
	
	public void setSystemGroup(int groupIndex, Group group) {
		groups.set(groupIndex, group);
	}
	
	public ArrayList<Group> getGroups() {
		return groups;
	}

	public void setGroups(ArrayList<Group> groups) {
		this.groups = groups;
	}
		
	public int sumAcceptablePartnerCount() {
		int sum = 0;
		for(Group group: groups) {
			sum += group.sumAcceptablePartnerCount();
		}
		return sum;
	}
	
	public PreferenceSystem deepCopy() {
		PreferenceSystem preferenceSystem = new PreferenceSystem(groupSizes);
		for(int i = 0; i<groups.size(); ++i) {
			Group groupCopy = groups.get(i).deepCopy();
			preferenceSystem.setSystemGroup(i, groupCopy);
		}
		return preferenceSystem;
	}
	
	public List<PreferenceSystem> extend() {
		List<PreferenceSystem> newSystems = new ArrayList<PreferenceSystem>();
		Agent extender = getExtender();
		Collection<Integer> unacceptablePartners = filterUnacceptablePartners(extender);
		Integer[] unacceptablePartnerArray = new Integer[unacceptablePartners.size()];
		unacceptablePartners.toArray(unacceptablePartnerArray);
		for(int unacceptablePartner : unacceptablePartnerArray) {
			//set
			extender.append(unacceptablePartner);
			newSystems.add(this.deepCopy());
			extender.remove(unacceptablePartner);
		}
		return newSystems;
	}
	
	public Collection<Integer> filterUnacceptablePartners(Agent agent) {
		if(agent.getAcceptablePartnerCount() == 0) {
			ArrayList<Integer> retval = new ArrayList<Integer>();
			Set<Integer> unacceptablePartners = agent.getUnacceptablePartners();
			if(fixFirstChoice1(agent)) {
				if(unacceptablePartners.contains(1)) {
					retval.add(1);	
				}
				return retval;
			}else{
				if(fixFirstChoice2(agent)){
					if(unacceptablePartners.contains(2)) {
						retval.add(2);	
					}
					return retval;
				}else{
					unacceptablePartners.remove(0);
					retval.addAll(unacceptablePartners);
					return retval;
				}
			}
		}
		return agent.getUnacceptablePartners();
	}
	
	private boolean fixFirstChoice2(Agent agent) {
		return (agent.getIndex() == 1 && agent.getGroupIndex() == 0)
				|| (agent.getIndex() == 2 && agent.getGroupIndex() == 1)
				|| (agent.getIndex() == 2 && agent.getGroupIndex() == 2);
	}
	
	private boolean fixFirstChoice1(Agent agent) {
		return (agent.getIndex() == 0 && agent.getGroupIndex() == 0) 
				|| (agent.getIndex() == 1 && agent.getGroupIndex() == 1)
				|| (agent.getIndex() == 1 && agent.getGroupIndex() == 2);
	}
	
	private Agent getExtender() {
		Agent extender = groups.get(0).shortestAgent();
		int groupLength = groups.get(0).sumAcceptablePartnerCount();
		for(int i = 1; i < groups.size(); ++i) {
			Agent candidate = groups.get(i).shortestAgent();
			int candidateGroupLength = groups.get(i).sumAcceptablePartnerCount();
			if(candidate.getAcceptablePartnerCount() <= extender.getAcceptablePartnerCount() && candidateGroupLength <= groupLength) {
				extender = candidate;
				groupLength = candidateGroupLength;
			}
		}
		return extender;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i<groups.size(); ++i) {
			sb.append("Group " + i + ":\n");
			sb.append(groups.get(i).toString());
		}
		return sb.toString();
	}
	
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
	public void attemptStableMatch(CrossProduct<int[]> permutationProductIterator) {
		PerfectMatching perfectMatching = new PerfectMatching(permutationProductIterator.getDimension(),permutationProductIterator.getLength());
		while(permutationProductIterator.hasNext()) {
			ArrayList<int[]> permutationProduct = permutationProductIterator.next();
			perfectMatching.setMatching(permutationProduct);
			if (isStable(perfectMatching)) {
				hasStableMatch = true;
				stableMatching = perfectMatching;
				break;
			}
		}
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
			/**
			System.out.println("agentIndex " + agentIndex);
			System.out.println("blockPartner " + blockPartner);
			System.out.println("matchPartner " + matchPartner);
			**/
			retval = retval && !groups.get(i).getAgents().get(agentIndex).prefers(blockPartner, matchPartner);
		}
		return retval;
	}
	
	public PerfectMatching getStableMatching() {
		return stableMatching;
	}
}
