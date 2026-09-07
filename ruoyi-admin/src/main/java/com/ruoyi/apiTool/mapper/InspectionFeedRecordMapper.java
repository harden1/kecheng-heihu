package com.ruoyi.apiTool.mapper;

import com.ruoyi.apiTool.domain.feed.InspectionFeedRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 镜检投料上传记录Mapper接口
 *
 * @author wmin
 * @date 2026-09-03
 */
@Mapper
public interface InspectionFeedRecordMapper {

    /**
     * 根据主键查询投料记录
     *
     * @param id 投料记录ID
     * @return 投料记录
     */
    InspectionFeedRecord selectById(Long id);

    /**
     * 根据业务键（镜检主记录ID + 任务ID + 二维码）查询唯一投料记录
     *
     * @param summaryId 镜检主记录ID
     * @param taskId    黑湖生产任务ID
     * @param qrCode    扫码二维码
     * @return 投料记录，不存在时返回 null
     */
    InspectionFeedRecord selectByBusinessKey(@Param("summaryId") Long summaryId, @Param("taskId") Long taskId, @Param("qrCode") String qrCode);

    /**
     * 按条件查询投料记录列表
     *
     * @param query 查询条件
     * @return 投料记录列表
     */
    List<InspectionFeedRecord> selectList(InspectionFeedRecord query);

    /**
     * 新增投料记录
     *
     * @param record 投料记录
     * @return 影响行数
     */
    int insert(InspectionFeedRecord record);

    /**
     * 自动上传抢占：仅 PENDING 状态可抢占为 PROCESSING
     *
     * @param id 投料记录ID
     * @return 影响行数，0 表示抢占失败
     */
    int claimAutoUpload(Long id);

    /**
     * 手动重传抢占：仅 PENDING/FAILED 状态可抢占为 PROCESSING，并增加重传次数
     *
     * @param id 投料记录ID
     * @return 影响行数，0 表示抢占失败
     */
    int claimManualRetry(Long id);

    /**
     * 标记投料成功，保存请求和响应JSON
     *
     * @param id           投料记录ID
     * @param requestJson  请求JSON
     * @param responseJson 响应JSON
     * @param feedTime     上传成功时间
     * @return 影响行数
     */
    int markSuccess(@Param("id") Long id, @Param("requestJson") String requestJson, @Param("responseJson") String responseJson, @Param("feedTime") Date feedTime);

    /**
     * 标记投料失败，保存请求和响应JSON及错误信息
     *
     * @param id           投料记录ID
     * @param requestJson  请求JSON
     * @param responseJson 响应JSON
     * @param errorMessage 失败原因
     * @return 影响行数
     */
    int markFailed(@Param("id") Long id, @Param("requestJson") String requestJson, @Param("responseJson") String responseJson, @Param("errorMessage") String errorMessage, @Param("failType") String failType);

    /**
     * 标记投料结果未知，禁止直接重传
     *
     * @param id           投料记录ID
     * @param requestJson  请求JSON
     * @param responseJson 响应JSON
     * @param errorMessage 错误信息
     * @return 影响行数
     */
    int markUnknown(@Param("id") Long id, @Param("requestJson") String requestJson, @Param("responseJson") String responseJson, @Param("errorMessage") String errorMessage, @Param("failType") String failType);
}
