package wjtoth.cyclicstablematching;

import java.util.Arrays;
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
		if (extensionSystem.size() % extensionSystem.getNumberOfAgents()*extensionSystem.getNumberOfGroups() == 0) {
			extensionSystem.sortPreferences();
			hashPostSort = extensionSystem.computeHash();
		} else {
			hashPostSort = hashPreSort;
		}
		++unacceptablePartnersIndex;
		// if hash is unchanged we have canonical preference system
		// otherwise there is a preference system symmetric to this one that can
		// be considered instead
		if (hashPreSort.equals(hashPostSort)) {
			PreferenceSystemNode preferenceSystemNode = new PreferenceSystemNode(extensionSystem, this);
			return preferenceSystemNode;
		} else {
			return getNext();
		}
	}

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
