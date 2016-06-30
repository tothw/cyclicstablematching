package wjtoth.cyclicstablematching;

import java.util.PriorityQueue;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
		PreferenceSystem initialPreferenceSystem = new PreferenceSystem(new int[] { NUMBER_OF_AGENTS, NUMBER_OF_AGENTS, NUMBER_OF_AGENTS });
		priorityQueue.add(initialPreferenceSystem);
		
		int size = 0;
		int previousSize = 0;
		
		ArrayList<int[]> permutations = Permutations.permutations(NUMBER_OF_AGENTS);
		CrossProduct<int[]> crossProduct = new CrossProduct<int[]> (permutations, NUMBER_OF_GROUPS);
		while(!priorityQueue.isEmpty()){
			PreferenceSystem preferenceSystem = priorityQueue.remove();
			if(!preferenceSystem.hasStableMatch(crossProduct)){
				List<PreferenceSystem> extensions = preferenceSystem.extend();
				if(extensions.size() > 0 ) {
					priorityQueue.addAll(extensions);
				}else{
					System.out.println("Found Counter Example!");
					System.out.println(preferenceSystem);
					break;
				}
			}
			size = priorityQueue.size();
			if(size >= previousSize*10) {
				System.out.println(size);
				System.out.println(preferenceSystem);
				previousSize=size;
			}
		}
		System.out.println("DONE!");
	}

	public static String padLeft(String s, int n) {
		return String.format("%1$" + n + "s", s).replace(' ', '0');
	}
}
