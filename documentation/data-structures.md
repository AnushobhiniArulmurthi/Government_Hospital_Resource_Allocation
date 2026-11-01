# Data Structures & Complexity Analysis: GHRAS

---

## 1. Overview of Data Structures Employed

The **Government Hospital Resource Allocation System (GHRAS)** leverages specialized computer science data structures to deliver deterministic, high-throughput, and sub-millisecond clinical decision-making.

| Data Structure | Implementation File | Primary Clinical / Operational Use Case |
| :--- | :--- | :--- |
| **PriorityQueue** | `HospitalPriorityQueue.java` | Consultation waiting queue and resource starvation queues ordered strictly by triage severity. |
| **Custom Min-Heap** | `MinHeap.java` | Priority queue backing Dijkstra's algorithm for nearest referral hospital route selection. |
| **Weighted Graph** | `WeightedGraph.java` | Inter-hospital regional road network representation ($V$ nodes, $E$ weighted corridors). |
| **HashMap** | `WeightedGraph.java`, Repositories | $O(1)$ adjacency list lookup, doctor and resource state caching. |
| **ArrayList** | `DijkstraRouter.java`, Services | Dynamic sequence storage for shortest road path reconstruction and batch allocations. |

---

## 2. PriorityQueue (Java & Custom Synchronized Queue)

### 2.1 Clinical Purpose
In public healthcare, First-Come-First-Served (FCFS) queuing leads to preventable patient mortality. A patient with severe cardiovascular arrest or major trauma must never wait behind patients seeking mild dermatological or routine consultations.

### 2.2 Mathematical Ordering Invariant
The `ConsultationWaitingQueueItem` and `ResourceWaitingQueueItem` implement the Java `Comparable<T>` interface. The natural ordering is governed by a two-stage comparator:

$$\text{Rank}(A, B) = \begin{cases} -1, & \text{if } \text{Severity}(A) < \text{Severity}(B) \\ +1, & \text{if } \text{Severity}(A) > \text{Severity}(B) \\ \text{Compare}(\text{ArrivalTime}(A), \text{ArrivalTime}(B)), & \text{if } \text{Severity}(A) = \text{Severity}(B) \end{cases}$$

Where Severity values are:
- `1` = Critical (Emergency / Resuscitation)
- `2` = Serious (High Risk / Urgent)
- `3` = Minor (Stable Outpatient)

Because lower numerical values indicate higher clinical emergency, a Min-Heap naturally places Severity 1 at the root ($O(1)$ peek).

### 2.3 Time & Space Complexity
- **Enqueue (`add` / `offer`):** $O(\log N)$ via standard heap sift-up.
- **Dequeue (`poll`):** $O(\log N)$ via heap sift-down.
- **Peek (`peek`):** $O(1)$ inspection of top element.
- **Space Complexity:** $O(N)$, where $N$ is the number of currently waiting patients.

---

## 3. Custom Min-Heap (`MinHeap.java`)

### 3.1 Purpose & Algorithmic Need
Standard `java.util.PriorityQueue` does not expose an efficient $O(\log V)$ `decreaseKey` operation required by Dijkstra's shortest path algorithm. Re-inserting elements without decreaseKey increases the heap size to $O(E)$ and elevates extraction to $O(\log E)$. 

To achieve optimal textbook efficiency, GHRAS includes a custom `MinHeap` maintaining an internal index lookup map `nodeIndexMap` (`HashMap<String, Integer>`) tracking the array index of every vertex.

### 3.2 Heap Operations Implementation
1. **`bubbleUp(int index)`:**
   $$\text{Parent}(i) = \lfloor \frac{i - 1}{2} \rfloor$$
   If $\text{Weight}(\text{heap}[i]) < \text{Weight}(\text{heap}[\text{Parent}(i)])$, elements are swapped and indices in `nodeIndexMap` are updated.
2. **`bubbleDown(int index)`:**
   $$\text{Left}(i) = 2i + 1, \quad \text{Right}(i) = 2i + 2$$
   Finds the smaller child and sifts down recursively while maintaining the min-heap invariant:
   $$\forall i > 0, \quad \text{Weight}(\text{heap}[\text{Parent}(i)]) \le \text{Weight}(\text{heap}[i])$$
