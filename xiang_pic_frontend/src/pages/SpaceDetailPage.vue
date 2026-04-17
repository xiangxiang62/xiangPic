<template>
  <div id="spaceDetailPage">
    <!-- 空间信息 -->
    <a-flex justify="space-between">
      <h2>{{ space.spaceName }}（私有空间）</h2>
      <a-space size="middle">
        <a-button type="primary" :href="`/add_picture?spaceId=${id}`" target="_blank">
          + 创建图片
        </a-button>
        <a-button @click="doBatchEdit">
          <EditOutlined />
          批量编辑
        </a-button>
        <a-tooltip
          :title="`占用空间 ${formatSize(space.totalSize)} / ${formatSize(space.maxSize)}`"
        >
          <a-progress
            type="circle"
            :percent="((space.totalSize * 100) / space.maxSize).toFixed(1)"
            :size="42"
          />
        </a-tooltip>
      </a-space>
    </a-flex>
    <!-- 搜索表单 -->
    <PictureSearchForm :onSearch="onSearch" />
    <div style="margin-bottom: 16px" ></div>
    <!-- 按颜色搜索 -->
    <a-form-item label="按颜色搜索" style="margin-top: 16px">
      <input
        type="color"
        :value="selectedColor"
        @change="onColorChange"
        style="width: 32px; height: 32px; cursor: pointer;"
      />
    </a-form-item>
    <!-- 图片列表 -->
    <PictureList :dataList="dataList"
                 :loading="loading"
                 showOp
                 :onReload="fetchData"
    />
    <a-pagination
      style="text-align: right"
      v-model:current="searchParams.current"
      v-model:pageSize="searchParams.pageSize"
      :total="total"
      :show-total="() => `图片总数 ${total} / ${space.maxCount}`"
      @change="onPageChange"
    />
    <BatchEditPictureModal
      ref="batchEditPictureModalRef"
      :spaceId="id"
      :pictureList="dataList"
      :onSuccess="onBatchEditPictureSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import {
  listPictureVoByPageUsingPost, searchPictureByColorUsingPost
} from "@/api/pictureController";
import {onMounted, ref,h} from "vue";
import {message} from "ant-design-vue";
import {formatSize} from "@/utils";
import PictureList from "@/components/PictureList.vue";
import {getSpaceVoByIdUsingGet} from "@/api/spaceController";
import PictureSearchForm from "@/components/PictureSearchForm.vue";
import BatchEditPictureModal from "@/components/BatchEditPictureModal.vue";
const selectedColor = ref('#ffffff');
const props = defineProps<{
  id: string | number
}>()
const space = ref<API.SpaceVO>({})

// 获取空间详情
const fetchSpaceDetail = async () => {
  try {
    const res = await getSpaceVoByIdUsingGet({
      id: props.id,
    })
    if (res.data.code === 0 && res.data.data) {
      space.value = res.data.data
    } else {
      message.error('获取空间详情失败，' + res.data.message)
    }
  } catch (e: any) {
    message.error('获取空间详情失败：' + e.message)
  }
}

onMounted(() => {
  fetchSpaceDetail()
})

// 数据
const dataList = ref([])
const total = ref(0)
const loading = ref(true)

// 搜索条件
const searchParams = ref<API.PictureQueryRequest>({
  current: 1,
  pageSize: 12,
  sortField: 'createTime',
  sortOrder: 'descend',
})

// 分页参数
const onPageChange = (page, pageSize) => {
  searchParams.value.current = page
  searchParams.value.pageSize = pageSize
  fetchData()
}

// 搜索
const onSearch = (newSearchParams: API.PictureQueryRequest) => {
  searchParams.value = {
    ...searchParams.value,
    ...newSearchParams,
    current: 1,
  }
  fetchData()
}

// 获取数据
const fetchData = async () => {
  loading.value = true
  // 转换搜索参数
  const params = {
    spaceId: props.id,
    ...searchParams.value,
  }
  const res = await listPictureVoByPageUsingPost(params)
  if (res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
  loading.value = false
}

/**
 * 按照颜色搜索图片
 *
 * @param color
 */
const onColorChange = async (event: Event) => {
  const target = event.target as HTMLInputElement;
  const colorValue = target.value;
  console.log('选择的颜色：', colorValue);

  try {
    const res = await searchPictureByColorUsingPost({
      picColor: colorValue,
      spaceId: props.id,
    });

    if (res.data.code === 0 && res.data.data) {
      const data = res.data.data ?? [];
      dataList.value = data;
      total.value = data.length;
    } else {
      message.error('获取数据失败，' + res.data.message);
    }
  } catch (error: any) {
    message.error('获取数据失败：' + error.message);
  }
}

// 分享弹窗引用
const batchEditPictureModalRef = ref()

// 批量编辑成功后，刷新数据
const onBatchEditPictureSuccess = () => {
  fetchData()
}

// 打开批量编辑弹窗
const doBatchEdit = () => {
  if (batchEditPictureModalRef.value) {
    batchEditPictureModalRef.value.openModal()
  }
}

// 页面加载时请求一次
onMounted(() => {
  fetchData()
})
</script>

<style scoped>

</style>
