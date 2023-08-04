package io.github.annusshka.GraphUtils;

import io.github.annusshka.Tickets;

import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

public class Graph {

    /**
     * Список равный кол-ву вершин + 1 (0 - индекс), под каждым индексом хранится Map с номерами остановок,
     * до которых можно добраться из остановки с этим номером, и списком билетов, которыми можно воспользоваться,
     * чтобы туда добраться
     */
    private List<Map<Integer, List<Tickets>>> vList = new ArrayList<>();

    private int vCount = 0;
    private int eCount = 0;

    public int vertexCount() {
        return vCount;
    }

    public int edgeCount() {
        return eCount;
    }

    public Map<Integer, List<Tickets>> getVListStops(int index) {
        if (index <= 0 || index >= vList.size()) {
            return null;
        }
        return vList.get(index);
    }

    public void addRoads(int v1, int v2, Tickets stop) {
        Map<Integer, List<Tickets>> map = vList.get(v1);

        if (map.isEmpty() || !map.containsKey(v2)) {
            map.put(v2, new ArrayList<>());
        }
        if (!map.get(v2).contains(stop)) {
            map.get(v2).add(stop);
        }
    }

    public boolean isAdj(int v1, int v2) {
        if (vList.get(v1) == null || vList.get(v2) == null) {
            return false;
        }
        return vList.get(v1).containsKey(v2);
    }

    public void addEdge(int v1, int v2, Tickets road) {
        int maxV = Math.max(v1, v2);

        for (; vCount <= maxV; vCount++) {
            vList.add(null);
        }
        if (!isAdj(v1, v2)) {
            if (vList.get(v1) == null) {
                vList.set(v1, new TreeMap<>());
            }
            if (vList.get(v2) == null) {
                vList.set(v2, new TreeMap<>());
            }
        }
        addRoads(v1, v2, road);
        addRoads(v2, v1, road);
        eCount++;
    }

    public Set<Integer> adj(int v) {
        return vList.get(v) == null ? null : vList.get(v).keySet();
    }

    public Map<Integer, List<Tickets>> getAdjStops(int v) {
        return vList.get(v) == null ? null : vList.get(v);
    }

    public static Tickets contains(String s) {
        for (Tickets ticket : Tickets.values()) {
            if (ticket.toString().compareTo(s) == 0) {
                return ticket;
            }
        }
        return null;
    }

    public static Graph fromStr(String str, Class clz) throws Exception {
        Graph graph = (Graph) clz.newInstance();
        if (Pattern.compile("^\\s*\\d+").matcher(str).find()) {
            Scanner scanner = new Scanner(str);
            //int vertexCount = scanner.nextInt();
            int edgeCount = scanner.nextInt();
            for (int i = 0; i < edgeCount; i++) {
                int v1 = scanner.nextInt();
                int v2 = scanner.nextInt();
                String s = scanner.next();
                Tickets ticket = contains(s);
                if (ticket != null) {
                    graph.addEdge(v1, v2, ticket);
                } else {
                    throw new Exception("Ошибка в карте!");
                }
            }
        }

        return graph;
    }

    public static String toStr(Graph graph) {
        StringBuilder sb = new StringBuilder();
        String nl = System.getProperty("line.separator");
        for (int v1 = 0; v1 < graph.vertexCount(); v1++) {
            if (v1 != 0) {
                int count = 0;
                if (graph.adj(v1) != null) {
                    sb.append(String.format("  %d %s", v1, (" -- ")));
                    for (Map.Entry<Integer, List<Tickets>> v2: graph.getAdjStops(v1).entrySet()) {
                        sb.append(String.format(" { %d ", v2.getKey()));
                        for (Tickets roads : v2.getValue()) {
                            sb.append(" ").append(roads).append(" ");
                        }
                        sb.append("}, ");
                        count++;
                    }
                    sb.append(nl);
                    if (count == 0) {
                        sb.append(v1).append(nl);
                    }
                }
            }
        }
        sb.append(nl);

        return sb.toString();
    }
}
