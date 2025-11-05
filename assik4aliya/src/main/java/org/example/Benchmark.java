package org.example;


import org.example.data.GraphIO;
import org.example.graph.Graph;
import org.example.graph.dagsp.DagShortestPaths;
import org.example.graph.scc.TarjanSCC;
import org.example.graph.topo.KahnTopo;
import org.example.metrics.SimpleMetrics;

import java.io.File;
import java.util.Arrays;
import java.util.List;


public class Benchmark {
    public static void main(String[] args) throws Exception {
        File dataDir = new File("data");
        if (!dataDir.exists() || dataDir.listFiles() == null) {
            System.out.println("No data directory found. Generate datasets first.");
            return;
        }
        System.out.println("dataset,n,m,comps,scc_us,dfsVisits,dfsEdges,topo_us,kahnPushes,kahnPops,dagsp_short_us,relaxations,dagsp_long_us");
        for (File f : dataDir.listFiles((d, name) -> name.endsWith(".json"))) {
            Graph g = GraphIO.readJson(f);

            // SCC + condensation
            SimpleMetrics mscc = new SimpleMetrics();
            mscc.start();
            TarjanSCC tarjan = new TarjanSCC(g, mscc);
            List<List<Integer>> comps = tarjan.run();
            mscc.stop();
            long sccUs = mscc.elapsed() / 1000;
            Graph dag = tarjan.buildCondensation();
            int dfsVisits = mscc.snapshot().getOrDefault("dfsVisits", 0L).intValue();
            int dfsEdges = mscc.snapshot().getOrDefault("dfsEdges", 0L).intValue();

            // Topo on condensation
            SimpleMetrics mtopo = new SimpleMetrics();
            mtopo.start();
            KahnTopo topo = new KahnTopo(dag, mtopo);
            List<Integer> order = topo.topoOrder();
            mtopo.stop();
            long topoUs = mtopo.elapsed() / 1000;
            long kahnPushes = mtopo.snapshot().getOrDefault("kahnPushes", 0L);
            long kahnPops = mtopo.snapshot().getOrDefault("kahnPops", 0L);

            // DAG shortest from 0 if exists
            SimpleMetrics msp = new SimpleMetrics();
            msp.start();
            DagShortestPaths dsp = new DagShortestPaths(dag, msp);
            DagShortestPaths.Result shortest = order.isEmpty() ? new DagShortestPaths.Result(new long[0], new int[0]) : dsp.shortestFrom(0, order);
            msp.stop();
            long spUs = msp.elapsed() / 1000;
            long relax = msp.snapshot().getOrDefault("relaxations", 0L);

            // DAG longest
            SimpleMetrics mlong = new SimpleMetrics();
            mlong.start();
            DagShortestPaths.Result longest = dsp.longestPath();
            mlong.stop();
            long longUs = mlong.elapsed() / 1000;

            int m = 0;
            for (int u = 0; u < g.n; u++) m += g.adj.get(u).size();
            System.out.printf("%s,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d\n",
                    f.getName(), g.n, m, comps.size(), sccUs, dfsVisits, dfsEdges,
                    topoUs, kahnPushes, kahnPops, spUs, relax, longUs);
        }
    }
}
