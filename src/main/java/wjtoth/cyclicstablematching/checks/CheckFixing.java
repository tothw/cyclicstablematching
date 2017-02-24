package wjtoth.cyclicstablematching.checks;

import java.util.List;

import wjtoth.cyclicstablematching.Matching;
import wjtoth.cyclicstablematching.MatchingPS;
import wjtoth.cyclicstablematching.PreferenceSystem;

public class CheckFixing extends Check {

	Matching[] matchings;

	/**
	 * Based on Lemma 4.3.14
	 * 
	 * @param matchings
	 */
	public CheckFixing(Matching[] matchings, boolean track) {
		super(track);
		this.matchings = matchings;
	}

	@Override
	public boolean checkImpl(PreferenceSystem preferenceSystem) {
		for (Matching matching : matchings) {
			if(matching.getPartner(preferenceSystem.lastExtensionGroup(), preferenceSystem.lastExtensionAgent()) != preferenceSystem.lastExtensionChoice()) {
				continue;
			}
			MatchingPS matchingPS = new MatchingPS(matching, preferenceSystem);
			if (matchingPS.size() >= preferenceSystem.numberOfAgents - 2
					&& matchingPS.size() < preferenceSystem.numberOfAgents) {
				// fixing lemma 4.3.14
				if (checkImpl(matchingPS, preferenceSystem)) {
					return true;
				}
			}

		}
		return false;
	}

	// only works for 3 groups atm
	private boolean checkImpl(MatchingPS matchingPS, PreferenceSystem preferenceSystem) {
		for (int group = 0; group < preferenceSystem.numberOfGroups; ++group) {
			if (checkImpl(matchingPS, preferenceSystem, group)) {
				return true;
			}
		}
		return false;
	}

	private boolean checkImpl(MatchingPS matchingPS, PreferenceSystem preferenceSystem, int group) {
		for (int exceptionAgent = 0; exceptionAgent < preferenceSystem.numberOfAgents; ++exceptionAgent) {
			if (matchingPS.isMatchedInGroup(group, exceptionAgent)) {
				continue;
			}
			if (checkImpl(matchingPS, preferenceSystem, group, exceptionAgent)) {
				return true;
			}
		}
		return false;
	}

	private boolean checkImpl(MatchingPS matchingPS, PreferenceSystem preferenceSystem, int group,
			int exceptionAgent) {
		int n = preferenceSystem.numberOfGroups;
		for (int exceptionPartner = 0; exceptionPartner < preferenceSystem.numberOfAgents; ++exceptionPartner) {
			if (matchingPS.isMatchedInGroup((group + 1) % n, exceptionPartner)
					|| preferenceSystem.ranks[group][exceptionAgent][exceptionPartner] < preferenceSystem.numberOfAgents) {
				continue;
			}
			if (checkImpl(matchingPS, preferenceSystem, group, exceptionAgent, exceptionPartner)) {
				return true;
			}
		}
		return false;
	}

	private boolean checkImpl(MatchingPS matchingPS, PreferenceSystem preferenceSystem, int group,
			int exceptionAgent, int exceptionPartner) {
		int n = preferenceSystem.numberOfGroups;
		for (int c = 0; c < preferenceSystem.numberOfAgents; ++c) {
			if (matchingPS.isMatchedInGroup((group + 2) % n, c)
					|| preferenceSystem.ranks[(group + 1) % n][exceptionPartner][c] == preferenceSystem.numberOfAgents
					|| preferenceSystem.ranks[(group + 2) % n][c][exceptionAgent] == preferenceSystem.numberOfAgents) {
				continue;
			}
			int[] triple = new int[n];
			triple[group] = exceptionAgent;
			triple[(group + 1) % n] = exceptionPartner;
			triple[(group + 2) % n] = c;
			if (attemptFix(matchingPS, preferenceSystem, group, triple)) {
				return true;
			}
		}
		return false;
	}

	private boolean attemptFix(MatchingPS matchingPS, PreferenceSystem preferenceSystem, int group, int[] triple) {
		// valid no internal blocks
		if (!matchingPS.isInternallyBlocked(group, triple)) {
			List<List<Integer>> potentialBlocks = matchingPS.firstOrderDissatisfied(group, triple);
			for (int i = 0; i < preferenceSystem.numberOfGroups; ++i) {
				List<Integer> groupBlocks = potentialBlocks.get(i);
				for (int v : groupBlocks) {
					if (!matchingPS.isMatchedInGroup(i, v, triple)) {
						return false;
					}
				}
			}
			// perform fixing
			int newLastGroup = group;
			int newLastAgent = triple[group];
			int newLastChoice = triple[(group + 1) % triple.length];
			if (preferenceSystem.fixedLastGroup == preferenceSystem.numberOfGroups
					&& preferenceSystem.fixedLastAgent == preferenceSystem.numberOfAgents
					&& preferenceSystem.fixedLastChoice == preferenceSystem.numberOfAgents) {
				preferenceSystem.fixedLastGroup = newLastGroup;
				preferenceSystem.fixedLastAgent = newLastAgent;
				preferenceSystem.fixedLastChoice = newLastChoice;
			} else {
				if (preferenceSystem.fixedLastGroup == newLastGroup && preferenceSystem.fixedLastAgent == newLastAgent
						&& preferenceSystem.fixedLastChoice != newLastChoice) {
					return true;
				}
			}
		}
		return false;
	}

	public String toString() {
		return "Check Fixing (Lemma 4.3.14)";
	}
}
