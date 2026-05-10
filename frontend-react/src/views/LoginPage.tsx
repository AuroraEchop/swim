import { useState } from 'react'
import { useNavigate } from 'react-router'
import { Form, Input, Button, Typography, message } from 'antd'
import { UserOutlined, LockOutlined, ContainerOutlined } from '@ant-design/icons'
import { useAuthStore } from '../stores/auth'

const { Title, Text } = Typography

export default function LoginPage() {
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()
  const login = useAuthStore((s) => s.login)

  const handleSubmit = async (values: { username: string; password: string }) => {
    setLoading(true)
    try {
      await login(values.username, values.password)
      navigate('/dashboard')
    } catch (err: any) {
      message.error(err?.message || '登录失败')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{
      minHeight: '100vh',
      display: 'flex',
      background: 'var(--color-bg-page)',
    }}>
      <div style={{
        flex: 1,
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        padding: '0 80px',
        background: 'var(--color-bg-sidebar)',
      }}>
        <ContainerOutlined style={{ fontSize: 48, color: 'var(--color-primary)', marginBottom: 24 }} />
        <Title level={2} style={{ marginBottom: 8 }}>航运公司管理系统</Title>
        <Text type="secondary" style={{ fontSize: 16 }}>统一管理船舶、船员、运输任务与结算流程</Text>
        <div style={{ marginTop: 48 }}>
          <div style={{ display: 'flex', gap: 32 }}>
            <div>
              <Text strong>船舶管理</Text>
              <br />
              <Text type="secondary">维护船舶档案与状态</Text>
            </div>
            <div>
              <Text strong>运输任务</Text>
              <br />
              <Text type="secondary">创建与跟踪运输订单</Text>
            </div>
            <div>
              <Text strong>财务结算</Text>
              <br />
              <Text type="secondary">运费结算与收款管理</Text>
            </div>
          </div>
        </div>
      </div>
      <div style={{
        flex: 1,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
      }}>
        <div style={{ width: 360 }}>
          <Title level={3} style={{ marginBottom: 32 }}>登录</Title>
          <Form
            onFinish={handleSubmit}
            autoComplete="off"
            size="large"
            layout="vertical"
          >
            <Form.Item
              name="username"
              rules={[{ required: true, message: '请输入用户名' }]}
            >
              <Input prefix={<UserOutlined />} placeholder="用户名" />
            </Form.Item>
            <Form.Item
              name="password"
              rules={[
                { required: true, message: '请输入密码' },
                { min: 6, max: 30, message: '密码长度 6-30 位' },
              ]}
            >
              <Input.Password prefix={<LockOutlined />} placeholder="密码" />
            </Form.Item>
            <Form.Item>
              <Button type="primary" htmlType="submit" loading={loading} block>
                登录
              </Button>
            </Form.Item>
          </Form>
        </div>
      </div>
    </div>
  )
}
