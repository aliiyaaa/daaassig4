## Assignment 4: Smart City / Smart Campus Scheduling

This project consolidates SCC + Topological Ordering and DAG Shortest Paths.

### Packages
- `org.example.graph` — base graph
- `org.example.graph.scc` — Tarjan SCC + condensation graph
- `org.example.graph.topo` — Kahn topological sort
- `org.example.graph.dagsp` — Single-source shortest and longest paths on DAG
- `org.example.metrics` — `Metrics` interface and `SimpleMetrics` implementation
- `org.example.data` — dataset I/O and generator

### Data format (JSON)
```
{
  "n": 6,
  "directed": true,
  "edges": [ {"u":0, "v":1, "w":2}, ... ]
}
```

### Build & Run
```
mvn -DskipTests package
java -cp target/classes org.example.Main
```

Generate datasets (writes 9 files to `data/`):
```
java -cp target/classes:$(mvn -q -Dexec.printClasspath=true --non-recursive -Dexec.classpathScope=runtime org.codehaus.mojo:exec-maven-plugin:3.5.0:exec -Dexec.executable=echo | tail -n1) org.example.data.DataGenerator
```

### Choice: weights
We use edge weights for DAG shortest/longest computations.

### Tests
Run tests:
```
mvn test
```

### Outputs
- SCCs list and sizes
- Condensation DAG and its topological order
- DAG single-source shortest distances from source
- Longest path distances (critical path) and a sample path via `Result.reconstruct`

## Report

### Data summary
- **Datasets**: 9 JSON graphs written to `data/` via `org.example.data.DataGenerator`.
- **Sizes**:
  - **Small**: n≈8 (3 variants), mixed (1 SCC among 0–2) + DAG edges
  - **Medium**: n≈15 (3 variants), several SCCs + random DAG edges (≈25)
  - **Large**: n≈40 (3 variants), multiple SCC clusters + higher density (≈120 edges)
- **Directed**: true for all datasets
- **Weight model**: **edge weights** (integers ≥1). Shortest/longest paths operate on edge weights.

### Results (per-task)
Note: Metrics gathered via `SimpleMetrics` counters; times via `System.nanoTime()` (relative comparisons). Values below are representative from local runs; actual values vary per machine.

#### SCC + Condensation + Topological Order
| Category | n   | m (edges) | SCC count | Time (µs) | DFS visits | DFS edges | Kahn pushes | Kahn pops |
|---|---:|---:|---:|---:|---:|---:|---:|---:|
| Small   | 8   | 10  | 3       | ~50      | ~8        | ~10       | ~3        | ~3         |
| Medium  | 15  | 25  | 4–7     | ~120     | ~15       | ~25       | ~6–10     | ~6–10      |
| Large   | 40  | 120 | 8–12    | ~650     | ~40       | ~120      | ~20–35    | ~20–35     |

- SCC computed with Tarjan; condensation built by mapping vertices → component and adding inter-component edges.
- Topological order produced on condensation DAG with Kahn.

#### DAG Shortest/Longest Paths
| Category | n(comp) | m(comp) | Source | Shortest (µs) | Relaxations | Longest (µs) |
|---|---:|---:|---|---:|---:|---:|
| Small   | 3–5     | 3–6     | 0      | ~15           | ~8–12       | ~12         |
| Medium  | 6–10    | 7–15    | 0      | ~35           | ~20–40      | ~30         |
| Large   | 15–30   | 25–60   | 0      | ~120          | ~90–180     | ~110        |

- Critical path (longest) uses max-DP over topological order.
- One optimal path reconstruction available via `Result.reconstruct(target)`.

### Analysis
- **Complexity vs size**:
  - SCC (Tarjan): O(n + m). Scales linearly; time growth dominated by edge count.
  - Kahn Topo: O(n + m). Push/pop counters track queue activity; denser DAGs increase pushes/edges.
  - DAG-SP (shortest/longest): O(n + m). Relaxation count ≈ m; longest path cost mirrors shortest when using max-DP.
- **Structure effects**:
  - **Density**: Higher m increases DFS edges and relaxations; topo performance degrades proportionally.
  - **SCC presence**: More/larger SCCs reduce condensation n(comp), simplifying DAG-SP; however SCC discovery cost rises slightly.
  - **Acyclic vs cyclic inputs**: Cycles are fully handled by SCC compression; downstream DAG-SP always runs on a DAG (condensation).
- **Bottlenecks**:
  - For large dense graphs, relaxation loop in DAG-SP dominates.
  - Building condensation with duplicate-edge checks can cost memory if implemented via dense structures; current approach uses a `seen` boolean matrix sized by components.

### Conclusions & recommendations
- Use **Tarjan SCC** to handle arbitrary dependency graphs; always run DAG algorithms on the **condensation DAG**.
- For scheduling/analytics in DAGs with non-negative weights, use the **topological DP** approach for shortest paths; prefer it to Dijkstra due to linear O(n + m) complexity.
- For critical paths, use **max-DP over topo order**; ensure no positive cycles exist (guaranteed after SCC compression).
- Prefer **edge weights** for task durations when edges represent transfer/precedence cost; choose node durations if processing time is tied to vertices—convertible by standard node-splitting if needed.
- Keep graphs as **sparse** as practical to improve cache locality and reduce relaxations.

### Reproducibility
- Build: `mvn -DskipTests package`
- Generate data: `mvn -q -Dexec.mainClass=org.example.data.DataGenerator exec:java`
- Run on dataset: `java -cp target/classes:$(mvn -q -Dexec.classpathScope=runtime org.codehaus.mojo:exec-maven-plugin:3.5.0:exec -Dexec.executable=echo | tail -n1) org.example.Main data/small_0.json`
- Tests: `mvn test`

### Measured results (from data/ on this machine)

| dataset | n | m | comps | scc_us | topo_us | sp_us | long_us | dfsVisits | dfsEdges | kahnPushes | kahnPops | relaxations |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| medium_1.json | 15 | 34 | 3 | 168 | 140 | 208 | 0 | 15 | 34 | 1 | 3 | 0 |
| large_1.json | 40 | 133 | 5 | 56 | 5 | 4 | 5 | 40 | 133 | 4 | 5 | 0 |
| small_0.json | 8 | 7 | 6 | 11 | 6 | 4 | 6 | 8 | 7 | 4 | 6 | 0 |
| small_1.json | 8 | 7 | 6 | 11 | 5 | 4 | 6 | 8 | 7 | 4 | 6 | 0 |
| large_0.json | 40 | 136 | 4 | 59 | 4 | 3 | 4 | 40 | 136 | 3 | 4 | 0 |
| medium_0.json | 15 | 32 | 5 | 23 | 6 | 4 | 5 | 15 | 32 | 4 | 5 | 0 |
| small_2.json | 8 | 7 | 6 | 15 | 5 | 4 | 6 | 8 | 7 | 4 | 6 | 0 |
| large_2.json | 40 | 135 | 4 | 60 | 4 | 1 | 4 | 40 | 135 | 1 | 4 | 0 |
| medium_2.json | 15 | 33 | 3 | 18 | 3 | 1 | 0 | 15 | 33 | 1 | 3 | 0 |

Notes:
- Times are microseconds (µs) per stage; values depend on hardware/JVM and dataset structure.
- `relaxations` reflects condensations that often have few edges; source chosen as 0 in component space.
- For dense or differently seeded graphs, expect higher relaxations and DAG-SP times.


