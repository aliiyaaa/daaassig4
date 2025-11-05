package org.example.data;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.graph.Graph;

import java.io.File;
import java.io.IOException;


public class GraphIO {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static Graph readJson(File file) throws IOException {
        JsonNode root = MAPPER.readTree(file);
        int n = root.get("n").asInt();
        boolean directed = root.path("directed").asBoolean(true);
        Graph g = new Graph(n, directed);
        ArrayNode edges = (ArrayNode) root.get("edges");
        for (JsonNode e : edges) {
            int u = e.get("u").asInt();
            int v = e.get("v").asInt();
            long w = e.path("w").asLong(1);
            g.addEdge(u, v, w);
        }
        return g;
    }

    public static void writeJson(Graph g, File file) throws IOException {
        ObjectNode root = MAPPER.createObjectNode();
        root.put("n", g.n);
        root.put("directed", g.directed);
        ArrayNode edges = MAPPER.createArrayNode();
        for (int u = 0; u < g.n; u++) {
            for (Graph.Edge e : g.adj.get(u)) {
                ObjectNode en = MAPPER.createObjectNode();
                en.put("u", u);
                en.put("v", e.to);
                en.put("w", e.w);
                edges.add(en);
            }
        }
        root.set("edges", edges);
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(file, root);
    }
}


