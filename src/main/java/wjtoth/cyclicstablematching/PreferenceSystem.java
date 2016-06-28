package wjtoth.cyclicstablematching;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PreferenceSystem {

	private ArrayList<Group> groups;
	private int[] groupSizes;
	
	//TODO iterate over PerfectMatchings finding a Stable one, return success or fail
	
	public PreferenceSystem(int[] groupSizes) {
		int n = groupSizes.length;
		groups = new ArrayList<Group>(n);
		for(int i = 0; i<groupSizes.length; ++i) {
			Group group = new Group(groupSizes[i], groupSizes[(i+1)%n]);
			groups.add(group);			
		}
		this.groupSizes = groupSizes;
	}
	
	public void setSystemGroup(int groupIndex, Group group) {
		groups.set(groupIndex, group);
	}
	
	public ArrayList<Group> getGroups() {
		return groups;
	}

	public void setGroups(ArrayList<Group> groups) {
		this.groups = groups;
	}
		
	public int sumAcceptablePartnerCount() {
		int sum = 0;
		for(Group group: groups) {
			sum += group.sumAcceptablePartnerCount();
		}
		return sum;
	}
	
	public PreferenceSystem deepCopy() {
		PreferenceSystem preferenceSystem = new PreferenceSystem(groupSizes);
		for(int i = 0; i<groups.size(); ++i) {
			Group groupCopy = groups.get(i).deepCopy();
			preferenceSystem.setSystemGroup(i, groupCopy);
		}
		return preferenceSystem;
	}
	
	public List<PreferenceSystem> extend() {
		List<PreferenceSystem> newSystems = new ArrayList<PreferenceSystem>();
		Agent extender = getExtender();
		Integer[] unacceptablePartners = extender.getUnacceptablePartners().toArray(new Integer[extender.getUnacceptablePartners().size()]);
		for(int unacceptablePartner : unacceptablePartners) {
			//set
			extender.append(unacceptablePartner);
			newSystems.add(this.deepCopy());
			extender.remove(unacceptablePartner);
		}
		return newSystems;
	}
	
	private Agent getExtender() {
		Agent extender = groups.get(0).shortestAgent();
		for(int i = 1; i < groups.size(); ++i) {
			Agent candidate = groups.get(i).shortestAgent();
			if(candidate.getAcceptablePartnerCount() < extender.getAcceptablePartnerCount()) {
				extender = candidate;
			}
		}
		return extender;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i<groups.size(); ++i) {
			sb.append("Group " + i + ":\n");
			sb.append(groups.get(i).toString());
		}
		return sb.toString();
	}
}
