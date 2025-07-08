package com.ruoyi.inspection.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import com.ruoyi.common.utils.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.inspection.domain.InspectionReport;
import com.ruoyi.inspection.mapper.InspectionSummaryMapper;
import com.ruoyi.inspection.domain.InspectionSummary;
import com.ruoyi.inspection.service.IInspectionSummaryService;

/**
 * 镜检统计主Service业务层处理
 * 
 * @author w
 * @date 2025-07-08
 */
@Service
public class InspectionSummaryServiceImpl implements IInspectionSummaryService 
{
    @Autowired
    private InspectionSummaryMapper inspectionSummaryMapper;

    /**
     * 查询镜检统计主
     * 
     * @param id 镜检统计主主键
     * @return 镜检统计主
     */
    @Override
    public InspectionSummary selectInspectionSummaryById(Long id)
    {
        return inspectionSummaryMapper.selectInspectionSummaryById(id);
    }

    /**
     * 查询镜检统计主列表
     * 
     * @param inspectionSummary 镜检统计主
     * @return 镜检统计主
     */
    @Override
    public List<InspectionSummary> selectInspectionSummaryList(InspectionSummary inspectionSummary)
    {
        return inspectionSummaryMapper.selectInspectionSummaryList(inspectionSummary);
    }

    /**
     * 新增镜检统计主
     * 
     * @param inspectionSummary 镜检统计主
     * @return 结果
     */
    @Transactional
    @Override
    public int insertInspectionSummary(InspectionSummary inspectionSummary)
    {
        inspectionSummary.setCreateTime(DateUtils.getNowDate());
        int rows = inspectionSummaryMapper.insertInspectionSummary(inspectionSummary);
        insertInspectionReport(inspectionSummary);
        return rows;
    }

    /**
     * 修改镜检统计主
     * 
     * @param inspectionSummary 镜检统计主
     * @return 结果
     */
    @Transactional
    @Override
    public int updateInspectionSummary(InspectionSummary inspectionSummary)
    {
        inspectionSummary.setUpdateTime(DateUtils.getNowDate());
        inspectionSummaryMapper.deleteInspectionReportBySummaryId(inspectionSummary.getId());
        insertInspectionReport(inspectionSummary);
        return inspectionSummaryMapper.updateInspectionSummary(inspectionSummary);
    }

    /**
     * 批量删除镜检统计主
     * 
     * @param ids 需要删除的镜检统计主主键
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteInspectionSummaryByIds(Long[] ids)
    {
        inspectionSummaryMapper.deleteInspectionReportBySummaryIds(ids);
        return inspectionSummaryMapper.deleteInspectionSummaryByIds(ids);
    }

    /**
     * 删除镜检统计主信息
     * 
     * @param id 镜检统计主主键
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteInspectionSummaryById(Long id)
    {
        inspectionSummaryMapper.deleteInspectionReportBySummaryId(id);
        return inspectionSummaryMapper.deleteInspectionSummaryById(id);
    }

    /**
     * 新增报工记录信息
     * 
     * @param inspectionSummary 镜检统计主对象
     */
    public void insertInspectionReport(InspectionSummary inspectionSummary)
    {
        List<InspectionReport> inspectionReportList = inspectionSummary.getInspectionReportList();
        Long id = inspectionSummary.getId();
        if (StringUtils.isNotNull(inspectionReportList))
        {
            List<InspectionReport> list = new ArrayList<InspectionReport>();
            for (InspectionReport inspectionReport : inspectionReportList)
            {
                inspectionReport.setSummaryId(id);
                list.add(inspectionReport);
            }
            if (list.size() > 0)
            {
                inspectionSummaryMapper.batchInspectionReport(list);
            }
        }
    }
}
