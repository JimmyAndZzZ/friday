package com.jimmy.friday.demo.entity;



import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import java.util.Date;


/**
 * (TransactionTest)表实体类
 *
 * @author makejava
 * @since 2024-01-24 13:50:01
 */
@Data
public class TransactionTest  {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String name;
}

