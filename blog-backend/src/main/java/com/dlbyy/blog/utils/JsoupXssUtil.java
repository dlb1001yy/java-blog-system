package com.dlbyy.blog.utils;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

/**
 * 基于 Jsoup 的 XSS 过滤工具
 * <p>
 * 使用 {@link Safelist#relaxed()} 白名单策略，适用于富文本编辑器和评论内容：
 * <ul>
 *     <li>允许常见格式标签：a, b, blockquote, br, caption, code, col, colgroup, dd, dl, dt,
 *         em, h1-h6, hr, i, img, li, ol, p, pre, q, small, span, strike, strong, sub, sup, table, tbody, td, tfoot, th, thead, tr, u, ul</li>
 *     <li>允许 style、class 属性</li>
 *     <li>禁止 script、iframe、object、embed 等危险标签</li>
 *     <li>禁止 on* 事件属性和 javascript: 协议</li>
 * </ul>
 */
@Slf4j
public class JsoupXssUtil {

    private static final Safelist SAFELIST = Safelist.relaxed()
            // 允许 style 属性（富文本编辑器常用于内联样式）
            .addAttributes(":all", "style", "class")
            // 图片允许宽高
            .addAttributes("img", "width", "height")
            // 链接允许 target
            .addAttributes("a", "target");

    private JsoupXssUtil() {
    }

    /**
     * 过滤富文本 HTML，移除 XSS 危险内容
     *
     * @param content 原始 HTML 内容
     * @return 过滤后的安全 HTML
     */
    public static String cleanHtml(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }
        String cleaned = Jsoup.clean(content, SAFELIST);
        log.debug("XSS 过滤完成，原始长度={}, 过滤后长度={}", content.length(), cleaned.length());
        return cleaned;
    }

    /**
     * 过滤纯文本（移除所有 HTML 标签），适用于评论昵称等纯文本字段
     *
     * @param text 原始内容
     * @return 纯文本内容
     */
    public static String cleanText(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        return Jsoup.clean(text, Safelist.none());
    }
}
