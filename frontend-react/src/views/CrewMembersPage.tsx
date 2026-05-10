import { useEffect, useState, useCallback } from 'react'
import { Table, Button, Card, Space, Input, Form, Drawer, Select, Popconfirm, message, Row, Col, Dropdown } from 'antd'
import { PlusOutlined, SearchOutlined, ReloadOutlined, EyeOutlined, EditOutlined, DeleteOutlined, DownOutlined } from '@ant-design/icons'
import { getCrewMembers, getCrewMember, createCrewMember, updateCrewMember, deleteCrewMember, updateCrewMemberStatus } from '../api/crewMembers'
import type { CrewMember, CrewMemberQuery, CrewStatus, CreateCrewMemberRequest } from '../api/crewMembers'
import StatusTag from '../components/StatusTag'
import ShipSelect from '../components/ShipSelect'
import DictionarySelect from '../components/DictionarySelect'
import { crewStatusOptions } from '../constants/status'
import { useAuthStore } from '../stores/auth'

type DrawerMode = 'create' | 'edit' | 'view'

export default function CrewMembersPage() {
  const [data, setData] = useState<CrewMember[]>([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)
  const [query, setQuery] = useState<CrewMemberQuery>({ page: 1, pageSize: 10 })
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [drawerMode, setDrawerMode] = useState<DrawerMode>('create')
  const [currentId, setCurrentId] = useState<number | null>(null)
  const [form] = Form.useForm()
  const canManage = useAuthStore((s) => s.user?.roleCode === 'ADMIN')

  const loadData = useCallback(async () => {
    setLoading(true)
    try {
      const res = await getCrewMembers(query)
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
    form.setFieldsValue({ status: 'ON_DUTY', gender: '男' })
    setDrawerOpen(true)
  }

  const openDetail = async (id: number, mode: 'view' | 'edit') => {
    setDrawerMode(mode)
    setCurrentId(id)
    const crew = await getCrewMember(id)
    form.setFieldsValue(crew)
    setDrawerOpen(true)
  }

  const handleSubmit = async () => {
    const values = await form.validateFields()
    if (drawerMode === 'create') {
      await createCrewMember(values as CreateCrewMemberRequest)
      message.success('创建成功')
    } else {
      await updateCrewMember(currentId!, values)
      message.success('修改成功')
    }
    setDrawerOpen(false)
    loadData()
  }

  const handleDelete = async (id: number) => {
    await deleteCrewMember(id)
    message.success('删除成功')
    loadData()
  }

  const handleStatusChange = async (id: number, status: CrewStatus) => {
    await updateCrewMemberStatus(id, status)
    message.success('状态修改成功')
    loadData()
  }

  const columns = [
    { title: '船员编号', dataIndex: 'crewNo', key: 'crewNo' },
    { title: '姓名', dataIndex: 'name', key: 'name' },
    { title: '性别', dataIndex: 'gender', key: 'gender', width: 60 },
    { title: '联系电话', dataIndex: 'phone', key: 'phone' },
    { title: '证件编号', dataIndex: 'certificateNo', key: 'certificateNo' },
    { title: '岗位', dataIndex: 'position', key: 'position' },
    { title: '所属船舶', dataIndex: 'shipName', key: 'shipName', render: (v: string) => v || '未分配' },
    { title: '状态', dataIndex: 'status', key: 'status', render: (s: string) => <StatusTag status={s} /> },
    {
      title: '操作', key: 'action', width: 200, fixed: 'right' as const,
      render: (_: any, record: CrewMember) => (
        <Space size="small">
          <Button type="link" size="small" icon={<EyeOutlined />} onClick={() => openDetail(record.id, 'view')}>查看</Button>
          {canManage && (
            <>
              <Button type="link" size="small" icon={<EditOutlined />} onClick={() => openDetail(record.id, 'edit')}>编辑</Button>
              <Dropdown menu={{
                items: crewStatusOptions
                  .filter((o) => o.value !== record.status)
                  .map((o) => ({ key: o.value, label: o.label, onClick: () => handleStatusChange(record.id, o.value as CrewStatus) })),
              }}>
                <Button type="link" size="small">状态 <DownOutlined /></Button>
              </Dropdown>
              <Popconfirm title="确认删除此船员？" onConfirm={() => handleDelete(record.id)}>
                <Button type="link" size="small" danger icon={<DeleteOutlined />}>删除</Button>
              </Popconfirm>
            </>
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
              placeholder="姓名或证件编号"
              value={query.keyword}
              onChange={(e) => setQuery((q) => ({ ...q, keyword: e.target.value || undefined, page: 1 }))}
              allowClear
            />
          </Col>
          <Col span={4}>
            <DictionarySelect dictType="CREW_POSITION" value={query.position} onChange={(v) => setQuery((q) => ({ ...q, position: v, page: 1 }))} placeholder="岗位" />
          </Col>
          <Col span={4}>
            <ShipSelect value={query.shipId} onChange={(v) => setQuery((q) => ({ ...q, shipId: v, page: 1 }))} placeholder="所属船舶" />
          </Col>
          <Col span={4}>
            <Select
              placeholder="船员状态"
              value={query.status}
              onChange={(v) => setQuery((q) => ({ ...q, status: v, page: 1 }))}
              options={crewStatusOptions}
              allowClear
              style={{ width: '100%' }}
            />
          </Col>
          <Col span={7} style={{ textAlign: 'right' }}>
            <Space>
              <Button icon={<SearchOutlined />} onClick={() => loadData()}>查询</Button>
              <Button icon={<ReloadOutlined />} onClick={() => { setQuery({ page: 1, pageSize: 10 }) }}>重置</Button>
              {canManage && <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>新增船员</Button>}
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
          scroll={{ x: 1200 }}
          size="middle"
        />
      </Card>
      <Drawer
        title={drawerMode === 'create' ? '新增船员' : drawerMode === 'edit' ? '编辑船员' : '船员详情'}
        width={600}
        open={drawerOpen}
        onClose={() => setDrawerOpen(false)}
        extra={drawerMode !== 'view' && (
          <Button type="primary" onClick={handleSubmit}>保存</Button>
        )}
      >
        <Form form={form} layout="vertical" disabled={drawerMode === 'view'}>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="crewNo" label="船员编号" rules={[{ required: true, message: '请输入船员编号' }]}>
                <Input disabled={drawerMode === 'edit'} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="name" label="姓名" rules={[{ required: true, message: '请输入姓名' }]}>
                <Input />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="gender" label="性别" rules={[{ required: true, message: '请选择性别' }]}>
                <Select options={[{ label: '男', value: '男' }, { label: '女', value: '女' }]} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="phone" label="联系电话" rules={[{ required: true, message: '请输入联系电话' }]}>
                <Input />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="certificateNo" label="证件编号" rules={[{ required: true, message: '请输入证件编号' }]}>
                <Input />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="position" label="岗位" rules={[{ required: true, message: '请选择岗位' }]}>
                <DictionarySelect dictType="CREW_POSITION" />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="shipId" label="所属船舶">
                <ShipSelect />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="status" label="状态" rules={[{ required: true, message: '请选择状态' }]}>
                <Select options={crewStatusOptions} />
              </Form.Item>
            </Col>
          </Row>
          <Form.Item name="remark" label="备注">
            <Input.TextArea rows={3} />
          </Form.Item>
        </Form>
      </Drawer>
    </div>
  )
}
