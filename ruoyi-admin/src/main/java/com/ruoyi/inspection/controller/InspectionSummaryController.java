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
import com.ruoyi.inspection.domain.InspectionSummary;
import com.ruoyi.inspection.service.IInspectionSummaryService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 镜检统计主Controller
 * 
 * @author w
 * @date 2025-07-08
 */
@RestController
@RequestMapping("/inspection/summary")
public class InspectionSummaryController extends BaseController
{
    @Autowired
    private IInspectionSummaryService inspectionSummaryService;

    /**
     * 查询镜检统计主列表
     */
    @PreAuthorize("@ss.hasPermi('inspection:summary:list')")
    @GetMapping("/list")
    public TableDataInfo list(InspectionSummary inspectionSummary)
    {
        System.out.println("查询镜检统计主列表"+inspectionSummary);
        startPage();
        List<InspectionSummary> list = inspectionSummaryService.selectInspectionSummaryList(inspectionSummary);
        return getDataTable(list);
    }

    /**
     * 导出镜检统计主列表
     */
    @PreAuthorize("@ss.hasPermi('inspection:summary:export')")
    @Log(title = "镜检统计主", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, InspectionSummary inspectionSummary)
    {
        List<InspectionSummary> list = inspectionSummaryService.selectInspectionSummaryList(inspectionSummary);
        ExcelUtil<InspectionSummary> util = new ExcelUtil<InspectionSummary>(InspectionSummary.class);
        util.exportExcel(response, list, "镜检统计主数据");
    }

    /**
     * 获取镜检统计主详细信息
     */
    @PreAuthorize("@ss.hasPermi('inspection:summary:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(inspectionSummaryService.selectInspectionSummaryById(id));
    }

    /**
     * 新增镜检统计主
     */
    @PreAuthorize("@ss.hasPermi('inspection:summary:add')")
    @Log(title = "镜检统计主", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody InspectionSummary inspectionSummary)
    {
        return toAjax(inspectionSummaryService.insertInspectionSummary(inspectionSummary));
    }

    /**
     * 修改镜检统计主
     */
    @PreAuthorize("@ss.hasPermi('inspection:summary:edit')")
    @Log(title = "镜检统计主", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody InspectionSummary inspectionSummary)
    {
        return toAjax(inspectionSummaryService.updateInspectionSummaryNoSubfom(inspectionSummary));
    }

    /**
     * 删除镜检统计主
     */
    @PreAuthorize("@ss.hasPermi('inspection:summary:remove')")
    @Log(title = "镜检统计主", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(inspectionSummaryService.deleteInspectionSummaryByIds(ids));
    }
}
