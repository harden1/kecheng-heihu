package com.ruoyi.apiTool.mapper;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ruoyi.apiTool.domain.BlacklackUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.session.ResultHandler;
import org.mybatis.spring.annotation.MapperScan;

/**
 * 黑湖用户信息Mapper接口
 * 
 * @author w
 * @date 2025-07-17
 */
@Mapper
public interface BlacklackUserMapper extends BaseMapper<BlacklackUser> {
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
     * 删除黑湖用户信息
     * 
     * @param id 黑湖用户信息主键
     * @return 结果
     */
    public int deleteBlacklackUserById(Long id);

    /**
     * 批量删除黑湖用户信息
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteBlacklackUserByIds(Long[] ids);
    @Override
    int insert(BlacklackUser entity);

//    @Override
//    int deleteById(BlacklackUser entity);

//    @Override
//    int delete(Wrapper<BlacklackUser> queryWrapper);

    @Override
    int updateById(BlacklackUser entity);

    @Override
    int update(BlacklackUser entity, Wrapper<BlacklackUser> updateWrapper);

    @Override
    BlacklackUser selectById(Serializable id);

    @Override
    List<BlacklackUser> selectByIds(Collection<? extends Serializable> idList);

    @Override
    void selectByIds(Collection<? extends Serializable> idList, ResultHandler<BlacklackUser> resultHandler);

    @Override
    Long selectCount(Wrapper<BlacklackUser> queryWrapper);

    @Override
    List<BlacklackUser> selectList(Wrapper<BlacklackUser> queryWrapper);

    @Override
    void selectList(Wrapper<BlacklackUser> queryWrapper, ResultHandler<BlacklackUser> resultHandler);

    @Override
    List<BlacklackUser> selectList(IPage<BlacklackUser> page, Wrapper<BlacklackUser> queryWrapper);

    @Override
    void selectList(IPage<BlacklackUser> page, Wrapper<BlacklackUser> queryWrapper, ResultHandler<BlacklackUser> resultHandler);

    @Override
    List<Map<String, Object>> selectMaps(Wrapper<BlacklackUser> queryWrapper);

    @Override
    void selectMaps(Wrapper<BlacklackUser> queryWrapper, ResultHandler<Map<String, Object>> resultHandler);

    @Override
    List<Map<String, Object>> selectMaps(IPage<? extends Map<String, Object>> page, Wrapper<BlacklackUser> queryWrapper);

    @Override
    void selectMaps(IPage<? extends Map<String, Object>> page, Wrapper<BlacklackUser> queryWrapper, ResultHandler<Map<String, Object>> resultHandler);

    @Override
    <E> List<E> selectObjs(Wrapper<BlacklackUser> queryWrapper);

    @Override
    <E> void selectObjs(Wrapper<BlacklackUser> queryWrapper, ResultHandler<E> resultHandler);
}
