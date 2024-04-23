package com.jimmy.friday.agent.memory;


import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.jimmy.friday.agent.base.Segment;
import com.jimmy.friday.agent.core.ConfigLoad;
import com.jimmy.friday.agent.other.IntObjectHashMap;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MemoryPool {

    private final static int DEFAULT_CAPACITY = 32;

    private int bufferSize = 4096;

    private AtomicInteger index;

    private List<Segment> bufferPool;

    private IntObjectHashMap<Segment> extraPool = new IntObjectHashMap<>(216);

    private static MemoryPool single;

    public static MemoryPool get() {
        if (single == null) {
            synchronized (MemoryPool.class) {
                if (single == null) {
                    single = new MemoryPool();
                }
            }
        }

        return single;
    }

    private MemoryPool() {
        String bufferSize = ConfigLoad.getDefault().get("BUFFER_SIZE");
        if (!Strings.isNullOrEmpty(bufferSize)) {
            this.bufferSize = Integer.valueOf(bufferSize);
        }

        this.index = new AtomicInteger(this.bufferSize + 1);
        this.bufferPool = Lists.newArrayListWithCapacity(this.bufferSize);

        for (int i = 0; i < this.bufferSize; i++) {
            bufferPool.add(new HeapMemorySegment(DEFAULT_CAPACITY));
        }
    }

    public List<Integer> allocateString(String str) {
        return this.allocate(str.getBytes(StandardCharsets.UTF_8));
    }

    public List<Integer> allocate(byte[] bytes) {
        try {
            //压缩
            List<Integer> index = Lists.newArrayList();
            //切割
            List<byte[]> spilt = this.splitByteArray(bytes);
            //缓冲区已用完
            if (bufferPool.stream().filter(buffer -> buffer.isFree()).count() == 0) {
                DiskSegment diskSegment = new DiskSegment();
                diskSegment.write(bytes);

                int l = this.index.incrementAndGet();
                extraPool.put(l, diskSegment);
                index.add(l);
                return index;
            }

            for (byte[] b : spilt) {
                Integer freeSegmentNext = this.getFreeSegmentNext(b);
                if (freeSegmentNext != null) {
                    index.add(freeSegmentNext);
                    continue;
                }
                //缓冲区已用完
                DiskSegment diskSegment = new DiskSegment();
                diskSegment.write(b);
                int l = this.index.incrementAndGet();
                extraPool.put(l, diskSegment);
                index.add(l);
            }

            return index;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public byte[] getAndFree(List<Integer> indices) {
        byte[] bytes = this.get(indices);
        this.free(indices);
        return bytes;
    }

    public String getString(List<Integer> indices) {
        byte[] bytes = this.getAndFree(indices);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public byte[] get(List<Integer> indices) {
        try {
            if (indices == null || indices.isEmpty()) {
                return null;
            }

            Integer first = indices.get(0);
            byte[] bytes = this.get(first);
            if (bytes == null) {
                throw new RuntimeException("数组为空");
            }

            for (int i = 1; i < indices.size(); i++) {
                byte[] other = this.get(indices.get(i));
                if (other == null) {
                    throw new RuntimeException("数组为空");
                }

                bytes = this.mergeByteArray(bytes, other);
            }

            return bytes;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void free(Collection<Integer> indices) {
        if (indices == null || indices.isEmpty()) {
            return;
        }

        for (Integer index : indices) {
            this.free(index);
        }
    }

    public void free(Integer index) {
        if (index > bufferSize) {
            Segment segment = extraPool.get(index);
            if (segment != null) {
                segment.free();
                extraPool.remove(index);
            }

            return;
        }

        Segment segment = bufferPool.get(index);
        if (segment != null) {
            segment.free();
        }
    }

    /**
     * 根据下标获取数据
     *
     * @param index
     * @return
     */
    private byte[] get(Integer index) {
        return index > bufferSize ? extraPool.get(index).read() : bufferPool.get(index).read();
    }

    /**
     * 获取空闲内存下标，若用完则返回空
     *
     * @param bytes
     * @return
     */
    private Integer getFreeSegmentNext(byte[] bytes) {
        for (int i = 0; i < bufferPool.size(); i++) {
            Segment segment = bufferPool.get(i);
            if (segment.isFree()) {
                if (segment.write(bytes)) {
                    return i;
                }

                return getFreeSegmentNext(bytes);
            }
        }

        return null;
    }

    /**
     * 切割byte数组
     *
     * @param array
     * @return
     */
    private List<byte[]> splitByteArray(byte[] array) {
        int amount = array.length / DEFAULT_CAPACITY;
        List<byte[]> split = Lists.newLinkedList();
        if (amount == 0) {
            split.add(array);
            return split;
        }
        //判断余数
        int remainder = array.length % DEFAULT_CAPACITY;
        if (remainder != 0) {
            ++amount;
        }

        byte[] arr;
        for (int i = 0; i < amount; i++) {
            if (i == amount - 1 && remainder != 0) {
                // 有剩余，按照实际长度创建
                arr = new byte[remainder];
                System.arraycopy(array, i * DEFAULT_CAPACITY, arr, 0, remainder);
            } else {
                arr = new byte[DEFAULT_CAPACITY];
                System.arraycopy(array, i * DEFAULT_CAPACITY, arr, 0, DEFAULT_CAPACITY);
            }

            split.add(arr);
        }
        return split;
    }

    /**
     * 合并byte数组
     *
     * @param bt1
     * @param bt2
     * @return
     */
    private byte[] mergeByteArray(byte[] bt1, byte[] bt2) {
        byte[] bt3 = new byte[bt1.length + bt2.length];
        System.arraycopy(bt1, 0, bt3, 0, bt1.length);
        System.arraycopy(bt2, 0, bt3, bt1.length, bt2.length);
        return bt3;
    }
}
