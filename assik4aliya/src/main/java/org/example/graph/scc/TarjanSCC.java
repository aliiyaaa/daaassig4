package org.example.graph.scc;


import org.example.graph.Graph;
import org.example.metrics.Metrics;

import java.util.*;


public class TarjanSCC {
    private final Graph g;
    private final Metrics m;
    private int time = 0;
    private final int[] disc;
    private final int[] low;
    private final boolean[] onStack;
    private final Deque<Integer> stack = new ArrayDeque<>();
    private final List<List<Integer>> comps = new ArrayList<>();
    public TarjanSCC(Graph g, Metrics m) {
        this.g = g;
        this.m = m;
        this.disc = new int[g.n];
        this.low = new int[g.n];
        this.onStack = new boolean[g.n];
        Arrays.fill(disc, -1);
    }
    public List<List<Integer>> run() {
        for (int i = 0; i < g.n; i++) if (disc[i] == -1) dfs(i);
        return comps;
    }
    private void dfs(int u) {
        disc[u] = low[u] = time++;
        stack.push(u);
        onStack[u] = true;
        m.increment("dfsVisits");
        for (Graph.Edge e : g.adj.get(u)) {
            m.increment("dfsEdges");
            int v = e.to;
            if (disc[v] == -1) {
                dfs(v);
                low[u] = Math.min(low[u], low[v]);
            } else if (onStack[v]) {
                low[u] = Math.min(low[u], disc[v]);
            }
        }
        if (low[u] == disc[u]) {
            List<Integer> comp = new ArrayList<>();
            while (true) {
                int v = stack.pop();
                onStack[v] = false;
                comp.add(v);
                if (v == u) break;
            }
            comps.add(comp);
        }
    }
    public List<List<Integer>> components() { return comps; }

    public Graph buildCondensation() {
        int compCount = comps.size();
        Graph dag = new Graph(compCount, true);
        int[] compId = new int[g.n];
        for (int i = 0; i < comps.size(); i++) {
            for (int v : comps.get(i)) compId[v] = i;
        }
        boolean[][] seen = new boolean[compCount][compCount];
        for (int u = 0; u < g.n; u++) {
            for (Graph.Edge e : g.adj.get(u)) {
                int cu = compId[u], cv = compId[e.to];
                if (cu != cv && !seen[cu][cv]) {
                    seen[cu][cv] = true;
                    dag.addEdge(cu, cv, 1);
                }
            }
        }
        return dag;
    }
}
