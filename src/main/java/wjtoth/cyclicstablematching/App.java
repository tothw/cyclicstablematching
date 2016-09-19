package wjtoth.cyclicstablematching;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.Scanner;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		Scanner scanner = new Scanner(System.in);
		System.out.println("Check System?(y/n)");
		char checkSystem = scanner.nextLine().toLowerCase().charAt(0);
		
		if (checkSystem == 'y') {
			System.out.println("Input system to test:");
			checkSystem(scanner);
		} else {
			//DFS for 3 genders and 5 agents each gender
			final int NUMBER_OF_GROUPS = 3;
			final int NUMBER_OF_AGENTS = 5;
			System.out.println("Performing Search");
			depthFirstSearch(NUMBER_OF_GROUPS, NUMBER_OF_AGENTS);
		}

	}

	/**
	 * Check a given preferenceSystem for stability on either
	 * a collection of matchings
	 * or all possible.
	 * 
	 *  Input format: numberofGroups numberOfAgents then
	 *  integers in ranked order for each agent starting with group 0 agent 0
	 *  down to group numberOfGroups-1 agent numberOfAgents-1
	 *  followed by numberOfMatchingsToCheck (-1 if all possible) and
	 *  if not -1 then specify matchings via integers tuple by tuple
	 */
	private static void checkSystem(Scanner scanner) {
		int numberOfGroups = scanner.nextInt();
		int numberOfAgents = scanner.nextInt();
		PreferenceSystem preferenceSystem = new PreferenceSystem(numberOfGroups, numberOfAgents);
		for (int i = 0; i < numberOfGroups; ++i) {
			Group group = new Group(numberOfAgents, numberOfAgents, i);
			for (int j = 0; j < numberOfAgents; ++j) {
				Agent agent = new Agent(numberOfAgents, j, i);
				int[] preferences = new int[numberOfAgents];
				for (int k = 0; k < numberOfAgents; ++k) {
					int partner = scanner.nextInt();
					preferences[partner] = numberOfAgents - k;
				}
				agent.setPreferences(preferences);
				group.setGroupAgent(j, agent);
			}
			preferenceSystem.setSystemGroup(i, group);
		}
		System.out.println(preferenceSystem);
		System.out.println("Matchings to check (input -1 to check all possible):");
		int numMatchingsToCheck = scanner.nextInt();
		System.out.println("Read in matchingsToCheck: " + numMatchingsToCheck);
		StabilityChecker stabilityChecker = new StabilityChecker(numberOfAgents, numberOfGroups);
		stabilityChecker.setPreferenceSystem(preferenceSystem);
		System.out.println("Constructed checker");
		if (numMatchingsToCheck == -1) {
			System.out.println("Checking all possible");
			stabilityChecker.setPreferenceSystem(preferenceSystem);
			System.out.println(stabilityChecker.checkAllPossible());

		} else {
			ArrayList<Matching> matchingsToCheck = new ArrayList<Matching>();
			for (int i = 0; i < numMatchingsToCheck; ++i) {
				Matching perfectMatching = new Matching(numberOfGroups, numberOfAgents);
				ArrayList<int[]> matching = new ArrayList<int[]>();
				for (int j = 0; j < numberOfAgents; ++j) {
					int[] match = new int[numberOfGroups];
					for (int k = 0; k < numberOfGroups; ++k) {
						match[k] = scanner.nextInt();
					}
					matching.add(match);
				}
				perfectMatching.setMatching(matching);
				matchingsToCheck.add(perfectMatching);
				System.out.println("Read in matching");
			}
			for (Matching perfectMatching : matchingsToCheck) {
				System.out.println("Checking matching:");
				System.out.println(perfectMatching);
				System.out.println("Is Stable : " + stabilityChecker.loudIsStable(perfectMatching));
			}
		}
		System.out.println("Done checking");
	}

	/**
	 * Main DFS algorithm starting with empty preference system and extending
	 * searching for either a counterexample system with no stable matching
	 * or verifying that all systems have a stable matching
	 * @param NUMBER_OF_GROUPS
	 * @param NUMBER_OF_AGENTS
	 */
	public static void depthFirstSearch(final int NUMBER_OF_GROUPS, final int NUMBER_OF_AGENTS) {
		PreferenceSystem initialPreferenceSystem = new PreferenceSystem(NUMBER_OF_GROUPS, NUMBER_OF_AGENTS);
		PreferenceSystemNode root = new PreferenceSystemNode(initialPreferenceSystem, null);
		PreferenceSystemNode preferenceSystemNode = root;

		System.out.println("Constructing Stablility Checker");
		StabilityChecker stabilityChecker = new StabilityChecker(NUMBER_OF_AGENTS, NUMBER_OF_GROUPS);
		System.out.println("Done Constructing Stability Checker");

		Duration printingInterval = Duration.ofMinutes(1);
		Instant previousInstant = Instant.now();
		Instant currentInstant = Instant.now();
		while (preferenceSystemNode != null && preferenceSystemNode.hasNext()) {
			PreferenceSystem data = preferenceSystemNode.getData();
			currentInstant = Instant.now();
			if (previousInstant.plus(printingInterval).isBefore(currentInstant)) {
				System.out.println(data);
				System.out.println("System size: " + data.size());
				previousInstant = currentInstant;
			}
			stabilityChecker.setPreferenceSystem(data);
			if (stabilityChecker.hasStableMatch()) {
				preferenceSystemNode = preferenceSystemNode.getParent();
			} else {
				if (data.size() == NUMBER_OF_AGENTS * NUMBER_OF_AGENTS * NUMBER_OF_GROUPS) {
					System.out.println("Found Counterexample");
					System.out.println(data);
					break;
				}
				preferenceSystemNode = preferenceSystemNode.getNext();
			}

		}
		if (preferenceSystemNode == null) {
			System.out.println("null terminate");
		}
		System.out.println("Done!");
	}
}
