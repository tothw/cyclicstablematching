package wjtoth.cyclicstablematching;

public class SymmetryChecker {

	// a new definition of symmetry is used in checking this lemma for n=4
	public static boolean isLexMin(PreferenceSystem preferenceSystem) {
		int n = preferenceSystem.numberOfAgents;
		// fixed m0, w0, and d0 choices:
		for (int i = 0; i < n; ++i) {
			if ((preferenceSystem.preferences[0][0][i] != i && preferenceSystem.preferences[0][0][i] != n)
					|| (preferenceSystem.preferences[1][0][i] != i && preferenceSystem.preferences[1][0][i] != n)
					|| (preferenceSystem.preferences[2][0][i] != (i + 1) % n
							&& preferenceSystem.preferences[2][0][i] != n)) {
				return false;
			}
		}
		// fixed m1-m4 last choice, fixed d1-d4 last choice
		for (int i = 1; i < n; ++i) {
			for (int c = 0; c < n - 1; ++c) {
				if (preferenceSystem.preferences[0][i][c] == 0 || preferenceSystem.preferences[2][i][c] == 0) {
					return false;
				}
			}
		}

		return true;
	}

}
