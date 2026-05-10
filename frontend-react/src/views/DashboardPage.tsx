import { useEffect, useState } from 'react'
import { Card, Col, Row, Statistic, Table, Typography } from 'antd'
import { ContainerOutlined, TeamOutlined, CarOutlined, DollarOutlined, ClockCircleOutlined } from '@ant-design/icons'
import { getDashboardSummary, getRecentTransportOrders } from '../api/dashboard'
import type { DashboardSummary, RecentTransportOrder } from '../api/dashboard'
import StatusTag from '../components/StatusTag'

const { Title, Text } = Typography

export default function DashboardPage() {
  const [summary, setSummary] = useState<DashboardSummary | null>(null)
  const [orders, setOrders] = useState<RecentTransportOrder[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    setLoading(true)
    Promise.all([getDashboardSummary(), getRecentTransportOrders(5)])
      .then(([s, o]) => { setSummary(s); setOrders(o) })
      .finally(() => setLoading(false))
  }, [])

  const collectionRate = summary && summary.totalReceivableAmount > 0
    ? ((summary.totalReceivedAmount / summary.totalReceivableAmount) * 100).toFixed(1)
    : '0.0'

  const columns = [
    { title: '任务编号', dataIndex: 'orderNo', key: 'orderNo' },
    { title: '货物', dataIndex: 'cargoName', key: 'cargoName' },
    { title: '船舶', dataIndex: 'shipName', key: 'shipName' },
    {
      title: '航线', key: 'route',
      render: (_: any, r: RecentTransportOrder) => `${r.originPort} → ${r.destinationPort}`,
    },
    {
      title: '状态', dataIndex: 'status', key: 'status',
      render: (status: string) => <StatusTag status={status} />,
    },
    { title: '预计出发', dataIndex: 'plannedDepartureTime', key: 'plannedDepartureTime' },
  ]

  return (
    <div>
      <Title level={4} style={{ marginBottom: 24 }}>业务工作台</Title>
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={4}>
          <Card loading={loading}>
            <Statistic title="船舶数量" value={summary?.shipCount ?? 0} prefix={<ContainerOutlined />} />
          </Card>
        </Col>
        <Col span={4}>
          <Card loading={loading}>
            <Statistic title="船员数量" value={summary?.crewCount ?? 0} prefix={<TeamOutlined />} />
          </Card>
        </Col>
        <Col span={4}>
          <Card loading={loading}>
            <Statistic title="待出发任务" value={summary?.pendingTransportCount ?? 0} prefix={<ClockCircleOutlined />} valueStyle={{ color: '#d48806' }} />
          </Card>
        </Col>
        <Col span={4}>
          <Card loading={loading}>
            <Statistic title="运输中任务" value={summary?.inTransitCount ?? 0} prefix={<CarOutlined />} valueStyle={{ color: 'var(--color-primary)' }} />
          </Card>
        </Col>
        <Col span={4}>
          <Card loading={loading}>
            <Statistic title="未结清记录" value={summary?.unsettledCount ?? 0} prefix={<DollarOutlined />} valueStyle={{ color: '#d48806' }} />
          </Card>
        </Col>
      </Row>
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={8}>
          <Card loading={loading}>
            <Statistic title="应收总金额" value={summary?.totalReceivableAmount ?? 0} precision={2} prefix="¥" />
          </Card>
        </Col>
        <Col span={8}>
          <Card loading={loading}>
            <Statistic title="实收总金额" value={summary?.totalReceivedAmount ?? 0} precision={2} prefix="¥" />
          </Card>
        </Col>
        <Col span={8}>
          <Card loading={loading}>
            <Statistic title="回款比例" value={collectionRate} suffix="%" />
          </Card>
        </Col>
      </Row>
      <Card title="近期运输任务">
        <Table
          columns={columns}
          dataSource={orders}
          rowKey="id"
          loading={loading}
          pagination={false}
          size="middle"
        />
      </Card>
    </div>
  )
}
