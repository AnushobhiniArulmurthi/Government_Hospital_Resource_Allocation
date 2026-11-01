package com.hospital.resourceallocation.algorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Custom Min-Heap Data Structure.
 * Implemented from scratch for efficient Dijkstra's shortest-path processing.
 *
 * @param <T> Element type
 */
public class MinHeap<T> {

    public static class HeapNode<T> {
        public T element;
        public int priority; // e.g., shortest travel-time distance

        public HeapNode(T element, int priority) {
            this.element = element;
            this.priority = priority;
        }
    }

    private final ArrayList<HeapNode<T>> heap;
    private final Map<T, Integer> positionMap; // Fast element index lookup for decreaseKey

    public MinHeap() {
        this.heap = new ArrayList<>();
        this.positionMap = new HashMap<>();
    }

    public int size() {
        return heap.size();
    }

    public boolean isEmpty() {
        return heap.isEmpty();
    }

    public void insert(T element, int priority) {
        HeapNode<T> node = new HeapNode<>(element, priority);
        heap.add(node);
        int index = heap.size() - 1;
        positionMap.put(element, index);
        bubbleUp(index);
    }

    public HeapNode<T> extractMin() {
        if (isEmpty()) {
            throw new NoSuchElementException("Min-Heap is empty.");
        }
        HeapNode<T> minNode = heap.get(0);
        HeapNode<T> lastNode = heap.remove(heap.size() - 1);
        positionMap.remove(minNode.element);

        if (!heap.isEmpty()) {
            heap.set(0, lastNode);
            positionMap.put(lastNode.element, 0);
            bubbleDown(0);
        }
        return minNode;
    }

    public void decreaseKey(T element, int newPriority) {
        Integer index = positionMap.get(element);
        if (index == null) {
            insert(element, newPriority);
            return;
        }
        if (newPriority < heap.get(index).priority) {
            heap.get(index).priority = newPriority;
            bubbleUp(index);
        }
    }

    public boolean contains(T element) {
        return positionMap.containsKey(element);
    }

    private void bubbleUp(int index) {
        while (index > 0) {
            int parentIndex = (index - 1) / 2;
            if (heap.get(index).priority < heap.get(parentIndex).priority) {
                swap(index, parentIndex);
                index = parentIndex;
            } else {
                break;
            }
        }
    }

    private void bubbleDown(int index) {
        int size = heap.size();
        while (index < size) {
            int left = 2 * index + 1;
            int right = 2 * index + 2;
            int smallest = index;

            if (left < size && heap.get(left).priority < heap.get(smallest).priority) {
                smallest = left;
            }
            if (right < size && heap.get(right).priority < heap.get(smallest).priority) {
                smallest = right;
            }

            if (smallest != index) {
                swap(index, smallest);
                index = smallest;
            } else {
                break;
            }
        }
    }

    private void swap(int i, int j) {
        HeapNode<T> temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
        positionMap.put(heap.get(i).element, i);
        positionMap.put(heap.get(j).element, j);
    }
}
