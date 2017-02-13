package wjtoth.cyclicstablematching;

import java.util.concurrent.ExecutionException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * Hello world!
 *
 */
public class App {

	public static boolean quit = false;
	public static boolean save = false;
	public static boolean print = false;

	public static void main(String[] args) throws InterruptedException, ExecutionException, FileNotFoundException {
		Scanner scanner = new Scanner(System.in);

		System.out.println("Read System (y/n)?");
		char c = scanner.nextLine().toLowerCase().charAt(0);
		final PreferenceSystem preferenceSystem;
		if (c == 'y') {
			preferenceSystem = readSystem();
		} else {
			preferenceSystem = new PreferenceSystem(3, 5);
		}

		Thread thread = new Thread() {
			@Override
			public void run() {
				try {
					depthFirstSearch(preferenceSystem);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		};
		System.out.println("starting search thread");
		thread.start();
		inputHandler(scanner);
		scanner.close();

	}

	private static PreferenceSystem readSystem() throws FileNotFoundException {
		File file = new File("output.txt");
		Scanner scanner = new Scanner(file);
		int numberOfGroups = scanner.nextInt();
		int numberOfAgents = scanner.nextInt();
		PreferenceSystem preferenceSystem = new PreferenceSystem(numberOfGroups, numberOfAgents);

		int extenderGroup = scanner.nextInt();
		int extenderAgent = scanner.nextInt();
		int length = scanner.nextInt();
		preferenceSystem.extenderGroup = extenderGroup;
		preferenceSystem.extenderAgent = extenderAgent;
		preferenceSystem.length = length;

		for (int group = 0; group < numberOfGroups; ++group) {
			for (int agent = 0; agent < numberOfAgents; ++agent) {
				for (int i = 0; i < numberOfAgents; ++i) {
					int preference = scanner.nextInt();
					preferenceSystem.preferences[group][agent][i] = preference;
					if (preference < preferenceSystem.numberOfAgents) {
						preferenceSystem.ranks[group][agent][preference] = i;
					}
				}
			}
		}
		System.out.println(preferenceSystem);
		System.out.println(preferenceSystem.extenderGroup + " " + preferenceSystem.extenderAgent + " " + preferenceSystem.length);
		scanner.close();
		return preferenceSystem;
	}

	/**
	 * Main DFS algorithm starting with empty preference system and extending
	 * searching for either a counterexample system with no stable matching or
	 * verifying that all systems have a stable matching
	 * 
	 * @param NUMBER_OF_GROUPS
	 * @param NUMBER_OF_AGENTS
	 * @throws FileNotFoundException
	 */
	public static void depthFirstSearch(PreferenceSystem preferenceSystem) throws FileNotFoundException {
		StabilityChecker stabilityChecker = new StabilityChecker(preferenceSystem.numberOfGroups,
				preferenceSystem.numberOfAgents);

		
		while (!quit && (preferenceSystem.hasParent() || preferenceSystem.canExtend())) {
			if (print) {
				System.out.println(preferenceSystem);
				print = false;
			}
			if (save) {
				writeSystem(preferenceSystem);
				save = false;
			}
			if (!SymmetryChecker.isLexMin(preferenceSystem) || stabilityChecker.isStable(preferenceSystem)) {
				if (preferenceSystem.hasParent()) {
					preferenceSystem = preferenceSystem.parent();
				} else {
					System.out.println("Error. Stable System has no parent");
					System.out.println(preferenceSystem);
					preferenceSystem.nextChoice = preferenceSystem.numberOfAgents;
				}
			} else if (preferenceSystem.isComplete()) {
				System.out.println("Counter Example found");
				System.out.println(preferenceSystem);
				break;
			} else {
				preferenceSystem = preferenceSystem.next();
			}
		}

		System.out.println("Terminated");
	}

	private static void writeSystem(PreferenceSystem preferenceSystem) throws FileNotFoundException {
		File output = new File("output.txt");
		PrintWriter printWriter = new PrintWriter(output);
		printWriter.println(preferenceSystem.numberOfGroups + " " + preferenceSystem.numberOfAgents);
		printWriter.println(
				preferenceSystem.extenderGroup + " " + preferenceSystem.extenderAgent + " " + preferenceSystem.length);
		for (int group = 0; group < preferenceSystem.numberOfGroups; ++group) {
			for (int agent = 0; agent < preferenceSystem.numberOfAgents; ++agent) {
				printWriter.print(preferenceSystem.preferences[group][agent][0]);
				for (int i = 1; i < preferenceSystem.numberOfAgents; ++i) {
					printWriter.print(" " + preferenceSystem.preferences[group][agent][i]);
				}
				printWriter.println();
			}
		}

		printWriter.close();
	}

	private static void inputHandler(Scanner scanner) {
		char c = '?';
		while (!quit) {
			if (scanner.hasNextLine()) {
				try {
				c = scanner.nextLine().toLowerCase().charAt(0);
				} catch(Exception e) {
					c = '?';
				}
				System.out.println(c);
			}
			if (c == 'q') {
				quit = true;
			}
			if (c == 's') {
				save = true;
			}
			if (c == 'p') {
				print = true;
			}
			c = '?';
		}
	}
}
