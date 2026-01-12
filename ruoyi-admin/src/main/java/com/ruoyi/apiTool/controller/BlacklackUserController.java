package com.ruoyi.apiTool.controller;

import java.text.SimpleDateFormat;
import java.util.*;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ruoyi.apiTool.*;
import com.ruoyi.badItem.domain.CreateBadItemsTable;
import com.ruoyi.badItem.mapper.CreateBadItemsTableMapper;
import com.ruoyi.inspection.domain.InspectionReport;
import com.ruoyi.inspection.domain.InspectionSummary;
import com.ruoyi.inspection.service.IInspectionRecordService;
import com.ruoyi.inspection.service.IInspectionReportService;
import com.ruoyi.inspection.service.IInspectionSummaryService;
import com.ruoyi.system.domain.SysUserPost;
import com.ruoyi.system.mapper.SysConfigMapper;
import com.ruoyi.system.service.ISysUserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.apiTool.domain.BlacklackUser;
import com.ruoyi.apiTool.service.IBlacklackUserService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.apiTool.domain.ReportRecord;

/**
 * 黑湖用户信息Controller
 *
 * @author w
 * @date 2025-07-17
 */
@RestController
@RequestMapping("/blacklackUser/BlacklackUser")
public class BlacklackUserController extends BaseController {
    @Autowired
    private IBlacklackUserService blacklackUserService;
    @Autowired
    private ApiReportRecordForBlackLack reportRecordForBlackLack;
    @Autowired
    private ApiMaterialDetailForBlackLack materialDetailForBlackLack;
    @Autowired
    private ApiProcessListForBlackLack processListForBlackLack;
    @Autowired
    private IInspectionSummaryService inspectionSummaryService;
    @Autowired
    private IInspectionRecordService inspectionRecordService;
    @Autowired
    private IInspectionReportService inspectionReportService;
    @Autowired
    private AccessTokenService accessTokenService;
    @Autowired
    private ApiBatchReportForBlackLack batchReportForBlackLack;
    @Autowired
    private ApiReportRecordForBlackLack apiReportRecordForBlackLack;
    @Autowired
    private ISysUserService sysUserService;
    @Autowired
    private SysConfigMapper sysConfigMapper;
    /**
     * 扫码二维码结果
     * @param taskCode 二维码信息
     * @return
     */

    @PostMapping("/queryScanTaskResult")
    public AjaxResult checkUserToBlackLack(@RequestParam("taskCode") String taskCode) {
//        System.out.println("收到的数据" + taskCode);

        Map<String, String> reportRecordResult = null;
        //报工记录
        reportRecordResult = reportRecordForBlackLack.getReportRecordDetailForBlackLack(taskCode);
        if (reportRecordResult == null){
            return error("该条码没有查询到报工记录，请检查或者联系管理员");
        }
        //查主表记录：并返回给前端做提示
        try {
            List<InspectionSummary> list = blacklackUserService.selectInspectionMainByQrcode(taskCode);

            InspectionSummary inspectionSummary = null;
            if (list != null && !list.isEmpty()) {
                inspectionSummary = list.get(0);
                String apiDetail = inspectionSummary.getApiDetail();
                reportRecordResult.put("flag", apiDetail != null ? apiDetail : "0");
            } else {
                reportRecordResult.put("flag", "-1");
            }

            if (inspectionSummary != null) {
                reportRecordResult.put("creatBy", inspectionSummary.getCreateBy() != null ? inspectionSummary.getCreateBy() : "");
                if (inspectionSummary.getCreateTime() != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    reportRecordResult.put("creatDate", sdf.format(inspectionSummary.getCreateTime()));
                } else {
                    reportRecordResult.put("creatDate", "");
                }
            } else {
                reportRecordResult.put("creatBy", "");
                reportRecordResult.put("creatDate", "");
            }
        } catch (Exception e) {
//            System.out.println("查询二维码对应的检验信息异常:" + taskCode + " => " + e.getMessage());
            reportRecordResult.put("creatBy", "");
            reportRecordResult.put("creatDate", "");
            reportRecordResult.put("flag", "-1");
        }

        //工序列表
       List<Map<String, String>> processList = processListForBlackLack.getProcessListForBlackLack(reportRecordResult.get("workOrderId"));
        //处理工序列表，判断是否在工序设置中，如果有则继续下一步，如果没有则提示检查配置
//        System.out.println("工序列表：" + processList);
        //查询前工序配置
        String previousProcessSetting = blacklackUserService.queryPreviousProcessSetting();
        List<String> previousProcessSettingList = Arrays.asList(previousProcessSetting.split("/"));
//        System.out.println("查询前工序配置：" + previousProcessSettingList);
//        String processCode = reportRecordResult.get("processCode");
        for (String pName : previousProcessSettingList) {
            for (Map<String, String> process : processList) {
                System.out.println("工序查找：" + process + " == " + pName);
                if (pName.equals(process.get("processCode"))) {
//                    System.out.println("找到镜检工序：" + process.get("processCode"));
                    reportRecordResult.put("taskId", process.get("taskId"));
                    reportRecordResult.put("taskCode", process.get("taskCode"));
                    reportRecordResult.put("processId", process.get("processId"));
                    reportRecordResult.put("processName", process.get("processName"));
                    reportRecordResult.put("status", process.get("status"));
                    reportRecordResult.put("statusCode", process.get("statusCode"));
                    if (Objects.equals(process.get("statusCode"), "4")|| Objects.equals(process.get("statusCode"), "5")){
                        continue;
                    }
                    reportRecordResult.put("workOrderCode", process.get("workOrderCode"));
                    reportRecordResult.put("materialId", process.get("materialId"));
                    reportRecordResult.put("lineId", process.get("lineId"));
                    reportRecordResult.put("processCode", process.get("processCode"));
                    System.out.println("扫码反返回数据：" + reportRecordResult);
                    //返回确认信息
                    return success(reportRecordResult);
                }
            }
//            if (pName.equals(processCode)) {
//                System.out.println("当前工序：" + processCode);
//                //返回确认信息
//                return success(reportRecordResult);
//            }
        }
        return error("该物料未找到镜检工序，或镜检已完工，请检查物料的工序、镜检软件后台工序编码，或联系管理员！");
    }

