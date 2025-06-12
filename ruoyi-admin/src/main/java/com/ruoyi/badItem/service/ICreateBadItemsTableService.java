package com.ruoyi.badItem.service;

import java.util.List;
import com.ruoyi.badItem.domain.CreateBadItemsTable;

/**
 * badItemService接口
 * 
 * @author ruoyi
 * @date 2025-06-11
 */
public interface ICreateBadItemsTableService 
{
    /**
     * 查询badItem
     * 
     * @param id badItem主键
     * @return badItem
     */
    public CreateBadItemsTable selectCreateBadItemsTableById(Long id);

    /**
     * 查询badItem列表
     * 
     * @param createBadItemsTable badItem
     * @return badItem集合
     */
    public List<CreateBadItemsTable> selectCreateBadItemsTableList(CreateBadItemsTable createBadItemsTable);

    /**
     * 新增badItem
     * 
     * @param createBadItemsTable badItem
     * @return 结果
     */
    public int insertCreateBadItemsTable(CreateBadItemsTable createBadItemsTable);

    /**
     * 修改badItem
     * 
     * @param createBadItemsTable badItem
     * @return 结果
     */
    public int updateCreateBadItemsTable(CreateBadItemsTable createBadItemsTable);

    /**
     * 批量删除badItem
     * 
     * @param ids 需要删除的badItem主键集合
     * @return 结果
     */
    public int deleteCreateBadItemsTableByIds(Long[] ids);

    /**
     * 删除badItem信息
     * 
     * @param id badItem主键
     * @return 结果
     */
    public int deleteCreateBadItemsTableById(Long id);
}
