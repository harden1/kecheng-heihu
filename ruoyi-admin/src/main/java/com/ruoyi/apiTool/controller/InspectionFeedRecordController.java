package com.ruoyi.apiTool.controller;

import com.ruoyi.apiTool.domain.feed.InspectionFeedRecord;
import com.ruoyi.apiTool.service.FeedUploadService;
import com.ruoyi.apiTool.service.IInspectionFeedRecordService;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 镜检投料记录Controller
 * <p>
 * 提供投料记录的列表查询、详情查询和手动重传接口。
 *
 * @author wmin
 * @date 2026-09-03
 */
@RestController
@RequestMapping("/inspection/feedRecord")
public class InspectionFeedRecordController extends BaseController {

    @Autowired
    private IInspectionFeedRecordService feedRecordService;
    @Autowired
    private FeedUploadService feedUploadService;

    /**
     * 查询投料记录列表（分页）
     * 支持按二维码、工单号、任务ID、上传状态、创建时间查询
     */
    @PreAuthorize("@ss.hasPermi('inspection:feedRecord:list')")
    @GetMapping("/list")
    public TableDataInfo list(InspectionFeedRecord query) {
        startPage();
        return getDataTable(feedRecordService.selectList(query));
    }

    /**
     * 获取投料记录详细信息
     * 返回请求JSON、响应JSON和错误原因，不返回Token或应用密钥
     */
    @PreAuthorize("@ss.hasPermi('inspection:feedRecord:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(feedRecordService.selectById(id));
    }

    /**
     * 手动重传投料记录
     * 仅 PENDING/FAILED 状态可重传，重新查询库存和投料关系
     */
    @PreAuthorize("@ss.hasPermi('inspection:feedRecord:retry')")
    @Log(title = "投料记录", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/retry")
    public AjaxResult retry(@PathVariable Long id) {
        return success(feedUploadService.manualRetry(id));
    }

    /**
     * 触发自动上传（测试用）：根据镜检主记录ID执行完整投料链路
     */
    @PreAuthorize("@ss.hasPermi('inspection:feedRecord:retry')")
    @Log(title = "投料记录", businessType = BusinessType.INSERT)
    @PostMapping("/autoUpload/{summaryId}")
    public AjaxResult autoUpload(@PathVariable Long summaryId) {
        return success(feedUploadService.autoUpload(summaryId));
    }
}