    //点击开始报工，主表新增记录
    //返回给前端需要的数据//包括：物料信息，主表当前记录，工单信息
    @PostMapping("/addOrReadInspectionMain")
    public AjaxResult addOrReadInspectionMain(@RequestBody ReportRecord reportRecord) {
//        System.out.println("-------------------------------------");
//        System.out.println("新增或读取数据：" + reportRecord);
//        System.out.println("查询的数据"+blacklackUserService.querySummaryByQrCode(reportRecord.getQrCode()) );
        //判断页面刷新是否重复传了接口，使用qrcode查询，如果存在，则返回主表记录
        List<InspectionSummary> inspectionSummarys = blacklackUserService.selectInspectionMainByQrcode(reportRecord.getQrCode());
        if (inspectionSummarys != null && !inspectionSummarys.isEmpty()) {
            InspectionSummary inspectionSummary = inspectionSummarys.get(0);
            // 查询防抖时间
            String debounce = sysConfigMapper.selectDebounce();
            inspectionSummary.setDebounce(debounce);
            return AjaxResult.success(inspectionSummary);
        } else {
            Map<String, String> materialDetailResult1 = materialDetailForBlackLack.getMaterialDetailForBlackLack(reportRecord.getMaterialCode());
            materialDetailResult1.put("batchNo", reportRecord.getBatchNo());
            materialDetailResult1.put("batchNoId", reportRecord.getBatchNoId());
            Map<String, String> processResult3 =  new HashMap<>();
//            processResult3 = processListForBlackLack.getProcessListForBlackLack(reportRecord.getWorkOrderId(), reportRecord.getProcessId());
//            System.out.println("工序列表精确查询：" + processResult3);
            processResult3.put("flag", reportRecord.getFlag());
//            System.out.println("扫码用户：" + reportRecord.getMesUserName());
            String mesUserId  =  reportRecord.getMesUserId();
            if (mesUserId == null || mesUserId.equals("")) {
                mesUserId = sysUserService.getMesUserId(reportRecord.getMesUserName());
            }
            if (mesUserId == null || mesUserId.equals("")) {
                //mes用户不存在
                return error("用户不存在,请重新登录");
            }
//            System.out.println("mesUserId: " + mesUserId);
            processResult3.put("batchNo", reportRecord.getBatchNo());
            processResult3.put("mesUserId",mesUserId);
            processResult3.put("amount", reportRecord.getAmount());
            processResult3.put("qrCode", reportRecord.getQrCode());
            processResult3.put("color", materialDetailResult1.get("color"));
            processResult3.put("unitId", materialDetailResult1.get("unitId"));
            processResult3.put("taskId", reportRecord.getTaskId());
            processResult3.put("taskCode", reportRecord.getTaskCode());
            processResult3.put("processId", reportRecord.getProcessId());
            processResult3.put("processName", reportRecord.getProcessName());
            processResult3.put("status", reportRecord.getStatus());
            processResult3.put("workOrderCode", reportRecord.getWorkOrderCode());
            processResult3.put("materialId", reportRecord.getMaterialId());
            processResult3.put("lineId", reportRecord.getLineId());
            processResult3.put("processCode", reportRecord.getProcessCode());
            processResult3.put("degrees", reportRecord.getSpecification());
//            System.out.println("度数数据degrees: " +  reportRecord.getSpecification());
            //返回确认信息
            //新建、修改主表记录，返回给前端显示
            InspectionSummary result = blacklackUserService.addOrUpdateInspectionMain(processResult3, reportRecord);
            return success(result);
        }
    }
    /**
     * 重新批量良品报工
     */


