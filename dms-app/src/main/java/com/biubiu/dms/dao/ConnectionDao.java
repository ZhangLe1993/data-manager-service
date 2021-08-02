package com.biubiu.dms.dao;

import com.biubiu.dms.pojo.Connection;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Set;

@Mapper
public interface ConnectionDao {
    @Insert({
            "<script>",
                "insert into `connection`",
                "<trim prefix='(' suffix=')' suffixOverrides=','>",
                    "`name`,",
                    "<if test='config != null'>",
                        "`config`,",
                    "</if>",
                "</trim>",
                "<trim prefix='values (' suffix=')' suffixOverrides=','>",
                    "#{name},",
                    "<if test='config != null'>",
                        "#{config},",
                    "</if>",
                "</trim>",
            "</script>"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(Connection connection);

    @Update({
            "<script>",
            "update `connection` ",
                "<set>",
                    "<if test='name != null'>",
                        "`name` = #{name},",
                    "</if>",
                    "<if test='config != null'>",
                        "`config` = #{config},",
                    "</if>",
                "</set>",
                "where id = #{id}",
            "</script>"
    })
    int update(Connection connection);

    @Select({
            "select * from `connection`"
    })
    List<Connection> list();

    @Select({
            "select * from `connection` where id = #{id} limit 0, 1"
    })
    Connection findById(@Param("id") Long id);


    @Select({
            "select id from `connection` where name = #{name} limit 0, 1"
    })
    Long getByName(@Param("name") String name);

    @Delete({
            "<script>",
                "delete from `connection` where id in ",
                "<foreach collection='ids' item='item' index='index' open='(' separator=',' close=')'>",
                    "#{item}",
                "</foreach>",
            "</script>",
    })
    int batchDelete(@Param("ids") Set<Long> ids);

}
