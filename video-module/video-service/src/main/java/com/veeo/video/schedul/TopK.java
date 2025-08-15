package com.veeo.video.schedul;

import com.veeo.common.entity.vo.HotVideo;

import java.util.*;

/**
 * description:
 */
public class TopK {

    private final Queue<HotVideo> queue;
    private final int k;

    public TopK(int k, PriorityQueue<HotVideo> queue) {
        this.k = k;
        this.queue = queue;
    }

    public void add(HotVideo hotVideo) {
        if (queue.size() < k) {
            queue.add(hotVideo);
        } else if (queue.peek().getHot() < hotVideo.getHot()) {
            queue.poll();
            queue.add(hotVideo);
        }
    }


    public List<HotVideo> get() {
        final ArrayList<HotVideo> list = new ArrayList<>(queue.size());
        while (!queue.isEmpty()) {
            list.add(queue.poll());
        }
        Collections.reverse(list);
        return list;
    }


}
