package com.ruoyi.badItem.mapper;

import java.util.List;
import com.ruoyi.badItem.domain.CreateBadItemsTable;

/**
 * badItemMapper接口
 * 
 * @author ruoyi
 * @date 2025-06-11
 */
public interface CreateBadItemsTableMapper 
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
     * 删除badItem
     * 
     * @param id badItem主键
     * @return 结果
     */
    public int deleteCreateBadItemsTableById(Long id);

    /**
     * 批量删除badItem
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCreateBadItemsTableByIds(Long[] ids);
}
