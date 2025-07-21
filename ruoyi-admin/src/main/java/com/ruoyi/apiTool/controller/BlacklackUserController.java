package com.ruoyi.apiTool.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.ruoyi.apiTool.ApiTaskForBlackLack;
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

/**
 * 黑湖用户信息Controller
 * 
 * @author w
 * @date 2025-07-17
 */
@RestController
@RequestMapping("/blacklackUser/BlacklackUser")
public class BlacklackUserController extends BaseController
{
    @Autowired
    private IBlacklackUserService blacklackUserService;
    @Autowired
    private ApiTaskForBlackLack apiTaskForBlackLack;
    @PostMapping("/queryScanTaskResult")
    public String checkUserToBlackLack( @RequestParam("taskCode") String taskCode) {
        System.out.println("收到的数据" + taskCode);

        //0表示错误
        return apiTaskForBlackLack.getTackForBlackLack(taskCode);
    }
    /**
     * 查询黑湖用户信息列表
     */
    @PreAuthorize("@ss.hasPermi('blacklackUser:BlacklackUser:list')")
    @GetMapping("/list")
    public TableDataInfo list(BlacklackUser blacklackUser)
    {
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
    public void export(HttpServletResponse response, BlacklackUser blacklackUser)
    {
        List<BlacklackUser> list = blacklackUserService.selectBlacklackUserList(blacklackUser);
        ExcelUtil<BlacklackUser> util = new ExcelUtil<BlacklackUser>(BlacklackUser.class);
        util.exportExcel(response, list, "黑湖用户信息数据");
    }

    /**
     * 获取黑湖用户信息详细信息
     */
    @PreAuthorize("@ss.hasPermi('blacklackUser:BlacklackUser:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(blacklackUserService.selectBlacklackUserById(id));
    }

    /**
     * 新增黑湖用户信息
     */
    @PreAuthorize("@ss.hasPermi('blacklackUser:BlacklackUser:add')")
    @Log(title = "黑湖用户信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BlacklackUser blacklackUser)
    {
        return toAjax(blacklackUserService.insertBlacklackUser(blacklackUser));
    }

    /**
     * 修改黑湖用户信息
     */
    @PreAuthorize("@ss.hasPermi('blacklackUser:BlacklackUser:edit')")
    @Log(title = "黑湖用户信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BlacklackUser blacklackUser)
    {
        return toAjax(blacklackUserService.updateBlacklackUser(blacklackUser));
    }

    /**
     * 删除黑湖用户信息
     */
    @PreAuthorize("@ss.hasPermi('blacklackUser:BlacklackUser:remove')")
    @Log(title = "黑湖用户信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(blacklackUserService.deleteBlacklackUserByIds(ids));
    }
}
