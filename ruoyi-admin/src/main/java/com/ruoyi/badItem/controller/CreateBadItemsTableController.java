package com.ruoyi.badItem.controller;

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
import com.ruoyi.badItem.domain.CreateBadItemsTable;
import com.ruoyi.badItem.service.ICreateBadItemsTableService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * badItemController
 * 
 * @author ruoyi
 * @date 2025-06-11
 */
@RestController
@RequestMapping("/badItem/badItem")
public class CreateBadItemsTableController extends BaseController
{
    @Autowired
    private ICreateBadItemsTableService createBadItemsTableService;

    /**
     * 查询badItem列表
     */
    @PreAuthorize("@ss.hasPermi('badItem:badItem:list')")
    @GetMapping("/list")
    public TableDataInfo list(CreateBadItemsTable createBadItemsTable)
    {
        startPage();
        List<CreateBadItemsTable> list = createBadItemsTableService.selectCreateBadItemsTableList(createBadItemsTable);
        return getDataTable(list);
    }

    /**
     * 导出badItem列表
     */
    @PreAuthorize("@ss.hasPermi('badItem:badItem:export')")
    @Log(title = "badItem", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, CreateBadItemsTable createBadItemsTable)
    {
        List<CreateBadItemsTable> list = createBadItemsTableService.selectCreateBadItemsTableList(createBadItemsTable);
        ExcelUtil<CreateBadItemsTable> util = new ExcelUtil<CreateBadItemsTable>(CreateBadItemsTable.class);
        util.exportExcel(response, list, "badItem数据");
    }

    /**
     * 获取badItem详细信息
     */
    @PreAuthorize("@ss.hasPermi('badItem:badItem:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(createBadItemsTableService.selectCreateBadItemsTableById(id));
    }

    /**
     * 新增badItem
     */
    @PreAuthorize("@ss.hasPermi('badItem:badItem:add')")
    @Log(title = "badItem", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody CreateBadItemsTable createBadItemsTable)
    {
        return toAjax(createBadItemsTableService.insertCreateBadItemsTable(createBadItemsTable));
    }

    /**
     * 修改badItem
     */
    @PreAuthorize("@ss.hasPermi('badItem:badItem:edit')")
    @Log(title = "badItem", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CreateBadItemsTable createBadItemsTable)
    {
        return toAjax(createBadItemsTableService.updateCreateBadItemsTable(createBadItemsTable));
    }

    /**
     * 删除badItem
     */
    @PreAuthorize("@ss.hasPermi('badItem:badItem:remove')")
    @Log(title = "badItem", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(createBadItemsTableService.deleteCreateBadItemsTableByIds(ids));
    }
}
