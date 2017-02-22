package wjtoth.cyclicstablematching.checks;

import java.util.Set;
import java.util.TreeSet;

import wjtoth.cyclicstablematching.Matching;
import wjtoth.cyclicstablematching.PreferenceSystem;

public class CheckGenderStability extends Check {

	Matching[] oneGenderMatchings;

	public CheckGenderStability(Matching[] oneGenderMatchings) {
		super();
		this.oneGenderMatchings = oneGenderMatchings;
	}
	
	@Override
	public boolean checkImpl(PreferenceSystem preferenceSystem) {
		for (int i = 0; i < preferenceSystem.numberOfGroups; ++i) {
			if (checkImpl(i, preferenceSystem)) {
				return true;
			}
		}
		return false;
	}


	private boolean checkImpl(int group, PreferenceSystem preferenceSystem) {
		for (Matching matching : oneGenderMatchings) {
			if (matching.size() != preferenceSystem.numberOfAgents) {
				continue;
			}

			if (checkImpl(matching, group, preferenceSystem)) {
				return true;
			}
		}
		return false;
	}

	private boolean checkImpl(Matching matching, int group, PreferenceSystem preferenceSystem) {
		Set<Integer> preferredPartners = new TreeSet<Integer>();
		for (int i = 0; i < preferenceSystem.numberOfAgents; ++i) {
			int partnerOfI = matching.getPartner(0, i);
			int partnersRank = preferenceSystem.ranks[group][i][partnerOfI];
			if (partnersRank == preferenceSystem.numberOfAgents) {
				return false;
			}
			for (int j = 0; j < partnersRank; ++j) {
				preferredPartners.add(preferenceSystem.preferences[group][i][j]);
			}
		}
		boolean[] possibleCollisions = new boolean[preferenceSystem.numberOfAgents];
		for (int partner : preferredPartners) {
			int firstChoiceOfPartner = preferenceSystem.preferences[(group + 1)
					% preferenceSystem.numberOfGroups][partner][0];
			if (firstChoiceOfPartner == preferenceSystem.numberOfAgents) {
				return false;
			}
			if (possibleCollisions[firstChoiceOfPartner]) {
				return false;
			} else {
				possibleCollisions[firstChoiceOfPartner] = true;
			}
		}
		return true;
	}
	
	public String toString() {
		return "Gender Stability Check";
	}
	
}
