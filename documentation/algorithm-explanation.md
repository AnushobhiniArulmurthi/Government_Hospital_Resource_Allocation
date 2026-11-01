# Algorithmic Analysis: Dijkstra's Algorithm for Hospital Referrals

---

## 1. Algorithmic Overview & Referral Objective

When a district government hospital (such as Nalam District GH, `H-001`) experiences complete saturation of specialized clinical resources—such as zero remaining Intensive Care Unit (ICU) beds, emergency trauma bays, or specialized surgical suites—deteriorating patients must be immediately transferred to an affiliated public facility.

Rather than making arbitrary, ad-hoc phone calls or transferring patients blindly to the closest geographical hospital (which might lack an on-duty specialist or available ICU bed), GHRAS implements a two-stage routing engine:
1. **Stage 1: Clinical Eligibility Pre-Filtering**
2. **Stage 2: Shortest Travel-Time Route Computation via Dijkstra's Algorithm**

---

## 2. Stage 1: Clinical Eligibility Pre-Filtering

Before any graph routing algorithm is executed, the system evaluates all candidate destination hospitals $H_{cand} = \{H_2, H_3, \dots, H_k\}$ against strict clinical predicates:

$$\text{Eligible}(H) \iff \begin{cases}
\text{AvailableBeds}(H, \text{RequiredBedType}) > 0 \\
\land \; \text{HasDoctorOnDuty}(H, \text{RequiredSpecialty}) = \text{TRUE} \\
\land \; \forall r \in \text{RequiredResources}, \; \text{AvailableQty}(H, r) \ge \text{RequestedQty}(r)
\end{cases}$$

If a hospital fails any of these criteria (e.g. zero ICU beds available), it is pruned from the candidate set regardless of how close it is geographically. Only hospitals satisfying all clinical prerequisites are admitted to Stage 2.

---

## 3. Stage 2: Dijkstra's Algorithm on Road Corridors

### 3.1 Mathematical Formulation
Let the regional road network be represented as a weighted graph $G = (V, E)$, where:
- $V$ is the set of vertices (hospitals and road junctions).
- $E$ is the set of directed/undirected edges (corridors connecting vertices).
- $w(u, v) \ge 0$ is the non-negative edge weight representing **travel time in minutes** for an emergency ambulance.

### 3.2 State Variables
- $D[v]$: Array of shortest known travel times from source $s$ to vertex $v$.
- $P[v]$: Predecessor map storing the prior vertex in the optimal path to $v$.
- $S$: Set of visited vertices whose shortest travel times have been finalized.
- $Q$: Min-Priority Queue (implemented via `MinHeap.java`) storing vertices keyed by $D[v]$.

### 3.3 Algorithm Pseudocode
```text
Algorithm DijkstraShortestPath(G, source, eligibleDestinations):
    Input: Graph G = (V, E, w), source vertex s, set of target vertices T
    Output: Optimal target t* in T, minimum travel time D[t*], and shortest path sequence

    1. For each vertex v in V:
           D[v] = +infinity
           P[v] = null
    2. D[s] = 0
    3. Initialize MinHeap Q with all v in V, keyed by D[v]
    
    4. While Q is not empty:
           u = Q.extractMin()
           If D[u] == +infinity:
               Break // Remaining vertices are unreachable
               
           For each outgoing edge (u, v) in Adj[u]:
               // Relaxation Step:
               If D[u] + w(u, v) < D[v]:
                   D[v] = D[u] + w(u, v)
                   P[v] = u
                   Q.decreaseKey(v, D[v])
                   
    5. Find t* in T such that D[t*] is minimized:
           t* = argmin_{t in T} (D[t])
           
    6. Reconstruct optimal path sequence from s to t* using P[]:
           curr = t*
           path = empty list
           While curr is not null:
               path.prepend(curr)
               curr = P[curr]
               
    7. Return RouteResult(destination=t*, travelTime=D[t*], path=path)
```

---

## 4. Step-by-Step Numerical Walkthrough

Consider a real case study from our regional road network:
- **Origin ($s$):** `NODE_H001` (Nalam District GH)
- **Patient Requirement:** Critical STEMI (Acute Coronary Syndrome) requiring emergency ICU bed and Cardiologist.
- **Candidate Hospitals:**
  - `H-002` (Kaveri Sub-District Hospital) $\rightarrow$ **Ineligible** (0 ICU beds available).
  - `H-003` (Chengalpattu Medical College) $\rightarrow$ **Eligible** (4 ICU beds, Cardiologist Dr. Radhakrishnan on duty).
  - `H-004` (Kanchipuram District Hospital) $\rightarrow$ **Eligible** (2 ICU beds, Cardiologist Dr. Srinivasan on duty).

