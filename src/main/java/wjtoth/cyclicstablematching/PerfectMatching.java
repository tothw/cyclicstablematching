package wjtoth.cyclicstablematching;

import java.util.ArrayList;

public class PerfectMatching implements Comparable<PerfectMatching>{
	
	private final int NUMBER_OF_GROUPS;
	private final int NUMBER_OF_AGENTS;
	
	//Store matching
	ArrayList<int[]> matching;
	
	public PerfectMatching(int numberOfGroups, int numberOfAgents) {
		NUMBER_OF_GROUPS = numberOfGroups;
		NUMBER_OF_AGENTS = numberOfAgents;
		matching = new ArrayList<int[]>(numberOfGroups);
	}

	public void setMatchingFromPermutations(ArrayList<int[]> permutationProduct) {
		for(int i =0; i<NUMBER_OF_AGENTS; ++i) {
			int[] match = new int[NUMBER_OF_GROUPS];
			for(int j = 0; j<NUMBER_OF_GROUPS; ++j) {
				match[j] = permutationProduct.get(j)[i];
			}
			try{
				matching.set(i, match);
			}catch(IndexOutOfBoundsException e) {
				matching.add(i,match);
			}
		}
	}
	
	public void setMatching(ArrayList<int[]> matching) {
		this.matching = matching;
	}
	
	public int getPartner(int group, int agent) {
		int partner = -1;
		for(int[] match : matching) {
			if(match[group] == agent) {
				partner = match[(group+1)%match.length];
			}
		}
		return partner;
	}

	public ArrayList<int[]> getMatching() {
		return matching;
	}

	public String toString() {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("---------\n");
		for(int i = 0; i< matching.size(); ++i) {
			int[] orderedMatch = matching.get(i);
			stringBuffer.append("(");
			for(int j = 0; j< orderedMatch.length; ++j) {
				stringBuffer.append(orderedMatch[j] + " ");
			}
			stringBuffer.append(")\n");
		}
		stringBuffer.append("---------");
		return stringBuffer.toString();
	}

	public int getNUMBER_OF_GROUPS() {
		return NUMBER_OF_GROUPS;
	}

	public int getNUMBER_OF_AGENTS() {
		return NUMBER_OF_AGENTS;
	}

	public int compareTo(PerfectMatching perfectMatching) {
		final int pmNumberOfGroups = perfectMatching.getNUMBER_OF_GROUPS();
		final int pmNumberOfAgents = perfectMatching.getNUMBER_OF_AGENTS();
		if(pmNumberOfGroups != NUMBER_OF_GROUPS) {
			return NUMBER_OF_GROUPS-pmNumberOfGroups;
		}else {
			if(pmNumberOfAgents != NUMBER_OF_AGENTS) {
				return NUMBER_OF_AGENTS - pmNumberOfAgents;
			}else{
				int thisValueTotal = 0;
				int pmValueTotal = 0;
				for(int i = 0; i< NUMBER_OF_AGENTS; ++i) {
					int[] thisMatch = matching.get(i);
					int[] pmMatch = perfectMatching.getMatching().get(i);
					int thisValue = 0;
					int pmValue = 0;
					for(int j = 0; j< NUMBER_OF_GROUPS; ++j) {
						thisValue = thisValue*NUMBER_OF_GROUPS + thisMatch[j];
						pmValue = pmValue*NUMBER_OF_GROUPS + pmMatch[j];
					}
					thisValueTotal = thisValueTotal*NUMBER_OF_GROUPS*NUMBER_OF_AGENTS + thisValue;
					pmValueTotal = pmValueTotal*NUMBER_OF_GROUPS*NUMBER_OF_AGENTS + pmValue;
				}
				return thisValueTotal-pmValueTotal;
			}
		}
	}
}
