package com.hospital.resourceallocation.algorithms;

import com.hospital.resourceallocation.model.ConsultationWaitingQueueItem;
import com.hospital.resourceallocation.model.ResourceWaitingQueueItem;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Thread-safe PriorityQueue Manager maintaining independent priority queues:
 * 1. Consultation Waiting Queue
 * 2. Resource Allocation Waiting Queue
 *
 * Implements deterministic severity-first, arrival-time second priority ranking.
 */
@Component
public class HospitalPriorityQueue {

    private final PriorityQueue<ConsultationWaitingQueueItem> consultationQueue;
    private final PriorityQueue<ResourceWaitingQueueItem> resourceQueue;
    private final ReentrantLock lock = new ReentrantLock();

    public HospitalPriorityQueue() {
        this.consultationQueue = new PriorityQueue<>();
        this.resourceQueue = new PriorityQueue<>();
    }

    // --- Consultation Queue Operations ---

    public void addConsultationRequest(ConsultationWaitingQueueItem item) {
        lock.lock();
        try {
            consultationQueue.add(item);
        } finally {
            lock.unlock();
        }
    }

    public ConsultationWaitingQueueItem peekConsultationRequest() {
        lock.lock();
        try {
            return consultationQueue.peek();
        } finally {
            lock.unlock();
        }
    }

    public ConsultationWaitingQueueItem pollConsultationRequest() {
        lock.lock();
        try {
            return consultationQueue.poll();
        } finally {
            lock.unlock();
        }
    }

    public boolean removeConsultationRequest(String queueId) {
        lock.lock();
        try {
            return consultationQueue.removeIf(item -> item.getQueueId().equals(queueId));
        } finally {
            lock.unlock();
        }
    }

    public List<ConsultationWaitingQueueItem> getAllConsultationRequests() {
        lock.lock();
        try {
            List<ConsultationWaitingQueueItem> list = new ArrayList<>(consultationQueue);
            list.sort(null); // Sort by natural Comparable ordering
            return list;
        } finally {
            lock.unlock();
        }
    }

    public int getConsultationQueueSize() {
        lock.lock();
        try {
            return consultationQueue.size();
        } finally {
            lock.unlock();
        }
    }

    // --- Resource Queue Operations ---

    public void addResourceRequest(ResourceWaitingQueueItem item) {
        lock.lock();
        try {
            resourceQueue.add(item);
        } finally {
            lock.unlock();
        }
    }

    public ResourceWaitingQueueItem peekResourceRequest() {
        lock.lock();
        try {
            return resourceQueue.peek();
        } finally {
            lock.unlock();
        }
    }

    public ResourceWaitingQueueItem pollResourceRequest() {
        lock.lock();
        try {
            return resourceQueue.poll();
        } finally {
            lock.unlock();
        }
    }

    public boolean removeResourceRequest(String queueId) {
        lock.lock();
        try {
            return resourceQueue.removeIf(item -> item.getQueueId().equals(queueId));
        } finally {
            lock.unlock();
        }
    }

    public List<ResourceWaitingQueueItem> getAllResourceRequests() {
        lock.lock();
        try {
            List<ResourceWaitingQueueItem> list = new ArrayList<>(resourceQueue);
            list.sort(null);
            return list;
        } finally {
            lock.unlock();
        }
    }

    public int getResourceQueueSize() {
        lock.lock();
        try {
            return resourceQueue.size();
        } finally {
            lock.unlock();
        }
    }

    public void clearAll() {
        lock.lock();
        try {
            consultationQueue.clear();
            resourceQueue.clear();
        } finally {
            lock.unlock();
        }
    }
}
