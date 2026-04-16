package com.xiang.pic.xiangPicBackend.manager;

import cn.hutool.core.io.FileUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.MultiObjectDeleteException;
import com.qcloud.cos.model.*;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import com.xiang.pic.xiangPicBackend.config.CosClientConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CosManager {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    /**
     * 上传对象
     *
     * @param key  唯一键
     * @param file 文件
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 下载对象
     *
     * @param key 唯一键
     */
    public COSObject getObject(String key) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(cosClientConfig.getBucket(), key);
        return cosClient.getObject(getObjectRequest);
    }


    /**
     * 上传对象（附带图片信息）
     *
     * @param key  唯一键
     * @param file 文件
     */
    public PutObjectResult putPictureObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key,
                file);
        // 对图片进行处理（获取基本信息也被视作为一种处理）
        PicOperations picOperations = new PicOperations();
        // 1 表示返回原图信息
        picOperations.setIsPicInfo(1);
        List<PicOperations.Rule> rules = new ArrayList<>();
        // 图片压缩（转成 webp 格式）
        String webpKey = FileUtil.mainName(key) + ".webp";
        PicOperations.Rule compressRule = new PicOperations.Rule();
        compressRule.setRule("imageMogr2/format/webp");
        compressRule.setBucket(cosClientConfig.getBucket());
        compressRule.setFileId(webpKey);
        rules.add(compressRule);
        // 缩略图处理，仅对 > 20 KB 的图片生成缩略图
        if (file.length() > 2 * 1024) {
            PicOperations.Rule thumbnailRule = new PicOperations.Rule();
            thumbnailRule.setBucket(cosClientConfig.getBucket());
            String thumbnailKey = FileUtil.mainName(key) + "_thumbnail." + FileUtil.getSuffix(key);
            thumbnailRule.setFileId(thumbnailKey);
            // 缩放规则 /thumbnail/<Width>x<Height>>（如果大于原图宽高，则不处理）
            thumbnailRule.setRule(String.format("imageMogr2/thumbnail/%sx%s>", 128, 128));
            rules.add(thumbnailRule);
        }

        // 构造处理参数
        picOperations.setRules(rules);
        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 删除对象
     *
     * @param key 文件 key
     */
    public void deleteObject(String key) throws CosClientException {
        cosClient.deleteObject(cosClientConfig.getBucket(), key);
    }


    /**
     * 批量删除对象
     *
     * @param keys 要删除的文件key列表
     */
    @Async
    public void deleteBatchObjects(List<String> keys) {
        // 1. 构建批量删除请求
        DeleteObjectsRequest deleteRequest = new DeleteObjectsRequest(cosClientConfig.getBucket());

        // 2. 将key列表转换为KeyVersion对象列表
        List<DeleteObjectsRequest.KeyVersion> keyList = keys.stream()
                .map(DeleteObjectsRequest.KeyVersion::new)
                .collect(Collectors.toList());
        deleteRequest.setKeys(keyList);

        // 可选：设置返回模式，true=只返回失败信息，false=返回全部信息（默认）
        deleteRequest.setQuiet(false);

        try {
            // 3. 执行批量删除
            DeleteObjectsResult result = cosClient.deleteObjects(deleteRequest);

            // 4. 处理成功删除的结果
            List<DeleteObjectsResult.DeletedObject> deletedObjects = result.getDeletedObjects();
            for (DeleteObjectsResult.DeletedObject obj : deletedObjects) {
                log.info("删除成功{},当前时间{}", obj.getKey(), new Date());
            }
        } catch (MultiObjectDeleteException mde) {
            // 5. 部分成功、部分失败时抛出的异常
            log.info("部分删除失败");
            List<MultiObjectDeleteException.DeleteError> errors = mde.getErrors();
            for (MultiObjectDeleteException.DeleteError error : errors) {
                log.info("key: " + error.getKey() + ", 错误码: " +
                        error.getCode() + ", 消息: " + error.getMessage());
            }

        } catch (Exception e) {
            // 6. 其他异常（如网络错误、权限不足等）
            e.printStackTrace();
        }
    }

}
