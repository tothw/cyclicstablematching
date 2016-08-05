package wjtoth.cyclicstablematching;

import java.util.Arrays;
import java.util.Scanner;
import java.util.Set;

public class PreferenceSystemNode {
	PreferenceSystem data;
	PreferenceSystemNode parent;
	Agent extender;
	Integer[] unacceptablePartners;
	int unacceptablePartnersIndex;

	public PreferenceSystemNode(PreferenceSystem data, PreferenceSystemNode parent) {
		this.data = data;
		this.parent = parent;
		this.extender = data.getExtender();
		Set<Integer> unacceptablePartnersSet = extender.getUnacceptablePartners();
		unacceptablePartners = new Integer[unacceptablePartnersSet.size()];
		unacceptablePartnersSet.toArray(unacceptablePartners);
		Arrays.sort(unacceptablePartners);
		unacceptablePartnersIndex = 0;
	}

	public PreferenceSystem getData() {
		return this.data;
	}

	public PreferenceSystemNode getParent() {
		return this.parent;
	}

	public boolean hasParent() {
		return parent != null;
	}

	public boolean hasChild() {
		return unacceptablePartnersIndex < unacceptablePartners.length;
	}

	public PreferenceSystemNode getChild() {
		PreferenceSystem extensionSystem = data.extend(extender, unacceptablePartners[unacceptablePartnersIndex]);
		
		/**
		 * } else { hashPostSort = hashPreSort; }
		 **/
		++unacceptablePartnersIndex;
		// if hash is unchanged we have canonical preference system
		// otherwise there is a preference system symmetric to this one that can
		// be considered instead
		if (isLexMin(extensionSystem)) {
			PreferenceSystemNode preferenceSystemNode = new PreferenceSystemNode(extensionSystem, this);
			return preferenceSystemNode;
		} else {
			//lexicographic minimal symmetry based elimination
			return getNext();
		}
	}
	
	private boolean isLexMin(PreferenceSystem extensionSystem) {
		
		//verifies that preference vertices have been chosen in lexicographically minimal way
		for(int i = 0; i<extensionSystem.getNumberOfGroups()-1; ++i) {
			Group group = extensionSystem.getGroups().get(i);
			int maxSeen = 0;
			for(Agent agent : group.getAgents()) {
				for(int preference : agent.rankedOrder()) {
					if(preference > maxSeen) {
						return false;
					}else {
						if(preference == maxSeen) {
							++maxSeen;
						}
					}
				}
			}
		}
		
		//sorting based reasoning
		//tries to rotate each group to the front and sort rows lexicographically
		for(int i = 0; i<data.getNumberOfGroups(); ++i) {
			String hashPreSort = extensionSystem.getGroups().get(i).computeHash();
			PreferenceSystem extensionCopy = extensionSystem.deepCopy();
			extensionCopy.sortPreferences(i);
			String hashPostSort = extensionCopy.getGroups().get(i).computeHash();
			if(hashPostSort.compareTo(hashPreSort) < 0) {
				return false;
			}
		}
		
		return true;
	}

	private boolean groupLexMax(String hash) {
		final int NUMBER_OF_GROUPS = data.getNumberOfGroups();
		final int INTERVAL_LENGTH = hash.length() / NUMBER_OF_GROUPS;
		String[] groupStrings = new String[NUMBER_OF_GROUPS];
		for (int i = 0; i < NUMBER_OF_GROUPS; ++i) {
			groupStrings[i] = hash.substring(i * INTERVAL_LENGTH, (i + 1) * INTERVAL_LENGTH);
		}
		if (groupStrings[0].compareTo(groupStrings[1]) < 0) {
			return false;
		}
		if(groupStrings[0].compareTo(groupStrings[NUMBER_OF_GROUPS-1]) < 0) {
			return false;
		}
		return true;
	}

	//slightly broken TODO
	public boolean hasNext() {
		return hasChild() || hasParent();
	}

	public PreferenceSystemNode getNext() {
		if (hasChild()) {
			return getChild();
		} else {
			return getParent();
		}
	}
}
