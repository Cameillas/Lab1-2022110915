import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.util.List;

public class GraphGUI extends JFrame {

    private static Map<String, Map<String, Integer>> graph; // 邻接表表示的有向带权图
    private static List<String> words; // 存储处理后的单词序列

    public static Map<String, Map<String, Integer>> getGraph() {
        return graph;
    }

    private JTextArea outputArea;
    private JTextField word1Field, word2Field, inputTextField;
    private File selectedFile = null;
    private JLabel statusLabel;

    public GraphGUI() {
        // Initialize graph and words collections
        graph = new HashMap<>();
        words = new ArrayList<>();

        // Set up the JFrame
        setTitle("Graph Analysis Tool");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        // Create main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        // Create button panel
        JPanel buttonPanel = new JPanel(new GridLayout(7, 1, 0, 5));

        // Add file selection button
        JButton loadFileButton = new JButton("Load Text File");
        loadFileButton.addActionListener(e -> selectFile());
        buttonPanel.add(loadFileButton);

        // Add function buttons
        JButton showGraphButton = new JButton("Display Graph");
        showGraphButton.addActionListener(e -> showDirectedGraph(graph));
        buttonPanel.add(showGraphButton);

        JButton bridgeWordsButton = new JButton("Show Bridge Words");
        bridgeWordsButton.addActionListener(e -> showBridgeWordsGUI());
        buttonPanel.add(bridgeWordsButton);

        JButton generateTextButton = new JButton("Generate New Text");
        generateTextButton.addActionListener(e -> generateNewTextGUI());
        buttonPanel.add(generateTextButton);

        JButton shortestPathButton = new JButton("Calculate Shortest Path");
        shortestPathButton.addActionListener(e -> calculateShortestPathGUI());
        buttonPanel.add(shortestPathButton);

        JButton pageRankButton = new JButton("Calculate PageRank");
        pageRankButton.addActionListener(e -> calculatePageRankGUI());
        buttonPanel.add(pageRankButton);

        JButton randomWalkButton = new JButton("Random Walk");
        randomWalkButton.addActionListener(e -> performRandomWalk());
        buttonPanel.add(randomWalkButton);

        JButton visualizeButton = new JButton("Save Graph as Image");
        visualizeButton.addActionListener(e -> saveGraphImage());
        buttonPanel.add(visualizeButton);

        // Create output area
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        // Create status label
        statusLabel = new JLabel("Status: Ready");
        statusLabel.setBorder(new EmptyBorder(5, 0, 0, 0));

        // Add components to main panel
        mainPanel.add(buttonPanel, BorderLayout.WEST);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(statusLabel, BorderLayout.SOUTH);

        // Add main panel to frame
        add(mainPanel);
    }

