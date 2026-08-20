package com.dlbyy.blog.storage.impl;

import com.dlbyy.blog.common.exception.BusinessException;
import com.dlbyy.blog.storage.FileUploadResult;
import com.dlbyy.blog.storage.StorageProperties;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.SetBucketPolicyArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * {@link MinioStorageServiceImpl} 单元测试。
 * <p>
 * 覆盖：上传成功（bucket 懒初始化 + object key 生成规则 + 返回结果字段）、
 * bucket 已存在时跳过创建、bucket 就绪标记幂等、空文件校验、putObject 异常包装。
 * <p>
 * 说明：
 * <ul>
 *     <li>MinioClient 全部 Mock（MinIO SDK 8.5.10 中该类与方法均非 final，可直接 mock）；</li>
 *     <li>StorageProperties.MinioConfig 使用真实对象；</li>
 *     <li>上传文件使用 spring-test 提供的 MockMultipartFile。</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MinioStorageServiceImpl MinIO 存储策略单元测试")
class MinioStorageServiceImplTest {

    private static final String BUCKET = "blog";

    private static final String URL_PREFIX = "http://localhost:9000/blog/";

    /** 未指定 path 时的 object key 格式：yyyy/MM/dd/时间戳_随机数.后缀 */
    private static final Pattern OBJECT_KEY_PATTERN =
            Pattern.compile("^\\d{4}/\\d{2}/\\d{2}/\\d+_\\d+\\.\\w+$");

    @Mock
    private MinioClient minioClient;

    private MinioStorageServiceImpl storageService;

    @BeforeEach
    void setUp() {
        // 真实配置对象：endpoint/密钥仅透传，测试关注 bucketName 与 urlPrefix
        StorageProperties.MinioConfig config = new StorageProperties.MinioConfig();
        config.setEndpoint("http://localhost:9000");
        config.setAccessKey("minioadmin");
        config.setSecretKey("minioadmin");
        config.setBucketName(BUCKET);
        config.setUrlPrefix(URL_PREFIX);
        storageService = new MinioStorageServiceImpl(config, minioClient);
    }

    private MockMultipartFile pngFile(String originalFilename) {
        return new MockMultipartFile(
                "file", originalFilename, "image/png", new byte[]{1, 2, 3, 4, 5});
    }

    // ==================== 1. 上传成功（bucket 不存在，懒初始化） ====================

    @Test
    @DisplayName("上传成功：bucket 不存在时创建并设置匿名只读策略，object key 符合 yyyy/MM/dd/数字_数字.后缀，结果字段正确")
    void upload_bucketMissing_shouldCreateBucketWithPolicyAndUpload() throws Exception {
        // bucketExists 默认返回 false（Mockito boolean 默认值），走 makeBucket + setBucketPolicy 分支
        FileUploadResult result = storageService.upload(pngFile("avatar.png"), null);

        // ---- 返回结果字段 ----
        assertThat(result).isNotNull();
        assertThat(result.getStorageType()).isEqualTo("minio");
        assertThat(result.getOriginalFilename()).isEqualTo("avatar.png");
        assertThat(result.getSize()).isEqualTo(5L);
        String objectKey = result.getPath();
        // 未指定 path 时按日期目录生成：yyyy/MM/dd/时间戳_随机数.png
        assertThat(objectKey).matches(OBJECT_KEY_PATTERN).endsWith(".png");
        assertThat(result.getFilename()).isEqualTo(objectKey);
        // url = urlPrefix + objectKey
        assertThat(result.getUrl()).isEqualTo(URL_PREFIX + objectKey);

        // ---- bucket 懒初始化：检查存在 → 创建 → 设置策略 ----
        verify(minioClient).bucketExists(any());
        verify(minioClient).makeBucket(any());
        ArgumentCaptor<SetBucketPolicyArgs> policyCaptor = ArgumentCaptor.forClass(SetBucketPolicyArgs.class);
        verify(minioClient).setBucketPolicy(policyCaptor.capture());
        // 匿名只读策略：作用于目标 bucket，仅允许 s3:GetObject
        assertThat(policyCaptor.getValue().bucket()).isEqualTo(BUCKET);
        assertThat(policyCaptor.getValue().config())
                .contains("s3:GetObject")
                .contains("arn:aws:s3:::" + BUCKET + "/*");

        // ---- putObject：bucket 与 object 参数正确 ----
        ArgumentCaptor<PutObjectArgs> putCaptor = ArgumentCaptor.forClass(PutObjectArgs.class);
        verify(minioClient).putObject(putCaptor.capture());
        assertThat(putCaptor.getValue().bucket()).isEqualTo(BUCKET);
        assertThat(putCaptor.getValue().object()).isEqualTo(objectKey);
    }

    // ==================== 2. bucket 已存在 ====================

