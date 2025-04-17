import { Layout, Avatar, Dropdown, Skeleton, message } from 'antd';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { UserOutlined, SettingOutlined, LogoutOutlined, LockOutlined, DeleteOutlined } from '@ant-design/icons';
import { logout } from '@/services/userService';
import { useUser } from '@/contexts/UserContext';
import { useEffect, useState } from 'react';

const { Header } = Layout;

interface NavItemProps {
  title: string;
  link: string;
  active: boolean;
}

export default function NavBar({ activeItem }: { activeItem: string }) {
  const router = useRouter();
  const { currentUser, clearUserInfo, loading, refreshUserInfo, forceUpdate } = useUser();
  const [localUser, setLocalUser] = useState<any>(null);
  const [isClient, setIsClient] = useState(false);

  // 客户端渲染检测
  useEffect(() => {
    setIsClient(true);
  }, []);

  // 添加状态同步逻辑
  useEffect(() => {
    if (!isClient) return; // 只在客户端执行

    // 如果Context中有用户，使用Context中的用户
    if (currentUser) {
      setLocalUser(currentUser);
    } 
    // 否则检查localStorage - 在首次启动时不检查localStorage以确保显示未登录状态
    else {
      const isFirstStart = sessionStorage.getItem('appStarted') !== 'true';
      
      if (!isFirstStart) {
        try {
          const storedUser = localStorage.getItem('userInfo');
          if (storedUser) {
            const parsedUser = JSON.parse(storedUser);
            setLocalUser(parsedUser);
            // 触发Context更新但不显示加载状态
            refreshUserInfo(true);
            // 强制更新UI
            forceUpdate();
          }
        } catch (error) {
          console.error('解析存储的用户信息失败:', error);
        }
      } else {
        // 首次启动，确保清除任何现有的用户数据
        setLocalUser(null);
        localStorage.removeItem('userInfo');
        localStorage.removeItem('userToken');
        sessionStorage.setItem('appStarted', 'true');
      }
    }
  }, [currentUser, refreshUserInfo, forceUpdate, isClient]);

  // 添加调试日志，帮助排查状态问题
  useEffect(() => {
    if (!isClient) return; // 只在客户端执行
    
    console.log('NavBar状态更新:', { 
      isLoggedIn: !!currentUser, 
      localUserExists: !!localUser,
      loading,
      username: currentUser?.username || localUser?.username
    });
  }, [currentUser, localUser, loading, isClient]);

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
    if (item === 'delete-account' && activeItem === 'delete-account') return true;
    return false;
  };

  // 处理退出登录
  const handleLogout = async () => {
    try {
      // 调用登出API
      await logout();
      
      // 显示退出成功提示
      message.success('退出登录成功');
      
      // 清除用户信息
      setLocalUser(null);
      clearUserInfo();
      
      // 强制更新UI
      forceUpdate();
      
      // 使用Next.js路由器导航到首页
      if (activeItem !== 'home') {
        router.push('/');
      }
    } catch (error) {
      console.error('退出登录失败:', error);
      // 显示退出失败提示
      message.error('退出登录失败，请重试');
      
      // 即使API调用失败，也确保状态被清除
      setLocalUser(null);
      clearUserInfo();
      
      // 强制更新UI
      forceUpdate();
      
      if (activeItem !== 'home') {
        router.push('/');
      }
    }
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
                display: item.title === '用户管理' && (loading || !currentUser || currentUser?.userRole !== 1) ? 'none' : 'block'
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
          {loading ? (
            <Skeleton.Avatar active size="small" style={{ marginRight: 8 }} />
          ) : (currentUser || localUser) ? (
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
                  key: '4',
                  icon: <DeleteOutlined />,
                  label: (
                    <Link 
                      href="/dashboard/delete-account" 
                      style={{
                        color: isActive('delete-account') ? '#1890ff' : 'inherit'
                      }}
                    >
                      注销账号
                    </Link>
                  ),
                },
                {
                  type: 'divider',
                },
                {
                  key: '5',
                  icon: <LogoutOutlined />,
                  label: '退出登录',
                  onClick: handleLogout,
                },
              ],
            }}>
              <span style={{ cursor: 'pointer', color: '#000' }}>
                <Avatar 
                  size="small" 
                  icon={<UserOutlined />} 
                  src={(currentUser || localUser)?.avatarUrl} 
                  style={{ marginRight: 8 }} 
                />
                {(currentUser || localUser)?.username || '用户'}
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