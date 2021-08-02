-- DROP TABLE IF EXISTS `connection`;
CREATE TABLE IF NOT EXISTS `connection`
(
    `id`             int(11)   NOT NULL AUTO_INCREMENT,
    `name`           varchar(255) NOT NULL,
    `config`         text , --
    `create_time`    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP , -- '创建时间'
    `update_time`    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP , -- '修改时间'
    PRIMARY KEY (`id`)
);