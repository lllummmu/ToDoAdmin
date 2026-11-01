<template>
  <el-dialog
    :model-value="visible"
    :title="task ? '编辑任务' : '新增任务'"
    width="640px"
    @update:model-value="emit('update:visible', $event)"
    @open="onOpen"
  >
    <el-form :model="form" label-width="90px" ref="formRef" :rules="rules">
      <el-form-item label="任务内容" prop="text">
        <el-input v-model="form.text" placeholder="请输入任务内容" maxlength="255" show-word-limit />
      </el-form-item>
      <el-row :gutter="12">
        <el-col :span="8">
          <el-form-item label="优先级" label-width="60px">
            <el-select v-model="form.priority" class="w-full">
              <el-option label="高" value="high" />
              <el-option label="中" value="medium" />
              <el-option label="低" value="low" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="16">
          <el-form-item label="截止时间" label-width="70px">
            <el-date-picker
              v-model="form.dueDate"
              type="datetime"
              placeholder="选择截止时间"
              value-format="YYYY-MM-DDTHH:mm:ss"
              class="w-full"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="执行人">
        <UserSelect v-model="form.assigneeId" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input
          v-model="form.remark"
          type="textarea"
          :rows="4"
          placeholder="支持 Markdown 语法，例如 **加粗**、# 标题、- 列表"
        />
        <div v-if="form.remark" class="remark-preview">
          <div class="preview-title">预览</div>
          <MarkdownView :content="form.remark" />
        </div>
      </el-form-item>
      <el-form-item label="附件">
        <div class="upload-area">
          <el-upload
            :show-file-list="false"
            :http-request="onUpload"
            accept="image/*,.pdf,.txt,.md,.zip"
          >
            <el-button :icon="Upload" :loading="uploading">上传附件</el-button>
          </el-upload>
          <div v-if="form.attachment" class="uploaded">
            <el-image
              v-if="isImage"
              :src="'/' + form.attachment"
              fit="contain"
              class="upload-preview"
            />
            <span class="upload-name">{{ form.attachment.split('/').pop() }}</span>
            <el-button link type="danger" :icon="Delete" @click="form.attachment = null" />
          </div>
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="emit('update:visible', false)">取消</el-button>
      <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { ElMessage, type FormInstance, type FormRules, type UploadRequestOptions } from 'element-plus'
import { Upload, Delete } from '@element-plus/icons-vue'
import type { Priority, Task } from '../api'
import { uploadApi } from '../api'
import { useTaskStore } from '../store/task'
import UserSelect from './UserSelect.vue'
import MarkdownView from './MarkdownView.vue'

const props = defineProps<{ visible: boolean; task: Task | null }>()
const emit = defineEmits<{ 'update:visible': [value: boolean] }>()

const taskStore = useTaskStore()
const formRef = ref<FormInstance>()
const saving = ref(false)
const uploading = ref(false)

interface EditForm {
  text: string
  priority: Priority
  dueDate: string | null
  assigneeId: number | null
  remark: string
  attachment: string | null
}

const form = reactive<EditForm>({
  text: '',
  priority: 'medium',
  dueDate: null,
  assigneeId: null,
  remark: '',
  attachment: null
})

const rules: FormRules = {
  text: [{ required: true, message: '请输入任务内容', trigger: 'blur' }]
}

const isImage = computed(() => /\.(png|jpe?g|gif|webp|bmp|svg)$/i.test(form.attachment ?? ''))

// 打开对话框时回填（编辑）或重置（新增）
function onOpen() {
  const t = props.task
  form.text = t?.text ?? ''
  form.priority = t?.priority ?? 'medium'
  form.dueDate = t?.dueDate ?? null
  form.assigneeId = t?.assigneeId ?? null
  form.remark = t?.remark ?? ''
  form.attachment = t?.attachment ?? null
}

async function onUpload(options: UploadRequestOptions) {
  uploading.value = true
  try {
    const res = await uploadApi(options.file as File)
    form.attachment = res.url
    ElMessage.success('附件上传成功')
  } finally {
    uploading.value = false
  }
}

async function onSave() {
  await formRef.value?.validate()
  saving.value = true
  try {
    const payload = {
      text: form.text,
      priority: form.priority,
      dueDate: form.dueDate,
      assigneeId: form.assigneeId,
      remark: form.remark,
      attachment: form.attachment
    }
    if (props.task) {
      await taskStore.updateTask(props.task.id, payload)
      ElMessage.success('任务已更新')
    } else {
      await taskStore.createTask(payload)
      ElMessage.success('任务已创建')
    }
    emit('update:visible', false)
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.w-full {
  width: 100%;
}

.remark-preview {
  width: 100%;
  margin-top: 8px;
  padding: 8px 12px;
  background: #fafafa;
  border: 1px dashed #dcdfe6;
  border-radius: 6px;
}

.preview-title {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.upload-area {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.uploaded {
  display: flex;
  align-items: center;
  gap: 8px;
}

.upload-preview {
  width: 64px;
  height: 64px;
  border-radius: 6px;
  border: 1px solid #ebeef5;
}

.upload-name {
  font-size: 12px;
  color: #606266;
}
</style>
