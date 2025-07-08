package com.ruoyi.inspection.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.inspection.domain.InspectionReport;
import com.ruoyi.inspection.service.IInspectionReportService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 报工记录Controller
 * 
 * @author w
 * @date 2025-07-08
 */
@RestController
@RequestMapping("/inspection/report")
public class InspectionReportController extends BaseController
{
    @Autowired
    private IInspectionReportService inspectionReportService;

    /**
     * 查询报工记录列表
     */
    @PreAuthorize("@ss.hasPermi('inspection:report:list')")
    @GetMapping("/list")
    public TableDataInfo list(InspectionReport inspectionReport)
    {
        startPage();
        List<InspectionReport> list = inspectionReportService.selectInspectionReportList(inspectionReport);
        return getDataTable(list);
    }

    /**
     * 导出报工记录列表
     */
    @PreAuthorize("@ss.hasPermi('inspection:report:export')")
    @Log(title = "报工记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, InspectionReport inspectionReport)
    {
        List<InspectionReport> list = inspectionReportService.selectInspectionReportList(inspectionReport);
        ExcelUtil<InspectionReport> util = new ExcelUtil<InspectionReport>(InspectionReport.class);
        util.exportExcel(response, list, "报工记录数据");
    }

    /**
     * 获取报工记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('inspection:report:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(inspectionReportService.selectInspectionReportById(id));
    }

    /**
     * 新增报工记录
     */
    @PreAuthorize("@ss.hasPermi('inspection:report:add')")
    @Log(title = "报工记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody InspectionReport inspectionReport)
    {
        return toAjax(inspectionReportService.insertInspectionReport(inspectionReport));
    }

    /**
     * 修改报工记录
     */
    @PreAuthorize("@ss.hasPermi('inspection:report:edit')")
    @Log(title = "报工记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody InspectionReport inspectionReport)
    {
        return toAjax(inspectionReportService.updateInspectionReport(inspectionReport));
    }

    /**
     * 删除报工记录
     */
    @PreAuthorize("@ss.hasPermi('inspection:report:remove')")
    @Log(title = "报工记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(inspectionReportService.deleteInspectionReportByIds(ids));
    }
}
