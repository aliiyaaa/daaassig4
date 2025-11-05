package org.example.graph.dagsp;


import org.example.graph.Graph;
import org.example.metrics.Metrics;

import java.util.*;


public class DagShortestPaths {
    private final Graph g;
    private final Metrics m;

    public DagShortestPaths(Graph g, Metrics m) { this.g = g; this.m = m; }

    public Result shortestFrom(int src, List<Integer> topo) {
        long INF = Long.MAX_VALUE / 4;
        long[] dist = new long[g.n];
        int[] parent = new int[g.n];
        Arrays.fill(dist, INF);
        Arrays.fill(parent, -1);
        dist[src] = 0;
        for (int u : topo) {
            if (dist[u] == INF) continue;
            for (Graph.Edge e : g.adj.get(u)) {
                m.increment("relaxations");
                if (dist[e.to] > dist[u] + e.w) {
                    dist[e.to] = dist[u] + e.w;
                    parent[e.to] = u;
                }
            }
        }
        return new Result(dist, parent);
    }

    public Result longestPath() {
        List<Integer> topo = new KahnTopoAdapter(g).topoOrder();
        if (topo.isEmpty()) return new Result(new long[0], new int[0]);
        long NEG_INF = Long.MIN_VALUE / 4;
        long[] dist = new long[g.n];
        int[] parent = new int[g.n];
        Arrays.fill(dist, NEG_INF);
        Arrays.fill(parent, -1);
        for (int s : topo) {
            if (dist[s] == NEG_INF) dist[s] = 0;
            for (Graph.Edge e : g.adj.get(s)) {
                if (dist[e.to] < dist[s] + e.w) {
                    dist[e.to] = dist[s] + e.w;
                    parent[e.to] = s;
                }
            }
        }
        return new Result(dist, parent);
    }

    public static class Result {
        public final long[] dist;
        public final int[] parent;

        public Result(long[] dist, int[] parent) { this.dist = dist; this.parent = parent; }

        public List<Integer> reconstruct(int target) {
            List<Integer> path = new ArrayList<>();
            if (target < 0 || target >= parent.length) return path;
            int cur = target;
            while (cur != -1) {
                path.add(cur);
                cur = parent[cur];
            }
            Collections.reverse(path);
            return path;
        }
    }

    private static class KahnTopoAdapter {
        private final Graph g;

        KahnTopoAdapter(Graph g) { this.g = g; }

        List<Integer> topoOrder() {
            int n = g.n;
            int[] indeg = new int[n];
            for (int u = 0; u < n; u++) for (Graph.Edge e : g.adj.get(u)) indeg[e.to]++;
            Deque<Integer> q = new ArrayDeque<>();
            for (int i = 0; i < n; i++) if (indeg[i] == 0) q.add(i);
            List<Integer> order = new ArrayList<>();
            while (!q.isEmpty()) {
                int u = q.removeFirst();
                order.add(u);
                for (Graph.Edge e : g.adj.get(u)) {
                    indeg[e.to]--;
                    if (indeg[e.to] == 0) q.add(e.to);
                }
            }
            if (order.size() != n) return Collections.emptyList();
            return order;
        }
    }
}
