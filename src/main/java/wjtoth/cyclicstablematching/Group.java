package wjtoth.cyclicstablematching;
import java.util.ArrayList;

public class Group {

	//number of agents in group
	private int groupSize;
	//number of agents in group agents have preferences over
	private int partnerGroupSize;
	//agents in group
	private ArrayList<Agent> agents;
	//index number of gorup
	private int index;
	
	public Group(int groupSize, int partnerGroupSize, int index) {
		this.groupSize = groupSize;
		this.partnerGroupSize = partnerGroupSize;
		this.index = index;
		initializeAgents();
	}
	
	public int getIndex() {
		return index;
	}

	/**
	 * set agent at agentIndex to agent
	 * @param agentIndex
	 * @param agent
	 */
	public void setGroupAgent(int agentIndex, Agent agent) {
		agents.set(agentIndex, agent);
	}
	
	public int getGroupSize() {
		return groupSize;
	}
	
	public ArrayList<Agent> getAgents() {
		return agents;
	}

	//fill group with empty preference list agents
	private void initializeAgents() {
		agents = new ArrayList<Agent>(groupSize);
		for(int i = 0; i<groupSize; ++i) {
			Agent agent = new Agent(partnerGroupSize, i, index);
			agents.add(agent);
		}
	}
	
	/*
	 * Sum over count of acceptable partners for each agent
	 */
	public int sumAcceptablePartnerCount(){
		int sum = 0;
		for(Agent agent : agents) {
			sum += agent.getAcceptablePartnerCount();
		}
		return sum;
	}
	
	/**
	 * Copy of this Group
	 * @return
	 */
	public Group deepCopy() {
		Group group = new Group(groupSize, partnerGroupSize, index);
		for(int i = 0; i<agents.size(); ++i) {
			Agent agentCopy = agents.get(i).deepCopy();
			group.setGroupAgent(i, agentCopy);
		}
		return group;
	}
	
	/**
	 * Return agent with shortest preference list
	 * @return
	 */
	public Agent shortestAgent() {
		Agent shortest = agents.get(0);
		for(int i = 1; i<agents.size(); ++i) {
			Agent candidate = agents.get(i);
			if (candidate.getAcceptablePartnerCount() < shortest.getAcceptablePartnerCount()) {
				shortest = candidate;
			}
		}
		return shortest;
	}
	
	/**
	 * pretty print
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i<agents.size(); ++i) {
			sb.append(i+": ");
			sb.append(agents.get(i).toString());
			sb.append("\n");
		}
		return sb.toString();
	}

	/**
	 * Compute hash string representing group by appending
	 * strings for each agent in the group
	 * @return
	 */
	public String computeHash() {
		StringBuffer sb = new StringBuffer();
		for(Agent agent : agents) {
			sb.append(agent.computeHash());
		}
		return sb.toString();
	}
}
