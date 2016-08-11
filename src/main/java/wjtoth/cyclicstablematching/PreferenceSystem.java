package wjtoth.cyclicstablematching;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Class abstracts a problem instance for cyclic stable matching
 * @author wjtoth
 *
 */
public class PreferenceSystem implements Comparable<PreferenceSystem>{

	//list of genders involved
	private ArrayList<Group> groups;
	//lists of group sizes TODO superfluous with new Group impl
	private int[] groupSizes;
	//size of longest preference list of an agent in any group
	//in this instance
	private int maxCardinality;

	//hash representing preferenceSystem
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
	
	//constructs groups of groupSizes
	//all agents are empty initially
	//and need to populated with setters
	//or populated via extend
	public void init(int[] groupSizes) {
		int n = groupSizes.length;
		groups = new ArrayList<Group>(n);
		for(int i = 0; i<groupSizes.length; ++i) {
			Group group = new Group(groupSizes[i], groupSizes[(i+1)%n], i);
			groups.add(group);			
		}
		this.groupSizes = groupSizes;
		computeHash();
		computeMaxCardinality();
	}
	
	//find length of longest preference list
	public void computeMaxCardinality() {
		maxCardinality = 0;
		for(Group group: groups) {
			for(Agent agent : group.getAgents()) {
				int listSize = agent.getAcceptablePartnerCount();
				if(listSize > maxCardinality) {
					maxCardinality = listSize;
				}
			}
		}
	}

	//set indexed group to group
	public void setSystemGroup(int groupIndex, Group group) {
		groups.set(groupIndex, group);
	}
	
	//deep copy entire system
	public PreferenceSystem deepCopy() {
		PreferenceSystem preferenceSystem = new PreferenceSystem(groupSizes);
		for(int i = 0; i<groups.size(); ++i) {
			Group groupCopy = groups.get(i).deepCopy();
			preferenceSystem.setSystemGroup(i, groupCopy);
		}
		computeHash();
		computeMaxCardinality();
		return preferenceSystem;
	}
	
	//find extender agent and return all possible extensions
	//not used in correct DFS implementation
	//was used in BFS implementation
	public List<PreferenceSystem> extend() {
		List<PreferenceSystem> newSystems = new ArrayList<PreferenceSystem>();
		Agent extender = getExtender();
		Collection<Integer> unacceptablePartners = filterUnacceptablePartners(extender);
		Integer[] unacceptablePartnerArray = new Integer[unacceptablePartners.size()];
		unacceptablePartners.toArray(unacceptablePartnerArray);
		for(int unacceptablePartner : unacceptablePartnerArray) {
			newSystems.add(extend(extender, unacceptablePartner));
		}
		return newSystems;
	}
	
	//pass in agent to extend and unacceptablePartner of that agent
	//that will be appended to end of extender's ranking
	//return resulting PreferenceSystem
	//retval is a deepCopy, not this
	public PreferenceSystem extend(Agent extender, int unacceptablePartner) {
		extender.append(unacceptablePartner);
		PreferenceSystem newSystem = this.deepCopy();
		extender.remove(unacceptablePartner);
		return newSystem;
	}
	
	//not used in correct impl
	//was used in BFS to reduce some symmetry by preventing agents from extending by
	//unacceptable partners that we may assume without loss of generality they do not
	//extend to 
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
	
	
	//get lexicographically minimal agent with shortest preference list
	//in this system
	public Agent getExtender() {
		Agent extender = groups.get(0).shortestAgent();
		int groupLength = groups.get(0).sumAcceptablePartnerCount();
		for(int i = 1; i < groups.size(); ++i) {
			Agent candidate = groups.get(i).shortestAgent();
			int candidateGroupLength = groups.get(i).sumAcceptablePartnerCount();
			if(candidate.getAcceptablePartnerCount() < extender.getAcceptablePartnerCount() && candidateGroupLength < groupLength) {
				extender = candidate;
				groupLength = candidateGroupLength;
			}
		}
		return extender;
	}
	
	//pretty print this preferenceSystem
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i<groups.size(); ++i) {
			sb.append("Group " + i + ":\n");
			sb.append(groups.get(i).toString());
		}
		return sb.toString();
	}
	
	public void sortPreferences(int group) {
		//java Collections sort using Agent.compare()
		sortPreferences(groups.get(group));
	}

	//sort agents list of passed in group
	//TODO should this method be implemented here?
	public void sortPreferences(Group group) {
		Collections.sort(group.getAgents());
	}

	public String getHash() {
		return hash;
	}

	//process hash by appending each group hash
	public String computeHash() {
		StringBuffer sb = new StringBuffer();
		for(Group group : groups) {
			sb.append(group.computeHash());
		}
		hash = sb.toString();
		return hash;
	}

	@Override
	//compare systems lexicographically by their hashes
	//given how hashes are computed this should be equivalent
	//to listing ranked orders of agents from group 0 agent 0 to
	//last group last agent and comparing lexicographically
	public int compareTo(PreferenceSystem preferenceSystem) {
		int sizeDifference = size() - preferenceSystem.size();
		if(sizeDifference != 0) {
			return sizeDifference;
		} else {
			return computeHash().compareTo(preferenceSystem.computeHash());
		}
	}

	public ArrayList<Group> getGroups() {
		return groups;
	}

	public ArrayList<Agent> getAgents(int group) {
		return groups.get(group).getAgents();
	}
	
	//returns sum total of acceptable partner counts
	//for all agents
	public int size() {
		int retval = 0;
		for(Group group : groups ) {
			retval += group.sumAcceptablePartnerCount();
		}
		return retval;
	}

	public int getNumberOfGroups() {
		return groups.size();
	}

	//TODO what if groups aren't same size?
	public int getNumberOfAgents() {
		return groupSizes[0];
	}
	
	public int getMaxCardinality() {
		return maxCardinality;
	}
}