    private void selectFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Text File");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text files", "txt"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            try {
                processTextFile(selectedFile.getAbsolutePath());
                statusLabel.setText("Status: File loaded - " + selectedFile.getName());
                outputArea.setText("File loaded successfully: " + selectedFile.getName() + "\n");
                outputArea.append("Number of unique words: " + graph.size() + "\n");
                outputArea.append("Total words processed: " + words.size());
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error loading file: " + e.getMessage(),
                        "File Error", JOptionPane.ERROR_MESSAGE);
                statusLabel.setText("Status: Error loading file");
            }
        }
    }


    private void showBridgeWordsGUI() {
        if (graph.isEmpty()) {
            showNoGraphError();
            return;
        }

        // Create dialog for input
        JDialog dialog = new JDialog(this, "Bridge Words", true);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        inputPanel.add(new JLabel("Word 1:"));
        word1Field = new JTextField();
        inputPanel.add(word1Field);

        inputPanel.add(new JLabel("Word 2:"));
        word2Field = new JTextField();
        inputPanel.add(word2Field);

        JButton submitButton = new JButton("Find Bridge Words");
        submitButton.addActionListener(e -> {
            String word1 = word1Field.getText().trim();
            String word2 = word2Field.getText().trim();

            if (word1.isEmpty() || word2.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter both words",
                        "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String bridgeWords = showBridgeWords(word1, word2);
            if (bridgeWords.equals("err1")) {
                outputArea.setText("No \"" + word1 + "\" in the graph!");
            } else if(bridgeWords.equals("err2")) {
                outputArea.setText("No \"" + word2 + "\" in the graph!");
            } else if (bridgeWords.equals("err3")) {
                outputArea.setText("No bridge words from \"" + word1 + "\" to \"" + word2 + "\"!");
            } else {
                outputArea.setText("The bridge words from \"" + word1 + "\" to \"" + word2 + "\" are:\n\n" +
                        bridgeWords);
            }

            dialog.dispose();
        });

        dialog.add(inputPanel, BorderLayout.CENTER);
        dialog.add(submitButton, BorderLayout.SOUTH);
        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void generateNewTextGUI() {
        if (graph.isEmpty()) {
            showNoGraphError();
            return;
        }

        // Create dialog for input
        JDialog dialog = new JDialog(this, "Generate Text with Bridge Words", true);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        inputPanel.add(new JLabel("Input Text:"), BorderLayout.NORTH);
        inputTextField = new JTextField();
        inputPanel.add(inputTextField, BorderLayout.CENTER);

        JButton submitButton = new JButton("Generate Text");
        submitButton.addActionListener(e -> {
            String inputText = inputTextField.getText().trim();

            if (inputText.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter some text",
                        "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String newText = generateNewText(inputText);
            outputArea.setText("Original text:\n" + inputText + "\n\nGenerated text with bridge words:\n" + newText);
            statusLabel.setText("Status: New text generated");
            dialog.dispose();
        });

        dialog.add(inputPanel, BorderLayout.CENTER);
        dialog.add(submitButton, BorderLayout.SOUTH);
        dialog.setSize(400, 150);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void calculateShortestPathGUI() {
        if (graph.isEmpty()) {
            showNoGraphError();
            return;
        }

        // Create dialog for input
        JDialog dialog = new JDialog(this, "Calculate Shortest Path", true);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        inputPanel.add(new JLabel("From word:"));
        word1Field = new JTextField();
        inputPanel.add(word1Field);

        inputPanel.add(new JLabel("To word (optional):"));
        word2Field = new JTextField();
        inputPanel.add(word2Field);

        JButton submitButton = new JButton("Calculate Path");
        submitButton.addActionListener(e -> {
            String word1 = word1Field.getText().trim();

            if (word1.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter at least the starting word",
                        "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String word2 = word2Field.getText().trim();
            // word2 may be empty, that's valid for our new functionality

            String result = calcShortestPath(word1, word2);
            outputArea.setText(result);
            statusLabel.setText("Status: Shortest path(s) calculated");
            dialog.dispose();
        });

        dialog.add(inputPanel, BorderLayout.CENTER);
        dialog.add(submitButton, BorderLayout.SOUTH);
        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }


    private void calculatePageRankGUI() {
        if (graph.isEmpty()) {
            showNoGraphError();
            return;
        }

        // Create dialog for input
        JDialog dialog = new JDialog(this, "Calculate PageRank", true);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel inputPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        inputPanel.add(new JLabel("Word:"));
        word1Field = new JTextField();
        inputPanel.add(word1Field);

        JButton submitButton = new JButton("Calculate PageRank");
        submitButton.addActionListener(e -> {
            String word = word1Field.getText().trim();

            if (word.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter a word",
                        "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Double prValue = calPageRank(word);
            outputArea.setText("PageRank of \"" + word + "\": " + String.format("%.6f", prValue));
            statusLabel.setText("Status: PageRank calculated");
            dialog.dispose();
        });

        dialog.add(inputPanel, BorderLayout.CENTER);
        dialog.add(submitButton, BorderLayout.SOUTH);
        dialog.setSize(300, 120);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

//    private void performRandomWalk() {
//        if (graph.isEmpty()) {
//            showNoGraphError();
//            return;
//        }
//
//        String result = randomWalk();
//        outputArea.setText(result);
//        statusLabel.setText("Status: Random walk completed");
//    }

    private void performRandomWalk() {
        if (graph.isEmpty()) {
            showNoGraphError();
            return;
        }
        String result = randomWalk();
        outputArea.setText(result);
        statusLabel.setText("Status: Random walk completed");
        // 添加文件保存功能
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Random Walk Result");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Text files", "txt"));
        int userChoice = fileChooser.showSaveDialog(this);
        if (userChoice == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();

            // 确保文件扩展名为.txt
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith(".txt")) {
                fileToSave = new File(filePath + ".txt");
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave))) {
                writer.write(result);
                statusLabel.setText("Status: Random walk saved to " + fileToSave.getName());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage(),
                        "Save Error", JOptionPane.ERROR_MESSAGE);
                statusLabel.setText("Status: Error saving random walk");
            }
        }
    }

    private void saveGraphImage() {
        if (graph.isEmpty()) {
            showNoGraphError();
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Graph Image");
        fileChooser.setFileFilter(new FileNameExtensionFilter("PNG images", "png"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String filePath = file.getAbsolutePath();

            // Ensure filename ends with .png
            if (!filePath.toLowerCase().endsWith(".png")) {
                filePath += ".png";
            }

            try {
                String imagePath = GraphVisualizer.visualizeDirectedGraph(
                        graph,
                        filePath.substring(0, filePath.lastIndexOf('.')),
                        "png"
                );
                outputArea.setText("Graph image saved to: " + imagePath);
                statusLabel.setText("Status: Graph image saved");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error saving graph image: " + e.getMessage(),
                        "Save Error", JOptionPane.ERROR_MESSAGE);
                statusLabel.setText("Status: Error saving graph image");
            }
        }
    }

    private void showNoGraphError() {
        JOptionPane.showMessageDialog(this, "Please load a text file first",
                "No Graph", JOptionPane.WARNING_MESSAGE);
    }

    /**
    * 处理文本文件，提取文本内容，去除换行符，保留字母
     * @param filePath 文件路径
     */
    public static void processTextFile(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // 将换行符替换为空格
                content.append(line).append(" ");
            }
        }

        // 处理文本：去除标点，只保留字母，转换为小写
        String processedText = content.toString().replaceAll("[^a-zA-Z]", " ").toLowerCase();
        String[] wordArray = processedText.split("\\s+");

        words = new ArrayList<>(Arrays.asList(wordArray));
        words.removeIf(String::isEmpty); // 移除空字符串

        buildDirectedWeightedGraph();
    }

    /**
     * 构建加权有向图
     */
    public static void buildDirectedWeightedGraph() {
        graph.clear();

        for (int i = 0; i < words.size() - 1; i++) {
            String current = words.get(i);
            String next = words.get(i + 1);

            // 更新或创建边及其权重
            graph.putIfAbsent(current, new HashMap<>());
            Map<String, Integer> edges = graph.get(current);
            edges.put(next, edges.getOrDefault(next, 0) + 1);
        }
    }

    /**
     * 要求函数1：展示有向图
     * @param G 处理文件得到的有向图
     */
    private void showDirectedGraph(Map<String, Map<String, Integer>> G) {
        if (G.isEmpty()) {
            showNoGraphError();
            return;
        }

        outputArea.setText("");
        outputArea.append("Directed Weighted Graph Structure:\n\n");

        // Sort the entries alphabetically for better readability
        List<Map.Entry<String, Map<String, Integer>>> sortedEntries =
                new ArrayList<>(G.entrySet());
        Collections.sort(sortedEntries,
                Comparator.comparing(Map.Entry::getKey));

        for (Map.Entry<String, Map<String, Integer>> entry : sortedEntries) {
            outputArea.append(entry.getKey() + " -> ");
            List<String> edges = new ArrayList<>();

            // Sort edges alphabetically
            List<Map.Entry<String, Integer>> sortedEdges =
                    new ArrayList<>(entry.getValue().entrySet());
            Collections.sort(sortedEdges,
                    Comparator.comparing(Map.Entry::getKey));

            for (Map.Entry<String, Integer> edge : sortedEdges) {
                edges.add(edge.getKey() + "(" + edge.getValue() + ")");
            }
            outputArea.append(String.join(", ", edges) + "\n");
        }

        statusLabel.setText("Status: Graph displayed");
    }

    /**
     * 要求函数2：查询桥接词
     * @param word1 带查询的词语1
     * @param word2 带查询的词语2
     * @return bridgeWords 形如桥接词1, 桥接词2, ... 的桥接词列表
     */
    public static String showBridgeWords(String word1, String word2) {
        word1 = word1.toLowerCase();
        word2 = word2.toLowerCase();

        if (!graph.containsKey(word1) || !graph.containsKey(word2)) {
            if (!graph.containsKey(word1)) {
//                System.out.println("No " + word1 + " in the graph!");
                return "err1";
            }
            else
            {
//                System.out.println("No " + word2 + " in the graph!");
                return "err2";
            }
        }

        Set<String> bridgeWords = new HashSet<>();
        Map<String, Integer> neighbors1 = graph.get(word1);

        for (String neighbor : neighbors1.keySet()) {
            if (graph.containsKey(neighbor) && graph.get(neighbor).containsKey(word2)) {
                bridgeWords.add(neighbor);
            }
        }

        if (bridgeWords.isEmpty()) {
            return "err3";
        } else {
            return String.join(", ", bridgeWords);
        }
    }

    /**
     * 要求函数3：根据桥接词生成新文本
     * @param inputText 输入文本
     * @return 输入文本和输出文本拼接结果
     */
    public static String generateNewText(String inputText) {
        String[] inputWords = inputText.toLowerCase().split("\\s+");
        List<String> result = new ArrayList<>();

        for (int i = 0; i < inputWords.length - 1; i++) {
            result.add(inputWords[i]);

            String word1 = inputWords[i];
            String word2 = inputWords[i + 1];

            if (graph.containsKey(word1) && graph.containsKey(word2)) {
                Set<String> bridgeWords = new HashSet<>();
                Map<String, Integer> neighbors1 = graph.get(word1);

                for (String neighbor : neighbors1.keySet()) {
                    if (graph.containsKey(neighbor) && graph.get(neighbor).containsKey(word2)) {
                        bridgeWords.add(neighbor);
                    }
                }

                if (!bridgeWords.isEmpty()) {
                    // 随机选择一个桥接词
                    String[] bridges = bridgeWords.toArray(new String[0]);
                    String selected = bridges[new Random().nextInt(bridges.length)];
                    result.add(selected);
                }
            }
        }

        result.add(inputWords[inputWords.length - 1]);
        return String.join(" ", result);
    }

    /**
     * 要求函数4：计算最短路径
     * @param word1 用于查找最短路径的词1
     * @param word2 用于查找最短路径的词2（如果为空，则计算word1到所有其他单词的最短路径）
     * @return 最短路径，可以直接用于输出
     */
    public static String calcShortestPath(String word1, String word2) {
        word1 = word1.toLowerCase();
        // 检查word1是否在图中
        if (!graph.containsKey(word1)) {
            return "Word \"" + word1 + "\" not in the graph!";
        }

        // 如果word2为空或为空字符串，表示计算word1到所有单词的最短路径
        boolean findAllPaths = word2 == null || word2.trim().isEmpty();

        if (!findAllPaths) {
            word2 = word2.toLowerCase();
            if (!graph.containsKey(word2)) {
                return "Word \"" + word2 + "\" not in the graph!";
            }
        }

        // 初始化距离和前驱节点
        Map<String, Integer> distances = new HashMap<>();
        Map<String, List<String>> predecessors = new HashMap<>();

        for (String node : graph.keySet()) {
            distances.put(node, Integer.MAX_VALUE);
            predecessors.put(node, new ArrayList<>());
        }
        distances.put(word1, 0);

        // 使用优先队列，比较器需要防止null值
        PriorityQueue<String> queue = new PriorityQueue<>(
                (a, b) -> {
                    Integer distA = distances.get(a);
                    Integer distB = distances.get(b);
                    if (distA == null) distA = Integer.MAX_VALUE;
                    if (distB == null) distB = Integer.MAX_VALUE;
                    return distA.compareTo(distB);
                }
        );
        queue.add(word1);

        Set<String> processed = new HashSet<>();

        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (processed.contains(current)) continue;
            processed.add(current);

            // 如果我们只找特定单词的路径并且已经找到了，可以提前终止
            if (!findAllPaths && current.equals(word2)) break;

            // 获取当前节点的距离，确保不为null
            Integer currentDistance = distances.get(current);
            if (currentDistance == null || currentDistance == Integer.MAX_VALUE) continue;

            // 遍历所有邻居
            Map<String, Integer> neighbors = graph.get(current);
            if (neighbors == null) continue;

            for (Map.Entry<String, Integer> neighborEntry : neighbors.entrySet()) {
                String neighbor = neighborEntry.getKey();
                Integer weight = neighborEntry.getValue();

                // 确保权重不为null
                if (weight == null) continue;

                int alt = currentDistance + weight;
                Integer neighborDistance = distances.get(neighbor);

                // 确保邻居距离不为null
                if (neighborDistance == null) {
                    neighborDistance = Integer.MAX_VALUE;
                    distances.put(neighbor, neighborDistance);
                }

                if (alt < neighborDistance) {
                    // 找到了更短的路径
                    distances.put(neighbor, alt);
                    List<String> predecessorList = predecessors.get(neighbor);
                    if (predecessorList == null) {
                        predecessorList = new ArrayList<>();
                        predecessors.put(neighbor, predecessorList);
                    } else {
                        predecessorList.clear();
                    }
                    predecessorList.add(current);
                    queue.add(neighbor);
                } else if (alt == neighborDistance) {
                    // 找到了等长的路径
                    List<String> predecessorList = predecessors.get(neighbor);
                    if (predecessorList == null) {
                        predecessorList = new ArrayList<>();
                        predecessors.put(neighbor, predecessorList);
                    }
                    predecessorList.add(current);
                }
            }
        }

        StringBuilder result = new StringBuilder();

        // 如果是找所有路径
        if (findAllPaths) {
            result.append("Shortest paths from \"").append(word1).append("\" to all other words:\n\n");

            List<Map.Entry<String, Integer>> sortedPaths = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : distances.entrySet()) {
                if (entry.getValue() != null && entry.getValue() != Integer.MAX_VALUE) {
                    sortedPaths.add(entry);
                }
            }
            sortedPaths.sort(Map.Entry.comparingByValue());

            for (Map.Entry<String, Integer> entry : sortedPaths) {
                String target = entry.getKey();
                Integer distance = entry.getValue();

                // 跳过起始点和不可达点
                if (target.equals(word1) || distance == null || distance == Integer.MAX_VALUE) continue;

                result.append("To \"").append(target).append("\" (distance: ").append(distance).append("):\n");

                // 获取所有可能的最短路径
                List<List<String>> allPaths = getAllPaths(predecessors, word1, target);

                for (int i = 0; i < allPaths.size(); i++) {
                    List<String> path = allPaths.get(i);
                    result.append("  Path ").append(i + 1).append(": ").append(String.join(" -> ", path)).append("\n");
                }
                result.append("\n");
            }
        } else {
            // 单一目标单词的路径
            Integer targetDistance = distances.get(word2);
            if (targetDistance == null || targetDistance == Integer.MAX_VALUE) {
                return "No path from \"" + word1 + "\" to \"" + word2 + "\"!";
            }

            result.append("Shortest path from \"").append(word1).append("\" to \"").append(word2).append("\":\n");
            result.append("Distance: ").append(targetDistance).append("\n\n");

            // 获取所有可能的最短路径
            List<List<String>> allPaths = getAllPaths(predecessors, word1, word2);

            for (int i = 0; i < allPaths.size(); i++) {
                List<String> path = allPaths.get(i);
                result.append("Path ").append(i + 1).append(": ").append(String.join(" -> ", path)).append("\n");
            }
        }

        return result.toString();
    }

    /**
     * 使用回溯法获取所有可能的最短路径
     */
    private static List<List<String>> getAllPaths(Map<String, List<String>> predecessors,
                                                  String start, String end) {
        List<List<String>> result = new ArrayList<>();

        // 使用DFS递归构建所有路径
        findAllPaths(predecessors, result, new ArrayList<>(), end, start);

        return result;
    }

    /**
     * 递归辅助方法 - 回溯法找出所有路径
     */
    private static void findAllPaths(Map<String, List<String>> predecessors,
                                     List<List<String>> result,
                                     List<String> currentPath,
                                     String current,
                                     String start) {
        // 添加当前节点
        currentPath.add(0, current);

        // 基本情况：如果当前节点是起点
        if (current.equals(start)) {
            result.add(new ArrayList<>(currentPath));
        } else {
            // 获取前驱列表，确保不为null
            List<String> predecessorList = predecessors.get(current);
            if (predecessorList != null) {
                // 递归检查所有前驱节点
                for (String predecessor : predecessorList) {
                    findAllPaths(predecessors, result, currentPath, predecessor, start);
                }
            }
        }

        // 回溯
        currentPath.remove(0);
    }


    /**
     * 要求函数5：计算单词的PageRank值
     * @param word 要查询的单词
     * @return 该单词的PageRank值
     */
    public static Double calPageRank(String word) {
        word = word.toLowerCase();

        // 参数设置
        final double d = 0.85; // 阻尼系数
        final int maxIterations = 100; // 最大迭代次数
        final double tolerance = 1e-4; // 收敛阈值

        // 初始化PR值
        Map<String, Double> prValues = new HashMap<>();
        int numNodes = graph.size();
//        double initialValue = 1.0 / numNodes;
//
//        for (String node : graph.keySet()) {
//            prValues.put(node, initialValue);
//        }

        // 进行随机游走统计访问频次
        Map<String, Integer> visitCounts = new HashMap<>();
        Random rand = new Random();
        List<String> nodes = new ArrayList<>(graph.keySet());
        String currentNode = nodes.get(rand.nextInt(nodes.size()));

        for (int i = 0; i < 10000; i++) {
            visitCounts.put(currentNode, visitCounts.getOrDefault(currentNode, 0) + 1);

            // 检查当前节点是否在图中，以及该节点是否有出边
            if (!graph.containsKey(currentNode) || graph.get(currentNode).isEmpty()) {
                // 如果当前节点不在图中或没有出边，随机选择一个新节点
                currentNode = nodes.get(rand.nextInt(nodes.size()));
                continue;
            }

            // 安全地获取邻居节点
            Map<String, Integer> currentNeighbors = graph.get(currentNode);
            List<String> neighbors = new ArrayList<>(currentNeighbors.keySet());

            if (neighbors.isEmpty()) {
                // 如果没有邻居，随机选择一个新节点
                currentNode = nodes.get(rand.nextInt(nodes.size()));
                continue;
            }

            currentNode = neighbors.get(rand.nextInt(neighbors.size()));
        }

        // 归一化为初始PR值
        double sumPre = visitCounts.values().stream().mapToInt(Integer::intValue).sum();
        for (String node : graph.keySet()) {
            System.out.println(node + " -> " + visitCounts.get(node));
            prValues.put(node, visitCounts.getOrDefault(node, 0) / sumPre);
        }


        // PageRank迭代计算
        for (int i = 0; i < maxIterations; i++) {
            Map<String, Double> newPrValues = new HashMap<>();
            double danglingSum = 0.0; // 处理悬挂节点

            // 计算悬挂节点贡献
            for (String node : graph.keySet()) {
                if (graph.get(node).isEmpty()) {
                    danglingSum += prValues.get(node);
                }
            }

            // 计算每个节点的新PR值
            for (String p : graph.keySet()) {
                double sum = 0.0;

                // 计算所有指向p的节点的贡献
                for (String q : graph.keySet()) {
                    if (graph.get(q).containsKey(p)) {
                        sum += prValues.get(q) / graph.get(q).size();
                    }
                }

                // 添加悬挂节点的贡献
                sum += danglingSum / numNodes;

                // 应用PageRank公式
                newPrValues.put(p, (1 - d) / numNodes + d * sum);
            }

            // 检查是否收敛
            boolean converged = true;
            for (String node : graph.keySet()) {
                if (Math.abs(newPrValues.get(node) - prValues.get(node)) > tolerance) {
                    converged = false;
                    break;
                }
            }

            prValues = newPrValues;
            if (converged) {
                break;
            }
        }

        return prValues.getOrDefault(word, 0.0);
    }

    /**
     * 要求函数6：随机游走
     * @return 随机游走路径信息
     */
    public static String randomWalk() {
        if (graph.isEmpty()) {
            return "Graph is empty!";
        }

        Random random = new Random();
        List<String> nodes = new ArrayList<>(graph.keySet());
        String current = nodes.get(random.nextInt(nodes.size()));
        Set<String> visitedEdges = new HashSet<>();
        List<String> path = new ArrayList<>();
        path.add(current);

        while (true) {
            Map<String, Integer> neighbors = graph.get(current);
            if (neighbors == null || neighbors.isEmpty()) {
                break;
            }

            // 根据权重选择下一个节点
            int totalWeight = neighbors.values().stream().mapToInt(Integer::intValue).sum();
            int randomValue = random.nextInt(totalWeight);
            int cumulativeWeight = 0;
            String next = null;

            for (Map.Entry<String, Integer> entry : neighbors.entrySet()) {
                cumulativeWeight += entry.getValue();
                if (randomValue < cumulativeWeight) {
                    next = entry.getKey();
                    break;
                }
            }

            if (next == null) {
                break;
            }

            String edge = current + "->" + next;


            path.add(next);
            current = next;

            if (visitedEdges.contains(edge)) {
                break;
            }
            visitedEdges.add(edge);


        }

        return "Random walk: " + String.join(" -> ", path);
    }

    // 项目主函数
    public static void main(String[] args) {

        // Set Nimbus look and feel if available for better appearance
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // If Nimbus is not available, use the default look and feel
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        // Create and display the GUI
        SwingUtilities.invokeLater(() -> {
            GraphGUI gui = new GraphGUI();
            gui.setVisible(true);
        });
    }
}
