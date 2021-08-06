<template>
  <el-row style="background-color: #000;">
    <el-col :span="24">
      <div style="padding:10px;text-align:left;">
        <span style="margin-left:200px;letter-spacing:10px;font-size: 25px;color: #fff;font-family: Microsoft YaHei; font-weight: bold;font-style: italic;">DMS 数据库管理平台</span>
      </div>
    </el-col>
    <!--<el-col :span="16">
      <el-menu
          router
          :default-active="defaultActive"
          class="el-menu"
          mode="horizontal"
          @select="handleSelect"
          background-color="#009688"
          text-color="#fff"
          active-text-color="#ffd04b">

        <el-menu-item index="index"><i class="el-icon-loading"></i> 首页</el-menu-item>
        <el-menu-item index="agent"><i class="el-icon-tickets"></i> 反向代理</el-menu-item>
        <el-menu-item index="console"><i class="el-icon-s-data"></i> 数据仓库</el-menu-item>
        <el-menu-item index="terminal"><i class="el-icon-monitor"></i> 远程终端</el-menu-item>
        <el-menu-item index="user"><i class="el-icon-user"></i> 用户管理</el-menu-item>
        <el-menu-item index="system"><i class="el-icon-setting"></i> 系统管理</el-menu-item>
      </el-menu>
    </el-col>-->
  </el-row>

  <!--<el-header style="text-align: left !important;">
    <span class="select" style="text-align: left !important; margin-left:200px;letter-spacing:10px;font-size: 20px;">数据开发平台</span>

    <el-link :underline="false" class="menu-zyy" href="index"><i class="el-icon-loading"></i> 首页</el-link>
    <el-divider direction="vertical"></el-divider>
    <el-link :underline="false" class="menu-zyy" href="console"><i class="el-icon-tickets"></i> 代理管理</el-link>
    <el-divider direction="vertical"></el-divider>
    <el-link :underline="false" class="menu-zyy"><i class="el-icon-user"></i> 用户管理</el-link>
    <el-divider direction="vertical"></el-divider>
    <el-link :underline="false" class="menu-zyy"><i class="el-icon-setting"></i> 系统管理</el-link>
  </el-header>-->
</template>

<script>
export default {
  name: "Header",
  data() {
    return {
      defaultActive: 'index',
    }
  },
  methods: {
    // eslint-disable-next-line no-unused-vars
    handleSelect(key, keyPath) {
      // 切换的时候更换标签
      this.defaultActive = key;
    }
  },
  mounted() {
    // 初始化，刷新的时候激活标签
    const path = this.$route.path;
    this.defaultActive = path.replace("/", "");
  }
}
</script>

<style scoped>

    .el-header, .el-footer {
      background-color: #009688;
      color: #fff;
      text-align: center;
      line-height: 60px;
    }
    body {
      margin: 0 !important;
    }

    /* 下沉效果 */
    .el-link:hover {
      position:relative;
      top:1px;
      left:1px;
      color: #409EFF !important;
      font-weight: bold;
    }

    .menu-zyy {
      font-size: 15px;
      color:#fff !important;
      margin-left:40px;
      margin-right:40px;
    }

    .el-menu{
      color: #fff;
      font-weight: bold;
    }
/*    .el-icon-loading:before {
      color: #fff;
    }
    .el-icon-tickets:before {
      color: #fff;
    }
    .el-icon-user:before {
      color: #fff;
    }
    .el-icon-setting:before {
      color: #fff;
    }

    .el-icon-loading:after {
      color: #ffd04b;
    }
    .el-icon-tickets:after {
      color: #ffd04b;
    }
    .el-icon-user:after {
      color: #ffd04b;
    }
    .el-icon-setting:after {
      color: #ffd04b;
    }*/

    .el-menu.el-menu--horizontal {
      border-bottom: solid 1px #009688;
    }
</style>