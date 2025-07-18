package com.ruoyi.apiTool.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.apiTool.mapper.BlacklackUserMapper;
import com.ruoyi.apiTool.domain.BlacklackUser;
import com.ruoyi.apiTool.service.IBlacklackUserService;
import org.springframework.transaction.annotation.Transactional;

/**
 * 黑湖用户信息Service业务层处理
 * 
 * @author w
 * @date 2025-07-17
 */
@Service
public class BlacklackUserServiceImpl  implements IBlacklackUserService {

    @Autowired
    private BlacklackUserMapper blacklackUserMapper;
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
    public BlacklackUser selectBlacklackUserById(Long id)
    {
        return blacklackUserMapper.selectBlacklackUserById(id);
    }

    /**
     * 查询黑湖用户信息列表
     * 
     * @param blacklackUser 黑湖用户信息
     * @return 黑湖用户信息
     */
    @Override
    public List<BlacklackUser> selectBlacklackUserList(BlacklackUser blacklackUser)
    {
        return blacklackUserMapper.selectBlacklackUserList(blacklackUser);
    }

    /**
     * 新增黑湖用户信息
     * 
     * @param blacklackUser 黑湖用户信息
     * @return 结果
     */
    @Override
    public int insertBlacklackUser(BlacklackUser blacklackUser)
    {
        return blacklackUserMapper.insertBlacklackUser(blacklackUser);
    }

    /**
     * 修改黑湖用户信息
     * 
     * @param blacklackUser 黑湖用户信息
     * @return 结果
     */
    @Override
    public int updateBlacklackUser(BlacklackUser blacklackUser)
    {
        return blacklackUserMapper.updateBlacklackUser(blacklackUser);
    }

    /**
     * 批量删除黑湖用户信息
     *
     * @param ids 需要删除的黑湖用户信息主键
     * @return 结果
     */
    @Override
    public int deleteBlacklackUserByIds(Long[] ids)
    {
        return blacklackUserMapper.deleteBlacklackUserByIds(ids);
    }

    /**
     * 删除黑湖用户信息信息
     *
     * @param id 黑湖用户信息主键
     * @return 结果
     */
    @Override
    public int deleteBlacklackUserById(Long id)
    {
        return blacklackUserMapper.deleteBlacklackUserById(id);
    }
}
