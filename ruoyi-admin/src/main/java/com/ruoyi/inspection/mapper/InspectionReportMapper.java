package com.ruoyi.inspection.mapper;

import java.util.List;
import com.ruoyi.inspection.domain.InspectionReport;

/**
 * 报工记录Mapper接口
 * 
 * @author w
 * @date 2025-07-08
 */
public interface InspectionReportMapper 
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
     * 删除报工记录
     * 
     * @param id 报工记录主键
     * @return 结果
     */
    public int deleteInspectionReportById(Long id);

    /**
     * 批量删除报工记录
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteInspectionReportByIds(Long[] ids);
}
