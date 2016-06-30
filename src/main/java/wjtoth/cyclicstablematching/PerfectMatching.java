package wjtoth.cyclicstablematching;

import java.util.ArrayList;
import java.util.Arrays;

public class PerfectMatching {
	
	private final int NUMBER_OF_GROUPS;
	private final int NUMBER_OF_AGENTS;
	
	//Store matching
	ArrayList<int[]> matching;
	
	public PerfectMatching(int numberOfGroups, int numberOfAgents) {
		NUMBER_OF_GROUPS = numberOfGroups;
		NUMBER_OF_AGENTS = numberOfAgents;
		matching = new ArrayList<int[]>(numberOfGroups);
	}

	public void setMatching(ArrayList<int[]> permutationProduct) {
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
	
	public int getPartner(int group, int agent) {
		int partner = -1;
		for(int[] match : matching) {
			if(match[group] == agent) {
				partner = match[(group+1)%match.length];
			}
		}
		return partner;
	}
}
