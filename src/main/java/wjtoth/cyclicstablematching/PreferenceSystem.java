package wjtoth.cyclicstablematching;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class PreferenceSystem implements Comparable<PreferenceSystem>{

	private ArrayList<Group> groups;
	private int[] groupSizes;
	
	private String hash;

	
	public PreferenceSystem(int[] groupSizes) {
		init(groupSizes);
	}
	
	public PreferenceSystem(int numberOfGroups, int numberOfAgents) {
		int[] groupSizes = new int[numberOfGroups];
		for(int i = 0; i<groupSizes.length; ++i) {
			groupSizes[i] = numberOfAgents;
		}
		init(groupSizes);
	}
	
	public void init(int[] groupSizes) {
		int n = groupSizes.length;
		groups = new ArrayList<Group>(n);
		for(int i = 0; i<groupSizes.length; ++i) {
			Group group = new Group(groupSizes[i], groupSizes[(i+1)%n], i);
			groups.add(group);			
		}
		this.groupSizes = groupSizes;
		computeHash();
	}

	public void setSystemGroup(int groupIndex, Group group) {
		groups.set(groupIndex, group);
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
		return agent.getIndex() == 1 && agent.getGroupIndex() == 0
				|| agent.getIndex() == 2 && agent.getGroupIndex() == 1
				|| agent.getIndex() == 2 && agent.getGroupIndex() == 2;
	}
	
	private boolean fixFirstChoice1(Agent agent) {
		return agent.getIndex() == 0 && agent.getGroupIndex() == 0 
				|| agent.getIndex() == 1 && agent.getGroupIndex() == 1
				|| agent.getIndex() == 1 && agent.getGroupIndex() == 2;
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
	
	public void sortPreferences() {
		for(int i = groups.size()-1; i >0; --i) {
			Group group = groups.get(i);
			sortPreferences(group, 0, group.getGroupSize()-1);
		}
	}
	
	private void sortPreferences(Group group, int p, int r) {
		if( p < r) {
			int q = partition(group, p, r);
			sortPreferences(group, p, q-1);
			sortPreferences(group, q+1, r);
		}
	}
	
	private int partition(Group group, int p, int r) {
		Agent x = group.getAgents().get(r);
		int i = p-1;
		for(int j = p; j<r; ++j) {
			if(group.getAgents().get(j).compareTo(x)) {
				++i;
				exchange(group, i, j);
			}
		}
		exchange(group, i+1, r);
		return i+1;
	}
	private void exchange(Group group, int i, int j) {
		ArrayList<Agent> agents = group.getAgents();
		Agent tempAgent = agents.get(i);
		agents.set(i, agents.get(j));
		agents.set(j, tempAgent);
		//adjust preferences of group whose preferences face this one
		for(Agent agent : groups.get((group.getIndex() -1 + groups.size()) % groups.size()).getAgents()) {
			int[] preferences = agent.getPreferences();
			int tempRank = preferences[i];
			agent.setAgentPreference(i, preferences[j]);
			agent.setAgentPreference(j, tempRank);
		}
	}

	public String getHash() {
		return hash;
	}

	public String computeHash() {
		StringBuffer sb = new StringBuffer();
		for(Group group : groups) {
			for(Agent agent : group.getAgents()) {
				int[] preferences = agent.getPreferences();
				for(int i  = 0; i< preferences.length; ++i) {
					sb.append(preferences[i]);
				}
			}
		}
		hash = sb.toString();
		return hash;
	}

	@Override
	public int compareTo(PreferenceSystem preferenceSystem) {
		return computeHash().compareTo(preferenceSystem.computeHash());
	}

	public ArrayList<Group> getGroups() {
		return groups;
	}

	public ArrayList<Agent> getAgents(int i) {
		return groups.get(i).getAgents();
	}
}
