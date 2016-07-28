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
		String hashPreSort = extensionSystem.computeHash();
		String hashPostSort;
		// if (extensionSystem.size() %
		// extensionSystem.getNumberOfAgents()*extensionSystem.getNumberOfGroups()
		// == 0) {
		extensionSystem.sortPreferences();
		hashPostSort = extensionSystem.computeHash();
		/**
		 * } else { hashPostSort = hashPreSort; }
		 **/
		++unacceptablePartnersIndex;
		// if hash is unchanged we have canonical preference system
		// otherwise there is a preference system symmetric to this one that can
		// be considered instead
		if (hashPreSort.equals(hashPostSort) && groupLexMax(hashPostSort)) {
			PreferenceSystemNode preferenceSystemNode = new PreferenceSystemNode(extensionSystem, this);
			return preferenceSystemNode;
		} else {
			System.out.println("Eliminated node at depth " + data.size() + " by lex max symmetry");
			System.out.println("Progress\n" + data);
			return getNext();
		}
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
