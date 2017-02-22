package wjtoth.cyclicstablematching.checks;

import java.util.List;

import wjtoth.cyclicstablematching.Matching;
import wjtoth.cyclicstablematching.MatchingPS;
import wjtoth.cyclicstablematching.PreferenceSystem;

public class CheckAlmostInductive extends Check{

	Matching[] matchings;

	/**
	 * Checks Lemma 4.3.12 and 4.3.13
	 * @param matchings
	 */
	public CheckAlmostInductive(Matching[] matchings) {
		super();
		this.matchings = matchings;
	}
	

	@Override
	public boolean checkImpl(PreferenceSystem preferenceSystem) {
		for (Matching matching : matchings) {
			MatchingPS mathcingPS = new MatchingPS(matching, preferenceSystem);
			if (mathcingPS.size() == 0) {
				continue;
			}
			// compute internal blocking triples
			boolean isInternallyBlocked = mathcingPS.isInternallyBlocked();
			if (isInternallyBlocked) {
				return false;
			}
			// compute potential blocks against matching
			List<List<Integer>> potentialBlocks = mathcingPS.firstOrderDissatisfied();
			if (checkImpl(potentialBlocks, mathcingPS, preferenceSystem)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean checkImpl(List<List<Integer>> potentialBlocks, MatchingPS matchingPS,
			PreferenceSystem preferenceSystem) {
		int[] idol = new int[preferenceSystem.numberOfGroups];
		for (int i = 0; i < idol.length; ++i) {
			idol[i] = preferenceSystem.numberOfAgents;
		}
		for (int group = 0; group < preferenceSystem.numberOfGroups; ++group) {
			List<Integer> groupBlocks = potentialBlocks.get(group);
			for (int u : groupBlocks) {
				if (!matchingPS.isMatchedInGroup(group, u)) {
					if (idol[group] != preferenceSystem.numberOfAgents) {
						return false;
					} else {
						idol[group] = u;
					}
				}
			}
		}
		boolean[] isFirstChoiceVerified = new boolean[preferenceSystem.numberOfGroups];
		for (int i = 0; i < isFirstChoiceVerified.length; ++i) {
			if (idol[i] != preferenceSystem.numberOfAgents) {
				isFirstChoiceVerified[i] = verifyFirstChoice(idol[i], i, matchingPS, preferenceSystem);
			}
		}
		// check lemma 4_3_12
		for (int group = 0; group < preferenceSystem.numberOfGroups; ++group) {
			if (isFirstChoiceVerified[group]) {
				boolean noOtherIdols = true;
				for (int i = 0; i != group; i = (i + 1) % 3) {
					if (idol[i] != preferenceSystem.numberOfAgents) {
						noOtherIdols = false;
						break;
					}
				}
				if (noOtherIdols) {
					return true;
				}
			}
		}
		// check lemma 4_3_13
		if (matchingPS.size() >= preferenceSystem.numberOfGroups - 2) {
			for (int group = 0; group < preferenceSystem.numberOfGroups; ++group) {
				if (isFirstChoiceVerified[group] && isFirstChoiceVerified[(group + 1) % 3]) {
					if (idol[(group + 2) % 3] == preferenceSystem.numberOfAgents) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean verifyFirstChoice(int idol, int group, MatchingPS matchingPS, PreferenceSystem preferenceSystem) {
		for (int agent = 0; agent < preferenceSystem.numberOfAgents; ++agent) {
			if (!matchingPS.isMatchedInGroup(group, agent)) {
				int rank = preferenceSystem.ranks[group][agent][idol];
				for (int i = 0; i < rank; ++i) {
					int preferred = preferenceSystem.preferences[group][agent][i];
					if (!matchingPS.isMatchedInGroup((group + 1) % 3, preferred)) {
						return false;
					}
				}
			}
		}
		return true;
	}

	
	public String toString() {
		return "Almost Inductive Check (Lemma 4.3.12 and 4.3.13)";
	}
}
