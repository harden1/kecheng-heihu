package com.ruoyi.apiTool.domain.feed;

/**
 * 报工入队结果，包含镜检主记录ID和主记录摘要。
 * 用于在 Controller 层将报工入队与投料编排解耦。
 *
 * @author wmin
 * @date 2026-09-03
 */
public class ReportBatchResult {

    /** 镜检主记录ID */
    private Long summaryId;

    /** 镜检主记录 */
    private Object summary;

    public ReportBatchResult(Long summaryId, Object summary) {
        this.summaryId = summaryId;
        this.summary = summary;
    }

    public Long getSummaryId() {
        return summaryId;
    }

    public void setSummaryId(Long summaryId) {
        this.summaryId = summaryId;
    }

    public Object getSummary() {
        return summary;
    }

    public void setSummary(Object summary) {
        this.summary = summary;
    }
}
