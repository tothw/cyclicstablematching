package wjtoth.cyclicstablematching.checks;

import wjtoth.cyclicstablematching.PreferenceSystem;

public abstract class Check {

	public long successes;
	
	public Check() {
		successes = 0;
	}
	
	public boolean check(PreferenceSystem preferenceSystem) {
		if(checkImpl(preferenceSystem)) {
			++successes;
			return true;
		}
		return false;
	}
	
	public abstract boolean checkImpl(PreferenceSystem preferenceSystem);
	
}
