package org.example;


import java.util.ArrayList;
import java.util.List;


public class Graph {
    public final int n;
    public final boolean directed;
    public final List<List<Edge>> adj;
    public static class Edge {
        public final int to;
        public final long w;
        public Edge(int to, long w) { this.to = to; this.w = w; }
    }
    public Graph(int n, boolean directed) {
        this.n = n;
        this.directed = directed;
        this.adj = new ArrayList<>();
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
    }
    public void addEdge(int u, int v, long w) {
        adj.get(u).add(new Edge(v, w));
        if (!directed) adj.get(v).add(new Edge(u, w));
    }
}