### Road Network Corridors & Weights
- Edge(`NODE_H001`, `NODE_INT1`) = $12\text{ mins}$, $8\text{ km}$
- Edge(`NODE_H001`, `NODE_INT2`) = $18\text{ mins}$, $14\text{ km}$
- Edge(`NODE_INT1`, `NODE_INT3`) = $10\text{ mins}$, $9\text{ km}$
- Edge(`NODE_INT3`, `NODE_H003`) = $14\text{ mins}$, $11\text{ km}$
- Edge(`NODE_INT2`, `NODE_INT4`) = $15\text{ mins}$, $12\text{ km}$
- Edge(`NODE_INT4`, `NODE_H004`) = $20\text{ mins}$, $16\text{ km}$

### Iteration Table

| Iteration | Extracted Vertex $u$ | Known Shortest Time $D[u]$ | Adjacent Vertex $v$ | Edge Weight $w(u,v)$ | New Candidate Distance $D[u] + w$ | Relaxation Action | Updated Min-Heap Key |
| :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **0** | — | — | `NODE_H001` | — | $0$ | Initialization | $D[\text{H001}] = 0$ |
| **1** | `NODE_H001` | $0$ | `NODE_INT1` | $12$ | $0 + 12 = 12$ | Relaxed | $D[\text{INT1}] = 12$ |
| | | | `NODE_INT2` | $18$ | $0 + 18 = 18$ | Relaxed | $D[\text{INT2}] = 18$ |
| **2** | `NODE_INT1` | $12$ | `NODE_INT3` | $10$ | $12 + 10 = 22$ | Relaxed | $D[\text{INT3}] = 22$ |
| **3** | `NODE_INT2` | $18$ | `NODE_INT4` | $15$ | $18 + 15 = 33$ | Relaxed | $D[\text{INT4}] = 33$ |
| **4** | `NODE_INT3` | $22$ | `NODE_H003` | $14$ | $22 + 14 = 36$ | Relaxed | $D[\text{H003}] = 36$ |
| **5** | `NODE_INT4` | $33$ | `NODE_H004` | $20$ | $33 + 20 = 53$ | Relaxed | $D[\text{H004}] = 53$ |
| **6** | `NODE_H003` | $36$ | — | — | — | Target Reached! | — |

### Optimal Clinical Decision
- Path to `H-003` (Chengalpattu Medical College Hospital):
  $$\text{NODE\_H001} \longrightarrow \text{NODE\_INT1} \longrightarrow \text{NODE\_INT3} \longrightarrow \text{NODE\_H003}$$
  $$\text{Total Travel Time} = 12 + 10 + 14 = \mathbf{36\text{ minutes}} \quad (\text{Total Distance} = 28\text{ km})$$
- Path to `H-004` (Kanchipuram District Hospital):
  $$\text{Total Travel Time} = 18 + 15 + 20 = \mathbf{53\text{ minutes}}$$

**Result:** The algorithm deterministically dispatches the emergency referral ambulance to **`H-003`**, saving $17\text{ critical minutes}$ in clinical transit while guaranteeing an available ICU bed and specialist surgeon.

---

## 5. Algorithmic Correctness & Proof

1. **Optimal Substructure:** Any subpath of a shortest path between two vertices is itself a shortest path. If path $p = \langle v_1, v_2, \dots, v_k \rangle$ is the shortest path from $v_1$ to $v_k$, then for any $i, j$ with $1 \le i \le j \le k$, the subpath $p_{ij} = \langle v_i, \dots, v_j \rangle$ is a shortest path between $v_i$ and $v_j$.
2. **Greedy Choice Property:** At each step, selecting the vertex $u \notin S$ with minimum $D[u]$ guarantees that $D[u] = \delta(s, u)$ because all road weights are non-negative ($w(e) \ge 0$). No subsequent path through another unvisited vertex can reduce $D[u]$.
3. **Complexity Proof:**
   - Insertion of all $|V|$ vertices into Min-Heap: $O(|V| \log |V|)$
   - $|V|$ `extractMin` operations: $|V| \times O(\log |V|) = O(|V| \log |V|)$
   - At most $|E|$ `decreaseKey` operations across all edge relaxations: $|E| \times O(\log |V|) = O(|E| \log |V|)$
   - **Total Runtime:** $O((|V| + |E|) \log |V|)$, which easily executes in $< 1\text{ millisecond}$ for metropolitan road regional graphs.
