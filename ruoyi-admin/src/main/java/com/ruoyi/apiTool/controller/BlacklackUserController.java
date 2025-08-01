package com.ruoyi.apiTool.controller;

import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ruoyi.apiTool.ApiMaterialDetailForBlackLack;
import com.ruoyi.apiTool.ApiProcessListForBlackLack;
import com.ruoyi.apiTool.ApiReportRecordForBlackLack;
import com.ruoyi.apiTool.ApiTaskForBlackLack;
import com.ruoyi.badItem.domain.CreateBadItemsTable;
import com.ruoyi.badItem.mapper.CreateBadItemsTableMapper;
import com.ruoyi.inspection.domain.InspectionSummary;
import com.ruoyi.inspection.service.IInspectionSummaryService;
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

    @PostMapping("/queryScanTaskResult")
    public AjaxResult checkUserToBlackLack(@RequestParam("taskCode") String taskCode) {
        System.out.println("收到的数据" + taskCode);

        Map<String, String> reportRecordResult = null;
        reportRecordResult = reportRecordForBlackLack.getReportRecordDetailForBlackLack(taskCode);
        //查主表记录：并返回给前端做提示
        InspectionSummary inspectionSummary = blacklackUserService.selectInspectionMainByQrcode(taskCode);
        System.out.println(
                "主表记录：" + inspectionSummary
        );
        //将inspectionSummary对象的值赋给reportRecordResult
        if (inspectionSummary != null){
            reportRecordResult.put("creatBy", inspectionSummary.getCreateBy());
            reportRecordResult.put("creatDate", inspectionSummary.getCreateTime().toString());
        }else {
            reportRecordResult.put("creatBy", "-1");
            reportRecordResult.put("creatDate", "");
        }

        //返回确认信息
        return success(reportRecordResult);
    }

    //点击开始报工，主表新增记录
    //返回给前端需要的数据//包括：物料信息，主表当前记录，工单信息
    @PostMapping("/addOrReadInspectionMain")
    public AjaxResult addOrReadInspectionMain(@RequestBody ReportRecord reportRecord) {
        System.out.println("-------------------------------------");
        System.out.println("收到的数据：" + reportRecord);
        Map<String, String> materialDetailResult1 = materialDetailForBlackLack.getMaterialDetailForBlackLack(reportRecord.getMaterialCode());
        materialDetailResult1.put("batchNo", reportRecord.getBatchNo());
        materialDetailResult1.put("batchNoId", reportRecord.getBatchNoId());

        Map<String, String> processResult3 = null;
        processResult3 = processListForBlackLack.getWareHouseDetailForBlackLack(reportRecord.getWorkOrderId(),  reportRecord.getProcessId());
        processResult3.put("creatBy", reportRecord.getCreatBy());
        processResult3.put("amount", reportRecord.getAmount());
        processResult3.put("qrCode", reportRecord.getQrCode());
        processResult3.put("color", materialDetailResult1.get("color"));
        processResult3.put("unitId", materialDetailResult1.get("unitId"));

        System.out.println("工单信息："+processResult3.get("color"));
        //返回确认信息
        //新建、修改主表记录，返回给前端显示
        InspectionSummary result=blacklackUserService.addOrUpdateInspectionMain(processResult3);
        return success(result);
    }
    @PostMapping("/reportBadItemOne")
    public AjaxResult reportBadItemOne(@RequestBody Map<String, Object> params) {
        // 获取 color
        String color = (String) params.get("color");

        // 获取 reportJson（它是个 List）
        Map<String, Object> reportJson = (Map<String, Object>) params.get("reportJson");

        System.out.println("color = " + color);
        System.out.println("reportJson = " + reportJson);

        return AjaxResult.success("接收成功");
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
