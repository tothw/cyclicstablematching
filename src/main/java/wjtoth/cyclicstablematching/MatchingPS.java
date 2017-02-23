package wjtoth.cyclicstablematching;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MatchingPS {
	Matching matching;
	PreferenceSystem preferenceSystem;
	int size;
	
	public MatchingPS(Matching matching, PreferenceSystem preferenceSystem) {
		this.matching = matching;
		this.preferenceSystem = preferenceSystem;
		size = 0;
	}
	
	public int getPartner(int group, int agent) {
		if(agent == preferenceSystem.numberOfAgents) {
			return preferenceSystem.numberOfAgents;
		}
		int partner = matching.getPartner(group, agent);
		return preferenceSystem.isAcceptable(group, agent, partner) ? partner : preferenceSystem.numberOfAgents;
	}
	
	public boolean isMatchedInGroup(int group, int agent) {
		int a = agent;
		for(int i = 0; i<preferenceSystem.numberOfGroups; ++i) {
			int g = (group+i) % preferenceSystem.numberOfGroups;
			int partner = getPartner(g, a);
			if(!preferenceSystem.isAcceptable(g, a, partner)) {
				return false;
			}
			a = partner;
		}
		return true;
	}
	
	public int size() {
		if(size > 0) {
			return size;
		}
		for(int i =0; i< preferenceSystem.numberOfAgents; ++i) {
			if(isMatchedInGroup(0, i)) {
				++size;
			}
		}
		return size;
	}
	
	public boolean isInternallyBlocked() {
		for (int a = 0; a < preferenceSystem.numberOfAgents; ++a) {
			if (isMatchedInGroup(0, a)) {
				int partnerOfA = getPartner(0, a);
				for (int i = 0; i < preferenceSystem.ranks[0][a][partnerOfA]; ++i) {
					int b = preferenceSystem.preferences[0][a][i];
					if (isMatchedInGroup(1, b)) {
						int partnerOfB = getPartner(1, b);
						for (int j = 0; j < preferenceSystem.ranks[1][b][partnerOfB]; ++j) {
							int c = preferenceSystem.preferences[1][b][j];
							if (isMatchedInGroup(2, c)) {
								int partnerOfC = getPartner(2, c);
								if (preferenceSystem.prefers(2, c, a, partnerOfC)) {
									//System.out.println("Blocking: (" + a + "," + b + "," + c + ")");
									return true;
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

	
	public boolean isInternallyBlocked(int group, int[] triple) {
		for (int a = 0; a < preferenceSystem.numberOfAgents; ++a) {
			if(group == 0 && triple[group] == a) {
				continue;
			}
			if (isMatchedInGroup(0, a,triple)) {
				int partnerOfA = getPartner(0, a,triple);
				for (int i = 0; i < preferenceSystem.ranks[0][a][partnerOfA]; ++i) {
					int b = preferenceSystem.preferences[0][a][i];
					if(group == 1 && triple[group] == b) {
						continue;
					}
					if (isMatchedInGroup(1, b,triple)) {
						int partnerOfB = getPartner(1, b,triple);
						for (int j = 0; j < preferenceSystem.ranks[1][b][partnerOfB]; ++j) {
							int c = preferenceSystem.preferences[1][b][j];
							if(group == 2 && triple[group] == c) {
								continue;
							}
							if (isMatchedInGroup(2, c,triple)) {
								int partnerOfC = getPartner(2, c,triple);
								if (preferenceSystem.prefers(2, c, a, partnerOfC)) {
									return true;
								}
							}
						}
					}
				}
			}
		}
		return false;
	}
	
	private int getPartner(int group, int agent, int[] triple) {
		if(triple[group] == agent) {
			return triple[(group+1)%triple.length];
		}
		return getPartner(group, agent);
	}

	public boolean isMatchedInGroup(int group, int agent, int[] triple) {
		if(triple[group] == agent) {
			return true;
		}
		return isMatchedInGroup(group,agent);
	}
	
	public List<List<Integer>> firstOrderDissatisfied() {
		int n = preferenceSystem.numberOfGroups;
		List<List<Integer>> potentialBlocks = new ArrayList<List<Integer>>(n);

		for (int i = 0; i < n; ++i) {
			List<Integer> groupBlocks = new LinkedList<Integer>();
			for (int v = 0; v < preferenceSystem.numberOfAgents; ++v) {
				if (isMatchedInGroup((i - 1 + n) % n, v)) {
					int partnerOfV = getPartner((i - 1 + n) % n, v);
					for (int j = 0; j < preferenceSystem.ranks[(i - 1 + n) % n][v][partnerOfV]; ++j) {
						int u = preferenceSystem.preferences[(i - 1 + n) % n][v][j];
						groupBlocks.add(u);
					}
				}
			}
			potentialBlocks.add(i, groupBlocks);
		}
		return potentialBlocks;
	}
	
	public List<List<Integer>> firstOrderDissatisfied(int group, int[] triple) {
		int n = preferenceSystem.numberOfGroups;
		List<List<Integer>> potentialBlocks = new ArrayList<List<Integer>>(n);

		for (int i = 0; i < n; ++i) {
			List<Integer> groupBlocks = new LinkedList<Integer>();
			for (int v = 0; v < preferenceSystem.numberOfAgents; ++v) {
				if (isMatchedInGroup((i - 1 + n) % n, v,triple)) {
					int partnerOfV = getPartner((i - 1 + n) % n, v,triple);
					for (int j = 0; j < preferenceSystem.ranks[(i - 1 + n) % n][v][partnerOfV]; ++j) {
						int u = preferenceSystem.preferences[(i - 1 + n) % n][v][j];
						if(u != preferenceSystem.numberOfAgents) {
							groupBlocks.add(u);
						}
					}
				}
			}
			potentialBlocks.add(i, groupBlocks);
		}
		return potentialBlocks;
	}
}
