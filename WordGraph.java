import java.io.*;
import java.util.*;

public class WordGraph {
    private Map<String, Map<String, Integer>> graph;
    private Random random;

    public WordGraph() {
        graph = new HashMap<>();
        random = new Random();
    }

    // 读取并处理文本文件以构建图
    private void buildGraph(String filePath) throws IOException {
        graph.clear();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        StringBuilder text = new StringBuilder();

        while ((line = reader.readLine()) != null) {
            text.append(line).append(" ");
        }
        reader.close();

        // 处理文本：转换为小写，将非字母字符替换为空格
        String processedText = text.toString().toLowerCase()
                .replaceAll("[^a-zA-Z\\s]", " ")
                .replaceAll("\\s+", " ");

        String[] words = processedText.trim().split("\\s+");

        // 构建有向图
        for (int i = 0; i < words.length - 1; i++) {
            String word1 = words[i];
            String word2 = words[i + 1];

            if (!word1.isEmpty() && !word2.isEmpty()) {
                graph.computeIfAbsent(word1, k -> new HashMap<>())
                        .merge(word2, 1, Integer::sum);
            }
        }
    }

    // 展示有向图
    public void showDirectedGraph(Map<String, Map<String, Integer>> G) {
        System.out.println("有向图：");
        for (String word1 : G.keySet()) {
            for (Map.Entry<String, Integer> edge : G.get(word1).entrySet()) {
                System.out.printf("%s -> %s (权重: %d)\n",
                        word1, edge.getKey(), edge.getValue());
            }
        }
    }

    // 查询桥接词
    public String queryBridgeWords(String word1, String word2) {
        word1 = word1.toLowerCase();
        word2 = word2.toLowerCase();

        if (!graph.containsKey(word1) || !graph.containsKey(word2)) {
            return "图中不存在 word1 或 word2!";
        }

        List<String> bridgeWords = new ArrayList<>();
        for (String word3 : graph.keySet()) {
            if (graph.get(word1).containsKey(word3) &&
                    graph.get(word3).containsKey(word2)) {
                bridgeWords.add(word3);
            }
        }

        if (bridgeWords.isEmpty()) {
            return "从 " + word1 + " 到 " + word2 + " 不存在桥接词！";
        }

        StringBuilder result = new StringBuilder("从 " +
                word1 + " 到 " + word2 + " 的桥接词为：");
        for (int i = 0; i < bridgeWords.size(); i++) {
            result.append(bridgeWords.get(i));
            if (i < bridgeWords.size() - 2)
                result.append("、");
            else if (i == bridgeWords.size() - 2)
                result.append(" 和 ");
        }
        result.append("。");
        return result.toString();
    }

    // 根据桥接词生成新文本
    public String generateNewText(String inputText) {
        String[] words = inputText.toLowerCase()
                .replaceAll("[^a-zA-Z\\s]", " ")
                .trim()
                .split("\\s+");

        if (words.length < 2)
            return inputText;

        StringBuilder result = new StringBuilder();
        result.append(words[0]);

        for (int i = 0; i < words.length - 1; i++) {
            String word1 = words[i];
            String word2 = words[i + 1];

            List<String> bridgeWords = new ArrayList<>();
            if (graph.containsKey(word1)) {
                for (String word3 : graph.keySet()) {
                    if (graph.get(word1).containsKey(word3) &&
                            graph.get(word3).containsKey(word2)) {
                        bridgeWords.add(word3);
                    }
                }
            }

            if (!bridgeWords.isEmpty()) {
                String bridge = bridgeWords.get(
                        random.nextInt(bridgeWords.size()));
                result.append(" ").append(bridge);
            }
            result.append(" ").append(word2);
        }

        return result.toString();
    }

    // 计算两个单词之间的最短路径
    public String calcShortestPath(String word1, String word2) {
        word1 = word1.toLowerCase();
        word2 = word2.toLowerCase();

        if (!graph.containsKey(word1) || !graph.containsKey(word2)) {
            return "图中不存在 " + word1 + " 或 " + word2 + "!";
        }

        Map<String, Integer> distances = new HashMap<>();
        Map<String, String> predecessors = new HashMap<>();
        PriorityQueue<String> queue = new PriorityQueue<>(
                Comparator.comparingInt(distances::get));

        for (String word : graph.keySet()) {
            distances.put(word, Integer.MAX_VALUE);
        }
        distances.put(word1, 0);
        queue.offer(word1);

        while (!queue.isEmpty()) {
            String current = queue.poll();

            if (current.equals(word2))
                break;

            if (graph.containsKey(current)) {
                for (Map.Entry<String, Integer> neighbor : graph.get(current).entrySet()) {
                    String next = neighbor.getKey();
                    int weight = neighbor.getValue();
                    int newDist = distances.get(current) + weight;

                    if (newDist < distances.get(next)) {
                        distances.put(next, newDist);
                        predecessors.put(next, current);
                        queue.remove(next);
                        queue.offer(next);
                    }
                }
            }
        }

        if (distances.get(word2) == Integer.MAX_VALUE) {
            return "从 " + word1 + " 到 " + word2 + " 不可达！";
        }

        // 重构路径
        List<String> path = new ArrayList<>();
        String current = word2;
        while (current != null) {
            path.add(current);
            current = predecessors.get(current);
        }
        Collections.reverse(path);

        // 在图中高亮显示路径
        System.out.println("从 " + word1 + " 到 " + word2 + " 的最短路径：");
        for (String word : graph.keySet()) {
            for (Map.Entry<String, Integer> edge : graph.get(word).entrySet()) {
                String next = edge.getKey();
                boolean isPathEdge = false;
                for (int i = 0; i < path.size() - 1; i++) {
                    if (path.get(i).equals(word) &&
                            path.get(i + 1).equals(next)) {
                        isPathEdge = true;
                        break;
                    }
                }
                System.out.printf("%s -> %s (权重: %d)%s\n",
                        word, next, edge.getValue(),
                        isPathEdge ? " [路径]" : "");
            }
        }

        return "路径: " + String.join(" -> ", path) +
                "\n长度: " + distances.get(word2);
    }

