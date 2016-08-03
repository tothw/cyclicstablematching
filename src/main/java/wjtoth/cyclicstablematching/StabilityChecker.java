package wjtoth.cyclicstablematching;

import java.util.*;
import java.util.stream.Collectors;

public class StabilityChecker {

    private PreferenceSystem preferenceSystem;

    private boolean hasStableMatch;

    private boolean loud;

    // For PerfectMatching checking
    private CrossProduct<Integer> blockers;

    private PerfectMatching[] matchings;

    public StabilityChecker(int numberOfAgents, int numberOfGroups) {
        preferenceSystem = new PreferenceSystem(numberOfGroups, numberOfAgents);
        hasStableMatch = false;
        ArrayList<Integer> agents = new ArrayList<Integer>();
        for (int i = 0; i < numberOfAgents; ++i) {
            agents.add(i);
        }
        blockers = new CrossProduct<Integer>(agents, numberOfGroups);
        buildMatchings(numberOfAgents, numberOfGroups);
        for(PerfectMatching perfectMatching : matchings) {
            System.out.println(perfectMatching);
            new Scanner(System.in).nextInt();
        }
        loud = false;
    }

    private void buildMatchings(int numberOfAgents, int numberOfGroups) {
        List<int[]> permutations = new ArrayList<>();
        for(PermutationArray permutationArray : Permutations.permutationsOfAllSubsets(numberOfAgents)) {
            permutations.add(permutationArray.getArray());
        }
        System.out.println("Have permutations");
        ArrayList<List<int[]>> permutationsSplitByLength = splitByLength(permutations, numberOfAgents);
        System.out.println("Split permutations");
        ArrayList<PerfectMatching> matchingSet = new ArrayList<PerfectMatching>();
        for(List<int[]> permutationsOfALength : permutationsSplitByLength) {
            System.out.println("Processing: " + permutationsOfALength.size() + " permutations");
            List<PerfectMatching> uniqueMatchings = getMatchings(permutationsOfALength, numberOfAgents, numberOfGroups).stream().distinct().collect(Collectors.toList());
            matchingSet.addAll(uniqueMatchings);
        }
        new Scanner(System.in).nextInt();
        matchings = new PerfectMatching[matchingSet.size()];
        matchingSet.toArray(matchings);
    }

    private List<PerfectMatching> getMatchings(List<int[]> permutations, int numberOfAgents, int numberOfGroups) {
        CrossProduct<int[]> crossProduct = new CrossProduct<int[]>(permutations, 2);
        List<PerfectMatching> matchingSet = new LinkedList<>();
        while(crossProduct.hasNext()) {
            ArrayList<int[]> match = crossProduct.next();
            PerfectMatching perfectMatching = new PerfectMatching(2, numberOfAgents);
            perfectMatching.setMatchingFromPermutations(match);
            matchingSet.add(perfectMatching);
        }

        for(int i = 2; i<numberOfGroups; ++i) {
            matchingSet = extendMatchingsByPermutations(permutations, matchingSet);
        }
        return matchingSet.stream().distinct().collect(Collectors.toList());
    }

    private List<PerfectMatching> extendMatchingsByPermutations(List<int[]> permutations, List<PerfectMatching> perfectMatchings) {
        List<PerfectMatching> retval = new ArrayList<>();
        for(PerfectMatching perfectMatching: perfectMatchings) {
            for(int[] permutation : permutations) {
                retval.add(perfectMatching.extend(permutation));
            }
        }
        return retval.stream().distinct().collect(Collectors.toList());
    }

    private ArrayList<List<int[]>> splitByLength(List<int[]> permutations, int numberOfAgents) {
        ArrayList<List<int[]>> retval = new ArrayList<List<int[]>>(numberOfAgents);
        for(int i = 0; i<numberOfAgents; ++i) {
            retval.add(new ArrayList<int[]>());
        }
        for(int[] permutation : permutations) {
            int length = 0;
            for(int i = 0; i<numberOfAgents; ++i) {
                if (permutation[i] > -1) {
                    ++length;
                }
            }
            retval.get(length-1).add(permutation);
        }
        return retval;
    }

    public void setPreferenceSystem(PreferenceSystem preferenceSystem) {
        this.preferenceSystem = preferenceSystem;
        hasStableMatch = false;
    }

    public boolean hasStableMatch() {
        hasStableMatch = false;
        loud = false;
        if(sufficientChecks()) {
        	return hasStableMatch;
        }
        if (!hasStableMatch && preferenceSystem.size() % (preferenceSystem.getNumberOfAgents() * preferenceSystem.getNumberOfGroups()) == 0) {
            attemptStableMatch();
        }
        if (loud) {
            System.out.println("Has Stable: " + hasStableMatch);
        }
        return hasStableMatch;
    }

     private boolean sufficientChecks() {
        checkFirstChoiceCycle();
        if (!hasStableMatch) {
            checkFirstChoiceNineCycle();
        }
        checkAllSameDifferent();
        return hasStableMatch;
    }


