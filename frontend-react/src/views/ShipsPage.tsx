import { useEffect, useState, useCallback } from 'react'
import { Table, Button, Card, Space, Input, Form, Drawer, Select, InputNumber, Popconfirm, message, Row, Col, Dropdown } from 'antd'
import { PlusOutlined, SearchOutlined, ReloadOutlined, EyeOutlined, EditOutlined, DeleteOutlined, DownOutlined } from '@ant-design/icons'
import { getShips, getShip, createShip, updateShip, deleteShip, updateShipStatus } from '../api/ships'
import type { Ship, ShipQuery, ShipStatus, CreateShipRequest } from '../api/ships'
import StatusTag from '../components/StatusTag'
import DictionarySelect from '../components/DictionarySelect'
import { shipStatusOptions } from '../constants/status'
import { useAuthStore } from '../stores/auth'

type DrawerMode = 'create' | 'edit' | 'view'

export default function ShipsPage() {
  const [data, setData] = useState<Ship[]>([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)
  const [query, setQuery] = useState<ShipQuery>({ page: 1, pageSize: 10 })
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [drawerMode, setDrawerMode] = useState<DrawerMode>('create')
  const [currentId, setCurrentId] = useState<number | null>(null)
  const [form] = Form.useForm()
  const canManage = useAuthStore((s) => s.user?.roleCode === 'ADMIN')

  const loadData = useCallback(async () => {
    setLoading(true)
    try {
      const res = await getShips(query)
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
    form.setFieldsValue({ status: 'IDLE' })
    setDrawerOpen(true)
  }

  const openDetail = async (id: number, mode: 'view' | 'edit') => {
    setDrawerMode(mode)
    setCurrentId(id)
    const ship = await getShip(id)
    form.setFieldsValue(ship)
    setDrawerOpen(true)
  }

  const handleSubmit = async () => {
    const values = await form.validateFields()
    if (drawerMode === 'create') {
      await createShip(values as CreateShipRequest)
      message.success('创建成功')
    } else {
      await updateShip(currentId!, values)
      message.success('修改成功')
    }
    setDrawerOpen(false)
    loadData()
  }

  const handleDelete = async (id: number) => {
    await deleteShip(id)
    message.success('删除成功')
    loadData()
  }

  const handleStatusChange = async (id: number, status: ShipStatus) => {
    await updateShipStatus(id, status)
    message.success('状态修改成功')
    loadData()
  }

  const columns = [
    { title: '船舶编号', dataIndex: 'shipNo', key: 'shipNo' },
    { title: '船名', dataIndex: 'shipName', key: 'shipName' },
    { title: '船舶类型', dataIndex: 'shipType', key: 'shipType' },
    { title: '载重量(吨)', dataIndex: 'loadCapacity', key: 'loadCapacity', align: 'right' as const, render: (v: number) => v?.toLocaleString() },
    { title: '所属港口', dataIndex: 'homePort', key: 'homePort' },
    { title: '状态', dataIndex: 'status', key: 'status', render: (s: string) => <StatusTag status={s} /> },
    { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 170 },
    {
      title: '操作', key: 'action', width: 200, fixed: 'right' as const,
      render: (_: any, record: Ship) => (
        <Space size="small">
          <Button type="link" size="small" icon={<EyeOutlined />} onClick={() => openDetail(record.id, 'view')}>查看</Button>
          {canManage && (
            <>
              <Button type="link" size="small" icon={<EditOutlined />} onClick={() => openDetail(record.id, 'edit')}>编辑</Button>
              <Dropdown menu={{
                items: shipStatusOptions
                  .filter((o) => o.value !== record.status)
                  .map((o) => ({ key: o.value, label: o.label, onClick: () => handleStatusChange(record.id, o.value as ShipStatus) })),
              }}>
                <Button type="link" size="small">状态 <DownOutlined /></Button>
              </Dropdown>
              <Popconfirm title="确认删除此船舶？" onConfirm={() => handleDelete(record.id)}>
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
              placeholder="船名或编号"
              value={query.keyword}
              onChange={(e) => setQuery((q) => ({ ...q, keyword: e.target.value || undefined, page: 1 }))}
              allowClear
            />
          </Col>
          <Col span={4}>
            <DictionarySelect dictType="SHIP_TYPE" value={query.type} onChange={(v) => setQuery((q) => ({ ...q, type: v, page: 1 }))} placeholder="船舶类型" />
          </Col>
          <Col span={4}>
            <DictionarySelect dictType="PORT" value={query.homePort} onChange={(v) => setQuery((q) => ({ ...q, homePort: v, page: 1 }))} placeholder="所属港口" />
          </Col>
          <Col span={4}>
            <Select
              placeholder="船舶状态"
              value={query.status}
              onChange={(v) => setQuery((q) => ({ ...q, status: v, page: 1 }))}
              options={shipStatusOptions}
              allowClear
              style={{ width: '100%' }}
            />
          </Col>
          <Col span={7} style={{ textAlign: 'right' }}>
            <Space>
              <Button icon={<SearchOutlined />} onClick={() => loadData()}>查询</Button>
              <Button icon={<ReloadOutlined />} onClick={() => { setQuery({ page: 1, pageSize: 10 }) }}>重置</Button>
              {canManage && <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>新增船舶</Button>}
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
        title={drawerMode === 'create' ? '新增船舶' : drawerMode === 'edit' ? '编辑船舶' : '船舶详情'}
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
              <Form.Item name="shipNo" label="船舶编号" rules={[{ required: true, message: '请输入船舶编号' }]}>
                <Input disabled={drawerMode === 'edit'} />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="shipName" label="船名" rules={[{ required: true, message: '请输入船名' }]}>
                <Input />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="shipType" label="船舶类型" rules={[{ required: true, message: '请选择船舶类型' }]}>
                <DictionarySelect dictType="SHIP_TYPE" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="loadCapacity" label="载重量(吨)" rules={[{ required: true, message: '请输入载重量' }]}>
                <InputNumber min={0.01} precision={2} style={{ width: '100%' }} />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="homePort" label="所属港口" rules={[{ required: true, message: '请选择所属港口' }]}>
                <DictionarySelect dictType="PORT" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="status" label="状态" rules={[{ required: true, message: '请选择状态' }]}>
                <Select options={shipStatusOptions} />
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
