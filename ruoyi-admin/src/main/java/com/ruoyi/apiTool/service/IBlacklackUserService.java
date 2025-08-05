package com.ruoyi.apiTool.service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.apiTool.domain.BlacklackUser;
import com.ruoyi.apiTool.domain.ReportRecord;
import com.ruoyi.inspection.domain.InspectionSummary;

/**
 * 黑湖用户信息Service接口
 * 
 * @author w
 * @date 2025-07-17
 */
// 接口定义
public interface IBlacklackUserService  {
    public void replaceAll(List<BlacklackUser> list);
    /**
     * 查询黑湖用户信息
     * 
     * @param id 黑湖用户信息主键
     * @return 黑湖用户信息
     */
    public BlacklackUser selectBlacklackUserById(Long id);

    /**
     * 查询黑湖用户信息列表
     * 
     * @param blacklackUser 黑湖用户信息
     * @return 黑湖用户信息集合
     */
    public List<BlacklackUser> selectBlacklackUserList(BlacklackUser blacklackUser);

    /**
     * 新增黑湖用户信息
     * 
     * @param blacklackUser 黑湖用户信息
     * @return 结果
     */
    public int insertBlacklackUser(BlacklackUser blacklackUser);

    /**
     * 修改黑湖用户信息
     * 
     * @param blacklackUser 黑湖用户信息
     * @return 结果
     */
    public int updateBlacklackUser(BlacklackUser blacklackUser);

    /**
     * 批量删除黑湖用户信息
     * 
     * @param ids 需要删除的黑湖用户信息主键集合
     * @return 结果
     */
    public int deleteBlacklackUserByIds(Long[] ids);

    /**
     * 删除黑湖用户信息信息
     * 
     * @param id 黑湖用户信息主键
     * @return 结果
     */
    public int deleteBlacklackUserById(Long id);

    List<InspectionSummary>  selectInspectionMainByQrcode(String taskCode);

    InspectionSummary addOrUpdateInspectionMain(Map<String, String> processResult3, ReportRecord reportRecord);
}
