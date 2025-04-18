import { Layout, Avatar, Dropdown, Skeleton, message, Button } from 'antd';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { UserOutlined, SettingOutlined, LogoutOutlined, LockOutlined, DeleteOutlined } from '@ant-design/icons';
import { logout } from '@/services/userService';
import { useUser } from '@/contexts/UserContext';
import { useEffect, useState, useCallback } from 'react';

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
  const [navReady, setNavReady] = useState(false);

  // 客户端渲染检测
  useEffect(() => {
    setIsClient(true);
    // 确保导航栏总是在短时间内可用，即使其他操作还未完成
    setTimeout(() => setNavReady(true), 100);
  }, []);

  // 优化的状态同步逻辑
  const syncUserInfo = useCallback(async () => {
    if (!isClient) return;

    try {
      // 首先使用本地存储的用户信息（如果有）
      const storedUser = localStorage.getItem('userInfo');
      if (storedUser) {
        try {
          const parsedUser = JSON.parse(storedUser);
          setLocalUser(parsedUser);
        } catch (e) {
          console.error('解析用户信息失败', e);
        }
      }

      // 检查是否有token但还没有用户信息
      const userToken = localStorage.getItem('userToken');
      const loginSuccess = localStorage.getItem('loginSuccess');
      
      if (userToken) {
        // 静默更新用户信息，不影响UI显示
        refreshUserInfo(true).then(user => {
          if (user) {
            setLocalUser(user);
            if (loginSuccess === 'true') {
              localStorage.removeItem('loginSuccess');
            }
          }
        }).catch(e => {
          console.error('刷新用户信息失败', e);
        });
      }
    } catch (e) {
      console.error('同步用户信息失败:', e);
    }
  }, [isClient, refreshUserInfo]);

  useEffect(() => {
    syncUserInfo();
  }, [syncUserInfo]);

  // 当Context中的用户信息变化时更新本地状态
  useEffect(() => {
    if (currentUser) {
      setLocalUser(currentUser);
    }
  }, [currentUser]);

  // 基础导航项目
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
      message.error('退出登录失败，请重试');
      
      // 即使API调用失败，也确保状态被清除
      setLocalUser(null);
      clearUserInfo();
      
      if (activeItem !== 'home') {
        router.push('/');
      }
    }
  };

  // 获取显示的用户，优先使用currentUser，然后是localUser
  const displayUser = currentUser || localUser;
  
  // 总是渲染导航栏，不依赖于navReady或其他异步状态
  return (
    <Header style={{ 
      padding: 0, 
      background: '#fff', 
      position: 'sticky', 
      top: 0, 
      zIndex: 1,
      boxShadow: '0 1px 4px rgba(0, 0, 0, 0.1)',
      width: '100%',
      margin: 0,
      borderTop: 0
    }}>
      <div style={{ width: '100%', display: 'flex' }}>
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
                color: item.active ? '#1890ff' : '#000', 
                height: '64px', 
                lineHeight: '64px',
                fontWeight: item.active ? 'bold' : 'normal',
                display: item.title === '用户管理' && (!isClient || !displayUser || displayUser?.userRole !== 1) ? 'none' : 'block'
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
          {!isClient ? (
            // 客户端渲染前显示简单占位符
            <div style={{ width: '32px', height: '32px' }}></div>
          ) : loading && !displayUser ? (
            <Skeleton.Avatar active size="small" style={{ marginRight: 8 }} />
          ) : displayUser ? (
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
                  label: (
                    <span onClick={handleLogout} style={{ cursor: 'pointer' }}>
                      退出登录
                    </span>
                  ),
                },
              ],
            }}>
              <div style={{ display: 'flex', alignItems: 'center', cursor: 'pointer' }}>
                <Avatar 
                  size="small" 
                  src={displayUser.avatarUrl} 
                  icon={!displayUser.avatarUrl && <UserOutlined />} 
                  style={{ marginRight: 8 }}
                />
                <span style={{ maxWidth: '100px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                  {displayUser.username || displayUser.userAccount}
                </span>
              </div>
            </Dropdown>
          ) : (
            <Link href="/auth/login">
              <Button type="text" style={{ 
                color: '#1890ff', 
                padding: '4px 12px', 
                border: '1px solid #1890ff', 
                borderRadius: '4px',
                fontSize: '14px',
                height: '32px',
                display: 'flex',
                alignItems: 'center'
              }}>
                登录/注册
              </Button>
            </Link>
          )}
        </div>
      </div>
    </Header>
  );
} 