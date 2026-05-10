import { useEffect, useState, useCallback } from 'react'
import { Table, Button, Card, Space, Input, Form, Drawer, Select, InputNumber, DatePicker, Popconfirm, message, Row, Col, Descriptions } from 'antd'
import { PlusOutlined, SearchOutlined, ReloadOutlined, EyeOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
import dayjs from 'dayjs'
import { getTransportOrders, getTransportOrder, createTransportOrder, updateTransportOrder, deleteTransportOrder, updateTransportOrderStatus } from '../api/transportOrders'
import type { TransportOrder, TransportOrderQuery, TransportStatus, CreateTransportOrderRequest } from '../api/transportOrders'
import StatusTag from '../components/StatusTag'
import ShipSelect from '../components/ShipSelect'
import DictionarySelect from '../components/DictionarySelect'
import { transportStatusOptions } from '../constants/status'
import { useAuthStore } from '../stores/auth'

const { RangePicker } = DatePicker
type DrawerMode = 'create' | 'edit' | 'view'

const dateTimeFormat = 'YYYY-MM-DD HH:mm:ss'

export default function TransportOrdersPage() {
  const [data, setData] = useState<TransportOrder[]>([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)
  const [query, setQuery] = useState<TransportOrderQuery>({ page: 1, pageSize: 10 })
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [drawerMode, setDrawerMode] = useState<DrawerMode>('create')
  const [currentId, setCurrentId] = useState<number | null>(null)
  const [detail, setDetail] = useState<TransportOrder | null>(null)
  const [form] = Form.useForm()
  const canManage = useAuthStore((s) => s.user?.roleCode === 'ADMIN')

  const loadData = useCallback(async () => {
    setLoading(true)
    try {
      const res = await getTransportOrders(query)
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
    setDetail(null)
    form.resetFields()
    setDrawerOpen(true)
  }

  const openDetail = async (id: number, mode: 'view' | 'edit') => {
    setDrawerMode(mode)
    setCurrentId(id)
    const order = await getTransportOrder(id)
    setDetail(order)
    form.setFieldsValue({
      ...order,
      plannedDepartureTime: order.plannedDepartureTime ? dayjs(order.plannedDepartureTime) : null,
      plannedArrivalTime: order.plannedArrivalTime ? dayjs(order.plannedArrivalTime) : null,
    })
    setDrawerOpen(true)
  }

  const handleSubmit = async () => {
    const values = await form.validateFields()
    const payload: CreateTransportOrderRequest = {
      ...values,
      plannedDepartureTime: values.plannedDepartureTime?.format(dateTimeFormat),
      plannedArrivalTime: values.plannedArrivalTime?.format(dateTimeFormat),
    }
    if (drawerMode === 'create') {
      await createTransportOrder(payload)
      message.success('创建成功')
    } else {
      await updateTransportOrder(currentId!, payload)
      message.success('修改成功')
    }
    setDrawerOpen(false)
    loadData()
  }

  const handleDelete = async (id: number) => {
    await deleteTransportOrder(id)
    message.success('删除成功')
    loadData()
  }

  const handleStatusChange = async (record: TransportOrder, status: TransportStatus) => {
    const now = dayjs().format(dateTimeFormat)
    const payload: any = { status }
    if (status === 'IN_TRANSIT') payload.actualDepartureTime = now
    if (status === 'ARRIVED') payload.actualArrivalTime = now
    await updateTransportOrderStatus(record.id, payload)
    message.success('状态修改成功')
    loadData()
  }

  const getAvailableActions = (record: TransportOrder) => {
    const actions: { label: string; status: TransportStatus }[] = []
    if (record.status === 'PENDING') {
      actions.push({ label: '开始运输', status: 'IN_TRANSIT' })
      actions.push({ label: '取消任务', status: 'CANCELLED' })
    } else if (record.status === 'IN_TRANSIT') {
      actions.push({ label: '标记到达', status: 'ARRIVED' })
    }
    return actions
  }

  const columns = [
    { title: '任务编号', dataIndex: 'orderNo', key: 'orderNo', width: 180 },
    { title: '货物名称', dataIndex: 'cargoName', key: 'cargoName' },
    { title: '货物类型', dataIndex: 'cargoType', key: 'cargoType' },
    { title: '重量(吨)', dataIndex: 'cargoWeight', key: 'cargoWeight', align: 'right' as const, render: (v: number) => v?.toLocaleString() },
    { title: '航线', key: 'route', render: (_: any, r: TransportOrder) => `${r.originPort} → ${r.destinationPort}` },
    { title: '承运船舶', dataIndex: 'shipName', key: 'shipName' },
    { title: '客户', dataIndex: 'customerName', key: 'customerName' },
    { title: '预计出发', dataIndex: 'plannedDepartureTime', key: 'plannedDepartureTime', width: 170 },
    { title: '预计到达', dataIndex: 'plannedArrivalTime', key: 'plannedArrivalTime', width: 170 },
    { title: '运输状态', dataIndex: 'status', key: 'status', render: (s: string) => <StatusTag status={s} /> },
    { title: '结算状态', dataIndex: 'settlementStatus', key: 'settlementStatus', render: (s: string) => <StatusTag status={s} /> },
    {
      title: '操作', key: 'action', width: 240, fixed: 'right' as const,
      render: (_: any, record: TransportOrder) => (
        <Space size="small">
          <Button type="link" size="small" icon={<EyeOutlined />} onClick={() => openDetail(record.id, 'view')}>查看</Button>
          {canManage && record.status === 'PENDING' && (
            <Button type="link" size="small" icon={<EditOutlined />} onClick={() => openDetail(record.id, 'edit')}>编辑</Button>
          )}
          {canManage && getAvailableActions(record).map((a) => (
            <Popconfirm key={a.status} title={`确认${a.label}？`} onConfirm={() => handleStatusChange(record, a.status)}>
              <Button type="link" size="small">{a.label}</Button>
            </Popconfirm>
          ))}
          {canManage && (record.status === 'PENDING' || record.status === 'CANCELLED') && (
            <Popconfirm title="确认删除此任务？" onConfirm={() => handleDelete(record.id)}>
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
          <Col span={4}>
            <Input
              placeholder="任务编号/货物/客户"
              value={query.keyword}
              onChange={(e) => setQuery((q) => ({ ...q, keyword: e.target.value || undefined, page: 1 }))}
              allowClear
            />
          </Col>
          <Col span={3}>
            <ShipSelect value={query.shipId} onChange={(v) => setQuery((q) => ({ ...q, shipId: v, page: 1 }))} placeholder="船舶" />
          </Col>
          <Col span={3}>
            <DictionarySelect dictType="PORT" value={query.originPort} onChange={(v) => setQuery((q) => ({ ...q, originPort: v, page: 1 }))} placeholder="起运港" />
          </Col>
          <Col span={3}>
            <DictionarySelect dictType="PORT" value={query.destinationPort} onChange={(v) => setQuery((q) => ({ ...q, destinationPort: v, page: 1 }))} placeholder="目的港" />
          </Col>
          <Col span={3}>
            <Select
              placeholder="运输状态"
              value={query.status}
              onChange={(v) => setQuery((q) => ({ ...q, status: v, page: 1 }))}
              options={transportStatusOptions}
              allowClear
              style={{ width: '100%' }}
            />
          </Col>
          <Col span={4}>
            <RangePicker
              style={{ width: '100%' }}
              onChange={(dates) => {
                setQuery((q) => ({
                  ...q,
                  startDate: dates?.[0]?.format('YYYY-MM-DD'),
                  endDate: dates?.[1]?.format('YYYY-MM-DD'),
                  page: 1,
                }))
              }}
            />
          </Col>
          <Col span={4} style={{ textAlign: 'right' }}>
            <Space>
              <Button icon={<SearchOutlined />} onClick={() => loadData()}>查询</Button>
              <Button icon={<ReloadOutlined />} onClick={() => { setQuery({ page: 1, pageSize: 10 }) }}>重置</Button>
              {canManage && <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>新增任务</Button>}
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
          scroll={{ x: 1600 }}
          size="middle"
        />
      </Card>
      <Drawer
        title={drawerMode === 'create' ? '新增运输任务' : drawerMode === 'edit' ? '编辑运输任务' : '运输任务详情'}
        width={640}
        open={drawerOpen}
        onClose={() => setDrawerOpen(false)}
        extra={drawerMode !== 'view' && (
          <Button type="primary" onClick={handleSubmit}>保存</Button>
        )}
      >
        {drawerMode === 'view' && detail ? (
          <Descriptions column={2} bordered size="small">
            <Descriptions.Item label="任务编号">{detail.orderNo}</Descriptions.Item>
            <Descriptions.Item label="货物名称">{detail.cargoName}</Descriptions.Item>
            <Descriptions.Item label="货物类型">{detail.cargoType}</Descriptions.Item>
            <Descriptions.Item label="货物重量">{detail.cargoWeight} 吨</Descriptions.Item>
            <Descriptions.Item label="起运港">{detail.originPort}</Descriptions.Item>
            <Descriptions.Item label="目的港">{detail.destinationPort}</Descriptions.Item>
            <Descriptions.Item label="承运船舶">{detail.shipName}</Descriptions.Item>
            <Descriptions.Item label="客户">{detail.customerName}</Descriptions.Item>
            <Descriptions.Item label="客户电话">{detail.customerPhone || '-'}</Descriptions.Item>
            <Descriptions.Item label="运输状态"><StatusTag status={detail.status} /></Descriptions.Item>
            <Descriptions.Item label="预计出发">{detail.plannedDepartureTime}</Descriptions.Item>
            <Descriptions.Item label="预计到达">{detail.plannedArrivalTime}</Descriptions.Item>
            <Descriptions.Item label="实际出发">{detail.actualDepartureTime || '-'}</Descriptions.Item>
            <Descriptions.Item label="实际到达">{detail.actualArrivalTime || '-'}</Descriptions.Item>
            <Descriptions.Item label="结算状态"><StatusTag status={detail.settlementStatus} /></Descriptions.Item>
            <Descriptions.Item label="备注" span={2}>{detail.remark || '-'}</Descriptions.Item>
          </Descriptions>
        ) : (
          <Form form={form} layout="vertical">
            <Row gutter={16}>
              <Col span={12}>
                <Form.Item name="cargoName" label="货物名称" rules={[{ required: true, message: '请输入货物名称' }]}>
                  <Input />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item name="cargoType" label="货物类型" rules={[{ required: true, message: '请选择货物类型' }]}>
                  <DictionarySelect dictType="CARGO_TYPE" />
                </Form.Item>
              </Col>
            </Row>
            <Row gutter={16}>
              <Col span={12}>
                <Form.Item name="cargoWeight" label="货物重量(吨)" rules={[{ required: true, message: '请输入货物重量' }]}>
                  <InputNumber min={0.01} precision={2} style={{ width: '100%' }} />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item name="shipId" label="承运船舶" rules={[{ required: true, message: '请选择承运船舶' }]}>
                  <ShipSelect />
                </Form.Item>
              </Col>
            </Row>
            <Row gutter={16}>
              <Col span={12}>
                <Form.Item name="originPort" label="起运港" rules={[{ required: true, message: '请选择起运港' }]}>
                  <DictionarySelect dictType="PORT" />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item name="destinationPort" label="目的港" rules={[{ required: true, message: '请选择目的港' }]}>
                  <DictionarySelect dictType="PORT" />
                </Form.Item>
              </Col>
            </Row>
            <Row gutter={16}>
              <Col span={12}>
                <Form.Item name="customerName" label="客户名称" rules={[{ required: true, message: '请输入客户名称' }]}>
                  <Input />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item name="customerPhone" label="客户电话">
                  <Input />
                </Form.Item>
              </Col>
            </Row>
            <Row gutter={16}>
              <Col span={12}>
                <Form.Item name="plannedDepartureTime" label="预计出发时间" rules={[{ required: true, message: '请选择预计出发时间' }]}>
                  <DatePicker showTime format={dateTimeFormat} style={{ width: '100%' }} />
                </Form.Item>
              </Col>
              <Col span={12}>
                <Form.Item name="plannedArrivalTime" label="预计到达时间" rules={[{ required: true, message: '请选择预计到达时间' }]}>
                  <DatePicker showTime format={dateTimeFormat} style={{ width: '100%' }} />
                </Form.Item>
              </Col>
            </Row>
            <Form.Item name="remark" label="备注">
              <Input.TextArea rows={3} />
            </Form.Item>
          </Form>
        )}
      </Drawer>
    </div>
  )
}
