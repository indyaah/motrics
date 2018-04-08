package pro.anuj.challenge.motrics.utils;

import lombok.NonNull;

import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

import static java.util.Comparator.reverseOrder;

public class MedianHolder {

    private final Queue<Double> maxHeap = new PriorityBlockingQueue<>(1000, reverseOrder());
    private final Queue<Double> minHeap = new PriorityBlockingQueue<>(1000);

    public Double getMedian() {
        if (minHeap.size() == 0 && maxHeap.size() == 0)
            return null;
        int size = minHeap.size() + maxHeap.size();
        if (size % 2 == 0)
            return (maxHeap.peek() + minHeap.peek()) / 2.0;
        return maxHeap.peek();
    }

    private void balance() {
        if (maxHeap.size() < minHeap.size()) {
            maxHeap.add(minHeap.poll());
        } else if (maxHeap.size() > 1 + minHeap.size()) {
            minHeap.add(maxHeap.poll());
        }
    }

    public void add(@NonNull Double num) {
        if (maxHeap.size() == 0 || num <= maxHeap.peek()) {
            maxHeap.add(num);
        } else {
            minHeap.add(num);
        }
        balance();
    }
}
