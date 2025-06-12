package com.ruoyi.badItem.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.badItem.mapper.CreateBadItemsTableMapper;
import com.ruoyi.badItem.domain.CreateBadItemsTable;
import com.ruoyi.badItem.service.ICreateBadItemsTableService;

/**
 * badItemService业务层处理
 * 
 * @author ruoyi
 * @date 2025-06-11
 */
@Service
public class CreateBadItemsTableServiceImpl implements ICreateBadItemsTableService 
{
    @Autowired
    private CreateBadItemsTableMapper createBadItemsTableMapper;

    /**
     * 查询badItem
     * 
     * @param id badItem主键
     * @return badItem
     */
    @Override
    public CreateBadItemsTable selectCreateBadItemsTableById(Long id)
    {
        return createBadItemsTableMapper.selectCreateBadItemsTableById(id);
    }

    /**
     * 查询badItem列表
     * 
     * @param createBadItemsTable badItem
     * @return badItem
     */
    @Override
    public List<CreateBadItemsTable> selectCreateBadItemsTableList(CreateBadItemsTable createBadItemsTable)
    {
        return createBadItemsTableMapper.selectCreateBadItemsTableList(createBadItemsTable);
    }

    /**
     * 新增badItem
     * 
     * @param createBadItemsTable badItem
     * @return 结果
     */
    @Override
    public int insertCreateBadItemsTable(CreateBadItemsTable createBadItemsTable)
    {
        createBadItemsTable.setCreateTime(DateUtils.getNowDate());
        return createBadItemsTableMapper.insertCreateBadItemsTable(createBadItemsTable);
    }

    /**
     * 修改badItem
     * 
     * @param createBadItemsTable badItem
     * @return 结果
     */
    @Override
    public int updateCreateBadItemsTable(CreateBadItemsTable createBadItemsTable)
    {
        createBadItemsTable.setUpdateTime(DateUtils.getNowDate());
        return createBadItemsTableMapper.updateCreateBadItemsTable(createBadItemsTable);
    }

    /**
     * 批量删除badItem
     * 
     * @param ids 需要删除的badItem主键
     * @return 结果
     */
    @Override
    public int deleteCreateBadItemsTableByIds(Long[] ids)
    {
        return createBadItemsTableMapper.deleteCreateBadItemsTableByIds(ids);
    }

    /**
     * 删除badItem信息
     * 
     * @param id badItem主键
     * @return 结果
     */
    @Override
    public int deleteCreateBadItemsTableById(Long id)
    {
        return createBadItemsTableMapper.deleteCreateBadItemsTableById(id);
    }
}
