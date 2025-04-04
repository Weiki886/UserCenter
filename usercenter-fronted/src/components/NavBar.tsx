import { Layout, Avatar, Dropdown } from 'antd';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { UserOutlined, SettingOutlined, LogoutOutlined, LockOutlined } from '@ant-design/icons';
import { logout } from '@/services/userService';
import { useUser } from '@/contexts/UserContext';

const { Header } = Layout;

interface NavItemProps {
  title: string;
  link: string;
  active: boolean;
}

export default function NavBar({ activeItem }: { activeItem: string }) {
  const router = useRouter();
  const { currentUser } = useUser();

  const navItems = [
    { title: '首页', link: '/', active: activeItem === 'home' },
    { title: '用户管理', link: '/dashboard/users', active: activeItem === 'users' },
    { title: '关于系统', link: '/about', active: activeItem === 'about' },
    { title: '联系我们', link: '/contact', active: activeItem === 'contact' },
  ];

  // 处理用户下拉菜单链接的高亮状态
  const isActive = (item: string) => {
    if (item === 'dashboard' && activeItem === 'dashboard') return true;
    if (item === 'settings' && activeItem === 'settings') return true;
    if (item === 'change-password' && activeItem === 'change-password') return true;
    return false;
  };

  return (
    <Header style={{ 
      padding: 0, 
      background: '#fff', 
      position: 'sticky', 
      top: 0, 
      zIndex: 1,
      boxShadow: '0 1px 4px rgba(0, 0, 0, 0.1)'
    }}>
      <div style={{ maxWidth: '1400px', margin: '0 auto', display: 'flex' }}>
        <div style={{ 
          width: '180px', 
          height: '64px', 
          display: 'flex', 
          alignItems: 'center', 
          paddingLeft: '20px' 
        }}>
          <Link href="/" style={{ color: '#000', fontSize: '18px', fontWeight: 'bold' }}>
            用户中心系统
          </Link>
        </div>
        
        <nav style={{ 
          flex: 1, 
          display: 'flex', 
          overflowX: 'auto', 
          whiteSpace: 'nowrap',
          marginLeft: '-20px'
        }}>
          {navItems.map((item, index) => (
            <Link 
              key={index} 
              href={item.link}
              style={{ 
                padding: '0 30px', 
                color: '#000', 
                height: '64px', 
                lineHeight: '64px',
                backgroundColor: item.active ? '#f0f0f0' : 'transparent',
                display: item.title === '用户管理' && currentUser?.userRole !== 1 ? 'none' : 'block'
              }}
            >
              {item.title}
            </Link>
          ))}
        </nav>
        
        <div style={{ 
          width: '150px', 
          display: 'flex', 
          alignItems: 'center', 
          justifyContent: 'flex-end', 
          paddingRight: '30px' 
        }}>
          {currentUser ? (
            <Dropdown menu={{
              items: [
                {
                  key: '1',
                  icon: <UserOutlined />,
                  label: (
                    <Link 
                      href="/dashboard" 
                      style={{
                        color: isActive('dashboard') ? '#1890ff' : 'inherit'
                      }}
                    >
                      个人中心
                    </Link>
                  ),
                },
                {
                  key: '2',
                  icon: <SettingOutlined />,
                  label: (
                    <Link 
                      href="/dashboard/settings" 
                      style={{
                        color: isActive('settings') ? '#1890ff' : 'inherit'
                      }}
                    >
                      个人设置
                    </Link>
                  ),
                },
                {
                  key: '3',
                  icon: <LockOutlined />,
                  label: (
                    <Link 
                      href="/dashboard/change-password" 
                      style={{
                        color: isActive('change-password') ? '#1890ff' : 'inherit'
                      }}
                    >
                      修改密码
                    </Link>
                  ),
                },
                {
                  type: 'divider',
                },
                {
                  key: '4',
                  icon: <LogoutOutlined />,
                  label: '退出登录',
                  onClick: () => {
                    logout().then(() => {
                      router.push('/auth/login');
                    });
                  },
                },
              ],
            }}>
              <span style={{ cursor: 'pointer', color: '#000' }}>
                <Avatar 
                  size="small" 
                  icon={<UserOutlined />} 
                  src={currentUser?.avatarUrl} 
                  style={{ marginRight: 8 }} 
                />
                {currentUser?.username || '用户'}
              </span>
            </Dropdown>
          ) : (
            <Link href="/auth/login" style={{ color: '#000' }}>
              登录/注册
            </Link>
          )}
        </div>
      </div>
    </Header>
  );
} 