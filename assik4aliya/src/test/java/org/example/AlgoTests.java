package org.example;


import org.example.graph.Graph;
import org.example.graph.dagsp.DagShortestPaths;
import org.example.graph.scc.TarjanSCC;
import org.example.graph.topo.KahnTopo;
import org.example.metrics.SimpleMetrics;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class AlgoTests {
    @Test
    void testSCCSimpleCycle() {
        Graph g = new Graph(3, true);
        g.addEdge(0,1,1); g.addEdge(1,2,1); g.addEdge(2,0,1);
        TarjanSCC scc = new TarjanSCC(g, new SimpleMetrics());
        List<List<Integer>> comps = scc.run();
        assertEquals(1, comps.size());
        assertEquals(3, comps.get(0).size());
    }

    @Test
    void testTopoOrderOnDAG() {
        Graph g = new Graph(4, true);
        g.addEdge(0,1,1); g.addEdge(1,2,1); g.addEdge(0,3,1);
        KahnTopo topo = new KahnTopo(g, new SimpleMetrics());
        List<Integer> order = topo.topoOrder();
        assertEquals(4, order.size());
        assertTrue(order.indexOf(0) < order.indexOf(1));
        assertTrue(order.indexOf(1) < order.indexOf(2));
    }

    @Test
    void testDagShortestAndLongest() {
        Graph g = new Graph(4, true);
        g.addEdge(0,1,1); g.addEdge(1,2,2); g.addEdge(0,3,5);
        List<Integer> topo = new KahnTopo(g, new SimpleMetrics()).topoOrder();
        DagShortestPaths dsp = new DagShortestPaths(g, new SimpleMetrics());
        DagShortestPaths.Result s = dsp.shortestFrom(0, topo);
        assertEquals(0, s.dist[0]);
        assertEquals(1, s.dist[1]);
        assertEquals(3, s.dist[2]);
        DagShortestPaths.Result l = dsp.longestPath();
        assertTrue(l.dist[3] >= 5);
    }
}


