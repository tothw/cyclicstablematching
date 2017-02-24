package wjtoth.cyclicstablematching.checks;

import java.util.Arrays;

import wjtoth.cyclicstablematching.PreferenceSystem;

public class CheckInitialChoices extends Check{
	
	public CheckInitialChoices(boolean track) {
		super(track);
	}
	
	@Override
	public boolean checkImpl(PreferenceSystem preferenceSystem) {
		// only check when one gender is fully specified
				// don't check first system (null system)
				// only check first choices right now
				if (preferenceSystem.extenderAgent != 0 || (preferenceSystem.extenderGroup == 0 && preferenceSystem.length == 1)
						|| (preferenceSystem.length > 1 && preferenceSystem.extenderGroup != 0)
						|| preferenceSystem.length > 2) {
					return false;
				}
				int group = (preferenceSystem.extenderGroup - 1 + preferenceSystem.numberOfGroups)
						% preferenceSystem.numberOfGroups;
				// count number of times an agent is first choice;
				int[] choiceFrequency = new int[preferenceSystem.numberOfAgents];
				for (int i = 0; i < preferenceSystem.numberOfAgents; ++i) {
					int choice = preferenceSystem.preferences[group][i][0];
					if (choice < preferenceSystem.numberOfAgents) {
						choiceFrequency[choice] += 1;
					}
				}
				Arrays.sort(choiceFrequency);
				int mostFrequent = choiceFrequency[preferenceSystem.numberOfAgents - 1];
				// all same
				if (mostFrequent == preferenceSystem.numberOfAgents) {
					return true;
				} else {
					int secondMostFrequent = choiceFrequency[preferenceSystem.numberOfAgents - 2];
					// n-1 same, one different, needs lemma n4
					if (mostFrequent == preferenceSystem.numberOfAgents - 1 && secondMostFrequent == 1) {
						return true;
					}
				}
				// last check for all different, false if it fails
				for (int i = 0; i < preferenceSystem.numberOfAgents; ++i) {
					if (choiceFrequency[i] != 1) {
						return false;
					}
				}
				return true;
	}

	public String toString() {
		return "Initial Choices Check";
	}
}
