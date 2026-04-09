package com.ruoyi.apiTool.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.apiTool.domain.ReportRecord;
import com.ruoyi.apiTool.service.IBlacklackUserService;
import com.ruoyi.badItem.domain.CreateBadItemsTable;
import com.ruoyi.badItem.mapper.CreateBadItemsTableMapper;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.inspection.domain.InspectionSummary;
import com.ruoyi.inspection.mapper.InspectionSummaryMapper;
import com.ruoyi.system.mapper.SysConfigMapper;
import com.ruoyi.system.service.ISysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.apiTool.mapper.BlacklackUserMapper;
import com.ruoyi.apiTool.domain.BlacklackUser;
import org.springframework.transaction.annotation.Transactional;

/**
 * 黑湖用户信息Service业务层处理
 *
 * @author w
 * @date 2025-07-17
 */
@Service
public class BlacklackUserServiceImpl implements IBlacklackUserService {

    @Autowired
    private BlacklackUserMapper blacklackUserMapper;
    @Autowired
    private InspectionSummaryMapper inspectionSummaryMapper;
    @Autowired
    private CreateBadItemsTableMapper createBadItemsTableMapper;
    @Autowired
    private SysConfigMapper sysConfigMapper;
    @Autowired
    private BlacklackUserServiceImpl blacklackUserService;
    @Autowired
    private ISysConfigService configService;

    @Override
    @Transactional // 添加事务保证原子性
    public void replaceAll(List<BlacklackUser> list) {
        // 1. 删除所有记录
        blacklackUserMapper.deleteAll();

        // 2. 批量插入新数据（如果列表非空）
        if (!list.isEmpty()) {
            blacklackUserMapper.batchInsert(list);
        }
    }

    /**
     * 查询黑湖用户信息
     *
     * @param id 黑湖用户信息主键
     * @return 黑湖用户信息
     */
    @Override
    public BlacklackUser selectBlacklackUserById(Long id) {
        return blacklackUserMapper.selectBlacklackUserById(id);
    }

    /**
     * 查询黑湖用户信息列表
     *
     * @param blacklackUser 黑湖用户信息
     * @return 黑湖用户信息
     */
    @Override
    public List<BlacklackUser> selectBlacklackUserList(BlacklackUser blacklackUser) {
        return blacklackUserMapper.selectBlacklackUserList(blacklackUser);
    }

    /**
     * 新增黑湖用户信息
     *
     * @param blacklackUser 黑湖用户信息
     * @return 结果
     */
    @Override
    public int insertBlacklackUser(BlacklackUser blacklackUser) {
        return blacklackUserMapper.insertBlacklackUser(blacklackUser);
    }

    /**
     * 修改黑湖用户信息
     *
     * @param blacklackUser 黑湖用户信息
     * @return 结果
     */
    @Override
    public int updateBlacklackUser(BlacklackUser blacklackUser) {
        return blacklackUserMapper.updateBlacklackUser(blacklackUser);
    }

    /**
     * 批量删除黑湖用户信息
     *
     * @param ids 需要删除的黑湖用户信息主键
     * @return 结果
     */
    @Override
    public int deleteBlacklackUserByIds(Long[] ids) {
        return blacklackUserMapper.deleteBlacklackUserByIds(ids);
    }

    /**
     * 删除黑湖用户信息信息
     *
     * @param id 黑湖用户信息主键
     * @return 结果
     */
    @Override
    public int deleteBlacklackUserById(Long id) {
        return blacklackUserMapper.deleteBlacklackUserById(id);
    }

