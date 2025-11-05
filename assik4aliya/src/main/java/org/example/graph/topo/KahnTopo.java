package org.example.graph.topo;


import org.example.graph.Graph;
import org.example.metrics.Metrics;

import java.util.*;


public class KahnTopo {
    private final Graph g;
    private final Metrics m;
    public KahnTopo(Graph g, Metrics m) { this.g = g; this.m = m; }
    public List<Integer> topoOrder() {
        int n = g.n;
        int[] indeg = new int[n];
        for (int u = 0; u < n; u++) for (Graph.Edge e : g.adj.get(u)) indeg[e.to]++;
        Deque<Integer> q = new ArrayDeque<>();
        for (int i = 0; i < n; i++) if (indeg[i] == 0) q.add(i);
        List<Integer> order = new ArrayList<>();
        while (!q.isEmpty()) {
            int u = q.removeFirst();
            order.add(u);
            m.increment("kahnPops");
            for (Graph.Edge e : g.adj.get(u)) {
                m.increment("kahnEdges");
                indeg[e.to]--;
                if (indeg[e.to] == 0) {
                    q.add(e.to);
                    m.increment("kahnPushes");
                }
            }
        }
        if (order.size() != n) return Collections.emptyList();
        return order;
    }
}
