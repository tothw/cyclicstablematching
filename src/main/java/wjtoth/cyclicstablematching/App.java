package wjtoth.cyclicstablematching;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.Set;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) {
		final int NUMBER_OF_GROUPS = 3;
		final int NUMBER_OF_AGENTS = 5;
		
		List<PreferenceSystem> toCheckQueue = new LinkedList<PreferenceSystem>();
		Set<PreferenceSystem> toExtendQueue = new TreeSet<PreferenceSystem>();
		PreferenceSystem initialPreferenceSystem = new PreferenceSystem(NUMBER_OF_GROUPS, NUMBER_OF_AGENTS);

		toCheckQueue.add(initialPreferenceSystem);
		int size = 0;
		int previousSize = 0;

		StabilityChecker stabilityChecker = new StabilityChecker(NUMBER_OF_AGENTS, NUMBER_OF_GROUPS);
		
		while (!toCheckQueue.isEmpty() || !toExtendQueue.isEmpty()) {
			if (toCheckQueue.isEmpty()) {
				for (PreferenceSystem preferenceSystem : toExtendQueue) {
					if(toCheckQueue.size() > 50000) {
						previousSize /=2;
						break;
					}
					List<PreferenceSystem> extensions = preferenceSystem.extend();
					if (extensions.size() == 0){
						System.out.println("Found Counter Example!");
						stabilityChecker.setPreferenceSystem(preferenceSystem);
						System.out.println(stabilityChecker.loudHasStableMatch());
						System.out.println(preferenceSystem);
						break;
					}
					toCheckQueue.addAll(extensions);
				}
				toExtendQueue.clear();
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

	public static String padLeft(String s, int n) {
		return String.format("%1$" + n + "s", s).replace(' ', '0');
	}
}
