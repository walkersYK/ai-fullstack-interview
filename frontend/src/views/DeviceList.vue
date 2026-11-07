<template>
  <div class="app-shell">
    <header class="topbar">
      <div class="brand">
        <div class="brand-mark">EA</div>
        <div>
          <strong>Equipment Assets</strong>
          <span>Asset Registry</span>
        </div>
      </div>

      <div class="service-status" :class="`is-${apiState}`">
        <span class="status-dot" />
        {{ apiStateText }}
      </div>
    </header>

    <main class="workspace">
      <section class="hero-panel">
        <div>
          <div class="eyebrow">ASSET OPERATIONS</div>
          <h1>设备资产台账</h1>
          <p>设备基础信息与状态管理。</p>
        </div>
        <el-button type="primary" size="large" @click="openCreate">新增设备</el-button>
      </section>

      <section class="metrics-grid">
        <div class="metric-card">
          <span>设备总数</span>
          <strong>{{ devices.length }}</strong>
          <small>台账记录</small>
        </div>
        <div class="metric-card">
          <span>正常设备</span>
          <strong>{{ normalCount }}</strong>
          <small>状态为 NORMAL</small>
        </div>
        <div class="metric-card metric-card--meta">
          <span>数据存储</span>
          <strong>H2</strong>
          <small>本地开发环境</small>
        </div>
      </section>

      <el-card class="table-card" shadow="never">
        <template #header>
          <div class="card-header">
            <div>
              <strong>设备列表</strong>
              <span>设备基础信息与状态</span>
            </div>
            <el-button :loading="loading" @click="loadDevices">刷新</el-button>
          </div>
        </template>

        <el-table
          v-loading="loading"
          :data="devices"
          empty-text="暂无设备数据"
          stripe
          :row-class-name="rowClassName"
        >
          <el-table-column prop="code" label="设备编号" min-width="160" />
          <el-table-column prop="name" label="设备名称" min-width="260" />
          <el-table-column label="状态" width="130">
            <template #default="{ row }">
              <el-tag
                :type="row.status === 'NORMAL' ? 'success' : 'danger'"
                :effect="row.status === 'NORMAL' ? 'light' : 'dark'"
                round
              >
                {{ statusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" min-width="190">
            <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="170" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
              <el-button
                v-if="row.status === 'NORMAL'"
                link
                type="danger"
                @click="openDisable(row)"
              >停用</el-button>
              <span v-else class="op-muted">已停用</span>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <footer class="page-footer">
        <span>Java 17 · Spring Boot 4 · MyBatis-Plus</span>
        <span>Vue 3 · TypeScript · Vite 8</span>
      </footer>
    </main>

    <el-dialog
      v-model="dialogVisible"
      :title="editingId ? '编辑设备' : '新增设备'"
      width="460px"
      destroy-on-close
    >
      <el-form label-position="top" @submit.prevent>
        <el-form-item label="设备编号" required>
          <el-input v-model="form.code" maxlength="50" placeholder="例如 DEV-004" />
        </el-form-item>
        <el-form-item label="设备名称" required>
          <el-input v-model="form.name" maxlength="100" placeholder="请输入设备名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="disableDialogVisible"
      width="480px"
      destroy-on-close
      class="disable-dialog"
      :close-on-click-modal="false"
    >
      <template #header>
        <div class="disable-header">
          <div class="disable-icon">!</div>
          <div>
            <strong>停用设备确认</strong>
            <span>设备停用后将无法再次停用，请谨慎操作</span>
          </div>
        </div>
      </template>

      <div class="disable-summary">
        <div class="disable-summary-item">
          <label>设备编号</label>
          <strong>{{ disablingDevice?.code }}</strong>
        </div>
        <div class="disable-summary-item">
          <label>设备名称</label>
          <strong>{{ disablingDevice?.name }}</strong>
        </div>
      </div>

      <el-form label-position="top" @submit.prevent>
        <el-form-item label="停用原因" required>
          <el-input
            v-model="disableReason"
            type="textarea"
            :rows="4"
            maxlength="200"
            show-word-limit
            resize="none"
            placeholder="请填写停用原因，例如：设备老化、故障无法修复、达到使用年限等"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="disableDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="disabling" @click="confirmDisable">
          确认停用
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createDevice, disableDevice, getDevices, updateDevice } from '../api/device'
import { getApiErrorMessage } from '../api/http'
import { getHealth } from '../api/system'
import type { Device, DeviceForm } from '../types/device'

const devices = ref<Device[]>([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const apiState = ref<'checking' | 'online' | 'offline'>('checking')
const form = reactive<DeviceForm>({ code: '', name: '' })

const disableDialogVisible = ref(false)
const disabling = ref(false)
const disablingDevice = ref<Device | null>(null)
const disableReason = ref('')

const normalCount = computed(() => devices.value.filter((item) => item.status === 'NORMAL').length)
const apiStateText = computed(() => {
  if (apiState.value === 'online') return 'API 已连接'
  if (apiState.value === 'offline') return 'API 未连接'
  return '正在检查 API'
})

async function checkApi() {
  apiState.value = 'checking'
  try {
    const health = await getHealth()
    apiState.value = health.status === 'UP' ? 'online' : 'offline'
  } catch {
    apiState.value = 'offline'
  }
}

async function loadDevices() {
  loading.value = true
  try {
    devices.value = await getDevices()
  } catch (error) {
    ElMessage.error(getApiErrorMessage(error, '设备列表加载失败'))
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editingId.value = null
  form.code = ''
  form.name = ''
  dialogVisible.value = true
}

function openEdit(device: Device) {
  editingId.value = device.id
  form.code = device.code
  form.name = device.name
  dialogVisible.value = true
}

function openDisable(device: Device) {
  if (device.status !== 'NORMAL') {
    ElMessage.warning('仅正常状态的设备可以停用')
    return
  }
  disablingDevice.value = device
  disableReason.value = ''
  disableDialogVisible.value = true
}

async function confirmDisable() {
  const reason = disableReason.value.trim()
  if (!reason) {
    ElMessage.warning('请填写停用原因')
    return
  }
  if (reason.length > 200) {
    ElMessage.warning('停用原因不能超过 200 个字符')
    return
  }
  if (!disablingDevice.value) return

  disabling.value = true
  try {
    await disableDevice(disablingDevice.value.id, { reason })
    ElMessage.success('设备已停用')
    disableDialogVisible.value = false
    await loadDevices()
  } catch (error) {
    ElMessage.error(getApiErrorMessage(error, '设备停用失败'))
  } finally {
    disabling.value = false
  }
}

async function save() {
  if (!form.code.trim() || !form.name.trim()) {
    ElMessage.warning('请填写设备编号和设备名称')
    return
  }

  saving.value = true
  try {
    const payload: DeviceForm = {
      code: form.code.trim(),
      name: form.name.trim(),
    }

    if (editingId.value !== null) {
      await updateDevice(editingId.value, payload)
      ElMessage.success('设备已更新')
    } else {
      await createDevice(payload)
      ElMessage.success('设备已创建')
    }

    dialogVisible.value = false
    await loadDevices()
  } catch (error) {
    ElMessage.error(getApiErrorMessage(error))
  } finally {
    saving.value = false
  }
}

function statusText(status: string) {
  if (status === 'NORMAL') return '正常'
  if (status === 'DISABLED') return '已停用'
  return status
}

function rowClassName({ row }: { row: Device }) {
  return row.status === 'DISABLED' ? 'row-disabled' : ''
}

function formatTime(value: string) {
  return value ? value.replace('T', ' ').slice(0, 19) : '-'
}

onMounted(async () => {
  await Promise.all([checkApi(), loadDevices()])
})
</script>
