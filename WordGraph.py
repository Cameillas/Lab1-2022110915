import sys
import random
from collections import defaultdict
import heapq
import os
import networkx as nx
import matplotlib.pyplot as plt


class WordGraph:
    def __init__(self):
        self.graph = defaultdict(lambda: defaultdict(int))
        self.random = random.Random()

    # 读取并处理文本文件以构建图
    def build_graph(self, file_path):
        self.graph.clear()
        with open(file_path, 'r', encoding='utf-8') as file:
            text = file.read().replace('\n', ' ')

        # 处理文本：转换为小写，将非字母字符替换为空格
        processed_text = text.lower()
        processed_text = ''.join(
            c if c.isalpha() or c.isspace() else ' ' for c in processed_text)
        processed_text = ' '.join(processed_text.split())

        words = processed_text.split()

        # 构建有向图
        for i in range(len(words) - 1):
            word1, word2 = words[i], words[i + 1]
            if word1 and word2:
                self.graph[word1][word2] += 1

    # 使用 DFS 遍历图，计算每个节点的层次
    def _dfs_for_display(self, G, node, level, levels, visited):
        if node in visited:
            return
        visited.add(node)
        levels[node] = level
        if node in G:
            for next_node in G[node]:
                self._dfs_for_display(G, next_node, level + 1, levels, visited)

    # 展示有向图，并使用 networkx 和 matplotlib 绘制图形
    def show_directed_graph(self, G):
        # 计算每个节点的入度，用于确定起始节点
        in_degree = defaultdict(int)
        for word in G:
            in_degree[word] = in_degree.get(word, 0)
            for target in G[word]:
                in_degree[target] += 1

        # 找到入度为 0 的节点作为起始点，若无则随机选择一个节点
        start_node = next(iter(G))
        for node, degree in in_degree.items():
            if degree == 0:
                start_node = node
                break

        # 使用 DFS 遍历图，记录访问顺序和层次
        levels = {}
        visited = set()
        self._dfs_for_display(G, start_node, 0, levels, visited)

        # 在 CLI 上展示有向图（层次化格式）
        print("有向图（层次化展示）：")
        nodes = sorted(G.keys(), key=lambda x: levels.get(x, 0))
        for node in nodes:
            level = levels.get(node, 0)
            print("  " * level + node)
            if node in G:
                for target, weight in G[node].items():
                    print("  " * (level + 1) + f"--> {target} (权重: {weight})")

        # 使用 networkx 创建有向图
        G_nx = nx.DiGraph()
        for word1 in G:
            for word2, weight in G[word1].items():
                G_nx.add_edge(word1, word2, weight=weight)

        # 使用 spring_layout 布局
        pos = nx.spring_layout(G_nx)

        # 绘制图形
        plt.figure(figsize=(10, 8))
        nx.draw(G_nx, pos, with_labels=True, node_color='lightblue',
                node_size=2000, font_size=12, font_weight='bold',
                arrows=True, arrowstyle='->', arrowsize=20)

        # 添加边权重标签
        edge_labels = nx.get_edge_attributes(G_nx, 'weight')
        nx.draw_networkx_edge_labels(G_nx, pos, edge_labels=edge_labels)

        # 保存为图像文件
        plt.savefig("graph.png", format="png", bbox_inches="tight")
        print("有向图已保存为 graph.png 文件。")
        plt.close()

    # 查询桥接词
    def query_bridge_words(self, word1, word2):
        word1 = word1.lower()
        word2 = word2.lower()

        if word1 not in self.graph or word2 not in self.graph:
            return "图中不存在 word1 或 word2!"

        bridge_words = []
        for word3 in self.graph:
            if word3 in self.graph[word1] and word2 in self.graph[word3]:
                bridge_words.append(word3)

        if not bridge_words:
            return f"从 {word1} 到 {word2} 不存在桥接词！"

        result = f"从 {word1} 到 {word2} 的桥接词为："
        if len(bridge_words) == 1:
            result += f"{bridge_words[0]}。"
        elif len(bridge_words) == 2:
            result += f"{bridge_words[0]} 和 {bridge_words[1]}。"
        else:
            result += ", ".join(bridge_words[:-1]) + f" 和 {bridge_words[-1]}。"
        return result

    # 根据桥接词生成新文本
    def generate_new_text(self, input_text):
        words = input_text.lower()
        words = ''.join(c if c.isalpha() or c.isspace()
                        else ' ' for c in words)
        words = ' '.join(words.split()).split()

        if len(words) < 2:
            return input_text

        result = [words[0]]
        for i in range(len(words) - 1):
            word1, word2 = words[i], words[i + 1]
            bridge_words = []
            if word1 in self.graph:
                for word3 in self.graph:
                    if word3 in self.graph[word1] and word2 in self.graph[word3]:
                        bridge_words.append(word3)

            if bridge_words:
                bridge = random.choice(bridge_words)
                result.append(bridge)
            result.append(word2)

        return " ".join(result)

    # 计算两个单词之间的最短路径
    def calc_shortest_path(self, word1, word2):
        word1 = word1.lower()
        word2 = word2.lower()
        # 收集所有节点（包括只有入边没有出边的节点）
        all_nodes = set(self.graph.keys())
        for node in self.graph:
            all_nodes.update(self.graph[node].keys())
        if word1 not in self.graph or word2 not in self.graph:
            return f"图中不存在 {word1} 或 {word2}!"

        distances = {node: float('inf') for node in all_nodes}
        distances[word1] = 0
        predecessors = {word: None for word in self.graph}
        pq = [(0, word1)]

        while pq:
            dist, current = heapq.heappop(pq)
            if current == word2:
                break
            if dist > distances[current]:
                continue
            if current in self.graph:
                for neighbor, weight in self.graph[current].items():
                    new_dist = distances[current] + weight
                    if new_dist < distances[neighbor]:
                        distances[neighbor] = new_dist
                        predecessors[neighbor] = current
                        heapq.heappush(pq, (new_dist, neighbor))

        if distances[word2] == float('inf'):
            return f"从 {word1} 到 {word2} 不可达！"

        # 重构路径
        path = []
        current = word2
        while current is not None:
            path.append(current)
            current = predecessors[current]
        path.reverse()

        # 在图中高亮显示路径
        print(f"从 {word1} 到 {word2} 的最短路径：")
        for word in self.graph:
            for next_word, weight in self.graph[word].items():
                is_path_edge = False
                for i in range(len(path) - 1):
                    if path[i] == word and path[i + 1] == next_word:
                        is_path_edge = True
                        break
                print(f"{word} -> {next_word} (权重: {weight})" +
                      (" [路径]" if is_path_edge else ""))

        return f"路径: {' -> '.join(path)}\n长度: {distances[word2]}"

    # 计算 PageRank 值
    def cal_page_rank(self, word):
        word = word.lower()
        if word not in self.graph:
            return 0.0

        N = len(self.graph)
        pr = {w: 1.0 / N for w in self.graph}
        d = 0.85

        # 迭代计算直到收敛（简化为固定迭代次数）
        for _ in range(100):
            new_pr = {}
            for w in self.graph:
                total = 0
                for in_node in self.graph:
                    if w in self.graph[in_node]:
                        out_degree = len(self.graph[in_node])
                        total += pr[in_node] / out_degree
                new_pr[w] = (1 - d) / N + d * total
            pr = new_pr

        return pr[word]

    # 随机游走
    def random_walk(self):
        if not self.graph:
            return "图为空！"

        nodes = list(self.graph.keys())
        current = random.choice(nodes)
        visited_nodes = [current]
        visited_edges = []
        edge_set = set()

        print("随机游走开始。按回车继续，输入 'q' 退出。")
        while current in self.graph and self.graph[current]:
            user_input = input()
            if user_input.lower() == 'q':
                break

            neighbors = list(self.graph[current].keys())
            next_node = random.choice(neighbors)

            edge = f"{current}->{next_node}"
            if edge in edge_set:
                visited_nodes.append(next_node)
                visited_edges.append(edge)
                break

            visited_nodes.append(next_node)
            visited_edges.append(edge)
            edge_set.add(edge)
            current = next_node

            print(f"当前节点: {current}")

        # 保存到文件
        try:
            with open("random_walk.txt", "w", encoding="utf-8") as f:
                f.write(f"节点: {' -> '.join(visited_nodes)}\n")
                f.write(f"边: {', '.join(visited_edges)}")
        except IOError as e:
            print(f"写入文件错误: {e}")

        return f"节点: {' -> '.join(visited_nodes)}\n边: {', '.join(visited_edges)}"


