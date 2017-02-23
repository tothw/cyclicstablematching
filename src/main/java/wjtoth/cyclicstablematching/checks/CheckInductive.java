package wjtoth.cyclicstablematching.checks;

import java.util.List;

import wjtoth.cyclicstablematching.Matching;
import wjtoth.cyclicstablematching.MatchingPS;
import wjtoth.cyclicstablematching.PreferenceSystem;

/**
 *  Based on Lemma 4.3.7
 * @author wjtoth
 *
 */
public class CheckInductive extends Check {

	Matching[] matchings;

	public CheckInductive(Matching[] matchings) {
		super();
		this.matchings = matchings;
	}

	@Override
	public boolean checkImpl(PreferenceSystem preferenceSystem) {
		for (Matching matching : matchings) {
			if(checkImpl(matching, preferenceSystem)) {
				//System.out.println("Stable Matching:");
				//System.out.println(matching);
				return true;
			}
		}
		return false;
	}

	public boolean checkImpl(Matching matching, PreferenceSystem preferenceSystem) {
		MatchingPS mathcingPS = new MatchingPS(matching, preferenceSystem);
		if (mathcingPS.size() == 0) {
			return false;
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
		return false;
	}
	
	private boolean checkImpl(List<List<Integer>> potentialBlocks, MatchingPS mathcingPS,
			PreferenceSystem preferenceSystem) {
		int desiredGroup = preferenceSystem.numberOfGroups;
		int desiredAgent = preferenceSystem.numberOfAgents;
		for (int group = 0; group < preferenceSystem.numberOfGroups; ++group) {
			List<Integer> groupBlocks = potentialBlocks.get(group);
			for (int v : groupBlocks) {
				if (!mathcingPS.isMatchedInGroup(group, v)) {
					if (desiredGroup == preferenceSystem.numberOfGroups
							&& desiredAgent == preferenceSystem.numberOfAgents) {
						desiredGroup = group;
						desiredAgent = v;
					}
					if (desiredGroup != group || desiredAgent != v) {
						return false;
					}
				}
			}
		}
		return true;
	}
	
	public String toString() {
		return "Inductive Check (Lemma 4.3.7)";
	}

}
