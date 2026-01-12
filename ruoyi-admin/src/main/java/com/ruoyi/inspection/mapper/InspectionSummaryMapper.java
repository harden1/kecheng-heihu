package com.ruoyi.inspection.mapper;

import java.util.List;
import com.ruoyi.inspection.domain.InspectionSummary;
import com.ruoyi.inspection.domain.InspectionReport;
import org.apache.ibatis.annotations.Param;

/**
 * 镜检统计主Mapper接口
 * 
 * @author w
 * @date 2025-07-08
 */
public interface InspectionSummaryMapper 
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
    public List<InspectionSummary> selectInspectionSummaryList1(InspectionSummary inspectionSummary);

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
     * 删除镜检统计主
     * 
     * @param id 镜检统计主主键
     * @return 结果
     */
    public int deleteInspectionSummaryById(Long id);

    /**
     * 批量删除镜检统计主
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteInspectionSummaryByIds(Long[] ids);

    /**
     * 批量删除报工记录
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteInspectionReportBySummaryIds(Long[] ids);
    
    /**
     * 批量新增报工记录
     * 
     * @param inspectionReportList 报工记录列表
     * @return 结果
     */
    public int batchInspectionReport(List<InspectionReport> inspectionReportList);
    

    /**
     * 通过镜检统计主主键删除报工记录信息
     * 
     * @param id 镜检统计主ID
     * @return 结果
     */
    public int deleteInspectionReportBySummaryId(Long id);

    public List<InspectionSummary> selectInspectionSummaryByQrCode(String taskCode);

    String selectStopTime(String qrCode);

    int updateStopTime(@Param("id") int id, @Param("stopTime") String stopTime);

    List<InspectionSummary> selectInspectionSummaryListBySchedule( @Param("num") int num);

    List<InspectionSummary> selectInspectionSummaryListByIds( @Param("ids")List<Long> summaryIdList);

    InspectionSummary selectInspectionSummaryByIdNoDetail(long mainId);

    List<InspectionReport> selectInspectionReportBySummaryId(Long id);

    void deleteInspectionReportBatch(List<Long> toDelete);

    void batchUpdateInspectionReport(List<InspectionReport> toUpdate);

    int increaseDefectByNo(
            @Param("summaryId") Long summaryId,
            @Param("no") Integer no
    );
}
