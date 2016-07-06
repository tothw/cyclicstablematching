package wjtoth.cyclicstablematching;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class Agent {

	//value 0 means index agent is unacceptable
	//greater value -> agent preferred more
	private int[] preferences;
	private int index;
	public int getIndex() {
		return index;
	}

	public int getGroupIndex() {
		return groupIndex;
	}

	private int groupIndex;
	private int acceptablePartnerCount = 0;
	private Set<Integer> unacceptablePartners;
	
	public Agent(int partnerGroupSize, int index, int groupIndex) {
		preferences = new int[partnerGroupSize];
		unacceptablePartners = new HashSet<Integer>(partnerGroupSize);
		for(int i = 0; i<partnerGroupSize; ++i) {
			unacceptablePartners.add(i);
		}
		this.index = index;
		this.groupIndex = groupIndex;
	}
	
	public int[] getPreferences() {
		return preferences;
	}

	public void setPreferences(int[] preferences) {
		this.preferences = preferences;
		acceptablePartnerCount = 0;
		for(int i = 0; i<preferences.length; ++i) {
			if (preferences[i] != 0) {
				++acceptablePartnerCount;
				unacceptablePartners.remove(i);
			}else{
				unacceptablePartners.add(i);
			}
		}
	}
	
	public void setAgentPreference(int agentIndex, int ranking) {
		if(agentIndex < preferences.length && ranking >= 0) {
			if(this.preferences[agentIndex] == 0 && ranking != 0) {
				++acceptablePartnerCount;
				unacceptablePartners.remove(agentIndex);
			}
			if(this.preferences[agentIndex] !=0 && ranking==0) {
				--acceptablePartnerCount;
				unacceptablePartners.add(agentIndex);
			}
			this.preferences[agentIndex] = ranking;
		}else{
			System.out.println("Cannot assign " + agentIndex + " ranking of " + ranking);
		}
	}
	
	//true if prefers 1 to 2
	public boolean prefers(int agentIndex1, int agentIndex2) {
		if(agentIndex2 < 0) {
			return false;
		}
		return preferences[agentIndex1] > preferences[agentIndex2];
	}
	
	//returns array of partner indices in favourite to least favourite order
	public Collection<Integer> rankedOrder() {
		SortedMap<Integer,Integer> sortedMap = new TreeMap<Integer,Integer>(new Comparator<Integer>() {
			@Override
			public int compare(Integer a, Integer b) {
				return b.compareTo(a);
			}
		});
		for(int i = 0; i<preferences.length; ++i) {
			if(preferences[i] > 0) {
				sortedMap.put(preferences[i], i);
			}
		}
		return sortedMap.values();
	}
	
	public int getAcceptablePartnerCount() {
		return acceptablePartnerCount;
	}
	
	public Set<Integer> getUnacceptablePartners() {
		return unacceptablePartners;
	}
	
	public Agent deepCopy() {
		Agent agent = new Agent(preferences.length, index, groupIndex);
		for(int i = 0; i<preferences.length; ++i) {
			agent.setAgentPreference(i, preferences[i]);
		}
		return agent;
	}
	
	//adds agent as least ranked
	public void append(int agent) {
		if(preferences[agent] != 0 || agent >= preferences.length || agent < 0) {
			System.out.println("Unappendable agent");
		}else{
			for(int i = 0; i<preferences.length; ++i) {
				if(preferences[i] != 0) {
					preferences[i] += 1;
				}
			}
			preferences[agent] = 1;
			++acceptablePartnerCount;
			unacceptablePartners.remove(agent);
		}
	}
	
	//drops agent and everyone else's rank
	public void remove(int agent) {
		if(preferences[agent] == 0 || agent >= preferences.length || agent < 0) {
			System.out.println("Unremoveable agent");
		}else{
			for(int i = 0; i<preferences.length; ++i) {
				if(preferences[i] != 0) {
					preferences[i] -= 1;
				}
			}
			preferences[agent] = 0;
			--acceptablePartnerCount;
			unacceptablePartners.add(agent);
		}
	}
	
	public int getFirstChoice() {
		Iterator<Integer> rankedChoices = rankedOrder().iterator();
		if(rankedChoices.hasNext()) {
			return rankedChoices.next();		
		}
		return -1;
	}
	
	public String toString() {
		return rankedOrder().toString();
	}
	
	//true if <= in terms of preference system
	public boolean compareTo(Agent agent) {
		int[] agentPreferences = agent.getPreferences();
		Set<Integer> agentUnacceptablePartners = agent.getUnacceptablePartners();
		for(int i =0; i<agentPreferences.length; ++i) {
			boolean containsI = unacceptablePartners.contains(i);
			boolean agentContainsI = agentUnacceptablePartners.contains(i);
			//if i unacceptable to this but not agent
			if(containsI && !agentContainsI) {
				return true;
			}
			//if i acceptable to this but not agent
			if(!containsI && agentContainsI) {
				return false;
			}
			//if acceptable to both
			if(!(containsI || agentContainsI)){
				//return true if strictly less than
				if(preferences[i] < agentPreferences[i]) {
					return true;
				}
				//return false if strictly greater than
				if(preferences[i] > agentPreferences[i]) {
					return false;
				}
				//check next index if equal
			}
		}
		//if all indices survived checks then they are the same
		return true;
	}
}