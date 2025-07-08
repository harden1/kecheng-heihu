package com.ruoyi.inspection.service;

import java.util.List;
import com.ruoyi.inspection.domain.InspectionRecord;

/**
 * 镜检单条记录Service接口
 * 
 * @author w
 * @date 2025-07-08
 */
public interface IInspectionRecordService 
{
    /**
     * 查询镜检单条记录
     * 
     * @param id 镜检单条记录主键
     * @return 镜检单条记录
     */
    public InspectionRecord selectInspectionRecordById(Long id);

    /**
     * 查询镜检单条记录列表
     * 
     * @param inspectionRecord 镜检单条记录
     * @return 镜检单条记录集合
     */
    public List<InspectionRecord> selectInspectionRecordList(InspectionRecord inspectionRecord);

    /**
     * 新增镜检单条记录
     * 
     * @param inspectionRecord 镜检单条记录
     * @return 结果
     */
    public int insertInspectionRecord(InspectionRecord inspectionRecord);

    /**
     * 修改镜检单条记录
     * 
     * @param inspectionRecord 镜检单条记录
     * @return 结果
     */
    public int updateInspectionRecord(InspectionRecord inspectionRecord);

    /**
     * 批量删除镜检单条记录
     * 
     * @param ids 需要删除的镜检单条记录主键集合
     * @return 结果
     */
    public int deleteInspectionRecordByIds(Long[] ids);

    /**
     * 删除镜检单条记录信息
     * 
     * @param id 镜检单条记录主键
     * @return 结果
     */
    public int deleteInspectionRecordById(Long id);
}
