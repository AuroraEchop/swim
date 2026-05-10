import { useState } from 'react'
import { Outlet, useNavigate, useLocation } from 'react-router'
import { Layout, Menu, Dropdown, Button, Space, Typography } from 'antd'
import {
  DashboardOutlined,
  ContainerOutlined,
  TeamOutlined,
  CarOutlined,
  DollarOutlined,
  BookOutlined,
  UserOutlined,
  SafetyOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  LogoutOutlined,
  KeyOutlined,
} from '@ant-design/icons'
import { useAuthStore } from '../stores/auth'

const { Sider, Header, Content } = Layout
const { Text } = Typography

const menuItems = [
  {
    key: 'group-business',
    label: '业务工作台',
    type: 'group' as const,
    children: [
      { key: '/dashboard', icon: <DashboardOutlined />, label: '业务工作台' },
      { key: '/ships', icon: <ContainerOutlined />, label: '船舶管理' },
      { key: '/crew-members', icon: <TeamOutlined />, label: '船员管理' },
      { key: '/transport-orders', icon: <CarOutlined />, label: '运输任务' },
      { key: '/settlements', icon: <DollarOutlined />, label: '财务结算' },
    ],
  },
  {
    key: 'group-system',
    label: '系统配置',
    type: 'group' as const,
    children: [
      { key: '/dictionaries', icon: <BookOutlined />, label: '基础字典' },
      { key: '/users', icon: <UserOutlined />, label: '用户管理' },
      { key: '/roles', icon: <SafetyOutlined />, label: '角色管理' },
    ],
  },
]

const titleMap: Record<string, string> = {
  '/dashboard': '业务工作台',
  '/ships': '船舶管理',
  '/crew-members': '船员管理',
  '/transport-orders': '运输任务',
  '/settlements': '财务结算',
  '/dictionaries': '基础字典',
  '/users': '用户管理',
  '/roles': '角色管理',
}

interface AppLayoutProps {
  onChangePassword: () => void
}

export default function AppLayout({ onChangePassword }: AppLayoutProps) {
  const [collapsed, setCollapsed] = useState(false)
  const navigate = useNavigate()
  const location = useLocation()
  const user = useAuthStore((s) => s.user)
  const logout = useAuthStore((s) => s.logout)

  const currentPath = location.pathname === '/' ? '/dashboard' : location.pathname
  const pageTitle = titleMap[currentPath] || '航运公司管理系统'

  const handleLogout = async () => {
    await logout()
    navigate('/login')
  }

  const userMenuItems = [
    { key: 'password', icon: <KeyOutlined />, label: '修改密码', onClick: onChangePassword },
    { key: 'logout', icon: <LogoutOutlined />, label: '退出登录', onClick: handleLogout },
  ]

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider
        collapsible
        collapsed={collapsed}
        onCollapse={setCollapsed}
        width={224}
        collapsedWidth={64}
        theme="light"
        style={{ borderRight: '1px solid var(--color-border-light)' }}
      >
        <div style={{ height: 56, display: 'flex', alignItems: 'center', justifyContent: 'center', borderBottom: '1px solid var(--color-border-light)' }}>
          {collapsed ? (
            <ContainerOutlined style={{ fontSize: 22, color: 'var(--color-primary)' }} />
          ) : (
            <Text strong style={{ fontSize: 16, color: 'var(--color-primary)' }}>航运管理系统</Text>
          )}
        </div>
        <Menu
          mode="inline"
          selectedKeys={[currentPath]}
          items={menuItems}
          onClick={({ key }) => navigate(key)}
          style={{ borderRight: 0 }}
        />
      </Sider>
      <Layout>
        <Header style={{
          height: 56,
          lineHeight: '56px',
          padding: '0 24px',
          background: 'var(--color-bg-surface)',
          borderBottom: '1px solid var(--color-border-light)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
        }}>
          <Space>
            <Button
              type="text"
              icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
              onClick={() => setCollapsed(!collapsed)}
            />
            <Text strong style={{ fontSize: 16 }}>{pageTitle}</Text>
          </Space>
          <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
            <Button type="text" style={{ height: 40 }}>
              <Space>
                <UserOutlined />
                <span>{user?.realName || user?.username}</span>
              </Space>
            </Button>
          </Dropdown>
        </Header>
        <Content style={{ padding: 20, background: 'var(--color-bg-page)' }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  )
}
