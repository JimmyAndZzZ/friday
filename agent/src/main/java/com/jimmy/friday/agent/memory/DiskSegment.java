package com.jimmy.friday.agent.memory;

import com.google.common.base.Strings;
import com.jimmy.friday.agent.base.Segment;
import com.jimmy.friday.agent.exception.AgentException;
import com.jimmy.friday.agent.utils.FileUtil;
import com.jimmy.friday.boot.other.ShortUUID;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class DiskSegment implements Segment {

    private String fileName;

    DiskSegment() {
        fileName = new StringBuilder("/tmp/").append(ShortUUID.uuid()).append(".seg").toString();
        FileUtil.touch(fileName);
    }

    @Override
    public boolean write(byte[] bytes) {
        try (FileChannel channel = FileChannel.open(Paths.get(fileName), StandardOpenOption.WRITE)) {
            ByteBuffer buf = ByteBuffer.allocate(5);
            for (int i = 0; i < bytes.length; ) {
                buf.put(bytes, i, Math.min(bytes.length - i, buf.limit() - buf.position()));
                buf.flip();
                i += channel.write(buf);
                buf.compact();
            }
            channel.force(false);
            return true;
        } catch (Exception e) {
            throw new AgentException(e.getCause());
        }
    }

    @Override
    public byte[] read() {
        if (Strings.isNullOrEmpty(fileName)) {
            throw new AgentException("空闲无法读取");
        }

        try (FileChannel channel = new RandomAccessFile(new File(fileName), "r").getChannel()) {
            int fileSize = (int) channel.size();
            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, fileSize).load();
            byte[] result = new byte[fileSize];
            if (buffer.remaining() > 0) {
                buffer.get(result, 0, fileSize);
            }
            buffer.clear();
            return result;
        } catch (Exception e) {
            throw new AgentException(e.getCause());
        }
    }

    @Override
    public void free() {
        new File(fileName).deleteOnExit();
    }

    @Override
    public boolean isFree() {
        return true;
    }
}
