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
import com.ruoyi.inspection.domain.InspectionRecord;
import com.ruoyi.inspection.service.IInspectionRecordService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 镜检单条记录Controller
 * 
 * @author w
 * @date 2025-07-08
 */
@RestController
@RequestMapping("/inspection/record")
public class InspectionRecordController extends BaseController
{
    @Autowired
    private IInspectionRecordService inspectionRecordService;

    /**
     * 查询镜检单条记录列表
     */
    @PreAuthorize("@ss.hasPermi('inspection:record:list')")
    @GetMapping("/list")
    public TableDataInfo list(InspectionRecord inspectionRecord)
    {
        startPage();
        List<InspectionRecord> list = inspectionRecordService.selectInspectionRecordList(inspectionRecord);
        return getDataTable(list);
    }

    /**
     * 导出镜检单条记录列表
     */
    @PreAuthorize("@ss.hasPermi('inspection:record:export')")
    @Log(title = "镜检单条记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, InspectionRecord inspectionRecord)
    {
        List<InspectionRecord> list = inspectionRecordService.selectInspectionRecordList(inspectionRecord);
        ExcelUtil<InspectionRecord> util = new ExcelUtil<InspectionRecord>(InspectionRecord.class);
        util.exportExcel(response, list, "镜检单条记录数据");
    }

    /**
     * 获取镜检单条记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('inspection:record:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(inspectionRecordService.selectInspectionRecordById(id));
    }

    /**
     * 新增镜检单条记录
     */
    @PreAuthorize("@ss.hasPermi('inspection:record:add')")
    @Log(title = "镜检单条记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody InspectionRecord inspectionRecord)
    {
        return toAjax(inspectionRecordService.insertInspectionRecord(inspectionRecord));
    }

    /**
     * 修改镜检单条记录
     */
    @PreAuthorize("@ss.hasPermi('inspection:record:edit')")
    @Log(title = "镜检单条记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody InspectionRecord inspectionRecord)
    {
        return toAjax(inspectionRecordService.updateInspectionRecord(inspectionRecord));
    }

    /**
     * 删除镜检单条记录
     */
    @PreAuthorize("@ss.hasPermi('inspection:record:remove')")
    @Log(title = "镜检单条记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(inspectionRecordService.deleteInspectionRecordByIds(ids));
    }
}
