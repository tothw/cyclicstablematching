package wjtoth.cyclicstablematching;

import java.util.PriorityQueue;
import java.util.Comparator;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) {
		final int NUMBER_OF_GROUPS = 3;
		final int NUMBER_OF_AGENTS = 5;

		PriorityQueue<PreferenceSystem> priorityQueue = new PriorityQueue<PreferenceSystem>(
				NUMBER_OF_GROUPS * NUMBER_OF_AGENTS * NUMBER_OF_AGENTS, new Comparator<PreferenceSystem>() {
					public int compare(PreferenceSystem p1, PreferenceSystem p2) {
						return p1.sumAcceptablePartnerCount() - p2.sumAcceptablePartnerCount();
					}
				});
		PreferenceSystem initialPreferenceSystem = new PreferenceSystem(new int[] { 5, 5, 5 });
		priorityQueue.add(initialPreferenceSystem);

	}

	public static String padLeft(String s, int n) {
		return String.format("%1$" + n + "s", s).replace(' ', '0');
	}
}
