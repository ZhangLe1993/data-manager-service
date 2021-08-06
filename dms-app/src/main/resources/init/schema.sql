-- DROP TABLE IF EXISTS `connection`;
CREATE TABLE IF NOT EXISTS `connection`
(
    `id`             int(11)   NOT NULL AUTO_INCREMENT,
    `name`           varchar(255) NOT NULL,
    `config`         text , -- 连接信息
    -- 连接模式: 是实例还是连接,这里用来区分 mysql直接用IP +HOST+ 端口还是用连接传直连 schema, 因为后面还要做workbench那种类型的页面
    `mode`           varchar(15) NOT NULL DEFAULT 'standard',  -- standard : 标准模式,  separate: 分离模式, 不支持跨库查询
    `create_time`    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,                             -- '创建时间'
    `update_time`    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP , -- '修改时间'
    PRIMARY KEY (`id`)
);