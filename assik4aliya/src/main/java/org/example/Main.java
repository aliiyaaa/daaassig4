package org.example;


import org.example.data.GraphIO;
import org.example.graph.Graph;
import org.example.graph.dagsp.DagShortestPaths;
import org.example.graph.scc.TarjanSCC;
import org.example.graph.topo.KahnTopo;
import org.example.metrics.SimpleMetrics;

import java.util.*;


public class Main {
    public static void main(String[] args) {
        // If a dataset path is provided, load it; otherwise run a small demo
        Graph g;
        try {
            if (args.length > 0) {
                g = GraphIO.readJson(new java.io.File(args[0]));
            } else {
                g = new Graph(6, true);
                g.addEdge(0, 1, 1);
                g.addEdge(1, 2, 1);
                g.addEdge(2, 0, 1);
                g.addEdge(2, 3, 2);
                g.addEdge(3, 4, 2);
                g.addEdge(4, 5, 3);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        SimpleMetrics m = new SimpleMetrics();
        TarjanSCC scc = new TarjanSCC(g, m);
        List<List<Integer>> comps = scc.run();
        System.out.println("SCCs: " + comps);
        Graph dag = scc.buildCondensation();

        KahnTopo topo = new KahnTopo(dag, m);
        List<Integer> order = topo.topoOrder();
        System.out.println("Condensation topo order: " + order);

        DagShortestPaths sp = new DagShortestPaths(dag, m);
        DagShortestPaths.Result shortest = sp.shortestFrom(0, order);
        DagShortestPaths.Result longest = sp.longestPath();
        System.out.println("DAG shortest dist: " + Arrays.toString(shortest.dist));
        System.out.println("DAG longest dist:  " + Arrays.toString(longest.dist));
    }
}