package wjtoth.cyclicstablematching;

import java.util.Arrays;
import java.util.Set;

/**
 * Stores a PreferenceSystem as a node
 * in a DFS tree
 * @author wjtoth
 *
 */
public class PreferenceSystemNode {
	//PreferenceSystem stored by this node
	PreferenceSystem data;
	//Parent node in DFS tree
	PreferenceSystemNode parent;
	//agent to extend in data to obtain children
	Agent extender;
	//unacceptablePartners of extender
	Integer[] unacceptablePartners;
	//index of next unacceptablePartner to append
	//to extender to obtain a child node
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

	//get next child node to consider
	public PreferenceSystemNode getChild() {
		//extend data by appending next unacceptable partner to extender
		PreferenceSystem extensionSystem = data.extend(extender, unacceptablePartners[unacceptablePartnersIndex]);
		
		//increment for next child node
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
	
	//true iff extensionSystem is lexicographically minimal
	private boolean isLexMin(PreferenceSystem extensionSystem) {
		
		//verifies that preference vertices have been chosen in lexicographically minimal way
		for(int i = 0; i<extensionSystem.getNumberOfGroups()-1; ++i) {
			Group group = extensionSystem.getGroups().get(i);
			int maxSeen = 0;
			for(int j = 0; j<group.getGroupSize(); ++j) {
				for(Agent agent : group.getAgents()) {
					int preference;
					try {
						preference = (int) agent.rankedOrder().toArray()[j];
					} catch(ArrayIndexOutOfBoundsException e) {
						continue;
					}
					if(preference > maxSeen) {
						return false;
					}else {
						if(preference == maxSeen) {
							++maxSeen;
						}
					}
				}
			}
			/*
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
			*/
		}
		
		//sorting based reasoning
		//tries to rotate each group to the front and sort rows lexicographically
		String hashPreSort = extensionSystem.getGroups().get(0).computeHash();
		for(int i = 0; i< data.getNumberOfGroups(); ++i) {
			PreferenceSystem extensionCopy = extensionSystem.deepCopy();
			extensionCopy.sortPreferences(i);
			String hashPostSort = extensionCopy.getGroups().get(i).computeHash();
			if(hashPostSort.compareTo(hashPreSort) < 0) {
				return false;
			}
		}
		
		return true;
	} 

	//slightly broken TODO
	public boolean hasNext() {
		return hasChild() || hasParent();
	}

	//returns child if node has unexplored children
	//otherwise returns parent (DFS)
	public PreferenceSystemNode getNext() {
		if (hasChild()) {
			return getChild();
		} else {
			return getParent();
		}
	}
}
