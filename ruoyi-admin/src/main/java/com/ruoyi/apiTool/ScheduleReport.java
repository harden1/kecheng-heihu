package com.ruoyi.apiTool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.inspection.domain.InspectionReport;
import com.ruoyi.inspection.domain.InspectionSummary;
import com.ruoyi.inspection.service.IInspectionReportService;
import com.ruoyi.inspection.service.IInspectionSummaryService;
import com.ruoyi.system.service.ISysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * //TODO 记得写注释
 *
 * @author 汪敏
 * @date 2026.01.11 10:45
 */
@Component
public class ScheduleReport {
    @Autowired
    private IInspectionSummaryService inspectionSummaryService;
    @Autowired
    private IInspectionReportService inspectionReportService;
    @Autowired
    private ApiBatchReportForBlackLack batchReportForBlackLack;

    @Autowired
    private ISysConfigService configService;

    /*
     * 改为定时执行，每1分钟执行一组报工，如果定时器失效可以按钮激活

     *使用循环，一组存50条，
     *每分钟查询一次不良表，按时间降序查最早50条数据，每报一次延时0.5s
     *报工返回200后，标记成功并继续报工下一条
     *报工没有返回200，再次尝试，如还失败 标记失败，继续下一条

     *如果上一组没有报工完成，在单实例单线程中，任务计划没有执行完会跳过，直到执行完，可以手动控制，代码如下
     */
    public void goodReportBatch() {
        List<InspectionSummary> inspectionSummarys = inspectionSummaryService.selectInspectionSummaryListBySchedule(250);
//        System.out.println("按时间降序查最早50条良品数据" + inspectionSummarys);
        for (InspectionSummary inspectionSummary : inspectionSummarys) {
            String res = scheduleReportBatch(inspectionSummary,true);
            if (res.equals("-1")) {
                String res0 = scheduleReportBatch(inspectionSummary, false);
            }
            //延时0.5s
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //查询不良品表，按时间降序查最早50条数据
    public void badReportOne() {
        List<InspectionReport> inspectionReports = inspectionReportService.selectInspectionReportListForSchedule(250);
//        System.out.println("按时间降序查最早50条不良品数据" + inspectionReports);
        //批量查询50条不良品的主表
        //收集所有的summaryId
        List<Long> summaryIdList = inspectionReports.stream()
                .map(InspectionReport::getSummaryId)
                .collect(Collectors.toList());
        //批量查询50条不良品的主表
        if (summaryIdList.isEmpty()) {
            // 直接返回空结果或者抛异常
            return;
        } else {
            List<InspectionSummary> inspectionSummaryList = inspectionSummaryService.selectInspectionSummaryListByIds(summaryIdList);
//            System.out.println("按时间降序查最早50条不良品数据主表" + inspectionSummaryList);
            for (InspectionReport inspectionReport : inspectionReports) {
                //根据工单找到主表数据querySummaryByQrCode(params.getWorkOrderCode());
               for ( InspectionSummary inspectionSummary : inspectionSummaryList){
//                   System.out.println("匹配到工单：" + inspectionSummary.getQrCode()+"匹配到工单数据"+inspectionReport.getWorkOrderCode());
                   if (inspectionSummary.getQrCode().equals(inspectionReport.getWorkOrderCode())) {
//                       System.out.println("匹配到工单：" + inspectionSummary.getQrCode());
                       String res = scheduleReportBadItemOne(inspectionReport, inspectionSummary, true);
                       if (res.equals("-1")) {
                           String res0 = scheduleReportBadItemOne(inspectionReport, inspectionSummary, false);
                       }
                       //延时0.5s
                       try {
                           Thread.sleep(50);
                       } catch (InterruptedException e) {
                           e.printStackTrace();
                       }
                   }
               }
            }
        }
    }

    public String scheduleReportBatch(InspectionSummary inspectionSummary,boolean flag) {
        // 获取 reportJson（它是个 List）
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonStr = inspectionSummary.getApiReport();
        Map<String, Object> reportJson = null;
        try {
            reportJson = objectMapper.readValue(jsonStr, Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        long userId = Long.parseLong(reportJson.get("mesUserId").toString());
        String qrCode = reportJson.get("qrCode").toString();
        long reportUnitId = Long.parseLong(reportJson.get("unitId").toString());
        long reportProcessId = Long.parseLong(reportJson.get("processId").toString());
        long lineId = Long.parseLong(reportJson.get("materialLineId").toString());
        long materialId = Long.parseLong(reportJson.get("materialId").toString());
        long taskId = Long.parseLong(reportJson.get("taskId").toString());
        long  warehouseId = Long.parseLong(reportJson.get("warehouseId").toString());

        long batchNoId = 0L;
        Object batchNoIdObj = reportJson.get("batchNoId");
        if (batchNoIdObj != null) {
            String batchNoIdStr = batchNoIdObj.toString().trim();
            if (!batchNoIdStr.isEmpty() && !"null".equalsIgnoreCase(batchNoIdStr)) {
                batchNoId = Long.parseLong(batchNoIdStr);
            }
        }
        // batchNo 同理
        String batchNo = "";
        Object batchNoObj = reportJson.get("batchNo");
        if (batchNoObj != null) {
            String batchNoStr = batchNoObj.toString().trim();
            if (!"null".equalsIgnoreCase(batchNoStr)) {
                batchNo = batchNoStr;
            }
        }
        //时间
        String stopTime = "";
        if (reportJson.get("stopTime") == null) {
            stopTime = inspectionSummary.getStopTime();
        } else {
            stopTime = reportJson.get("stopTime").toString();
        }
        long timestamp = inspectionSummary.getCreateTime().getTime();
        long reportEndTime = System.currentTimeMillis();
        //报工数量1,质量不合格，扫码报工不合格
        int reportAmount = inspectionSummary.getTotalQuantity() - inspectionSummary.getDefectiveTotal();
        int qcStatus = 1;
        int reportType = 1;
//        long warehouseId =  Convert.toLong(configService.selectConfigByKey("warehouse_id"));

        Map<String, Object> res = batchReportForBlackLack.batchReportForBlackLack(
                userId,
                qrCode,
                reportUnitId,
                reportProcessId,
                reportAmount,
                lineId,
                materialId,
                qcStatus,
                reportType,
                taskId,
                stopTime,
                batchNoId,
                batchNo,
                timestamp,
                reportEndTime,
                warehouseId
        );
        //检验成功状态
        String message = res.get("message").toString();
        int code = (int) res.get("code");
        if (code != 200) {
            if ( flag){
                inspectionSummary.setApiDetail(message);
                inspectionSummary.setSuccessFlag(0L);
                inspectionSummaryService.updateInspectionSummaryNoSubfom(inspectionSummary);
                return "-1";
            }else{
                inspectionSummary.setApiDetail(message);
                inspectionSummary.setSuccessFlag(2L);
                inspectionSummaryService.updateInspectionSummaryNoSubfom(inspectionSummary);
                return "0";
            }

        }else{
            //更新主表
            if (res.get("message").equals("成功")) {
                inspectionSummary.setSuccessFlag(1L);
                inspectionSummary.setApiDetail(res.toString());
                inspectionSummaryService.updateInspectionSummaryNoSubfom(inspectionSummary);
                return "1";
            } else {
                if ( flag){
                    inspectionSummary.setSuccessFlag(0L);
                    inspectionSummary.setApiDetail(message);
                    inspectionSummaryService.updateInspectionSummaryNoSubfom(inspectionSummary);
                    return "-1";
                }else{
                    inspectionSummary.setSuccessFlag(2L);
                    inspectionSummary.setApiDetail(message);
                    inspectionSummaryService.updateInspectionSummaryNoSubfom(inspectionSummary);
                    return "0";
                }

            }
        }
    }

    public String scheduleReportBadItemOne(InspectionReport params, InspectionSummary inspectionSummary,boolean flag) {
        //查询主表匹配记录
//        InspectionSummary inspectionSummary = blacklackUserService.querySummaryByQrCode(params.getWorkOrderCode());
        // 获取 reportJson（它是个 List）
        String reportJsonStr = inspectionSummary.getApiReport();
        ObjectMapper mapperReport = new ObjectMapper();
        Map<String, Object> reportJson = new HashMap<>();
        try {
            reportJson = mapperReport.readValue(reportJsonStr, Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        long userId = Long.parseLong(reportJson.get("mesUserId").toString());
        String qrCode = reportJson.get("qrCode").toString();
        long reportUnitId = Long.parseLong(reportJson.get("unitId").toString());
        long reportProcessId = Long.parseLong(reportJson.get("processId").toString());
        long lineId = Long.parseLong(reportJson.get("materialLineId").toString());
        long materialId = Long.parseLong(reportJson.get("materialId").toString());
        long taskId = Long.parseLong(reportJson.get("taskId").toString());
        // Convert.toLong(configService.selectConfigByKey("warehouse_id"));
        long  warehouseId = Long.parseLong(reportJson.get("warehouseId").toString());
        String badItem = params.getDefectRemark();


        long batchNoId = 0L;
        Object batchNoIdObj = reportJson.get("batchNoId");
        if (batchNoIdObj != null) {
            String batchNoIdStr = batchNoIdObj.toString().trim();
            if (!batchNoIdStr.isEmpty() && !"null".equalsIgnoreCase(batchNoIdStr)) {
                batchNoId = Long.parseLong(batchNoIdStr);
            }
        }
        // batchNo 同理
        String batchNo = "";
        Object batchNoObj = reportJson.get("batchNo");
        if (batchNoObj != null) {
            String batchNoStr = batchNoObj.toString().trim();
            if (!"null".equalsIgnoreCase(batchNoStr)) {
                batchNo = batchNoStr;
            }
        }
        //时间
        String stopTime = "";
        Date createTime = params.getCreateTime();
        long reportStartTime = createTime.getTime();
        long reportEndTime = 0;
        //报工数量1,质量不合格，扫码报工不合格
        int reportAmount = 0;
        Object qtyObj = params.getQuantity();

            if (qtyObj instanceof Number) {
                reportAmount = ((Number) qtyObj).intValue();
            }
        int qcStatus = 4;
        int reportType = 4;


        //报工
        Map<String, Object> res = batchReportForBlackLack.batchReportForBlackLackOne(
                userId,
                qrCode,
                reportUnitId,
                reportProcessId,
                reportAmount,
                lineId,
                materialId,
                qcStatus,
                reportType,
                taskId,
                badItem,
                stopTime,
                batchNoId,
                batchNo,
                reportStartTime,
                reportEndTime,
                warehouseId
        );
//        检验成功状态
        String message = res.get("message").toString();
        int code = (int) res.get("code");
        if (code != 200) {
            if ( flag){
                params.setResultJson(message);
                params.setSuccessFlag(0);
                inspectionReportService.updateInspectionReport(params);
                return "-1";
            }else {
                params.setResultJson(message);
                params.setSuccessFlag(2);
                inspectionReportService.updateInspectionReport(params);
                return "0";
            }

        }else{
            //更新主表
            if (res.get("message").equals("成功")) {
                params.setSuccessFlag(1);
                params.setResultJson(res.toString());
                inspectionReportService.updateInspectionReport(params);
                return "1";
            } else {
                if ( flag){
                    params.setResultJson(message);
                    params.setSuccessFlag(0);
                    inspectionReportService.updateInspectionReport(params);
                    return "-1";
                }else {
                    params.setResultJson(message);
                    params.setSuccessFlag(2);
                    inspectionReportService.updateInspectionReport(params);
                    return "0";
                }
            }
        }
    }
}
