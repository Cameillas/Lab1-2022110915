import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class GraphVisualizer {

    /**
     * 使用Graphviz生成有向图图形文件
     * @param graph 有向图结构(Map<String, Map<String, Integer>>)
     * @param outputPath 输出文件路径(不含扩展名)
     * @param format 图像格式(png, pdf, svg等)
     * @return 生成的图像文件路径
     * @throws IOException
     * @throws InterruptedException
     */
    public static String visualizeDirectedGraph(
            Map<String, Map<String, Integer>> graph,
            String outputPath,
            String format) throws IOException, InterruptedException {

        // 1. 生成DOT文件
        String dotFilePath = outputPath + ".dot";
        generateDotFile(graph, dotFilePath);

        // 2. 调用Graphviz生成图像
        String imageFilePath = outputPath + "." + format;
        generateImage(dotFilePath, imageFilePath, format);

        return imageFilePath;
    }

    private static void generateDotFile(
            Map<String, Map<String, Integer>> graph,
            String dotFilePath
    ) throws IOException {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dotFilePath))) {
            writer.write("digraph G {\n");
            writer.write("  rankdir=LR;\n"); // 从左到右布局
            writer.write("  node [shape=circle];\n\n");

            // 添加所有节点
            for (String node : graph.keySet()) {
                writer.write("  \"" + node + "\";\n");
            }

            writer.write("\n");

            // 添加所有边
            for (Map.Entry<String, Map<String, Integer>> entry : graph.entrySet()) {
                String source = entry.getKey();
                for (Map.Entry<String, Integer> edge : entry.getValue().entrySet()) {
                    String target = edge.getKey();
                    int weight = edge.getValue();
                    writer.write("  \"" + source + "\" -> \"" + target +
                            "\" [label=\"" + weight + "\"];\n");
                }
            }

            writer.write("}\n");
        }
    }

    private static void generateImage(
            String dotFilePath,
            String outputImagePath,
            String format
    ) throws IOException, InterruptedException {

        // 检查Graphviz是否安装
        String graphvizPath = "dot"; // 默认在PATH中
        try {
            Process process = Runtime.getRuntime().exec(graphvizPath + " -V");
            process.waitFor();
        } catch (Exception e) {
            throw new IOException("Graphviz (dot) not found. Please install Graphviz first.");
        }

        // 执行dot命令生成图像
        String command = String.format("%s -T%s %s -o %s",
                graphvizPath, format, dotFilePath, outputImagePath);

        Process process = Runtime.getRuntime().exec(command);
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new IOException("Graphviz execution failed with exit code: " + exitCode);
        }
    }
}

