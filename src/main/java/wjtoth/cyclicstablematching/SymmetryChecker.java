package wjtoth.cyclicstablematching;

public class SymmetryChecker {

	public static boolean isLexMin(PreferenceSystem preferenceSystem) {

		for (int i = 0; i < preferenceSystem.numberOfAgents; ++i) {
			if (preferenceSystem.preferences[0][0][i] != preferenceSystem.numberOfAgents
					&& preferenceSystem.preferences[0][0][i] != i) {
				return false;
			}
			if (preferenceSystem.preferences[1][0][i] != preferenceSystem.numberOfAgents
					&& preferenceSystem.preferences[1][0][i] != i) {
				return false;
			}
		}

		return true;
	}

}
