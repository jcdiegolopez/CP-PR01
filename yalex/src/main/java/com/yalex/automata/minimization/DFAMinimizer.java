package com.yalex.automata.minimization;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.yalex.automata.dfa.DFA;
import com.yalex.automata.dfa.DFAState;

/**
 * Minimización de DFA por refinamiento de particiones (equivalencia de Myhill–Nerode).
 */
public final class DFAMinimizer {

    private DFAMinimizer() {
    }

    public static DFA minimize(DFA dfa) {
        Objects.requireNonNull(dfa, "dfa");
        int n = dfa.getStateCount();
        if (n == 0) {
            throw new IllegalArgumentException("DFA vacío");
        }

        List<Character> alphabet = new ArrayList<>(dfa.getAlphabet());

        Set<Integer> accepting = new TreeSet<>();
        Set<Integer> nonAccepting = new TreeSet<>();
        for (int i = 0; i < n; i++) {
            if (dfa.getState(i).isAccepting()) {
                accepting.add(i);
            } else {
                nonAccepting.add(i);
            }
        }

        List<Set<Integer>> partition = new ArrayList<>();
        if (!accepting.isEmpty()) {
            partition.add(accepting);
        }
        if (!nonAccepting.isEmpty()) {
            partition.add(nonAccepting);
        }

        while (true) {
            int[] groupOf = new int[n];
            java.util.Arrays.fill(groupOf, -1);
            for (int g = 0; g < partition.size(); g++) {
                for (int s : partition.get(g)) {
                    groupOf[s] = g;
                }
            }

            List<Set<Integer>> newPartition = new ArrayList<>();
            for (Set<Integer> group : partition) {
                if (group.size() <= 1) {
                    newPartition.add(group);
                    continue;
                }
                Map<String, TreeSet<Integer>> buckets = new LinkedHashMap<>();
                for (int s : group) {
                    StringBuilder sig = new StringBuilder();
                    for (char a : alphabet) {
                        int t = dfa.transition(s, a);
                        sig.append(t < 0 ? -1 : groupOf[t]).append(',');
                    }
                    buckets.computeIfAbsent(sig.toString(), k -> new TreeSet<>()).add(s);
                }
                newPartition.addAll(buckets.values());
            }

            if (partitionEquals(partition, newPartition)) {
                partition = newPartition;
                break;
            }
            partition = newPartition;
        }

        int groups = partition.size();
        int[] oldToGroup = new int[n];
        for (int g = 0; g < groups; g++) {
            for (int s : partition.get(g)) {
                oldToGroup[s] = g;
            }
        }

        int newInitial = oldToGroup[dfa.getInitialStateId()];

        Map<Integer, Map<Character, Integer>> newTrans = new TreeMap<>();
        List<DFAState> newStates = new ArrayList<>();

        for (int g = 0; g < groups; g++) {
            int rep = partition.get(g).iterator().next();
            boolean acc = dfa.getState(rep).isAccepting();
            TreeSet<Integer> mergedPos = new TreeSet<>();
            for (int s : partition.get(g)) {
                mergedPos.addAll(dfa.getState(s).getPositions());
            }
            newStates.add(new DFAState(g, mergedPos, acc));

            for (char a : alphabet) {
                int t = dfa.transition(rep, a);
                if (t >= 0) {
                    int tg = oldToGroup[t];
                    newTrans.computeIfAbsent(g, k -> new TreeMap<>()).put(a, tg);
                }
            }
        }

        NavigableSet<Character> alpha = dfa.getAlphabet();
        return new DFA(newStates, newInitial, newTrans, alpha);
    }

    private static boolean partitionEquals(List<Set<Integer>> a, List<Set<Integer>> b) {
        if (a.size() != b.size()) {
            return false;
        }
        List<String> sa = canonical(a);
        List<String> sb = canonical(b);
        return sa.equals(sb);
    }

    private static List<String> canonical(List<Set<Integer>> parts) {
        List<String> out = new ArrayList<>();
        for (Set<Integer> s : parts) {
            out.add(new TreeSet<>(s).toString());
        }
        out.sort(String::compareTo);
        return out;
    }
}
