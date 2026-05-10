import { useState, useEffect } from 'react'
import { BrowserRouter, Routes, Route, Navigate, useNavigate, useLocation } from 'react-router'
import { ConfigProvider, App as AntApp, Modal, Form, Input, message } from 'antd'
import zhCN from 'antd/locale/zh_CN'
import AppLayout from './layouts/AppLayout'
import LoginPage from './views/LoginPage'
import DashboardPage from './views/DashboardPage'
import ShipsPage from './views/ShipsPage'
import CrewMembersPage from './views/CrewMembersPage'
import TransportOrdersPage from './views/TransportOrdersPage'
import SettlementsPage from './views/SettlementsPage'
import DictionariesPage from './views/DictionariesPage'
import UsersPage from './views/UsersPage'
import RolesPage from './views/RolesPage'
import { useAuthStore } from './stores/auth'
import { changePassword } from './api/auth'

const theme = {
  token: {
    colorPrimary: 'oklch(44% 0.095 205)',
    colorSuccess: 'oklch(54% 0.12 150)',
    colorWarning: 'oklch(68% 0.13 75)',
    colorError: 'oklch(56% 0.15 28)',
    colorInfo: 'oklch(55% 0.07 235)',
    borderRadius: 6,
    fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", "Microsoft YaHei", system-ui, sans-serif',
  },
}

function RequireAuth({ children }: { children: React.ReactNode }) {
  const token = useAuthStore((s) => s.token)
  const location = useLocation()

  if (!token) {
    return <Navigate to="/login" state={{ from: location }} replace />
  }
  return <>{children}</>
}

function AppRoutes() {
  const [passwordOpen, setPasswordOpen] = useState(false)
  const [passwordLoading, setPasswordLoading] = useState(false)
  const [passwordForm] = Form.useForm()
  const navigate = useNavigate()
  const location = useLocation()
  const token = useAuthStore((s) => s.token)
  const refreshUser = useAuthStore((s) => s.refreshUser)

  useEffect(() => {
    if (token) {
      refreshUser().catch(() => {
        navigate('/login', { replace: true })
      })
    }
  }, [])

  const handleChangePassword = async (values: { oldPassword: string; newPassword: string; confirmPassword: string }) => {
    if (values.newPassword !== values.confirmPassword) {
      message.error('两次输入的新密码不一致')
      return
    }
    if (values.oldPassword === values.newPassword) {
      message.error('新密码不能与旧密码相同')
      return
    }
    setPasswordLoading(true)
    try {
      await changePassword(values.oldPassword, values.newPassword)
      message.success('密码修改成功')
      setPasswordOpen(false)
      passwordForm.resetFields()
    } catch {
      // error handled by interceptor
    } finally {
      setPasswordLoading(false)
    }
  }

  return (
    <>
      <Routes>
        <Route path="/login" element={
          token ? <Navigate to="/dashboard" replace /> : <LoginPage />
        } />
        <Route path="/" element={
          <RequireAuth>
            <AppLayout onChangePassword={() => setPasswordOpen(true)} />
          </RequireAuth>
        }>
          <Route index element={<Navigate to="/dashboard" replace />} />
          <Route path="dashboard" element={<DashboardPage />} />
          <Route path="ships" element={<ShipsPage />} />
          <Route path="crew-members" element={<CrewMembersPage />} />
          <Route path="transport-orders" element={<TransportOrdersPage />} />
          <Route path="settlements" element={<SettlementsPage />} />
          <Route path="dictionaries" element={<DictionariesPage />} />
          <Route path="users" element={<UsersPage />} />
          <Route path="roles" element={<RolesPage />} />
        </Route>
        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
      <Modal
        title="修改密码"
        open={passwordOpen}
        onCancel={() => { setPasswordOpen(false); passwordForm.resetFields() }}
        onOk={() => passwordForm.submit()}
        confirmLoading={passwordLoading}
      >
        <Form form={passwordForm} onFinish={handleChangePassword} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item name="oldPassword" label="旧密码" rules={[{ required: true, message: '请输入旧密码' }]}>
            <Input.Password />
          </Form.Item>
          <Form.Item name="newPassword" label="新密码" rules={[
            { required: true, message: '请输入新密码' },
            { min: 6, max: 30, message: '密码长度 6-30 位' },
          ]}>
            <Input.Password />
          </Form.Item>
          <Form.Item name="confirmPassword" label="确认新密码" rules={[{ required: true, message: '请确认新密码' }]}>
            <Input.Password />
          </Form.Item>
        </Form>
      </Modal>
    </>
  )
}

export default function App() {
  return (
    <ConfigProvider locale={zhCN} theme={theme}>
      <AntApp>
        <BrowserRouter>
          <AppRoutes />
        </BrowserRouter>
      </AntApp>
    </ConfigProvider>
  )
}
