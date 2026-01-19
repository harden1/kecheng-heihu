package com.ruoyi.inspection.service.impl;

import java.util.*;

import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

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
        return inspectionSummaryMapper.selectInspectionSummaryByIdNoDetail(id);
    }
    /**
     * 查询镜检统计主
     *
     * @param id 镜检统计主主键
     * @return 镜检统计主
     */
    @Override
    public InspectionSummary selectInspectionSummaryByIdNoDetailAndBadItems(Long id)
    {
        return inspectionSummaryMapper.selectInspectionSummaryByIdNoDetailAndBadItems(id);
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

    @Override
    public List<InspectionSummary> selectInspectionSummaryListBySchedule(int num) {
        return inspectionSummaryMapper.selectInspectionSummaryListBySchedule(num);
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
     * 修改镜检统计主
     *
     * @param inspectionSummary 镜检统计主
     * @return 结果
     */
    @Transactional
    @Override
    public int updateInspectionSummaryNoSubfom(InspectionSummary inspectionSummary)
    {
        return inspectionSummaryMapper.updateInspectionSummary(inspectionSummary);
    }
//    public int updateInspectionSummary(InspectionSummary inspectionSummary) {
//        // 更新时间
//        inspectionSummary.setUpdateTime(DateUtils.getNowDate());
//
//        List<InspectionReport> newReports = inspectionSummary.getInspectionReportList();
//        if (newReports != null && !newReports.isEmpty()) {
//            // 查询已有子表记录
//            List<InspectionReport> oldReports = inspectionSummaryMapper.selectInspectionReportBySummaryId(inspectionSummary.getId());
//
//            // 用 Map 存储老记录，方便对比
//            Map<Long, InspectionReport> oldMap = oldReports.stream()
//                    .collect(Collectors.toMap(InspectionReport::getId, r -> r));
//
//            List<InspectionReport> toInsert = new ArrayList<>();
//            List<InspectionReport> toUpdate = new ArrayList<>();
//            Set<Long> newIds = new HashSet<>();
//
//            for (InspectionReport report : newReports) {
//                report.setSummaryId(inspectionSummary.getId());
//                if (report.getId() == null) {
//                    // 新增记录
//                    toInsert.add(report);
//                } else if (oldMap.containsKey(report.getId())) {
//                    // 修改记录
//                    toUpdate.add(report);
//                    newIds.add(report.getId());
//                }
//            }
//
//            // 删除已删除的记录
//            List<Long> toDelete = oldReports.stream()
//                    .map(InspectionReport::getId)
//                    .filter(id -> !newIds.contains(id))
//                    .collect(Collectors.toList());
//            if (!toDelete.isEmpty()) {
//                inspectionSummaryMapper.deleteInspectionReportBatch(toDelete);
//            }
//
//            // 批量插入
//            if (!toInsert.isEmpty()) {
//                inspectionSummaryMapper.batchInspectionReport(toInsert);
//            }
//
//            // 批量更新
//            if (!toUpdate.isEmpty()) {
//                inspectionSummaryMapper.batchUpdateInspectionReport(toUpdate);
//            }
//        } else {
//            // 如果新列表为空，则删除所有子表
//            inspectionSummaryMapper.deleteInspectionReportBySummaryId(inspectionSummary.getId());
//        }
//
//        // 更新主表
//        return inspectionSummaryMapper.updateInspectionSummary(inspectionSummary);
//    }

//

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

    @Override
    public int updateStopTime(int mainId, String stopTime) {
        int rows = inspectionSummaryMapper.updateStopTime(mainId, stopTime);
        return rows;
    }

    @Override
    public List<InspectionSummary> selectInspectionSummaryListByIds(List<Long> summaryIdList) {
        return inspectionSummaryMapper.selectInspectionSummaryListByIds(summaryIdList);
    }

    @Override
    public InspectionSummary selectInspectionSummaryByIdNoDetail(long mainId) {
        return inspectionSummaryMapper.selectInspectionSummaryByIdNoDetail(mainId);
    }

    @Override
    public int updateInspectionSummaryForDefect(Long mainId, int no) {
       return inspectionSummaryMapper.increaseDefectByNo(mainId,no);
    }


    /**
     * 新增报工记录信息
     * 
     * @param inspectionSummary 镜检统计主对象
     */
    public void insertInspectionReport(InspectionSummary inspectionSummary)
    {
        List<InspectionReport> inspectionReportList = inspectionSummary.getInspectionReportList();
        System.out.println("新增报工记录信息"+inspectionReportList);
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
