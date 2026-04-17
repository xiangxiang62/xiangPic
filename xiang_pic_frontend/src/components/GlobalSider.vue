<template>
  <div id="globalSider">
    <a-layout-sider v-if="loginUserStore.loginUser.id"  class="sider" width="200" breakpoint="lg"
                    collapsed-width="0">
      <a-menu
        mode="inline"
        v-model:selectedKeys="current"
        :items="menuItems"
        @click="doMenuClick"
      />
    </a-layout-sider>
  </div>
</template>


<script lang="ts" setup>
import {h,ref} from "vue";
import {useRouter} from "vue-router";
import { PictureOutlined, UserOutlined } from '@ant-design/icons-vue';
const router = useRouter();
import {useLoginUserStore} from "@/stores/useLoginUserStore.ts";

const loginUserStore = useLoginUserStore();
// 路由跳转事件
const doMenuClick = ({key}: { key: string }) => {
  router.push({
    path: key,
  });
};

// 监听路由变化，更新当前选中菜单
router.afterEach((to, from, next) => {
  current.value = [to.path];
});

// 菜单列表
const menuItems = [
  {
    key: '/',
    label: '公共图库',
    icon: () => h(PictureOutlined),
  },
  {
    key: '/my_space',
    label: '我的空间',
    icon: () => h(UserOutlined),
  },
]

const current = ref<string[]>([])
// 当前选中菜单
// 监听路由变化，更新当前选中菜单
router.afterEach((to, from, failure) => {
  current.value = [to.path]
})


</script>

<style scoped>
.globalSider .ant-layout-sider {
  background-color: white;
}
</style>
