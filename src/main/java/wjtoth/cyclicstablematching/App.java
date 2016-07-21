package wjtoth.cyclicstablematching;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.Scanner;
import java.util.Set;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		final int NUMBER_OF_GROUPS = 3;
		final int NUMBER_OF_AGENTS = 5;

		Scanner scanner = new Scanner(System.in);
		System.out.println("Check System?(y/n)");
		char checkSystem = scanner.nextLine().toLowerCase().charAt(0);
		if (checkSystem == 'y') {
			System.out.println("Input system to test:");
			checkSystem(scanner);
		} else {
			System.out.println("Performing Search");
			depthFirstSearch(NUMBER_OF_GROUPS, NUMBER_OF_AGENTS);
		}

	}

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
		preferenceSystem.sortPreferences();
		System.out.println("Sorted");
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
			System.out.println(stabilityChecker.loudHasStableMatch());

		} else {
			ArrayList<PerfectMatching> matchingsToCheck = new ArrayList<PerfectMatching>();
			for (int i = 0; i < numMatchingsToCheck; ++i) {
				PerfectMatching perfectMatching = new PerfectMatching(numberOfGroups, numberOfAgents);
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
			}
			for (PerfectMatching perfectMatching : matchingsToCheck) {
				System.out.println("Checking matching:");
				System.out.println(perfectMatching);
				System.out.println("Is Stable : " + stabilityChecker.isStable(perfectMatching));
			}
		}
		System.out.println("Done checking");
	}

	private static void spaceSearch(final int NUMBER_OF_GROUPS, final int NUMBER_OF_AGENTS)
			throws InterruptedException, ExecutionException {

		List<PreferenceSystem> toCheckQueue = new LinkedList<PreferenceSystem>();
		List<PreferenceSystem> toExtendQueue = new LinkedList<PreferenceSystem>();
		PreferenceSystem initialPreferenceSystem = new PreferenceSystem(NUMBER_OF_GROUPS, NUMBER_OF_AGENTS);

		toCheckQueue.add(initialPreferenceSystem);

		System.out.println("Constructing Stability Checker");

		StabilityChecker stabilityChecker = new StabilityChecker(NUMBER_OF_AGENTS, NUMBER_OF_GROUPS);

		System.out.println("Done constructing Stability Checker");

		int sizeCount = 0;

		while (!toCheckQueue.isEmpty() || !toExtendQueue.isEmpty()) {
			if (toCheckQueue.isEmpty()) {
				System.out.println("Extending " + toExtendQueue.size() + " systems");
				int previousSize = NUMBER_OF_AGENTS*NUMBER_OF_AGENTS*NUMBER_OF_GROUPS + 1;
				if(!toExtendQueue.isEmpty()) {
					if(toExtendQueue.get(0).size() % NUMBER_OF_AGENTS*NUMBER_OF_GROUPS == 0 || true) {
						toExtendQueue = filterSymmetries(toExtendQueue);
					}
				}
				while (!toExtendQueue.isEmpty()) {
					PreferenceSystem preferenceSystem = toExtendQueue.remove(0);
					if(preferenceSystem.size() > previousSize) {
						toExtendQueue.add(preferenceSystem);
						break;
					}
					previousSize = preferenceSystem.size();
					toCheckQueue.add(preferenceSystem);
				}
				System.out.println("Done Extending: " + toExtendQueue.size() + " remain");
			}
			if (!toCheckQueue.isEmpty()) {
				System.out.println("Processing " + toCheckQueue.size() + " Inputs");
				List<List<PreferenceSystem>> extensions = processInputs(toCheckQueue, stabilityChecker);
				for (List<PreferenceSystem> extension : extensions) {
					if (!extension.isEmpty() && extension.get(0).size() >= sizeCount) {
						System.out.println("Extensions size: " + extensions.size());
						System.out.println(extension.get(0));
						System.out.println(extension.get(0).computeHash());
						++sizeCount;
					}
					toExtendQueue.addAll(extension);
				}
				System.out.println("Done Processing");
			}
		}
		System.out.println("DONE!");
	}

	/*
	 * *Method to Parallelize Computation
	 */
	public static List<List<PreferenceSystem>> processInputs(List<PreferenceSystem> inputs,
			StabilityChecker stabilityChecker) throws InterruptedException, ExecutionException {

		int threads = Runtime.getRuntime().availableProcessors();
		ExecutorService service = Executors.newFixedThreadPool(threads);

		List<Future<List<PreferenceSystem>>> futures = new ArrayList<Future<List<PreferenceSystem>>>();
		for (final PreferenceSystem input : inputs) {
			Callable<List<PreferenceSystem>> callable = new Callable<List<PreferenceSystem>>() {
				public List<PreferenceSystem> call() throws Exception {
					List<PreferenceSystem> output = processPreferenceSystem(input, stabilityChecker);
					return output;
				}
			};
			futures.add(service.submit(callable));
		}

		service.shutdown();

		List<List<PreferenceSystem>> outputs = new ArrayList<List<PreferenceSystem>>();
		for (Future<List<PreferenceSystem>> future : futures) {
			outputs.add(future.get());
		}
		inputs.clear();
		return outputs;
	}

	private static List<PreferenceSystem> processPreferenceSystem(PreferenceSystem preferenceSystem,
			StabilityChecker stabilityChecker) {
		// preferenceSystem.sortPreferences();
		stabilityChecker.setPreferenceSystem(preferenceSystem);
		if (!stabilityChecker.hasStableMatch()) {
			if (preferenceSystem.size() == preferenceSystem.getNumberOfGroups() * preferenceSystem.getNumberOfAgents()
					* preferenceSystem.getNumberOfAgents()) {
				System.out.println("Found Counter Example!");
				stabilityChecker.setPreferenceSystem(preferenceSystem);
				System.out.println(stabilityChecker.loudHasStableMatch());
				System.out.println(preferenceSystem);
				return new ArrayList<PreferenceSystem>();
			}
			return preferenceSystem.extend();
		}

		return new ArrayList<PreferenceSystem>();
	}

	private static List<PreferenceSystem> filterSymmetries(List<PreferenceSystem> queue) {
		System.out.println("Filtering Symmetries");
		int initalSize = queue.size();
		System.out.println("Initial Size: " + initalSize);
		Set<PreferenceSystem> filterSet = new TreeSet<PreferenceSystem>();
		for(PreferenceSystem preferenceSystem: queue) {
			filterSet.add(preferenceSystem);
		}
		System.out.println("Final Size: " + filterSet.size());
		return new ArrayList<PreferenceSystem>(filterSet);
	}
	
	public static void depthFirstSearch(final int NUMBER_OF_GROUPS, final int NUMBER_OF_AGENTS) {
		PreferenceSystem initialPreferenceSystem = new PreferenceSystem(NUMBER_OF_GROUPS, NUMBER_OF_AGENTS);
		PreferenceSystemNode root = new PreferenceSystemNode(initialPreferenceSystem, null);
		PreferenceSystemNode preferenceSystemNode = root;
		
		System.out.println("Constructing Stablility Checker");
		StabilityChecker stabilityChecker = new StabilityChecker(NUMBER_OF_AGENTS, NUMBER_OF_GROUPS);
		System.out.println("Done Constructing Stability Checker");
		int previousSize = 0;
		while(preferenceSystemNode.hasNext()) {
			PreferenceSystem data = preferenceSystemNode.getData();
			if(data.size() >= previousSize) {
				System.out.println(data);
				System.out.println("System size: " + data.size());
				++previousSize;
			}
			stabilityChecker.setPreferenceSystem(data);
			if(stabilityChecker.hasStableMatch()) {
				preferenceSystemNode = preferenceSystemNode.getParent();
			} else {
				if(data.size() == NUMBER_OF_AGENTS*NUMBER_OF_AGENTS*NUMBER_OF_GROUPS) {
					System.out.println("Found Counterexample");
					System.out.println(data);
					break;
				}
				preferenceSystemNode = preferenceSystemNode.getNext();
			}
			
		}
		System.out.println("Done!");
	}
}
