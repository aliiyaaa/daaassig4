package org.example.data;


import org.example.graph.Graph;

import java.io.File;
import java.util.Random;


public class DataGenerator {
    public static void main(String[] args) throws Exception {
        File dataDir = new File("data");
        if (!dataDir.exists()) dataDir.mkdirs();
        new DataGenerator().generateAll(dataDir);
        System.out.println("Datasets written to " + dataDir.getAbsolutePath());
    }

    public void generateAll(File dir) throws Exception {
        // 3 small, 3 medium, 3 large
        genSmall(dir, 0);
        genSmall(dir, 1);
        genSmall(dir, 2);
        genMedium(dir, 0);
        genMedium(dir, 1);
        genMedium(dir, 2);
        genLarge(dir, 0);
        genLarge(dir, 1);
        genLarge(dir, 2);
    }

    private void genSmall(File dir, int idx) throws Exception {
        Graph g = new Graph(8, true);
        // mix: one cycle among 0-2, some DAG edges
        g.addEdge(0,1,1); g.addEdge(1,2,1); g.addEdge(2,0,1);
        g.addEdge(2,3,2); g.addEdge(3,4,2); g.addEdge(4,5,1);
        g.addEdge(6,7,3);
        GraphIO.writeJson(g, new File(dir, "small_"+idx+".json"));
    }

    private void genMedium(File dir, int idx) throws Exception {
        int n = 15; Graph g = new Graph(n, true);
        // make a few SCCs
        cycle(g, 0,3,1); cycle(g, 4,6,1); cycle(g, 7,9,2);
        // random DAG edges
        Random rnd = new Random(100 + idx);
        for (int e = 0; e < 25; e++) {
            int u = rnd.nextInt(n), v = rnd.nextInt(n);
            if (u != v) g.addEdge(u, v, 1 + rnd.nextInt(5));
        }
        GraphIO.writeJson(g, new File(dir, "medium_"+idx+".json"));
    }

    private void genLarge(File dir, int idx) throws Exception {
        int n = 40; Graph g = new Graph(n, true);
        Random rnd = new Random(200 + idx);
        // several SCC clusters
        cycle(g, 0,4,1); cycle(g, 5,9,1); cycle(g, 10,12,2); cycle(g, 20,24,1);
        // add density
        for (int e = 0; e < 120; e++) {
            int u = rnd.nextInt(n), v = rnd.nextInt(n);
            if (u != v) g.addEdge(u, v, 1 + rnd.nextInt(10));
        }
        GraphIO.writeJson(g, new File(dir, "large_"+idx+".json"));
    }

    private void cycle(Graph g, int l, int r, int w) {
        for (int i = l; i < r; i++) g.addEdge(i, i+1, w);
        g.addEdge(r, l, w);
    }
}


