package wjtoth.cyclicstablematching;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TreePreferenceSystem implements PreferenceSystem {

	private PreferenceSystem parentPreferenceSystem;
	private int extensionGroup;
	private int extensionAgent;
	private int appendedAgent;
	
	public TreePreferenceSystem(PreferenceSystem parentPreferenceSystem, int extensionGroup, int extensionAgent, int appendedAgent) {
		this.parentPreferenceSystem = parentPreferenceSystem;
		this.extensionGroup = extensionGroup;
		this.extensionAgent = extensionAgent;
		this.appendedAgent = appendedAgent;
	}

	@Override
	public int sumAcceptablePartnerCount() {
		return parentPreferenceSystem.sumAcceptablePartnerCount() + 1;
	}

	@Override
	public PreferenceSystem deepCopy() {
		return new TreePreferenceSystem(parentPreferenceSystem.deepCopy(), extensionGroup, extensionAgent, appendedAgent);
	}

	@Override
	public List<PreferenceSystem> extend() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Integer> filterUnacceptablePartners(Agent agent) {
		Collection<Integer> previousUnacceptablePartners = parentPreferenceSystem.filterUnacceptablePartners(agent);
		if(agent.getIndex() == extensionAgent && agent.getGroupIndex() == extensionGroup) {
			previousUnacceptablePartners.remove(appendedAgent);
		}
		return previousUnacceptablePartners;
	}

	@Override
	public boolean hasStableMatch(CrossProduct<int[]> crossProduct) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void attemptStableMatch(CrossProduct<int[]> permutationProductIterator) {
		// TODO Auto-generated method stub

	}

	@Override
	public PerfectMatching getStableMatching() {
		// TODO Auto-generated method stub
		return null;
	}

}