def main():
    wg = WordGraph()

    # 获取文件路径
    file_path = sys.argv[1] if len(sys.argv) > 1 else input("请输入文本文件路径：")

    try:
        wg.build_graph(file_path)
    except IOError as e:
        print(f"读取文件错误: {e}")
        return

    while True:
        print("\n菜单:")
        print("1. 展示有向图")
        print("2. 查询桥接词")
        print("3. 生成新文本")
        print("4. 计算最短路径")
        print("5. 计算 PageRank")
        print("6. 随机游走")
        print("7. 退出")
        choice = input("请选择功能：")

        try:
            choice = int(choice)
        except ValueError:
            print("无效输入！")
            continue

        if choice == 1:
            wg.show_directed_graph(wg.graph)
        elif choice == 2:
            word1 = input("请输入 word1:")
            word2 = input("请输入 word2:")
            print(wg.query_bridge_words(word1, word2))
        elif choice == 3:
            new_text = input("请输入新文本：")
            print("生成的新文本：" + wg.generate_new_text(new_text))
        elif choice == 4:
            word1 = input("请输入 word1:")
            word2 = input("请输入 word2:")
            print(wg.calc_shortest_path(word1, word2))
        elif choice == 5:
            word = input("请输入单词：")
            print(f"PageRank 值：{wg.cal_page_rank(word)}")
        elif choice == 6:
            print(wg.random_walk())
        elif choice == 7:
            print("程序退出...")
            break
        else:
            print("无效选项！")


if __name__ == "__main__":
    main()
   