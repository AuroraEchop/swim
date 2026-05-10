import { useEffect, useState } from 'react'
import { Table, Button, Card, Space, Form, Drawer, Input, Popconfirm, message } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
import { getRoles, createRole, updateRole, deleteRole } from '../api/roles'
import type { Role, CreateRoleRequest } from '../api/roles'

type DrawerMode = 'create' | 'edit'

export default function RolesPage() {
  const [data, setData] = useState<Role[]>([])
  const [loading, setLoading] = useState(false)
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [drawerMode, setDrawerMode] = useState<DrawerMode>('create')
  const [currentId, setCurrentId] = useState<number | null>(null)
  const [form] = Form.useForm()

  const loadData = async () => {
    setLoading(true)
    try {
      const roles = await getRoles()
      setData(roles)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { loadData() }, [])

  const openCreate = () => {
    setDrawerMode('create')
    setCurrentId(null)
    form.resetFields()
    setDrawerOpen(true)
  }

  const openEdit = (record: Role) => {
    setDrawerMode('edit')
    setCurrentId(record.id)
    form.setFieldsValue(record)
    setDrawerOpen(true)
  }

  const handleSubmit = async () => {
    const values = await form.validateFields()
    if (drawerMode === 'create') {
      await createRole(values as CreateRoleRequest)
      message.success('创建成功')
    } else {
      await updateRole(currentId!, values)
      message.success('修改成功')
    }
    setDrawerOpen(false)
    loadData()
  }

  const handleDelete = async (id: number) => {
    await deleteRole(id)
    message.success('删除成功')
    loadData()
  }

  const columns = [
    { title: '角色名称', dataIndex: 'roleName', key: 'roleName' },
    { title: '角色编码', dataIndex: 'roleCode', key: 'roleCode' },
    { title: '角色说明', dataIndex: 'description', key: 'description', render: (v: string) => v || '-' },
    { title: '内置角色', key: 'builtin', render: (_: any, record: Role) => record.roleCode === 'ADMIN' || record.roleCode === 'BUSINESS' ? '是' : '否' },
    {
      title: '操作', key: 'action', width: 150,
      render: (_: any, record: Role) => (
        <Space size="small">
          <Button type="link" size="small" icon={<EditOutlined />} onClick={() => openEdit(record)}>编辑</Button>
          {record.roleCode !== 'ADMIN' && record.roleCode !== 'BUSINESS' && (
            <Popconfirm title="确认删除此角色？" onConfirm={() => handleDelete(record.id)}>
              <Button type="link" size="small" danger icon={<DeleteOutlined />}>删除</Button>
            </Popconfirm>
          )}
        </Space>
      ),
    },
  ]

  return (
    <div>
      <Card>
        <div style={{ marginBottom: 16, textAlign: 'right' }}>
          <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>新增角色</Button>
        </div>
        <Table
          columns={columns}
          dataSource={data}
          rowKey="id"
          loading={loading}
          pagination={false}
          size="middle"
        />
      </Card>
      <Drawer
        title={drawerMode === 'create' ? '新增角色' : '编辑角色'}
        width={480}
        open={drawerOpen}
        onClose={() => setDrawerOpen(false)}
        extra={<Button type="primary" onClick={handleSubmit}>保存</Button>}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="roleName" label="角色名称" rules={[{ required: true, message: '请输入角色名称' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="roleCode" label="角色编码" rules={[{ required: true, message: '请输入角色编码' }]}>
            <Input disabled={drawerMode === 'edit'} />
          </Form.Item>
          <Form.Item name="description" label="角色说明">
            <Input.TextArea rows={3} />
          </Form.Item>
        </Form>
      </Drawer>
    </div>
  )
}
