package com.dlbyy.blog.utils;

import com.dlbyy.blog.common.exception.BusinessException;
import com.dlbyy.blog.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 封面图生成器：根据标题生成 1200×630 的渐变 PNG 封面图
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CoverImageGenerator {

    private static final int WIDTH = 1200;
    private static final int HEIGHT = 630;
    private static final int MAX_LINES = 3;
    private static final int MAX_CHARS_PER_LINE = 18;
    private static final int PADDING_X = 80;
    private static final String DEFAULT_TITLE = "Java 码农笔记";

    private final FileStorageService fileStorageService;

    /**
     * 根据标题生成封面图并保存，返回访问 URL
     */
    public String generate(String title) {
        if (title == null || title.isBlank()) {
            title = DEFAULT_TITLE;
        }

        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        try {
            // 抗锯齿
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 对角线性渐变背景
            GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(0x66, 0x7e, 0xea),
                    WIDTH, HEIGHT, new Color(0x76, 0x4b, 0xa2)
            );
            g.setPaint(gradient);
            g.fillRect(0, 0, WIDTH, HEIGHT);

            // 字体与颜色
            Font font = new Font(Font.SANS_SERIF, Font.BOLD, 64);
            g.setFont(font);
            g.setColor(Color.WHITE);

            FontMetrics fm = g.getFontMetrics();
            List<String> lines = wrapTitle(title, fm);

            // 垂直居中
            int lineHeight = fm.getHeight();
            int totalHeight = lines.size() * lineHeight;
            int startY = (HEIGHT - totalHeight) / 2 + fm.getAscent();

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                int x = (WIDTH - fm.stringWidth(line)) / 2;
                int y = startY + i * lineHeight;
                g.drawString(line, x, y);
            }

            // 输出 PNG 字节数组
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                ImageIO.write(image, "png", baos);
            } catch (IOException e) {
                log.error("封面图编码失败", e);
                throw new BusinessException("封面图生成失败");
            }
            // 落盘走存储策略（storage.type 切换 local/minio/oss）
            return fileStorageService.saveBytes(baos.toByteArray(), "png", "image/png", null).getUrl();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("封面图生成失败", e);
            throw new BusinessException("封面图生成失败");
        } finally {
            g.dispose();
        }
    }

    /**
     * 标题按字符数自动换行，最多 3 行，每行约 18 字符，超出截断加 …
     */
    private List<String> wrapTitle(String title, FontMetrics fm) {
        List<String> lines = new ArrayList<>();
        int length = title.length();

        if (length <= MAX_CHARS_PER_LINE) {
            lines.add(title);
            return lines;
        }

        int pos = 0;
        while (pos < length && lines.size() < MAX_LINES) {
            int end = Math.min(pos + MAX_CHARS_PER_LINE, length);
            // 尝试按字符宽度裁剪：保证单行宽度不超过画布宽度减去左右 padding
            while (end > pos + 1 && fm.stringWidth(title.substring(pos, end)) > (WIDTH - 2 * PADDING_X)) {
                end--;
            }
            lines.add(title.substring(pos, end));
            pos = end;
        }

        // 超过 3 行的部分截断加 …
        if (pos < length && lines.size() == MAX_LINES) {
            String lastLine = lines.get(lines.size() - 1);
            // 确保末尾能容纳 …
            int lastEnd = lastLine.length();
            while (lastEnd > 1 && fm.stringWidth(title.substring(pos - lastLine.length(), pos - lastLine.length() + lastEnd) + "…") > (WIDTH - 2 * PADDING_X)) {
                lastEnd--;
            }
            lines.set(lines.size() - 1, lastLine.substring(0, lastEnd) + "…");
        }
        return lines;
    }
}