    // 计算 PageRank 值
    public Double calPageRank(String word) {
        word = word.toLowerCase();
        if (!graph.containsKey(word))
            return 0.0;

        int N = graph.size();
        Map<String, Double> pr = new HashMap<>();
        double d = 0.85;

        // 初始化 PageRank 值
        for (String w : graph.keySet()) {
            pr.put(w, 1.0 / N);
        }

        // 迭代计算直到收敛（简化为固定迭代次数）
        for (int iter = 0; iter < 100; iter++) {
            Map<String, Double> newPr = new HashMap<>();
            for (String w : graph.keySet()) {
                double sum = 0;
                for (String in : graph.keySet()) {
                    if (graph.get(in).containsKey(w)) {
                        int outDegree = graph.get(in).size();
                        sum += pr.get(in) / outDegree;
                    }
                }
                newPr.put(w, (1 - d) / N + d * sum);
            }
            pr = newPr;
        }

        return pr.get(word);
    }

    // 随机游走
    public String randomWalk() {
        if (graph.isEmpty())
            return "图为空！";

        List<String> nodes = new ArrayList<>(graph.keySet());
        String current = nodes.get(random.nextInt(nodes.size()));
        List<String> visitedNodes = new ArrayList<>();
        List<String> visitedEdges = new ArrayList<>();
        Set<String> edgeSet = new HashSet<>();

        visitedNodes.add(current);

        Scanner scanner = new Scanner(System.in);
        System.out.println("随机游走开始。按回车继续，输入 'q' 退出。");

        while (graph.containsKey(current) && !graph.get(current).isEmpty()) {
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("q"))
                break;

            List<String> neighbors = new ArrayList<>(graph.get(current).keySet());
            String next = neighbors.get(random.nextInt(neighbors.size()));

            String edge = current + "->" + next;
            if (edgeSet.contains(edge)) {
                visitedNodes.add(next);
                visitedEdges.add(edge);
                break;
            }

            visitedNodes.add(next);
            visitedEdges.add(edge);
            edgeSet.add(edge);
            current = next;

            System.out.println("当前节点: " + current);
        }

        // 保存到文件
        try {
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream("random_walk.txt"), "UTF-8"));
            writer.write("节点: " + String.join(" -> ", visitedNodes));
            writer.newLine();
            writer.write("边: " + String.join(", ", visitedEdges));
            writer.close();
        } catch (IOException e) {
            System.err.println("写入文件错误: " + e.getMessage());
        }

        return "节点: " + String.join(" -> ", visitedNodes) +
                "\n边: " + String.join(", ", visitedEdges);
    }

    // 主程序入口
    public static void main(String[] args) {
        WordGraph wg = new WordGraph();
        Scanner scanner = new Scanner(System.in);

        // 获取文件路径
        String filePath;
        if (args.length > 0) {
            filePath = args[0];
        } else {
            System.out.println("请输入文本文件路径：");
            filePath = scanner.nextLine();
        }

        try {
            wg.buildGraph(filePath);
        } catch (IOException e) {
            System.out.println("读取文件错误: " + e.getMessage());
            return;
        }

        while (true) {
            System.out.println("\n菜单:");
            System.out.println("1. 展示有向图");
            System.out.println("2. 查询桥接词");
            System.out.println("3. 生成新文本");
            System.out.println("4. 计算最短路径");
            System.out.println("5. 计算 PageRank");
            System.out.println("6. 随机游走");
            System.out.println("7. 退出");
            System.out.print("请选择功能：");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("无效输入！");
                continue;
            }

            switch (choice) {
                case 1:
                    wg.showDirectedGraph(wg.graph);
                    break;
                case 2:
                    System.out.print("请输入 word1:");
                    String word1 = scanner.nextLine();
                    System.out.print("请输入 word2:");
                    String word2 = scanner.nextLine();
                    System.out.println(wg.queryBridgeWords(word1, word2));
                    break;
                case 3:
                    System.out.print("请输入新文本：");
                    String newText = scanner.nextLine();
                    System.out.println("生成的新文本：" +
                            wg.generateNewText(newText));
                    break;
                case 4:
                    System.out.print("请输入 word1:");
                    word1 = scanner.nextLine();
                    System.out.print("请输入 word2:");
                    word2 = scanner.nextLine();
                    System.out.println(wg.calcShortestPath(word1, word2));
                    break;
                case 5:
                    System.out.print("请输入单词：");
                    String word = scanner.nextLine();
                    System.out.println("PageRank 值：" +
                            wg.calPageRank(word));
                    break;
                case 6:
                    System.out.println(wg.randomWalk());
                    break;
                case 7:
                    System.out.println("程序退出...");
                    scanner.close();
                    return;
                default:
                    System.out.println("无效选项！");
            }
        }
    }
}