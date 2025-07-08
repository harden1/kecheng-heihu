package com.ruoyi.inspection.service;

import java.util.List;
import com.ruoyi.inspection.domain.InspectionReport;

/**
 * 报工记录Service接口
 * 
 * @author w
 * @date 2025-07-08
 */
public interface IInspectionReportService 
{
    /**
     * 查询报工记录
     * 
     * @param id 报工记录主键
     * @return 报工记录
     */
    public InspectionReport selectInspectionReportById(Long id);

    /**
     * 查询报工记录列表
     * 
     * @param inspectionReport 报工记录
     * @return 报工记录集合
     */
    public List<InspectionReport> selectInspectionReportList(InspectionReport inspectionReport);

    /**
     * 新增报工记录
     * 
     * @param inspectionReport 报工记录
     * @return 结果
     */
    public int insertInspectionReport(InspectionReport inspectionReport);

    /**
     * 修改报工记录
     * 
     * @param inspectionReport 报工记录
     * @return 结果
     */
    public int updateInspectionReport(InspectionReport inspectionReport);

    /**
     * 批量删除报工记录
     * 
     * @param ids 需要删除的报工记录主键集合
     * @return 结果
     */
    public int deleteInspectionReportByIds(Long[] ids);

    /**
     * 删除报工记录信息
     * 
     * @param id 报工记录主键
     * @return 结果
     */
    public int deleteInspectionReportById(Long id);
}
