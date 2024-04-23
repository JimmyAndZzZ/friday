package com.jimmy.friday.framework.netty.codec;

import com.jimmy.friday.framework.utils.JsonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.TooLongFrameException;

import java.util.List;

public class NettyDecoder extends ByteToMessageDecoder {
    private int openBraces;
    private int idx;
    private int lastReaderIndex;
    private int state;
    private boolean insideString;
    private final int maxObjectLength;
    private final boolean streamArrayElements;

    private final Class<?> genericClass;

    public NettyDecoder(Class<?> genericClass) {
        this.genericClass = genericClass;
        this.maxObjectLength = 1048576;
        this.streamArrayElements = false;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (this.state == -1) {
            in.skipBytes(in.readableBytes());
        } else {
            if (this.idx > in.readerIndex() && this.lastReaderIndex != in.readerIndex()) {
                this.idx = in.readerIndex() + (this.idx - this.lastReaderIndex);
            }

            int idx = this.idx;
            int wrtIdx = in.writerIndex();
            if (wrtIdx > this.maxObjectLength) {
                in.skipBytes(in.readableBytes());
                this.reset();
                throw new TooLongFrameException("object length exceeds " + this.maxObjectLength + ": " + wrtIdx + " bytes discarded");
            } else {
                for (; idx < wrtIdx; ++idx) {
                    byte c = in.getByte(idx);
                    if (this.state == 1) {
                        this.decodeByte(c, in, idx);
                        if (this.openBraces == 0) {
                            ByteBuf json = this.extractObject(ctx, in, in.readerIndex(), idx + 1 - in.readerIndex());
                            if (json != null) {
                                out.add(parse(json));
                                json.release();
                            }

                            in.readerIndex(idx + 1);
                            this.reset();
                        }
                    } else if (this.state == 2) {
                        this.decodeByte(c, in, idx);
                        if (!this.insideString && (this.openBraces == 1 && c == 44 || this.openBraces == 0 && c == 93)) {
                            int idxNoSpaces;
                            for (idxNoSpaces = in.readerIndex(); Character.isWhitespace(in.getByte(idxNoSpaces)); ++idxNoSpaces) {
                                in.skipBytes(1);
                            }

                            for (idxNoSpaces = idx - 1; idxNoSpaces >= in.readerIndex() && Character.isWhitespace(in.getByte(idxNoSpaces)); --idxNoSpaces) {
                            }

                            ByteBuf json = this.extractObject(ctx, in, in.readerIndex(), idxNoSpaces + 1 - in.readerIndex());
                            if (json != null) {
                                out.add(parse(json));
                                json.release();
                            }

                            in.readerIndex(idx + 1);
                            if (c == 93) {
                                this.reset();
                            }
                        }
                    } else if (c != 123 && c != 91) {
                        if (!Character.isWhitespace(c)) {
                            this.state = -1;
                            throw new CorruptedFrameException("invalid JSON received at byte position " + idx + ": " + ByteBufUtil.hexDump(in));
                        }

                        in.skipBytes(1);
                    } else {
                        this.initDecoding(c);
                        if (this.state == 2) {
                            in.skipBytes(1);
                        }
                    }
                }

                if (in.readableBytes() == 0) {
                    this.idx = 0;
                } else {
                    this.idx = idx;
                }

                this.lastReaderIndex = in.readerIndex();
            }
        }
    }

    protected ByteBuf extractObject(ChannelHandlerContext ctx, ByteBuf buffer, int index, int length) {
        return buffer.retainedSlice(index, length);
    }

    private void decodeByte(byte c, ByteBuf in, int idx) {
        if ((c == 123 || c == 91) && !this.insideString) {
            ++this.openBraces;
        } else if ((c == 125 || c == 93) && !this.insideString) {
            --this.openBraces;
        } else if (c == 34) {
            if (!this.insideString) {
                this.insideString = true;
            } else {
                int backslashCount = 0;
                --idx;

                while (idx >= 0 && in.getByte(idx) == 92) {
                    ++backslashCount;
                    --idx;
                }

                if (backslashCount % 2 == 0) {
                    this.insideString = false;
                }
            }
        }

    }

    private void initDecoding(byte openingBrace) {
        this.openBraces = 1;
        if (openingBrace == 91 && this.streamArrayElements) {
            this.state = 2;
        } else {
            this.state = 1;
        }

    }

    private void reset() {
        this.insideString = false;
        this.state = 0;
        this.openBraces = 0;
    }

    private Object parse(ByteBuf json) {
        byte[] bytes = new byte[json.readableBytes()]; // 从 ByteBuf 中读取字节数组
        json.readBytes(bytes);

        String jsonString = new String(bytes);
        return JsonUtil.parseObject(jsonString, genericClass);
    }
}
