package com.dlbyy.blog.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * MP3 内嵌歌词解析器：解析 ID3v2 USLT（Unsynchronised Lyrics）帧。
 * 若源歌词本身带 LRC 时间轴（[mm:ss.xx]）则原样返回；否则返回纯文本歌词。
 */
@Slf4j
public final class Mp3LyricParser {

    private Mp3LyricParser() {
    }

    /**
     * 解析 MP3 文件中的内嵌歌词，未找到返回 null
     */
    public static String parse(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try (InputStream in = new ByteArrayInputStream(file.getBytes())) {
            return parse(in);
        } catch (IOException e) {
            log.warn("读取 MP3 文件失败：{}", e.getMessage());
            return null;
        }
    }

    public static String parse(InputStream in) throws IOException {
        // ID3v2 头："ID3" + 版本2字节 + 标志1字节 + 大小4字节（syncsafe）
        byte[] header = readN(in, 10);
        if (header == null || header[0] != 'I' || header[1] != 'D' || header[2] != '3') {
            return null;
        }
        int major = header[3] & 0xFF; // 3 => v2.3, 4 => v2.4
        int tagSize = syncsafe(header[6], header[7], header[8], header[9]);
        if (tagSize <= 0 || tagSize > 20 * 1024 * 1024) {
            return null;
        }
        byte[] tag = readN(in, tagSize);
        if (tag == null) {
            return null;
        }

        int pos = 0;
        while (pos + 10 <= tag.length) {
            String frameId = new String(tag, pos, 4, StandardCharsets.ISO_8859_1);
            if (!isValidFrameId(frameId)) {
                break; // padding 开始
            }
            int frameSize;
            if (major >= 4) {
                // v2.4 帧大小为 syncsafe
                frameSize = syncsafe(tag[pos + 4], tag[pos + 5], tag[pos + 6], tag[pos + 7]);
            } else {
                // v2.3 帧大小为普通 32 位整数
                frameSize = ((tag[pos + 4] & 0xFF) << 24) | ((tag[pos + 5] & 0xFF) << 16)
                        | ((tag[pos + 6] & 0xFF) << 8) | (tag[pos + 7] & 0xFF);
            }
            if (frameSize <= 0 || pos + 10 + frameSize > tag.length) {
                break;
            }
            if ("USLT".equals(frameId) || "SYLT".equals(frameId)) {
                byte[] body = new byte[frameSize];
                System.arraycopy(tag, pos + 10, body, 0, frameSize);
                String lyric = decodeText(body);
                if (lyric != null && !lyric.isBlank()) {
                    return lyric.trim();
                }
            }
            pos += 10 + frameSize;
        }
        return null;
    }

    private static boolean isValidFrameId(String id) {
        if (id.length() != 4) {
            return false;
        }
        for (char c : id.toCharArray()) {
            if (!(c >= 'A' && c <= 'Z') && !(c >= '0' && c <= '9')) {
                return false;
            }
        }
        return true;
    }

    /**
     * 解码 USLT 帧正文：编码字节 + 语言3字节 + 内容描述（0 结尾）+ 歌词文本
     */
    private static String decodeText(byte[] body) {
        if (body.length < 5) {
            return null;
        }
        int enc = body[0] & 0xFF;
        Charset charset;
        switch (enc) {
            case 0 -> charset = StandardCharsets.ISO_8859_1;
            case 1 -> charset = StandardCharsets.UTF_16; // 带 BOM
            case 2 -> charset = StandardCharsets.UTF_16BE;
            default -> charset = StandardCharsets.UTF_8;
        }
        // 跳过：编码(1) + 语言(3) + 内容描述（以终止符结束）
        int p = 4;
        p = skipString(body, p, charset);
        if (p < 0 || p >= body.length) {
            return null;
        }
        String text = new String(body, p, body.length - p, charset);
        // 去掉 BOM 与空字节
        text = text.replace("\uFEFF", "").replace("\u0000", "");
        return text.isBlank() ? null : text;
    }

    /** 跳过一个以终止符结尾的字符串，返回终止符之后的位置 */
    private static int skipString(byte[] body, int start, Charset charset) {
        if (charset.equals(StandardCharsets.UTF_16) || charset.equals(StandardCharsets.UTF_16BE)) {
            for (int i = start; i + 1 < body.length; i += 2) {
                if (body[i] == 0 && body[i + 1] == 0) {
                    return i + 2;
                }
            }
        } else {
            for (int i = start; i < body.length; i++) {
                if (body[i] == 0) {
                    return i + 1;
                }
            }
        }
        return -1;
    }

    private static int syncsafe(byte b1, byte b2, byte b3, byte b4) {
        return ((b1 & 0x7F) << 21) | ((b2 & 0x7F) << 14) | ((b3 & 0x7F) << 7) | (b4 & 0x7F);
    }

    private static byte[] readN(InputStream in, int n) throws IOException {
        byte[] buf = new byte[n];
        int read = 0;
        while (read < n) {
            int r = in.read(buf, read, n - read);
            if (r < 0) {
                return null;
            }
            read += r;
        }
        return buf;
    }
}