    @Test
    @DisplayName("bucket 已存在：不再 makeBucket / setBucketPolicy，仅直接上传")
    void upload_bucketExists_shouldSkipBucketCreation() throws Exception {
        when(minioClient.bucketExists(any())).thenReturn(true);

        FileUploadResult result = storageService.upload(pngFile("photo.png"), null);

        assertThat(result.getStorageType()).isEqualTo("minio");
        verify(minioClient).bucketExists(any());
        verify(minioClient, never()).makeBucket(any());
        verify(minioClient, never()).setBucketPolicy(any());
        verify(minioClient).putObject(any());
    }

    // ==================== 3. bucket 就绪标记幂等 ====================

    @Test
    @DisplayName("幂等：同一实例第二次上传不再调用 bucketExists / makeBucket / setBucketPolicy")
    void upload_secondCall_shouldNotRecheckBucket() throws Exception {
        storageService.upload(pngFile("first.png"), null);
        storageService.upload(pngFile("second.png"), null);

        // volatile 就绪标记生效：bucket 检查与初始化仅发生一次，上传执行两次
        verify(minioClient, times(1)).bucketExists(any());
        verify(minioClient, times(1)).makeBucket(any());
        verify(minioClient, times(1)).setBucketPolicy(any());
        verify(minioClient, times(2)).putObject(any());
    }

    // ==================== 4. 空文件 ====================

    @Test
    @DisplayName("空文件：抛出 BusinessException，且不与 MinIO 交互")
    void upload_emptyFile_shouldThrowBusinessException() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.png", "image/png", new byte[0]);

        BusinessException ex = catchThrowableOfType(
                () -> storageService.upload(emptyFile, null), BusinessException.class);

        assertThat(ex).isNotNull();
        assertThat(ex.getMessage()).isEqualTo("上传文件不能为空");
        verifyNoInteractions(minioClient);
    }

    // ==================== 5. putObject 异常包装 ====================

    @Test
    @DisplayName("putObject 抛异常：包装为 BusinessException，不向外泄露原始异常")
    void upload_putObjectFails_shouldWrapAsBusinessException() throws Exception {
        when(minioClient.putObject(any()))
                .thenThrow(new IOException("connection reset"));

        BusinessException ex = catchThrowableOfType(
                () -> storageService.upload(pngFile("broken.png"), null), BusinessException.class);

        assertThat(ex).isNotNull();
        assertThat(ex.getMessage()).contains("文件上传失败");
        // 失败路径不应返回结果相关交互之外的异常类型
        assertThat(ex).isNotInstanceOf(IOException.class);
    }

    // ==================== 6. saveBytes：懒建桶 + contentType 透传 ====================

    @Test
    @DisplayName("saveBytes：懒建桶（检查/创建/策略各一次）+ putObject 参数正确 + 结果字段正确")
    void saveBytes_shouldUploadWithLazyBucketAndCorrectContentType() throws Exception {
        FileUploadResult result = storageService.saveBytes(
                new byte[]{9, 9, 9}, "png", "image/png", "cover.png");

        assertThat(result).isNotNull();
        assertThat(result.getStorageType()).isEqualTo("minio");
        assertThat(result.getOriginalFilename()).isEqualTo("cover.png");
        assertThat(result.getSize()).isEqualTo(3L);
        // object key：日期目录/yyyy/MM/dd/时间戳_随机数.png
        assertThat(result.getPath()).matches("^\\d{4}/\\d{2}/\\d{2}/\\d+_\\d+\\.png$");
        assertThat(result.getUrl()).isEqualTo(URL_PREFIX + result.getPath());

        // 懒初始化：bucket 检查 → 创建 → 策略 各一次
        verify(minioClient, times(1)).bucketExists(any());
        verify(minioClient, times(1)).makeBucket(any());
        verify(minioClient, times(1)).setBucketPolicy(any());
        // putObject：显式 contentType 透传
        ArgumentCaptor<PutObjectArgs> putCaptor = ArgumentCaptor.forClass(PutObjectArgs.class);
        verify(minioClient).putObject(putCaptor.capture());
        assertThat(putCaptor.getValue().bucket()).isEqualTo(BUCKET);
        assertThat(putCaptor.getValue().object()).isEqualTo(result.getPath());
        assertThat(putCaptor.getValue().contentType()).isEqualTo("image/png");
    }

    // ==================== 7. saveBytes：contentType 按后缀推断 ====================

    @Test
    @DisplayName("saveBytes：contentType 为 null 时按后缀推断（png → image/png）")
    void saveBytes_nullContentType_shouldInferFromSuffix() throws Exception {
        storageService.saveBytes(new byte[]{1}, "png", null, null);

        ArgumentCaptor<PutObjectArgs> putCaptor = ArgumentCaptor.forClass(PutObjectArgs.class);
        verify(minioClient).putObject(putCaptor.capture());
        assertThat(putCaptor.getValue().contentType()).isEqualTo("image/png");
    }
}
