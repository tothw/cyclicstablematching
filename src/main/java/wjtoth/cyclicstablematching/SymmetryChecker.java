package wjtoth.cyclicstablematching;

public class SymmetryChecker {

	// based on lemma 4_3_15
	public static boolean isLexMin(PreferenceSystem preferenceSystem) {

		// Restructuring Symmetry Check
		// Changing Definition of Lex Min (read down A's preferences, then B's,
		// then C's, instead of across)

		// Previous Conditions Invalid
		/**
		 * //first two conditions for (int i = 0; i <
		 * preferenceSystem.numberOfAgents; ++i) { if
		 * (preferenceSystem.preferences[0][0][i] !=
		 * preferenceSystem.numberOfAgents &&
		 * preferenceSystem.preferences[0][0][i] != i) { return false; } if
		 * (preferenceSystem.preferences[1][0][i] !=
		 * preferenceSystem.numberOfAgents &&
		 * preferenceSystem.preferences[1][0][i] != i) { return false; } }
		 * 
		 * //third condition for (int i = 0; i < preferenceSystem.numberOfAgents
		 * - 1; ++i) { int k = 0; while (k < preferenceSystem.numberOfAgents &&
		 * preferenceSystem.preferences[0][i][k] ==
		 * preferenceSystem.preferences[0][i + 1][k]) { ++k; } if (k <
		 * preferenceSystem.numberOfAgents &&
		 * preferenceSystem.preferences[0][i][k] >
		 * preferenceSystem.preferences[0][i + 1][k]) { return false; } }
		 **/

		// New Condition: choices count up by one as new choices are seen down
		// the lists of all groups but last
		for (int group = 0; group < preferenceSystem.numberOfGroups - 1; ++group) {
			int maxSeen = -1;
			for (int choiceLevel = 0; choiceLevel < preferenceSystem.numberOfAgents; ++choiceLevel) {
				for (int agent = 0; agent < preferenceSystem.numberOfAgents; ++agent) {
					int choice = preferenceSystem.preferences[group][agent][choiceLevel];
					if(choice == preferenceSystem.numberOfAgents) {
						break;
					}
					if(choice > maxSeen + 1) {
						return false;
					} else {
						if(choice == maxSeen + 1) {
							maxSeen = choice;
						}
					}
				}
			}
		}

		// fourth condition
		int l = 0;
		int r = 1;
		int compare;
		while (l < 3) {
			compare = lexCompare(l, r, preferenceSystem);
			if (compare > 0) {
				return false;
			}
			if (compare < 0) {
				l = 3;
			}
			if (compare == 0) {
				l = l + 1;
				r = (r + 1) % preferenceSystem.numberOfGroups;
			}
		}
		l = 0;
		r = 2;
		while (l < 3) {
			compare = lexCompare(l, r, preferenceSystem);
			if (compare > 0) {
				return false;
			}
			if (compare < 0) {
				l = 3;
			}
			if (compare == 0) {
				l = l + 1;
				r = (r + 1) % preferenceSystem.numberOfGroups;
			}
		}

		return true;
	}

	/**
	 * 
	 * @param l
	 *            left hand group
	 * @param r
	 *            right hand group
	 * @param preferenceSystem
	 * @return -1 if group l is lex smaller than r, 0 if equal, 1 if greater
	 */
	private static int lexCompare(int l, int r, PreferenceSystem preferenceSystem) {
		int n = preferenceSystem.numberOfAgents;
		//transposed k and i since we changed definition of lexMin
		for (int i = 0; i < preferenceSystem.numberOfAgents; ++i) {
			int k = 0;
			while (k < n && preferenceSystem.preferences[l][k][i] == preferenceSystem.preferences[r][k][i]) {
				++k;
			}
			if (k < n) {
				int difference = preferenceSystem.preferences[l][k][i] - preferenceSystem.preferences[r][k][i];
				if (difference != 0) {
					return difference / Math.abs(difference);
				}
			}
		}
		return 0;
	}

}
