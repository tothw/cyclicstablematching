package wjtoth.cyclicstablematching;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface PreferenceSystem {

	int sumAcceptablePartnerCount();

	PreferenceSystem deepCopy();

	List<PreferenceSystem> extend();

	Collection<Integer> filterUnacceptablePartners(Agent agent);

	String toString();

	boolean hasStableMatch(CrossProduct<int[]> crossProduct);

	//assumes all groups have same size!
	void attemptStableMatch(CrossProduct<int[]> permutationProductIterator);

	PerfectMatching getStableMatching();

}