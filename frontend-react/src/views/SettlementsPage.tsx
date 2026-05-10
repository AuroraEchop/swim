import { useEffect, useState, useCallback } from 'react'
import { Table, Button, Card, Space, Input, Form, Drawer, Select, InputNumber, DatePicker, Popconfirm, message, Row, Col, Modal } from 'antd'
import { PlusOutlined, SearchOutlined, ReloadOutlined, EyeOutlined, EditOutlined, DeleteOutlined, DollarOutlined } from '@ant-design/icons'
import dayjs from 'dayjs'
import { getSettlements, getSettlement, createSettlement, updateSettlement, deleteSettlement, updatePayment } from '../api/settlements'
import type { Settlement, SettlementQuery, SettlementStatus, CreateSettlementRequest } from '../api/settlements'
import StatusTag from '../components/StatusTag'
import TransportOrderSelect from '../components/TransportOrderSelect'
import { settlementStatusOptions } from '../constants/status'
import { useAuthStore } from '../stores/auth'

const { RangePicker } = DatePicker
type DrawerMode = 'create' | 'edit' | 'view'

export default function SettlementsPage() {
  const [data, setData] = useState<Settlement[]>([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(false)
  const [query, setQuery] = useState<SettlementQuery>({ page: 1, pageSize: 10 })
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [drawerMode, setDrawerMode] = useState<DrawerMode>('create')
  const [currentId, setCurrentId] = useState<number | null>(null)
  const [form] = Form.useForm()
  const [paymentOpen, setPaymentOpen] = useState(false)
  const [paymentRecord, setPaymentRecord] = useState<Settlement | null>(null)
  const [paymentForm] = Form.useForm()
  const canManage = useAuthStore((s) => s.user?.roleCode === 'ADMIN')

  const loadData = useCallback(async () => {
    setLoading(true)
    try {
      const res = await getSettlements(query)
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
    form.setFieldsValue({ additionalFee: 0, discountAmount: 0, receivedAmount: 0 })
    setDrawerOpen(true)
  }

  const openDetail = async (id: number, mode: 'view' | 'edit') => {
    setDrawerMode(mode)
    setCurrentId(id)
    const settlement = await getSettlement(id)
    form.setFieldsValue(settlement)
    setDrawerOpen(true)
  }

  const handleSubmit = async () => {
    const values = await form.validateFields()
    if (drawerMode === 'create') {
      await createSettlement(values as CreateSettlementRequest)
      message.success('创建成功')
    } else {
      await updateSettlement(currentId!, values)
      message.success('修改成功')
    }
    setDrawerOpen(false)
    loadData()
  }

  const handleDelete = async (id: number) => {
    await deleteSettlement(id)
    message.success('删除成功')
    loadData()
  }

  const openPayment = (record: Settlement) => {
    setPaymentRecord(record)
    paymentForm.setFieldsValue({ receivedAmount: record.receivedAmount })
    setPaymentOpen(true)
  }

  const handlePayment = async () => {
    const values = await paymentForm.validateFields()
    await updatePayment(paymentRecord!.id, {
      receivedAmount: values.receivedAmount,
      paymentTime: dayjs().format('YYYY-MM-DD HH:mm:ss'),
    })
    message.success('收款成功')
    setPaymentOpen(false)
    loadData()
  }

  const freightAmount = Form.useWatch('freightAmount', form) ?? 0
  const additionalFee = Form.useWatch('additionalFee', form) ?? 0
  const discountAmount = Form.useWatch('discountAmount', form) ?? 0
  const receivableAmount = freightAmount + additionalFee - discountAmount

  const columns = [
    { title: '结算编号', dataIndex: 'settlementNo', key: 'settlementNo', width: 180 },
    { title: '运输任务', dataIndex: 'transportOrderNo', key: 'transportOrderNo', width: 180 },
    { title: '客户', dataIndex: 'customerName', key: 'customerName' },
    { title: '运费', dataIndex: 'freightAmount', key: 'freightAmount', align: 'right' as const, render: (v: number) => `¥${v?.toLocaleString(undefined, { minimumFractionDigits: 2 })}` },
    { title: '附加费', dataIndex: 'additionalFee', key: 'additionalFee', align: 'right' as const, render: (v: number) => `¥${v?.toLocaleString(undefined, { minimumFractionDigits: 2 })}` },
    { title: '优惠', dataIndex: 'discountAmount', key: 'discountAmount', align: 'right' as const, render: (v: number) => `¥${v?.toLocaleString(undefined, { minimumFractionDigits: 2 })}` },
    { title: '应收', dataIndex: 'receivableAmount', key: 'receivableAmount', align: 'right' as const, render: (v: number) => `¥${v?.toLocaleString(undefined, { minimumFractionDigits: 2 })}` },
    { title: '实收', dataIndex: 'receivedAmount', key: 'receivedAmount', align: 'right' as const, render: (v: number) => `¥${v?.toLocaleString(undefined, { minimumFractionDigits: 2 })}` },
    { title: '状态', dataIndex: 'status', key: 'status', render: (s: string) => <StatusTag status={s} /> },
    { title: '结算时间', dataIndex: 'settledAt', key: 'settledAt', width: 170, render: (v: string) => v || '-' },
    {
      title: '操作', key: 'action', width: 220, fixed: 'right' as const,
      render: (_: any, record: Settlement) => (
        <Space size="small">
          <Button type="link" size="small" icon={<EyeOutlined />} onClick={() => openDetail(record.id, 'view')}>查看</Button>
          {canManage && (
            <>
              <Button type="link" size="small" icon={<EditOutlined />} onClick={() => openDetail(record.id, 'edit')}>编辑</Button>
              {record.status !== 'SETTLED' && (
                <Button type="link" size="small" icon={<DollarOutlined />} onClick={() => openPayment(record)}>收款</Button>
              )}
              <Popconfirm title="确认删除此结算记录？" onConfirm={() => handleDelete(record.id)}>
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
              placeholder="结算编号/任务编号/客户"
              value={query.keyword}
              onChange={(e) => setQuery((q) => ({ ...q, keyword: e.target.value || undefined, page: 1 }))}
              allowClear
            />
          </Col>
          <Col span={4}>
            <Input
              placeholder="客户名称"
              value={query.customerName}
              onChange={(e) => setQuery((q) => ({ ...q, customerName: e.target.value || undefined, page: 1 }))}
              allowClear
            />
          </Col>
          <Col span={3}>
            <Select
              placeholder="结算状态"
              value={query.status}
              onChange={(v) => setQuery((q) => ({ ...q, status: v, page: 1 }))}
              options={settlementStatusOptions}
              allowClear
              style={{ width: '100%' }}
            />
          </Col>
          <Col span={5}>
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
          <Col span={7} style={{ textAlign: 'right' }}>
            <Space>
              <Button icon={<SearchOutlined />} onClick={() => loadData()}>查询</Button>
              <Button icon={<ReloadOutlined />} onClick={() => { setQuery({ page: 1, pageSize: 10 }) }}>重置</Button>
              {canManage && <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>新增结算</Button>}
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
        title={drawerMode === 'create' ? '新增结算' : drawerMode === 'edit' ? '编辑结算' : '结算详情'}
        width={600}
        open={drawerOpen}
        onClose={() => setDrawerOpen(false)}
        extra={drawerMode !== 'view' && (
          <Button type="primary" onClick={handleSubmit}>保存</Button>
        )}
      >
        <Form form={form} layout="vertical" disabled={drawerMode === 'view'}>
          {drawerMode === 'create' && (
            <Form.Item name="transportOrderId" label="运输任务" rules={[{ required: true, message: '请选择运输任务' }]}>
              <TransportOrderSelect />
            </Form.Item>
          )}
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="freightAmount" label="运费金额" rules={[{ required: true, message: '请输入运费金额' }]}>
                <InputNumber min={0} precision={2} style={{ width: '100%' }} prefix="¥" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="additionalFee" label="附加费用">
                <InputNumber min={0} precision={2} style={{ width: '100%' }} prefix="¥" />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="discountAmount" label="优惠金额">
                <InputNumber min={0} precision={2} style={{ width: '100%' }} prefix="¥" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item label="应收金额">
                <InputNumber value={receivableAmount} precision={2} style={{ width: '100%' }} prefix="¥" disabled />
              </Form.Item>
            </Col>
          </Row>
          <Form.Item name="receivedAmount" label="实收金额">
            <InputNumber min={0} precision={2} style={{ width: '100%' }} prefix="¥" />
          </Form.Item>
          <Form.Item name="remark" label="备注">
            <Input.TextArea rows={3} />
          </Form.Item>
        </Form>
      </Drawer>
      <Modal
        title="收款登记"
        open={paymentOpen}
        onCancel={() => setPaymentOpen(false)}
        onOk={handlePayment}
      >
        <Form form={paymentForm} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item label="应收金额">
            <InputNumber value={paymentRecord?.receivableAmount} precision={2} style={{ width: '100%' }} prefix="¥" disabled />
          </Form.Item>
          <Form.Item name="receivedAmount" label="实收金额" rules={[{ required: true, message: '请输入实收金额' }]}>
            <InputNumber min={0} max={paymentRecord?.receivableAmount} precision={2} style={{ width: '100%' }} prefix="¥" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}
