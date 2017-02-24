package wjtoth.cyclicstablematching.checks;

import wjtoth.cyclicstablematching.PreferenceSystem;

public class CheckCycle extends Check{

	public CheckCycle(boolean track) {
		super(track);
	}


	@Override
	public boolean checkImpl(PreferenceSystem preferenceSystem) {
		int agent = preferenceSystem.lastExtensionAgent();
		int group = preferenceSystem.lastExtensionGroup();
		int choice = preferenceSystem.lastExtensionChoice();
		
		return checkImpl(group,agent,choice,preferenceSystem);
	}
	

	private boolean checkImpl(int group, int agent, int choice, PreferenceSystem preferenceSystem) {
		// only checks 112 and 111 triples
		if (choice > 2 || choice == 0) {
			return false;
		}
		int next = preferenceSystem.preferences[group][agent][choice - 1];
		if (next == preferenceSystem.numberOfAgents) {
			return false;
		}
		int nextGroup = (group + 1) % preferenceSystem.numberOfGroups;
		for (int i = 0; i < preferenceSystem.numberOfGroups - 2; ++i) {
			next = preferenceSystem.preferences[nextGroup][next][0];
			nextGroup = (nextGroup + 1) % preferenceSystem.numberOfGroups;
			if (next == preferenceSystem.numberOfAgents) {
				return false;
			}
		}
		if (preferenceSystem.preferences[nextGroup][next][0] == agent) {
			return true;
		} else {
			return false;
		}
	}

	public String toString() {
		return "Cycle Check";
	}
}
