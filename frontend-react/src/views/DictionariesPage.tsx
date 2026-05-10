import { useEffect, useState, useCallback } from 'react'
import { Table, Button, Card, Space, Input, Form, Drawer, InputNumber, Switch, Tabs, Popconfirm, message, Row, Col } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
import { getDictItems, createDictItem, updateDictItem, deleteDictItem } from '../api/dictionaries'
import type { DictItem, DictType } from '../api/dictionaries'
import { useAuthStore } from '../stores/auth'

const dictTabs: { key: DictType; label: string }[] = [
  { key: 'SHIP_TYPE', label: '船舶类型' },
  { key: 'CARGO_TYPE', label: '货物类型' },
  { key: 'PORT', label: '港口' },
  { key: 'CREW_POSITION', label: '船员岗位' },
]

type DrawerMode = 'create' | 'edit'

export default function DictionariesPage() {
  const [activeTab, setActiveTab] = useState<DictType>('SHIP_TYPE')
  const [data, setData] = useState<DictItem[]>([])
  const [loading, setLoading] = useState(false)
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [drawerMode, setDrawerMode] = useState<DrawerMode>('create')
  const [currentId, setCurrentId] = useState<number | null>(null)
  const [form] = Form.useForm()
  const canManage = useAuthStore((s) => s.user?.roleCode === 'ADMIN')

  const loadData = useCallback(async () => {
    setLoading(true)
    try {
      const items = await getDictItems(activeTab)
      setData(items)
    } finally {
      setLoading(false)
    }
  }, [activeTab])

  useEffect(() => { loadData() }, [loadData])

  const openCreate = () => {
    setDrawerMode('create')
    setCurrentId(null)
    form.resetFields()
    form.setFieldsValue({ dictType: activeTab, sort: 0, enabled: true })
    setDrawerOpen(true)
  }

  const openEdit = (record: DictItem) => {
    setDrawerMode('edit')
    setCurrentId(record.id)
    form.setFieldsValue(record)
    setDrawerOpen(true)
  }

  const handleSubmit = async () => {
    const values = await form.validateFields()
    if (drawerMode === 'create') {
      await createDictItem(values)
      message.success('创建成功')
    } else {
      await updateDictItem(currentId!, values)
      message.success('修改成功')
    }
    setDrawerOpen(false)
    loadData()
  }

  const handleDelete = async (id: number) => {
    await deleteDictItem(id)
    message.success('删除成功')
    loadData()
  }

  const columns = [
    { title: '显示名称', dataIndex: 'label', key: 'label' },
    { title: '字典值', dataIndex: 'value', key: 'value' },
    { title: '排序号', dataIndex: 'sort', key: 'sort', width: 80, align: 'center' as const },
    { title: '启用状态', dataIndex: 'enabled', key: 'enabled', width: 100, render: (v: boolean) => v ? '启用' : '停用' },
    { title: '备注', dataIndex: 'remark', key: 'remark' },
    {
      title: '操作', key: 'action', width: 150,
      render: (_: any, record: DictItem) => (
        <Space size="small">
          <Button type="link" size="small" icon={<EditOutlined />} onClick={() => openEdit(record)}>编辑</Button>
          <Popconfirm title="确认删除此字典项？" onConfirm={() => handleDelete(record.id)}>
            <Button type="link" size="small" danger icon={<DeleteOutlined />}>删除</Button>
          </Popconfirm>
        </Space>
      ),
    },
  ]

  return (
    <div>
      <Card>
        <Tabs
          activeKey={activeTab}
          onChange={(key) => setActiveTab(key as DictType)}
          items={dictTabs.map((tab) => ({
            key: tab.key,
            label: tab.label,
            children: (
              <>
                <div style={{ marginBottom: 16, textAlign: 'right' }}>
                  {canManage && <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>新增字典项</Button>}
                </div>
                <Table
                  columns={columns}
                  dataSource={data}
                  rowKey="id"
                  loading={loading}
                  pagination={false}
                  size="middle"
                />
              </>
            ),
          }))}
        />
      </Card>
      <Drawer
        title={drawerMode === 'create' ? '新增字典项' : '编辑字典项'}
        width={480}
        open={drawerOpen}
        onClose={() => setDrawerOpen(false)}
        extra={<Button type="primary" onClick={handleSubmit}>保存</Button>}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="dictType" hidden>
            <Input />
          </Form.Item>
          <Form.Item name="label" label="显示名称" rules={[{ required: true, message: '请输入显示名称' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="value" label="字典值" rules={[{ required: true, message: '请输入字典值' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="sort" label="排序号">
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="enabled" label="启用状态" valuePropName="checked">
            <Switch checkedChildren="启用" unCheckedChildren="停用" />
          </Form.Item>
          <Form.Item name="remark" label="备注">
            <Input.TextArea rows={3} />
          </Form.Item>
        </Form>
      </Drawer>
    </div>
  )
}