    @PostMapping("/reReportBadItemOne")
    public AjaxResult reReportBadItemOne(@RequestBody InspectionReport params) {
//        System.out.println("map对象数据" + params);
        //查询主表匹配记录
        InspectionSummary inspectionSummary = blacklackUserService.querySummaryByQrCode(params.getWorkOrderCode());
        // 获取 reportJson（它是个 List）
        String reportJsonStr = inspectionSummary.getApiReport();
        ObjectMapper mapperReport = new ObjectMapper();
        Map<String, Object> reportJson = new HashMap<>();
        try {
            reportJson = mapperReport.readValue(reportJsonStr, Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
//        System.out.println("reportJson = " + reportJson);
        long userId = Long.parseLong(reportJson.get("mesUserId").toString());
        String qrCode = "";
        long reportUnitId = Long.parseLong(reportJson.get("unitId").toString());
        long reportProcessId = Long.parseLong(reportJson.get("processId").toString());
        long lineId = Long.parseLong(reportJson.get("materialLineId").toString());
        long materialId = Long.parseLong(reportJson.get("materialId").toString());
        long taskId = Long.parseLong(reportJson.get("taskId").toString());
        String badItem = params.getDefectRemark();
        long batchNoId = 0L;
        if (reportJson.get("batchNoId") != null) {
            batchNoId = Long.parseLong(reportJson.get("batchNoId").toString());
        }
        String batchNo = "";
        if (reportJson.get("batchNo") != null) {
            batchNo = reportJson.get("batchNo").toString();
        }
        //时间
        String stopTime = "";
        long reportStartTime = 0;
        long reportEndTime = 0;
        //报工数量1,质量不合格，扫码报工不合格
        int reportAmount = 1;
        int qcStatus = 4;
        int reportType = 4;
//        System.out.println("color = " + badItem);
//        System.out.println("reportJson = " + reportJson);
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
                reportEndTime
        );
//        检验成功状态
        String message = res.get("message").toString();
        int code = (int) res.get("code");
//        System.out.println("message1 = " + message);
//        System.out.println(" code1 = " + code);
        if (code != 200) {
            return AjaxResult.error(message);
        }
//        更新这条报工记录成功或失败
        params.setSuccessFlag(1);
        inspectionReportService.updateInspectionReport(params);
        return AjaxResult.success("重新报工成功");
    }
//    /**
//     * 正常单个不良报工
//     * @param params
//     * @return
//     */
//    @PostMapping("/reportBadItemOne")
//    public AjaxResult reportBadItemOne(@RequestBody Map<String, Object> params) {
//        // 获取 reportJson（它是个 List）
//        Map<String, Object> reportJson = (Map<String, Object>) params.get("reportJson");
////        System.out.println("reportJson = " + reportJson + "userID");
////        long userId = 0L;
////        try {
////            userId = Long.parseLong(reportJson.get("mesUserId").toString());
////        } catch (Exception e) {
////            return AjaxResult.error("报工失败，用户ID缺失，请重新登陆扫码，或者联系管理员");
////        }
////        long userId = Long.parseLong(reportJson.get("mesUserId").toString());
////        String qrCode = "";
////        long reportUnitId = Long.parseLong(reportJson.get("unitId").toString());
////        long reportProcessId = Long.parseLong(reportJson.get("processId").toString());
////        long lineId = Long.parseLong(reportJson.get("materialLineId").toString());
////        long materialId = Long.parseLong(reportJson.get("materialId").toString());
////        long taskId = Long.parseLong(reportJson.get("taskId").toString());
//        String badItem = (String) params.get("color");
//        int no = (int) params.get("no");
//        int mainId = (int) params.get("mainId");
////        long batchNoId = 0L;
////        if (reportJson.get("batchNoId") != null) {
////            batchNoId = Long.parseLong(reportJson.get("batchNoId").toString());
////        }
////        String batchNo = "";
////        if (reportJson.get("batchNo") != null) {
////            batchNo = reportJson.get("batchNo").toString();
////        }
////
////        //时间
////        String stopTime = "";
////        long reportStartTime = Long.parseLong(reportJson.get("reportStartTime").toString());
////        long reportEndTime = Long.parseLong(reportJson.get("reportEndTime").toString());
//        //报工数量1,质量不合格，扫码报工不合格
////        int reportAmount = 1;
////        int qcStatus = 4;
////        int reportType = 4;
////        System.out.println("color = " + badItem);
////        System.out.println("reportJson = " + reportJson);
//
////        Map<String, Object> res = batchReportForBlackLack.batchReportForBlackLackOne(
////                userId,
////                qrCode,
////                reportUnitId,
////                reportProcessId,
////                reportAmount,
////                lineId,
////                materialId,
////                qcStatus,
////                reportType,
////                taskId,
////                badItem,
////                stopTime,
////                batchNoId,
////                batchNo,
////                reportStartTime,
////                reportEndTime
////        );
////        System.out.println("res = " + res);
//        //检验成功状态
////        String message = res.get("message").toString();
////        int code = (int) res.get("code");
////        System.out.println("消息 = " + message);
////        System.out.println(" 代码code = " + code);
////        if (code != 200) {
////            return AjaxResult.error(message);
////        }
//        Map<String, Object> res = new HashMap<>();
//        //创建子表
//        inspectionReportService.insertReportOne(res, reportJson, mainId, no, badItem);
////        System.out.println("创建子表成功");
//        //更新主表
//        InspectionSummary inspectionSummary = inspectionSummaryService.selectInspectionSummaryById((long) mainId);
//        switch (no) {
//            case 0:
//                inspectionSummary.setDefect1(inspectionSummary.getDefect1() + 1);
//                break;
//            case 1:
//                inspectionSummary.setDefect2(inspectionSummary.getDefect2() + 1);
//                break;
//            case 2:
//                inspectionSummary.setDefect3(inspectionSummary.getDefect3() + 1);
//                break;
//            case 3:
//                inspectionSummary.setDefect4(inspectionSummary.getDefect4() + 1);
//                break;
//            case 4:
//                inspectionSummary.setDefect5(inspectionSummary.getDefect5() + 1);
//                break;
//            case 5:
//                inspectionSummary.setDefect6(inspectionSummary.getDefect6() + 1);
//                break;
//            case 6:
//                inspectionSummary.setDefect7(inspectionSummary.getDefect7() + 1);
//                break;
//            case 7:
//                inspectionSummary.setDefect8(inspectionSummary.getDefect8() + 1);
//                break;
//            case 8:
//                inspectionSummary.setDefect9(inspectionSummary.getDefect9() + 1);
//                break;
//            case 9:
//                inspectionSummary.setDefect10(inspectionSummary.getDefect10() + 1);
//                break;
//            case 10:
//                inspectionSummary.setDefect11(inspectionSummary.getDefect11() + 1);
//                break;
//            case 11:
//                inspectionSummary.setDefect12(inspectionSummary.getDefect12() + 1);
//                break;
//            case 12:
//                inspectionSummary.setDefect13(inspectionSummary.getDefect13() + 1);
//                break;
//            case 13:
//                inspectionSummary.setDefect14(inspectionSummary.getDefect14() + 1);
//                break;
//            case 14:
//                inspectionSummary.setDefect15(inspectionSummary.getDefect15() + 1);
//                break;
//            case 15:
//                inspectionSummary.setDefect16(inspectionSummary.getDefect16() + 1);
//                break;
//            case 16:
//                inspectionSummary.setDefect17(inspectionSummary.getDefect17() + 1);
//                break;
//            case 17:
//                inspectionSummary.setDefect18(inspectionSummary.getDefect18() + 1);
//                break;
//            case 18:
//                inspectionSummary.setDefect19(inspectionSummary.getDefect19() + 1);
//                break;
//            case 19:
//                inspectionSummary.setDefect20(inspectionSummary.getDefect20() + 1);
//                break;
//        }
//        inspectionSummary.setDefectiveTotal(inspectionSummary.getDefectiveTotal() + 1);
//        int i = inspectionSummaryService.updateInspectionSummary(inspectionSummary);
//        //查询主表记录
//
//        Map<String, Object> result1 = new HashMap<>();
//        if (i > 0) {
//            inspectionSummary.setInspectionReportList(null);
//            result1.put("summary", inspectionSummary);
//        }
//        return AjaxResult.success(result1);
//    }

//    /**
//     * 正常单个不良报工
//     * @param params
//     * @return
//     */
//    @PostMapping("/reportBadItemOne")
//    public AjaxResult reportBadItemOne(@RequestBody Map<String, Object> params) {
//        long start = System.currentTimeMillis(); // 方法开始时间
//
//        // 1. 获取参数
//        long t1 = System.currentTimeMillis();
//        Map<String, Object> reportJson = (Map<String, Object>) params.get("reportJson");
//        String badItem = (String) params.get("color");
//        int no = (int) params.get("no");
//        int mainId = (int) params.get("mainId");
//        long t2 = System.currentTimeMillis();
//        System.out.println("参数解析耗时: " + (t2 - t1) + "ms");
//
//        // 2. 创建子表
//        long t3 = System.currentTimeMillis();
//        Map<String, Object> res = new HashMap<>();
//        inspectionReportService.insertReportOne(res, reportJson, mainId, no, badItem);
//        long t4 = System.currentTimeMillis();
//        System.out.println("insertReportOne 耗时: " + (t4 - t3) + "ms");
//
//        // 3. 查询主表
//        long t5 = System.currentTimeMillis();
//        InspectionSummary inspectionSummary = inspectionSummaryService.selectInspectionSummaryByIdNoDetail((long) mainId);
//        long t6 = System.currentTimeMillis();
//        System.out.println("selectInspectionSummaryById 耗时: " + (t6 - t5) + "ms");
//
//        // 4. 更新对应 defect 字段（Java 8 传统 switch）
//        long t7 = System.currentTimeMillis();
//        switch (no) {
//            case 0:
//                inspectionSummary.setDefect1(inspectionSummary.getDefect1() + 1);
//                break;
//            case 1:
//                inspectionSummary.setDefect2(inspectionSummary.getDefect2() + 1);
//                break;
//            case 2:
//                inspectionSummary.setDefect3(inspectionSummary.getDefect3() + 1);
//                break;
//            case 3:
//                inspectionSummary.setDefect4(inspectionSummary.getDefect4() + 1);
//                break;
//            case 4:
//                inspectionSummary.setDefect5(inspectionSummary.getDefect5() + 1);
//                break;
//            case 5:
//                inspectionSummary.setDefect6(inspectionSummary.getDefect6() + 1);
//                break;
//            case 6:
//                inspectionSummary.setDefect7(inspectionSummary.getDefect7() + 1);
//                break;
//            case 7:
//                inspectionSummary.setDefect8(inspectionSummary.getDefect8() + 1);
//                break;
//            case 8:
//                inspectionSummary.setDefect9(inspectionSummary.getDefect9() + 1);
//                break;
//            case 9:
//                inspectionSummary.setDefect10(inspectionSummary.getDefect10() + 1);
//                break;
//            case 10:
//                inspectionSummary.setDefect11(inspectionSummary.getDefect11() + 1);
//                break;
//            case 11:
//                inspectionSummary.setDefect12(inspectionSummary.getDefect12() + 1);
//                break;
//            case 12:
//                inspectionSummary.setDefect13(inspectionSummary.getDefect13() + 1);
//                break;
//            case 13:
//                inspectionSummary.setDefect14(inspectionSummary.getDefect14() + 1);
//                break;
//            case 14:
//                inspectionSummary.setDefect15(inspectionSummary.getDefect15() + 1);
//                break;
//            case 15:
//                inspectionSummary.setDefect16(inspectionSummary.getDefect16() + 1);
//                break;
//            case 16:
//                inspectionSummary.setDefect17(inspectionSummary.getDefect17() + 1);
//                break;
//            case 17:
//                inspectionSummary.setDefect18(inspectionSummary.getDefect18() + 1);
//                break;
//            case 18:
//                inspectionSummary.setDefect19(inspectionSummary.getDefect19() + 1);
//                break;
//            case 19:
//                inspectionSummary.setDefect20(inspectionSummary.getDefect20() + 1);
//                break;
//            default:
//                break;
//        }
//        inspectionSummary.setDefectiveTotal(inspectionSummary.getDefectiveTotal() + 1);
//        long t8 = System.currentTimeMillis();
//        System.out.println("更新 defect 字段耗时: " + (t8 - t7) + "ms");
//
//        // 5. 更新主表
//        long t9 = System.currentTimeMillis();
//        int i = inspectionSummaryService.updateInspectionSummary(inspectionSummary);
//        long t10 = System.currentTimeMillis();
//        System.out.println("updateInspectionSummary 耗时: " + (t10 - t9) + "ms");
//
//        // 6. 组装返回结果
//        long t11 = System.currentTimeMillis();
//        Map<String, Object> result1 = new HashMap<>();
//        if (i > 0) {
//            inspectionSummary.setInspectionReportList(null);
//            result1.put("summary", inspectionSummary);
//        }
//        long t12 = System.currentTimeMillis();
//        System.out.println("组装返回结果耗时: " + (t12 - t11) + "ms");
//
//        long end = System.currentTimeMillis();
//        System.out.println("整个方法总耗时: " + (end - start) + "ms");
//
//        return AjaxResult.success(result1);
//    }

    @PostMapping("/reportBadItemOne")
    public AjaxResult reportBadItemOne(@RequestBody Map<String, Object> params) {
        // 获取 reportJson（它是个 List）
        Map<String, Object> reportJson = (Map<String, Object>) params.get("reportJson");
        String badItem = (String) params.get("color");
        int no = (int) params.get("no");
        int mainId = (int) params.get("mainId");
        Map<String, Object> res = new HashMap<>();
        //创建子表
        inspectionReportService.insertReportOne(res, reportJson, mainId, no, badItem);
        int updateFlag = inspectionSummaryService.updateInspectionSummaryForDefect((long) mainId,no);
//        //更新主表
//        InspectionSummary inspectionSummary = inspectionSummaryService.selectInspectionSummaryById((long) mainId);
//        switch (no) {
//            case 0:
//                inspectionSummary.setDefect1(inspectionSummary.getDefect1() + 1);
//                break;
//            case 1:
//                inspectionSummary.setDefect2(inspectionSummary.getDefect2() + 1);
//                break;
//            case 2:
//                inspectionSummary.setDefect3(inspectionSummary.getDefect3() + 1);
//                break;
//            case 3:
//                inspectionSummary.setDefect4(inspectionSummary.getDefect4() + 1);
//                break;
//            case 4:
//                inspectionSummary.setDefect5(inspectionSummary.getDefect5() + 1);
//                break;
//            case 5:
//                inspectionSummary.setDefect6(inspectionSummary.getDefect6() + 1);
//                break;
//            case 6:
//                inspectionSummary.setDefect7(inspectionSummary.getDefect7() + 1);
//                break;
//            case 7:
//                inspectionSummary.setDefect8(inspectionSummary.getDefect8() + 1);
//                break;
//            case 8:
//                inspectionSummary.setDefect9(inspectionSummary.getDefect9() + 1);
//                break;
//            case 9:
//                inspectionSummary.setDefect10(inspectionSummary.getDefect10() + 1);
//                break;
//            case 10:
//                inspectionSummary.setDefect11(inspectionSummary.getDefect11() + 1);
//                break;
//            case 11:
//                inspectionSummary.setDefect12(inspectionSummary.getDefect12() + 1);
//                break;
//            case 12:
//                inspectionSummary.setDefect13(inspectionSummary.getDefect13() + 1);
//                break;
//            case 13:
//                inspectionSummary.setDefect14(inspectionSummary.getDefect14() + 1);
//                break;
//            case 14:
//                inspectionSummary.setDefect15(inspectionSummary.getDefect15() + 1);
//                break;
//            case 15:
//                inspectionSummary.setDefect16(inspectionSummary.getDefect16() + 1);
//                break;
//            case 16:
//                inspectionSummary.setDefect17(inspectionSummary.getDefect17() + 1);
//                break;
//            case 17:
//                inspectionSummary.setDefect18(inspectionSummary.getDefect18() + 1);
//                break;
//            case 18:
//                inspectionSummary.setDefect19(inspectionSummary.getDefect19() + 1);
//                break;
//            case 19:
//                inspectionSummary.setDefect20(inspectionSummary.getDefect20() + 1);
//                break;
//        }
//        inspectionSummary.setDefectiveTotal(inspectionSummary.getDefectiveTotal() + 1);

//        int i = inspectionSummaryService.updateInspectionSummary(inspectionSummary);
//        Map<String, Object> result1 = new HashMap<>();
//        if (i > 0) {
//            inspectionSummary.setInspectionReportList(null);
//            result1.put("summary", inspectionSummary);
//        }
        if (updateFlag > 0){
            return AjaxResult.success(1);
        }else {
            return AjaxResult.error("新增失败");
        }
    }

    @PostMapping("/updateStopTime")
    public AjaxResult updateStopTime(@RequestBody Map<String, Object> params) {
        int mainId = (int) params.get("mainId");
        String stopTime = String.format("%.2f", (double) params.get("stopTime"));
        //更新记录
        int i = inspectionSummaryService.updateStopTime(mainId, stopTime);
        return AjaxResult.success("继续");
    }

    /**
     * 正常批量良品报工
     * @param params
     * @return
     */

    @PostMapping("/reportBatch")
    public AjaxResult reportBatch(@RequestBody Map<String, Object> params) {

        // 获取 reportJson（它是个 List）
//        Map<String, Object> reportJson = (Map<String, Object>) params.get("reportJson");
//        System.out.println("报工所有reportJson = " + reportJson);
//        long userId = Long.parseLong(reportJson.get("mesUserId").toString());
//        String qrCode = reportJson.get("qrCode").toString();
//        long reportUnitId = Long.parseLong(reportJson.get("unitId").toString());
//        long reportProcessId = Long.parseLong(reportJson.get("processId").toString());
//        long lineId = Long.parseLong(reportJson.get("materialLineId").toString());
//        long materialId = Long.parseLong(reportJson.get("materialId").toString());
//        long taskId = Long.parseLong(reportJson.get("taskId").toString());
        //String badItem = "黑点";// (String) params.get("color");
        int mainId = (int) params.get("mainId");
        InspectionSummary inspectionSummary = inspectionSummaryService.selectInspectionSummaryById((long) mainId);
//        long batchNoId = 0L;
//        if (reportJson.get("batchNoId") != null) {
//            batchNoId = Long.parseLong(reportJson.get("batchNoId").toString());
//        }
//        String batchNo = "";
//        if (reportJson.get("batchNo") != null) {
//            batchNo = reportJson.get("batchNo").toString();
//        }

        //时间
//        String stopTime = inspectionSummary.getStopTime();
//        long reportStartTime = Long.parseLong(reportJson.get("reportStartTime").toString());
//        long reportEndTime = Long.parseLong(reportJson.get("reportEndTime").toString());
//        //报工数量1,质量不合格，扫码报工不合格
//        int reportAmount = inspectionSummary.getTotalQuantity() - inspectionSummary.getDefectiveTotal();
//        int qcStatus = 1;
//        int reportType = 1;

//        Map<String, Object> res = batchReportForBlackLack.batchReportForBlackLack(
//                userId,
//                qrCode,
//                reportUnitId,
//                reportProcessId,
//                reportAmount,
//                lineId,
//                materialId,
//                qcStatus,
//                reportType,
//                taskId,
//                stopTime,
//                batchNoId,
//                batchNo,
//                reportStartTime,
//                reportEndTime
//        );
//        //检验成功状态
//        String message = res.get("message").toString();
//        int code = (int) res.get("code");
////        System.out.println("message1 = " + message);
////        System.out.println(" code1 = " + code);
//        if (code != 200) {
//            return AjaxResult.error(message);
//        }
//        //更新主表
//        System.out.println("报工状态：" + res.get("message"));
//        if (res.get("message").equals("成功")) {
//            inspectionSummary.setSuccessFlag(1L);
////            System.out.println("报工状态1：" + res.get("message"));
//        } else {
            inspectionSummary.setSuccessFlag(0L);
//            System.out.println("报工状态0：" + res.get("message"));
//        }
        Map<String, Object> res =new HashMap<>();
        inspectionSummary.setApiDetail(res.toString());
        int i = inspectionSummaryService.updateInspectionSummary(inspectionSummary);
        //查询主表记录
        Map<String, Object> result1 = new HashMap<>();
        if (i > 0) {
            inspectionSummary.setInspectionReportList(null);
            result1.put("summary", inspectionSummary);
        }
        return AjaxResult.success(result1);
    }
    /**
     * 重试批量良品报工
     */


    @PostMapping("/reReportBatch")
    public AjaxResult reReportBatch(@RequestBody InspectionSummary inspectionSummary) {
        // 获取 reportJson（它是个 List）
        ObjectMapper objectMapper = new ObjectMapper();

        String jsonStr = inspectionSummary.getApiReport(); // JSON 字符串
        Map<String, Object> reportJson = null;
        try {
            reportJson = objectMapper.readValue(jsonStr, Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
//        System.out.println("报工所有reportJson = " + reportJson);
        long userId = Long.parseLong(reportJson.get("mesUserId").toString());
        String qrCode = reportJson.get("qrCode").toString();
        long reportUnitId = Long.parseLong(reportJson.get("unitId").toString());
        long reportProcessId = Long.parseLong(reportJson.get("processId").toString());
        long lineId = Long.parseLong(reportJson.get("materialLineId").toString());
        long materialId = Long.parseLong(reportJson.get("materialId").toString());
        long taskId = Long.parseLong(reportJson.get("taskId").toString());
        //String badItem = "黑点";// (String) params.get("color");
        long batchNoId = 0L;
        if (reportJson.get("batchNoId") != null) {
            batchNoId = Long.parseLong(reportJson.get("batchNoId").toString());
        }
        String batchNo = "";
        if (reportJson.get("batchNo") != null) {
            batchNo = reportJson.get("batchNo").toString();
        }

        //时间
        String stopTime = "";
        if (reportJson.get("stopTime") == null) {
            stopTime =inspectionSummary.getStopTime();
        } else {
            stopTime = reportJson.get("stopTime").toString();
        }
        long timestamp = inspectionSummary.getCreateTime().getTime();
        long reportEndTime = System.currentTimeMillis();
        //报工数量1,质量不合格，扫码报工不合格
        int reportAmount = inspectionSummary.getTotalQuantity() - inspectionSummary.getDefectiveTotal();
        int qcStatus = 1;
        int reportType = 1;

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
                reportEndTime
        );
        //检验成功状态
        String message = res.get("message").toString();
        int code = (int) res.get("code");
//        System.out.println("message1 = " + message);
//        System.out.println(" code1 = " + code);
        if (code != 200) {
            return AjaxResult.error(message);
        }
        //更新主表
//        System.out.println("报工状态：" + res.get("message"));
        if (res.get("message").equals("成功")) {
            inspectionSummary.setSuccessFlag(1L);
//            System.out.println("报工状态1：" + res.get("message"));
        } else {
            inspectionSummary.setSuccessFlag(0L);
//            System.out.println("报工状态0：" + res.get("message"));
        }
        inspectionSummary.setApiDetail(res.toString());
        int i = inspectionSummaryService.updateInspectionSummary(inspectionSummary);
        //查询主表记录
        Map<String, Object> result1 = new HashMap<>();
        if (i > 0) {
            inspectionSummary.setInspectionReportList(null);
            result1.put("summary", inspectionSummary);
        }
        return AjaxResult.success(result1);
    }

    /**
     * 查询黑湖用户信息列表
     */
    @PreAuthorize("@ss.hasPermi('blacklackUser:BlacklackUser:list')")
    @GetMapping("/list")
    public TableDataInfo list(BlacklackUser blacklackUser) {
        startPage();
        List<BlacklackUser> list = blacklackUserService.selectBlacklackUserList(blacklackUser);
        return getDataTable(list);
    }

    /**
     * 导出黑湖用户信息列表
     */
    @PreAuthorize("@ss.hasPermi('blacklackUser:BlacklackUser:export')")
    @Log(title = "黑湖用户信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BlacklackUser blacklackUser) {
        List<BlacklackUser> list = blacklackUserService.selectBlacklackUserList(blacklackUser);
        ExcelUtil<BlacklackUser> util = new ExcelUtil<BlacklackUser>(BlacklackUser.class);
        util.exportExcel(response, list, "黑湖用户信息数据");
    }

    /**
     * 获取黑湖用户信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('blacklackUser:BlacklackUser:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(blacklackUserService.selectBlacklackUserById(id));
    }

    /**
     * 新增黑湖用户信息
     */
    @PreAuthorize("@ss.hasPermi('blacklackUser:BlacklackUser:add')")
    @Log(title = "黑湖用户信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BlacklackUser blacklackUser) {
        return toAjax(blacklackUserService.insertBlacklackUser(blacklackUser));
    }

    /**
     * 修改黑湖用户信息
     */
    @PreAuthorize("@ss.hasPermi('blacklackUser:BlacklackUser:edit')")
    @Log(title = "黑湖用户信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BlacklackUser blacklackUser) {
        return toAjax(blacklackUserService.updateBlacklackUser(blacklackUser));
    }

    /**
     * 删除黑湖用户信息
     */
    @PreAuthorize("@ss.hasPermi('blacklackUser:BlacklackUser:remove')")
    @Log(title = "黑湖用户信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(blacklackUserService.deleteBlacklackUserByIds(ids));
    }
}
