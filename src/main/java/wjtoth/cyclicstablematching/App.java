package wjtoth.cyclicstablematching;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.Scanner;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) {
		final int NUMBER_OF_GROUPS = 3;
		final int NUMBER_OF_AGENTS = 5;
		
		Scanner scanner = new Scanner(System.in);
		System.out.println("Check System?(y/n)");
		char checkSystem = scanner.nextLine().toLowerCase().charAt(0);
		if(checkSystem == 'y') {
			System.out.println("Input system to test:");
			checkSystem(scanner);
		} else {
			System.out.println("Performing Search");
			spaceSearch(NUMBER_OF_GROUPS, NUMBER_OF_AGENTS);
		}

	}
	
	private static void checkSystem(Scanner scanner) {
		int numberOfGroups = scanner.nextInt();
		int numberOfAgents = scanner.nextInt();
		PreferenceSystem preferenceSystem = new PreferenceSystem(numberOfGroups, numberOfAgents);
		for(int i = 0; i<numberOfGroups; ++i) {
			Group group = new Group(numberOfAgents, numberOfAgents, i);
			for(int j = 0; j<numberOfAgents; ++j) {
				Agent agent = new Agent(numberOfAgents, j, i);
				int[] preferences = new int[numberOfAgents];
				for(int k = 0; k<numberOfAgents; ++k) {
					int partner = scanner.nextInt();
					preferences[partner] = numberOfAgents-k;
				}
				agent.setPreferences(preferences);
				group.setGroupAgent(j, agent);
			}
			preferenceSystem.setSystemGroup(i, group);
		}
		System.out.println(preferenceSystem);
		System.out.println("Matchings to check (input -1 to check all possible):");
		int numMatchingsToCheck = scanner.nextInt();
		System.out.println("Read in matchingsToCheck: " +numMatchingsToCheck);
		StabilityChecker stabilityChecker = new StabilityChecker(numberOfAgents, numberOfGroups);
		stabilityChecker.setPreferenceSystem(preferenceSystem);
		System.out.println("Constructed checker");
		if(numMatchingsToCheck == -1) {
			System.out.println("Checking all possible");
			stabilityChecker.setPreferenceSystem(preferenceSystem);
			System.out.println(stabilityChecker.loudHasStableMatch());
			
		}else{
			ArrayList<PerfectMatching> matchingsToCheck = new ArrayList<PerfectMatching>();
			for(int i = 0; i< numMatchingsToCheck; ++i) {
				PerfectMatching perfectMatching = new PerfectMatching(numberOfGroups, numberOfAgents);
				ArrayList<int[]> matching = new ArrayList<int[]>();
				for(int j = 0; j < numberOfAgents; ++j) {
					int[] match = new int[numberOfGroups];
					for(int k = 0; k< numberOfGroups; ++k) {
						match[k] = scanner.nextInt();
					}
					matching.add(match);
				}
				perfectMatching.setMatching(matching);
				matchingsToCheck.add(perfectMatching);
			}
			for(PerfectMatching perfectMatching: matchingsToCheck) {
				System.out.println("Checking matching:");
				System.out.println(perfectMatching);
				System.out.println("Is Stable : "+ stabilityChecker.isStable(perfectMatching));
			}
		}
		System.out.println("Done checking");
	}
	
	private static void spaceSearch(final int NUMBER_OF_GROUPS, final int NUMBER_OF_AGENTS) {
		
		List<PreferenceSystem> toCheckQueue = new LinkedList<PreferenceSystem>();
		TreeSet<PreferenceSystem> toExtendQueue = new TreeSet<PreferenceSystem>();
		PreferenceSystem initialPreferenceSystem = new PreferenceSystem(NUMBER_OF_GROUPS, NUMBER_OF_AGENTS);

		toCheckQueue.add(initialPreferenceSystem);
		int size = 0;
		int previousSize = 0;
		int largestPreviousSize = 0;
		
		StabilityChecker stabilityChecker = new StabilityChecker(NUMBER_OF_AGENTS, NUMBER_OF_GROUPS);
		
		while (!toCheckQueue.isEmpty() || !toExtendQueue.isEmpty()) {
			if (toCheckQueue.isEmpty()) {
				while(toExtendQueue.size() > 0 ) {
					PreferenceSystem preferenceSystem = toExtendQueue.pollFirst();
					List<PreferenceSystem> extensions = preferenceSystem.extend();
					if (extensions.size() == 0){
						System.out.println("Found Counter Example!");
						stabilityChecker.setPreferenceSystem(preferenceSystem);
						System.out.println(stabilityChecker.loudHasStableMatch());
						System.out.println(preferenceSystem);
						break;
					}
					toCheckQueue.addAll(extensions);
					if(preferenceSystem.size() > largestPreviousSize) {
						largestPreviousSize = preferenceSystem.size();
						break;
					}
				}
			}
			if (!toCheckQueue.isEmpty()) {
				PreferenceSystem preferenceSystem = toCheckQueue.remove(0);
				preferenceSystem.sortPreferences();
				stabilityChecker.setPreferenceSystem(preferenceSystem);
				if (!stabilityChecker.hasStableMatch()) {
					toExtendQueue.add(preferenceSystem);
				}
				size = toCheckQueue.size();
				if (size >= previousSize * 2) {
					System.out.println("ToCheckQueue Size: " + size);
					System.out.println("ExtensionQueue Size: " + toExtendQueue.size());
					System.out.println(preferenceSystem);
					System.out.println(preferenceSystem.computeHash());
					previousSize = size;
				}
			}
		}
		System.out.println("DONE!");
	}
}
