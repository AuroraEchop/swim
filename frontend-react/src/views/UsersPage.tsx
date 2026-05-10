import { useEffect, useState, useCallback } from 'react'
import { Table, Button, Card, Space, Input, Form, Drawer, Select, Popconfirm, message, Row, Col, Typography } from 'antd'
import { PlusOutlined, SearchOutlined, ReloadOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
import { getUsers, getUser, createUser, updateUser, deleteUser } from '../api/users'
import type { User, UserQuery, CreateUserRequest } from '../api/users'
import RoleSelect from '../components/RoleSelect'
import { userStatusOptions } from '../constants/status'
import { useAuthStore } from '../stores/auth'

const { Text } = Typography
type DrawerMode = 'create' | 'edit'

export default function UsersPage() {
  const [data, setData] = useState<User[]>([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)
  const [query, setQuery] = useState<UserQuery>({ page: 1, pageSize: 10 })
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [drawerMode, setDrawerMode] = useState<DrawerMode>('create')
  const [currentId, setCurrentId] = useState<number | null>(null)
  const [form] = Form.useForm()
  const currentUser = useAuthStore((s) => s.user)

  const loadData = useCallback(async () => {
    setLoading(true)
    try {
      const res = await getUsers(query)
      setData(res.records)
      setTotal(res.total)
    } finally {
      setLoading(false)
    }
  }, [query])

  useEffect(() => { loadData() }, [loadData])

  const openCreate = () => {
    setDrawerMode('create')
    setCurrentId(null)
    form.resetFields()
    form.setFieldsValue({ status: 'ENABLED' })
    setDrawerOpen(true)
  }

  const openEdit = async (id: number) => {
    setDrawerMode('edit')
    setCurrentId(id)
    const user = await getUser(id)
    form.setFieldsValue(user)
    setDrawerOpen(true)
  }

  const handleSubmit = async () => {
    const values = await form.validateFields()
    if (drawerMode === 'create') {
      await createUser(values as CreateUserRequest)
      message.success('创建成功')
    } else {
      await updateUser(currentId!, values)
      message.success('修改成功')
    }
    setDrawerOpen(false)
    loadData()
  }

  const handleDelete = async (id: number) => {
    await deleteUser(id)
    message.success('删除成功')
    loadData()
  }

  const columns = [
    { title: '用户名', dataIndex: 'username', key: 'username' },
    { title: '真实姓名', dataIndex: 'realName', key: 'realName' },
    { title: '联系电话', dataIndex: 'phone', key: 'phone', render: (v: string) => v || '-' },
    { title: '邮箱', dataIndex: 'email', key: 'email', render: (v: string) => v || '-' },
    { title: '角色', dataIndex: 'roleName', key: 'roleName' },
    { title: '状态', dataIndex: 'status', key: 'status', render: (s: string) => s === 'ENABLED' ? '启用' : '停用' },
    { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 170 },
    {
      title: '操作', key: 'action', width: 150,
      render: (_: any, record: User) => (
        <Space size="small">
          <Button type="link" size="small" icon={<EditOutlined />} onClick={() => openEdit(record.id)}>编辑</Button>
          {record.username !== 'admin' && record.id !== currentUser?.id && (
            <Popconfirm title="确认删除此用户？" onConfirm={() => handleDelete(record.id)}>
              <Button type="link" size="small" danger icon={<DeleteOutlined />}>删除</Button>
            </Popconfirm>
          )}
        </Space>
      ),
    },
  ]

  return (
    <div>
      <Card style={{ marginBottom: 16 }}>
        <Row gutter={16} align="middle">
          <Col span={5}>
            <Input
              placeholder="用户名"
              value={query.username}
              onChange={(e) => setQuery((q) => ({ ...q, username: e.target.value || undefined, page: 1 }))}
              allowClear
            />
          </Col>
          <Col span={4}>
            <Input
              placeholder="真实姓名"
              value={query.realName}
              onChange={(e) => setQuery((q) => ({ ...q, realName: e.target.value || undefined, page: 1 }))}
              allowClear
            />
          </Col>
          <Col span={4}>
            <RoleSelect value={query.roleId} onChange={(v) => setQuery((q) => ({ ...q, roleId: v, page: 1 }))} placeholder="角色" />
          </Col>
          <Col span={3}>
            <Select
              placeholder="用户状态"
              value={query.status}
              onChange={(v) => setQuery((q) => ({ ...q, status: v, page: 1 }))}
              options={userStatusOptions}
              allowClear
              style={{ width: '100%' }}
            />
          </Col>
          <Col span={8} style={{ textAlign: 'right' }}>
            <Space>
              <Button icon={<SearchOutlined />} onClick={() => loadData()}>查询</Button>
              <Button icon={<ReloadOutlined />} onClick={() => { setQuery({ page: 1, pageSize: 10 }) }}>重置</Button>
              <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>新增用户</Button>
            </Space>
          </Col>
        </Row>
      </Card>
      <Card>
        <Table
          columns={columns}
          dataSource={data}
          rowKey="id"
          loading={loading}
          pagination={{
            current: query.page,
            pageSize: query.pageSize,
            total,
            showSizeChanger: true,
            showTotal: (t) => `共 ${t} 条`,
            onChange: (page, pageSize) => setQuery((q) => ({ ...q, page, pageSize })),
          }}
          size="middle"
        />
      </Card>
      <Drawer
        title={drawerMode === 'create' ? '新增用户' : '编辑用户'}
        width={520}
        open={drawerOpen}
        onClose={() => setDrawerOpen(false)}
        extra={<Button type="primary" onClick={handleSubmit}>保存</Button>}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="username" label="用户名" rules={[{ required: true, message: '请输入用户名' }]}>
            <Input disabled={drawerMode === 'edit'} />
          </Form.Item>
          {drawerMode === 'create' && (
            <>
              <Form.Item name="password" label="密码" rules={[
                { required: true, message: '请输入密码' },
                { min: 6, max: 30, message: '密码长度 6-30 位' },
              ]}>
                <Input.Password />
              </Form.Item>
              <Text type="secondary" style={{ display: 'block', marginTop: -16, marginBottom: 16, fontSize: 12 }}>
                本课程设计按简化方案保存普通文本密码
              </Text>
            </>
          )}
          <Form.Item name="realName" label="真实姓名" rules={[{ required: true, message: '请输入真实姓名' }]}>
            <Input />
          </Form.Item>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="phone" label="联系电话">
                <Input />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="email" label="邮箱">
                <Input />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="roleId" label="角色" rules={[{ required: true, message: '请选择角色' }]}>
                <RoleSelect />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="status" label="状态" rules={[{ required: true, message: '请选择状态' }]}>
                <Select options={userStatusOptions} />
              </Form.Item>
            </Col>
          </Row>
        </Form>
      </Drawer>
    </div>
  )
}
