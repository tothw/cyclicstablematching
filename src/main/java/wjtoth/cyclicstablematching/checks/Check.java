package wjtoth.cyclicstablematching.checks;

import java.math.BigInteger;

import wjtoth.cyclicstablematching.PreferenceSystem;

public abstract class Check {

	public BigInteger successes;
	private boolean track;

	public Check(boolean track) {
		this.track = track;
		successes = BigInteger.ZERO;
	}

	public boolean check(PreferenceSystem preferenceSystem) {
		if (checkImpl(preferenceSystem)) {
			if (track) {
				successes = successes.add(computeNumberOfCutNodes(preferenceSystem));
			}
			return true;
		}
		return false;
	}

	private BigInteger computeNumberOfCutNodes(PreferenceSystem preferenceSystem) {
		BigInteger shortfactorial = factorial(preferenceSystem.numberOfAgents - preferenceSystem.length);
		BigInteger longfactorial = shortfactorial.multiply(BigInteger.valueOf(preferenceSystem.length));

		BigInteger shortCount = shortfactorial.pow(preferenceSystem.extenderAgent + preferenceSystem.extenderGroup);
		BigInteger longCount = longfactorial.pow(preferenceSystem.numberOfAgents - preferenceSystem.extenderAgent
				+ preferenceSystem.numberOfGroups - preferenceSystem.extenderGroup - 1);

		return shortCount.add(longCount);
	}

	private BigInteger factorial(int n) {
		if (n == 1) {
			return BigInteger.ONE;
		} else {
			return factorial(n - 1).multiply(BigInteger.valueOf(n));
		}
	}

	public abstract boolean checkImpl(PreferenceSystem preferenceSystem);

}