3. **`decreaseKey(String nodeId, double newDistance)`:**
   Retrieves vertex position in $O(1)$ via `nodeIndexMap`, updates distance, and calls `bubbleUp` in $O(\log V)$.

---

## 4. Weighted Graph (`WeightedGraph.java`)

### 4.1 Graph Representation
The regional hospital road infrastructure is represented as a directed/undirected weighted graph $G = (V, E)$ using an **Adjacency List** implemented via `HashMap<String, List<Edge>>`.

```
                    [NODE_H001: Nalam District GH]
                             /          \
                     12 mins/            \ 18 mins
                           /              \
            [NODE_INT1: Bypass]        [NODE_INT2: Crossroad]
                 |            \               |
          14 mins|             \ 10 mins      | 8 mins
                 |              \             |
    [NODE_H002: Kaveri Sub]   [NODE_H003: Chengalpattu Med]
```

### 4.2 Structural Definitions
- **Vertex $v \in V$:** Represents a hospital complex (`NODE_H001`, `NODE_H002`, `NODE_H003`, `NODE_H004`) or road junction (`NODE_INT1` to `NODE_INT6`).
- **Edge $e = (u, v) \in E$:** Represents a road segment with attributes:
  - `sourceNode`: String
  - `destinationNode`: String
  - `distanceKm`: double (physical road distance)
  - `travelTimeMinutes`: double (edge weight for emergency ambulance transit)

### 4.3 Why Adjacency List over Adjacency Matrix?
For a transportation network, the graph is **sparse** ($|E| \ll |V|^2$). 
- An Adjacency Matrix would consume $O(|V|^2)$ memory and require $O(|V|)$ time to enumerate adjacent vertices for every visited node.
- The Adjacency List consumes only $O(|V| + |E|)$ memory and enumerates adjacent vertices in $O(\text{deg}(v))$, vastly accelerating Dijkstra relaxation.

---

## 5. HashMap & ArrayList Usage

### 5.1 HashMap
- **Adjacency Table:** `HashMap<String, List<Edge>>` maps vertex names to incident edges in $O(1)$ amortized lookup.
- **Distance & Parent Maps:** `HashMap<String, Double>` (shortest distances from source) and `HashMap<String, String>` (predecessors for path backtracking) ensure $O(1)$ retrieval during Dijkstra execution.
- **Resource Lookups:** Fast retrieval of inventory counters without repetitive full-table database scans.

### 5.2 ArrayList
- **Adjacency Lists:** Dynamic, cache-friendly array storage for incident edges.
- **Path Reconstruction:** Assembling the reverse traversal sequence from destination vertex back to origin vertex:
  ```java
  List<String> path = new ArrayList<>();
  for (String at = dest; at != null; at = parentMap.get(at)) {
      path.add(at);
  }
  Collections.reverse(path);
  ```

---

## 6. Comprehensive Complexity Matrix

| Data Structure / Algorithm | Operation | Average Time Complexity | Worst-Case Time Complexity | Space Complexity |
| :--- | :--- | :--- | :--- | :--- |
| **Hospital Priority Queue** | `enqueue()` | $O(\log N)$ | $O(\log N)$ | $O(N)$ |
| | `pollHighestPriority()` | $O(\log N)$ | $O(\log N)$ | $O(N)$ |
| | `peek()` | $O(1)$ | $O(1)$ | $O(N)$ |
| **Custom Min-Heap** | `insert()` | $O(\log V)$ | $O(\log V)$ | $O(V)$ |
| | `extractMin()` | $O(\log V)$ | $O(\log V)$ | $O(V)$ |
| | `decreaseKey()` | $O(\log V)$ | $O(\log V)$ | $O(V)$ |
| **Weighted Graph** | `addEdge()` | $O(1)$ | $O(1)$ | $O(V + E)$ |
| | `getAdjacentEdges()` | $O(1)$ lookup | $O(1)$ lookup | $O(\text{deg}(v))$ |
| **Dijkstra Router** | Shortest Route Run | $O((V + E) \log V)$ | $O((V + E) \log V)$ | $O(V + E)$ |
| | Path Backtracking | $O(V)$ | $O(V)$ | $O(V)$ |