    @Override
    public List<InspectionSummary> selectInspectionMainByQrcode(String taskCode) {
        try {
            return inspectionSummaryMapper.selectInspectionSummaryByQrCode(taskCode);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public InspectionSummary addOrUpdateInspectionMain(Map<String, String> processResult3, ReportRecord reportRecord) {
        System.out.println("是新单还是已存在：" + processResult3.get("creatBy"));
        String color = processResult3.get("color");
        //新增
        InspectionSummary inspectionSummary = null;
        if (Objects.equals(processResult3.get("flag"), "-1")) {
            List<CreateBadItemsTable> createBadItemsTableList = createBadItemsTableMapper.selectCreateBadItemsTableList(null);
            ObjectMapper mapper = new ObjectMapper();

            String jsonString;
            try {
                jsonString = mapper.writeValueAsString(createBadItemsTableList);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            //构建报工必须json
            /*
             * 当前操作人
             * 报工单位id
             * 物料行id
             * 物料id
             * 报工工序id
             * 生产任务id
             * 报工批号
             * 报工批号id
             * 报工二维码
             * 二维码数量
             * ********
             * 报工数量
             * 质量状态
             * 报工方式
             * 不良项目
             */
            Map<String, String> reportInfo = new HashMap<>();
            reportInfo.put("mesUserId", processResult3.get("mesUserId"));
            reportInfo.put("mesUserName", processResult3.get("mesUserName"));
            reportInfo.put("unitId", processResult3.get("unitId"));
            reportInfo.put("materialLineId", processResult3.get("lineId"));
            reportInfo.put("materialId", processResult3.get("materialId"));
//            System.out.println("工序id：" + processResult3.get("processId"));
            reportInfo.put("processId", processResult3.get("processId"));
            reportInfo.put("taskId", processResult3.get("taskId"));
            reportInfo.put("batchNo", processResult3.get("batchNo"));
            reportInfo.put("batchNoId", processResult3.get("batchNoId"));
            reportInfo.put("qrCode", processResult3.get("qrCode"));
            String warehouseId =sysConfigMapper.selectWarehouseIdSetting();
            reportInfo.put("warehouseId", warehouseId);
            //固定信息
            reportInfo.put("qrCodeNum", "1");

            System.out.println("报工信息：" + reportInfo);
            ObjectMapper reportMapper = new ObjectMapper();
            String reportjsonString;
            try {
                reportjsonString = reportMapper.writeValueAsString(reportInfo);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            //新增
            inspectionSummary = new InspectionSummary();
            inspectionSummary.setWorkOrderCode(processResult3.get("workOrderCode"));
            inspectionSummary.setTotalQuantity(
                    (int) Double.parseDouble(processResult3.get("amount").toString())
            );
            inspectionSummary.setQrCode(processResult3.get("qrCode"));
            inspectionSummary.setAllDefectItems(jsonString);
            inspectionSummary.setApiReport(reportjsonString);
            inspectionSummary.setDegrees(processResult3.get("degrees"));
            System.out.println(inspectionSummary);
            inspectionSummaryMapper.insertInspectionSummary(inspectionSummary);


        }
        List<InspectionSummary> inspectionSummarys = blacklackUserService.selectInspectionMainByQrcode(processResult3.get("qrCode"));
//        inspectionSummary = querySummaryByQrCode(processResult3.get("qrCode"));
        inspectionSummary = inspectionSummarys.get(0);
        inspectionSummary.setColor(color);
        // 查询防抖时间
        String debounce = sysConfigMapper.selectDebounce();
        inspectionSummary.setDebounce(debounce);
        return inspectionSummary;
    }

    @Override
    public String queryPreviousProcessSetting() {
        //查询前工序配置//configService.selectConfigByKey("Previous_process_setting");
        String previousProcessSetting =sysConfigMapper.selectPreviousProcessSetting();
        return previousProcessSetting;
    }

    public InspectionSummary querySummaryByQrCode(String qrcode) {
        InspectionSummary inspectionSummary = null;
        //更新,直接返回这个主表记录
        InspectionSummary ins = new InspectionSummary();
        ins.setQrCode(qrcode);
//        List<InspectionSummary> list = inspectionSummaryMapper.selectInspectionSummaryList1(ins);
        List<InspectionSummary> inspectionSummarys = blacklackUserService.selectInspectionMainByQrcode(qrcode);

        if (!inspectionSummarys.isEmpty()) {
            inspectionSummary = inspectionSummarys.get(0);
            // 查询防抖时间
            String debounce = sysConfigMapper.selectDebounce();
            inspectionSummary.setDebounce(debounce);
            //查询暂停时间，如果为空返回0
            String stopTime = inspectionSummary.getStopTime();// inspectionSummaryMapper.selectStopTime(processResult3.get("qrCode"));
            if (stopTime == null) {
                inspectionSummary.setStopTime("0");
            } else {
                inspectionSummary.setStopTime(stopTime);
            }
            return inspectionSummary;
        }
        return null;
    }

}
