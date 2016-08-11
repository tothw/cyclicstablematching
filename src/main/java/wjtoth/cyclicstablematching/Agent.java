package wjtoth.cyclicstablematching;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class Agent implements Comparable<Agent> {

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
	//number of partners agent finds acceptable
	private int acceptablePartnerCount = 0;
	//set of partners agent finds unacceptable
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

	//set preferences to int[]
	//int[i] should be ranking of agent i
	//updates acceptablePartnerCount and unacceptablePartners
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
	
	//sets agent preference over agentIndex to ranking
	//updates acceptablePartnerCount and unacceptablePartners
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
		if(unacceptablePartners.contains(agentIndex2)) {
			if(unacceptablePartners.contains(agentIndex1)) {
				return false;
			} else {
				return true;
			}
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
	
	//copy
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
	
	//returns first choice partner 
	public int getFirstChoice() {
		Iterator<Integer> rankedChoices = rankedOrder().iterator();
		if(rankedChoices.hasNext()) {
			return rankedChoices.next();		
		}
		return -1;
	}
	
	//pretty print preference list of agent
	//as list of partners in ranked order
	public String toString() {
		return rankedOrder().toString();
	}
	
	//true if <= in terms of preference system
	//that is lexicographic order of ranked agents
	public int compareTo(Agent agent) {
		return this.computeHash().compareTo(agent.computeHash());
	}
	
	//hash string consists of acceptable partners in ranked order
	//following by '?' characters for each unacceptable agent
	//note '?' follows '0' to '9' is ascii so unranked is lexicographically
	//larger than ranked
	public String computeHash() {
		StringBuffer sb = new StringBuffer();
		for(Integer integer : rankedOrder()) {
			sb.append(integer);
		}
		for(int i = 0; i<unacceptablePartners.size(); ++i) {
			sb.append('?');
		}
		return sb.toString();
	}
}