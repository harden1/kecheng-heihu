package com.ruoyi.inspection.service;

import java.util.List;
import com.ruoyi.inspection.domain.InspectionSummary;

/**
 * 镜检统计主Service接口
 * 
 * @author w
 * @date 2025-07-08
 */
public interface IInspectionSummaryService 
{
    /**
     * 查询镜检统计主
     * 
     * @param id 镜检统计主主键
     * @return 镜检统计主
     */
    public InspectionSummary selectInspectionSummaryById(Long id);

    /**
     * 查询镜检统计主列表
     * 
     * @param inspectionSummary 镜检统计主
     * @return 镜检统计主集合
     */
    public List<InspectionSummary> selectInspectionSummaryList(InspectionSummary inspectionSummary);

    /**
     * 查询镜检统计主列表
     *
     * @param num 镜检统计主
     * @return 镜检统计主集合
     */
    public List<InspectionSummary> selectInspectionSummaryListBySchedule(int num);

    /**
     * 新增镜检统计主
     * 
     * @param inspectionSummary 镜检统计主
     * @return 结果
     */
    public int insertInspectionSummary(InspectionSummary inspectionSummary);

    /**
     * 修改镜检统计主
     * 
     * @param inspectionSummary 镜检统计主
     * @return 结果
     */
    public int updateInspectionSummary(InspectionSummary inspectionSummary);

    /**
     * 批量删除镜检统计主
     * 
     * @param ids 需要删除的镜检统计主主键集合
     * @return 结果
     */
    public int deleteInspectionSummaryByIds(Long[] ids);

    /**
     * 删除镜检统计主信息
     * 
     * @param id 镜检统计主主键
     * @return 结果
     */
    public int deleteInspectionSummaryById(Long id);

    int updateStopTime(int mainId, String stopTime);

    List<InspectionSummary> selectInspectionSummaryListByIds(List<Long> summaryIdList);
}
