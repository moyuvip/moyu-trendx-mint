package com.moyu.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author : moyuvip666
 * @Since: 2023/10/25 18:23
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_py_trendx")
public class PyTrendX {


    @TableId(value = "Fsn", type = IdType.AUTO)
    private Integer sn;

    @TableField("Fuid")
    private String uid;

    @TableField("Ffull_name")
    private String fullName;

    @TableField("Fnews_id")
    private String newsId;

    @TableField("Fis_like")
    private Integer isLike;
}
