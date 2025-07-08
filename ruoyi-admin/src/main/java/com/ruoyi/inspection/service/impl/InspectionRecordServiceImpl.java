package com.ruoyi.inspection.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.inspection.mapper.InspectionRecordMapper;
import com.ruoyi.inspection.domain.InspectionRecord;
import com.ruoyi.inspection.service.IInspectionRecordService;

/**
 * 镜检单条记录Service业务层处理
 * 
 * @author w
 * @date 2025-07-08
 */
@Service
public class InspectionRecordServiceImpl implements IInspectionRecordService 
{
    @Autowired
    private InspectionRecordMapper inspectionRecordMapper;

    /**
     * 查询镜检单条记录
     * 
     * @param id 镜检单条记录主键
     * @return 镜检单条记录
     */
    @Override
    public InspectionRecord selectInspectionRecordById(Long id)
    {
        return inspectionRecordMapper.selectInspectionRecordById(id);
    }

    /**
     * 查询镜检单条记录列表
     * 
     * @param inspectionRecord 镜检单条记录
     * @return 镜检单条记录
     */
    @Override
    public List<InspectionRecord> selectInspectionRecordList(InspectionRecord inspectionRecord)
    {
        return inspectionRecordMapper.selectInspectionRecordList(inspectionRecord);
    }

    /**
     * 新增镜检单条记录
     * 
     * @param inspectionRecord 镜检单条记录
     * @return 结果
     */
    @Override
    public int insertInspectionRecord(InspectionRecord inspectionRecord)
    {
        inspectionRecord.setCreateTime(DateUtils.getNowDate());
        return inspectionRecordMapper.insertInspectionRecord(inspectionRecord);
    }

    /**
     * 修改镜检单条记录
     * 
     * @param inspectionRecord 镜检单条记录
     * @return 结果
     */
    @Override
    public int updateInspectionRecord(InspectionRecord inspectionRecord)
    {
        inspectionRecord.setUpdateTime(DateUtils.getNowDate());
        return inspectionRecordMapper.updateInspectionRecord(inspectionRecord);
    }

    /**
     * 批量删除镜检单条记录
     * 
     * @param ids 需要删除的镜检单条记录主键
     * @return 结果
     */
    @Override
    public int deleteInspectionRecordByIds(Long[] ids)
    {
        return inspectionRecordMapper.deleteInspectionRecordByIds(ids);
    }

    /**
     * 删除镜检单条记录信息
     * 
     * @param id 镜检单条记录主键
     * @return 结果
     */
    @Override
    public int deleteInspectionRecordById(Long id)
    {
        return inspectionRecordMapper.deleteInspectionRecordById(id);
    }
}