    private void checkFirstChoiceCycle() {
        for (Agent agent : preferenceSystem.getAgents(0)) {
            checkFirstChoiceCycle(agent);
            if (hasStableMatch) {
                break;
            }
        }
    }

    private void checkFirstChoiceCycle(Agent agent) {
        int agentFirstChoice = agent.getFirstChoice();
        int partnerIndex = agentFirstChoice;
        for (int i = 1; i < preferenceSystem.getGroups().size(); ++i) {
            if (partnerIndex == -1) {
                return;
            }
            partnerIndex = preferenceSystem.getAgents(i).get(partnerIndex).getFirstChoice();
        }
        if (agent.getIndex() == partnerIndex) {
            hasStableMatch = true;
        }
    }

    private void checkFirstChoiceNineCycle() {
        for (Agent agent : preferenceSystem.getAgents(0)) {
            checkFirstChoiceNineCycle(agent);
            if (hasStableMatch) {
                break;
            }
        }
    }

    private void checkFirstChoiceNineCycle(Agent agent) {
        int agentFirstChoice = agent.getFirstChoice();
        int partnerIndex = agentFirstChoice;
        for (int i = 1; i < 3 * preferenceSystem.getGroups().size(); ++i) {
            if (partnerIndex == -1) {
                return;
            }
            partnerIndex = preferenceSystem.getAgents(i % 3).get(partnerIndex).getFirstChoice();
        }
        if (agent.getIndex() == partnerIndex) {
            hasStableMatch = true;
        }
    }

    private void checkAllSameDifferent() {
        for (Group group : preferenceSystem.getGroups()) {
            checkAllSameDifferent(group);
        }
    }

    private void checkAllSameDifferent(Group group) {
        int[] firstChoices = new int[group.getGroupSize()];
        for (int i = 0; i < firstChoices.length; ++i) {
            firstChoices[i] = group.getAgents().get(i).getFirstChoice();
        }
        // check all -1
        boolean negativeOneChoices = true;
        for (int i = 0; i < firstChoices.length; ++i) {
            negativeOneChoices = negativeOneChoices && firstChoices[i] == -1;
        }
        if (negativeOneChoices == true) {
            return;
        }
        if (firstChoices.length == 1) {
            if (firstChoices[0] != -1) {
                hasStableMatch = true;
            }
            return;
        }
        Arrays.sort(firstChoices);
        if (loud) {
            System.out.println("First Choices: " + Arrays.toString(firstChoices));
        }
        if (firstChoices[0] == 0 && firstChoices[firstChoices.length - 1] == firstChoices.length - 1) {
            hasStableMatch = true;
            if (loud) {
                System.out.println("All Different");
            }
            return;
        }
        int choice = firstChoices[0];
        if (firstChoices[firstChoices.length - 1] == choice) {
            hasStableMatch = true;
            if (loud) {
                System.out.println("All Same");
            }
        }
    }

    // assumes all groups have same size!
    public void attemptStableMatch() {
        for (PerfectMatching perfectMatching : matchings) {
            if (loud) {
                System.out.println(perfectMatching);
            }
            if (isComplete(perfectMatching) && isStable(perfectMatching)) {
                hasStableMatch = true;
                break;
            }

        }
    }

    private boolean isComplete(PerfectMatching perfectMatching) {
        for (int[] match : perfectMatching.getMatching()) {
            if (!isAcceptable(match)) {
                if (loud) {
                    System.out.println("Not complete");
                }
                return false;
            }
        }
        if (loud) {
            System.out.println("Complete");
        }
        return true;
    }

    private boolean isAcceptable(int[] match) {
        for (int i = 0; i < match.length; ++i) {
            int agentIndex = match[i];
            int matchPartner = match[(i + 1) % match.length];
            if (preferenceSystem.getAgents(i).get(agentIndex).getUnacceptablePartners().contains(matchPartner)) {
                return false;
            }
        }
        return true;
    }

    public boolean isStable(PerfectMatching perfectMatching) {
        blockers.reset();
        while (blockers.hasNext()) {
            ArrayList<Integer> blocker = blockers.next();
            if (isBlocking(blocker, perfectMatching)) {
                if (loud) {
                    System.out.println("Is Blocking");
                }
                return false;
            }
        }
        return true;
    }

    private boolean isBlocking(ArrayList<Integer> blocker, PerfectMatching perfectMatching) {
        boolean retval = true;
        for (int i = 0; i < blocker.size(); ++i) {
            int agentIndex = blocker.get(i);
            int blockPartner = blocker.get((i + 1) % blocker.size());
            int matchPartner = perfectMatching.getPartner(i, agentIndex);
            retval = retval && preferenceSystem.getAgents(i).get(agentIndex).prefers(blockPartner, matchPartner);
        }
        return retval;
    }

    public boolean loudHasStableMatch() {
        loud = true;
        System.out.println("Loud: " + loud);
        sufficientChecks();
        if (!hasStableMatch) {
            attemptStableMatch();
        }
        return hasStableMatch;
    }
}
