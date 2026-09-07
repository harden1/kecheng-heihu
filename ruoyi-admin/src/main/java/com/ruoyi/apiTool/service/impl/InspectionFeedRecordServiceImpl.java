package com.ruoyi.apiTool.service.impl;

import com.ruoyi.apiTool.domain.feed.InspectionFeedRecord;
import com.ruoyi.apiTool.mapper.InspectionFeedRecordMapper;
import com.ruoyi.apiTool.service.IInspectionFeedRecordService;
import com.ruoyi.inspection.domain.InspectionSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 镜检投料上传记录Service实现
 *
 * @author wmin
 * @date 2026-09-03
 */
@Service
public class InspectionFeedRecordServiceImpl implements IInspectionFeedRecordService {

    private static final Logger log = LoggerFactory.getLogger(InspectionFeedRecordServiceImpl.class);

    @Autowired
    private InspectionFeedRecordMapper feedRecordMapper;

    /**
     * 根据主键查询投料记录
     */
    @Override
    public InspectionFeedRecord selectById(Long id) {
        return feedRecordMapper.selectById(id);
    }

    /**
     * 按条件查询投料记录列表
     */
    @Override
    public List<InspectionFeedRecord> selectList(InspectionFeedRecord query) {
        return feedRecordMapper.selectList(query);
    }

    /**
     * 按业务键获取或创建唯一投料记录。
     * 使用独立事务，保证投料记录创建不被外部报工事务回滚。
     * 并发插入遇到唯一键冲突时重新查询，不创建第二条记录。
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public InspectionFeedRecord getOrCreate(InspectionSummary summary, String qrCode, Long taskId, Long materialId) {
        // 先查询是否已存在
        InspectionFeedRecord existing = feedRecordMapper.selectByBusinessKey(summary.getId(), taskId, qrCode);
        if (existing != null) {
            return existing;
        }

        // 创建新记录，初始状态 PENDING
        InspectionFeedRecord record = new InspectionFeedRecord();
        record.setSummaryId(summary.getId());
        record.setQrCode(qrCode);
        record.setTaskId(taskId);
        record.setMaterialId(materialId);
        record.setWorkOrderCode(summary.getWorkOrderCode());
        record.setUploadStatus("PENDING");
        record.setRetryCount(0);
        try {
            feedRecordMapper.insert(record);
        } catch (DuplicateKeyException e) {
            // 并发插入遇到唯一键冲突，重新查询已创建的记录
            log.info("投料记录并发插入冲突，重新查询，summaryId={}, taskId={}, qrCode={}",
                    summary.getId(), taskId, qrCode);
            existing = feedRecordMapper.selectByBusinessKey(summary.getId(), taskId, qrCode);
            if (existing != null) {
                return existing;
            }
            throw e;
        }
        return record;
    }

    /**
     * 自动上传抢占，使用独立事务
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public boolean claimAutoUpload(Long id) {
        return feedRecordMapper.claimAutoUpload(id) > 0;
    }

    /**
     * 手动重传抢占，使用独立事务
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public boolean claimManualRetry(Long id) {
        return feedRecordMapper.claimManualRetry(id) > 0;
    }

    /**
     * 标记投料成功，使用独立事务
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public int markSuccess(Long id, String requestJson, String responseJson, Date feedTime) {
        return feedRecordMapper.markSuccess(id, requestJson, responseJson, feedTime);
    }

    /**
     * 标记投料失败，使用独立事务
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public int markFailed(Long id, String requestJson, String responseJson, String errorMessage, String failType) {
        return feedRecordMapper.markFailed(id, requestJson, responseJson, errorMessage, failType);
    }

    /**
     * 标记投料结果未知，使用独立事务
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public int markUnknown(Long id, String requestJson, String responseJson, String errorMessage, String failType) {
        return feedRecordMapper.markUnknown(id, requestJson, responseJson, errorMessage, failType);
    }
}
