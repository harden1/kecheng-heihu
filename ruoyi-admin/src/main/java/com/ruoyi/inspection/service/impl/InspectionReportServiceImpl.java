package com.ruoyi.inspection.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.inspection.mapper.InspectionReportMapper;
import com.ruoyi.inspection.domain.InspectionReport;
import com.ruoyi.inspection.service.IInspectionReportService;

/**
 * 报工记录Service业务层处理
 * 
 * @author w
 * @date 2025-07-08
 */
@Service
public class InspectionReportServiceImpl implements IInspectionReportService 
{
    @Autowired
    private InspectionReportMapper inspectionReportMapper;

    /**
     * 查询报工记录
     * 
     * @param id 报工记录主键
     * @return 报工记录
     */
    @Override
    public InspectionReport selectInspectionReportById(Long id)
    {
        return inspectionReportMapper.selectInspectionReportById(id);
    }

    /**
     * 查询报工记录列表
     * 
     * @param inspectionReport 报工记录
     * @return 报工记录
     */
    @Override
    public List<InspectionReport> selectInspectionReportList(InspectionReport inspectionReport)
    {
        return inspectionReportMapper.selectInspectionReportList(inspectionReport);
    }

    /**
     * 新增报工记录
     * 
     * @param inspectionReport 报工记录
     * @return 结果
     */
    @Override
    public int insertInspectionReport(InspectionReport inspectionReport)
    {
        inspectionReport.setCreateTime(DateUtils.getNowDate());
        return inspectionReportMapper.insertInspectionReport(inspectionReport);
    }

    /**
     * 修改报工记录
     * 
     * @param inspectionReport 报工记录
     * @return 结果
     */
    @Override
    public int updateInspectionReport(InspectionReport inspectionReport)
    {
        inspectionReport.setUpdateTime(DateUtils.getNowDate());
        return inspectionReportMapper.updateInspectionReport(inspectionReport);
    }

    /**
     * 批量删除报工记录
     * 
     * @param ids 需要删除的报工记录主键
     * @return 结果
     */
    @Override
    public int deleteInspectionReportByIds(Long[] ids)
    {
        return inspectionReportMapper.deleteInspectionReportByIds(ids);
    }

    /**
     * 删除报工记录信息
     * 
     * @param id 报工记录主键
     * @return 结果
     */
    @Override
    public int deleteInspectionReportById(Long id)
    {
        return inspectionReportMapper.deleteInspectionReportById(id);
    }
}
