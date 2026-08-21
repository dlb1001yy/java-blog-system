package com.dlbyy.blog.service.impl;

import com.dlbyy.blog.service.OnlineLyricService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * 网易云音乐公开接口实现：
 * 1. /api/search/get/web 搜索歌曲取 songId
 * 2. /api/song/lyric 获取带时间轴的 LRC
 * 非官方接口，可能限流/变更；任何异常吞掉返回 null，不影响上传主流程。
 */
@Slf4j
@Service
public class OnlineLyricServiceImpl implements OnlineLyricService {

    private static final String SEARCH_URL = "https://music.163.com/api/search/get/web";
    private static final String LYRIC_URL = "https://music.163.com/api/song/lyric";
    private static final String UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OnlineLyricServiceImpl() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);
        this.restTemplate = new RestTemplate(factory);
    }

    @Override
    public String fetchLrc(String title, String artist) {
        if (title == null || title.isBlank()) {
            return null;
        }
        try {
            Long songId = searchSongId(title.trim(), artist == null ? "" : artist.trim());
            if (songId == null) {
                return null;
            }
            return fetchLyricById(songId);
        } catch (Exception e) {
            log.warn("在线歌词获取失败（{} - {}）：{}", title, artist, e.getMessage());
            return null;
        }
    }

    private Long searchSongId(String title, String artist) {
        String keyword = artist.isBlank() ? title : title + " " + artist;
        String url = UriComponentsBuilder.fromHttpUrl(SEARCH_URL)
                .queryParam("s", keyword)
                .queryParam("type", 1)
                .queryParam("limit", 5)
                .toUriString();
        String body = exchange(url);
        if (body == null) {
            return null;
        }
        try {
            JsonNode songs = objectMapper.readTree(body).path("result").path("songs");
            if (!songs.isArray() || songs.isEmpty()) {
                return null;
            }
            // 优先取歌名完全匹配的第一条
            for (JsonNode song : songs) {
                if (title.equals(song.path("name").asText())) {
                    return song.path("id").asLong();
                }
            }
            return songs.get(0).path("id").asLong();
        } catch (Exception e) {
            log.warn("解析搜索结果失败：{}", e.getMessage());
            return null;
        }
    }

    private String fetchLyricById(Long songId) {
        String url = UriComponentsBuilder.fromHttpUrl(LYRIC_URL)
                .queryParam("id", songId)
                .queryParam("lv", 1)
                .queryParam("kv", 1)
                .queryParam("tv", -1)
                .toUriString();
        String body = exchange(url);
        if (body == null) {
            return null;
        }
        try {
            String lrc = objectMapper.readTree(body).path("lrc").path("lyric").asText(null);
            return (lrc == null || lrc.isBlank()) ? null : lrc.trim();
        } catch (Exception e) {
            log.warn("解析歌词结果失败：{}", e.getMessage());
            return null;
        }
    }

    private String exchange(String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, UA);
        headers.set(HttpHeaders.REFERER, "https://music.163.com");
        try {
            return restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class).getBody();
        } catch (Exception e) {
            log.warn("请求网易云接口失败：{}", e.getMessage());
            return null;
        }
    }
}
