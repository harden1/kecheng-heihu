package com.ruoyi.apiTool.service.impl;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
public class BlacklackUserServiceImpl
        extends ServiceImpl<BlacklackUserMapper, BlacklackUser>
        implements IBlacklackUserService {
    @Override
    @Transactional
    public void replaceAll(List<BlacklackUser> blacklackUserList) {
        if (blacklackUserList == null || blacklackUserList.isEmpty()) {
            // 空数据，直接返回
            return;
        }
        // 删除所有旧数据
        this.remove(new QueryWrapper<>());

        // 批量插入新数据
        this.saveBatch(blacklackUserList);
    }
    @Autowired
    private BlacklackUserMapper blacklackUserMapper;

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
