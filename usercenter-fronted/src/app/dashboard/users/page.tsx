'use client';

import { useState, useEffect, useCallback } from 'react';
import { Table, Button, Space, Input, Select, message, Popconfirm, Avatar, Image, Tag, Tooltip, Modal, Form, InputNumber } from 'antd';
import { SearchOutlined, DeleteOutlined, EditOutlined, UserOutlined, LockOutlined, UnlockOutlined } from '@ant-design/icons';
import { getUserPage, deleteUser, UserType, PageVO, banUser, unbanUser } from '@/services/userService';
import UserEditModal from '@/components/UserEditModal';
import { formatDateTime } from '@/utils/dateUtils';
import TextArea from 'antd/lib/input/TextArea';

const { Option } = Select;

const UserManagement = () => {
  const [loading, setLoading] = useState(false);
  const [users, setUsers] = useState<UserType[]>([]);
  const [total, setTotal] = useState(0);
  const [current, setCurrent] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [userAccount, setUserAccount] = useState('');
  const [userRole, setUserRole] = useState<number | undefined>(undefined);
  const [editModalVisible, setEditModalVisible] = useState(false);
  const [currentUser, setCurrentUser] = useState<UserType | null>(null);
  const [banModalVisible, setBanModalVisible] = useState(false);
  const [banForm] = Form.useForm();
  const [userToBan, setUserToBan] = useState<UserType | null>(null);

  const fetchUsers = useCallback(async () => {
    try {
      setLoading(true);
      const pageData = await getUserPage({
        current,
        pageSize,
        userAccount: userAccount || undefined,
        userRole,
      });
      
      setUsers(pageData.records);
      setTotal(pageData.total);
    } catch (error) {
      message.error('获取用户列表失败，请检查网络或权限');
      console.error('获取用户列表失败:', error);
    } finally {
      setLoading(false);
    }
  }, [current, pageSize, userAccount, userRole]);

  useEffect(() => {
    fetchUsers();
  }, [fetchUsers]);

  // 当筛选条件变化时不自动触发，而是等用户点击搜索按钮
  const handleSearch = () => {
    setCurrent(1); // 重置到第一页
    fetchUsers();
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteUser(id);
      message.success('删除用户成功');
      
      // 优化：先在本地更新状态，再异步刷新完整列表
      setUsers(prevUsers => prevUsers.filter(user => user.id !== id));
      fetchUsers();
    } catch (error: any) {
      // 增强错误处理，显示更具体的错误信息
      const errorMsg = error.message || '删除用户失败，请检查网络或权限';
      message.error(errorMsg);
      console.error('删除用户失败:', error);
    }
  };

  const handleEdit = (user: UserType) => {
    setCurrentUser(user);
    setEditModalVisible(true);
  };

  const handleEditSuccess = (updatedUser?: UserType) => {
    setEditModalVisible(false);
    
    // 如果返回了更新后的用户数据，则先在本地更新状态
    if (updatedUser) {
      setUsers(prevUsers => 
        prevUsers.map(user => 
          user.id === updatedUser.id ? updatedUser : user
        )
      );
    }
    
    // 然后异步刷新完整数据
    fetchUsers();
  };

  const handleBan = (user: UserType) => {
    // 检查用户是否为管理员
    if (user.userRole === 1) {
      message.error('管理员无法被封禁');
      return;
    }
    
    setUserToBan(user);
    setBanModalVisible(true);
    banForm.resetFields();
  };

  const handleBanSubmit = async () => {
    try {
      if (!userToBan) return;
      
      // 再次检查确保不能封禁管理员
      if (userToBan.userRole === 1) {
        message.error('管理员无法被封禁');
        setBanModalVisible(false);
        return;
      }
      
      const values = await banForm.validateFields();
      
      // 明确判断是否为永久封禁 - banDays为0表示永久封禁
      const isPermanent = values.banDays === 0;
      
      // 设置请求参数
      const banParams = {
        userId: userToBan.id,
        banDays: values.banDays, 
        reason: values.reason,
        isPermanent: isPermanent // 显式传递是否永久封禁标志
      };
      
      console.log('发送封禁请求:', JSON.stringify(banParams));
      
      // 发送请求前强制让form控件失去焦点，避免可能的状态问题
      document.activeElement && (document.activeElement as HTMLElement).blur();
      
      await banUser(banParams);
      
      message.success(`用户${isPermanent ? '永久' : '临时'}封禁成功`);
      setBanModalVisible(false);
      
      // 拉取最新数据
      fetchUsers();
    } catch (error: any) {
      message.error(error.message || '封禁用户失败');
      console.error('封禁失败:', error);
    }
  };

  const handleUnban = async (userId: number) => {
    try {
      await unbanUser(userId);
      message.success('用户解封成功');
      
      // 优化：本地立即更新状态
      setUsers(prevUsers => 
        prevUsers.map(user => {
          if (user.id === userId) {
            return {
              ...user,
              isBanned: 0,
              banReason: undefined,
              unbanDate: undefined
            };
          }
          return user;
        })
      );
      
      // 然后在后台异步刷新完整数据
      fetchUsers();
    } catch (error: any) {
      message.error(error.message || '解封用户失败');
    }
  };

  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
    },
    {
      title: '头像',
      dataIndex: 'avatarUrl',
      key: 'avatarUrl',
      render: (avatarUrl: string) => (
        avatarUrl ? (
          <Avatar 
            src={
              <Image
                src={avatarUrl}
                alt="用户头像"
                style={{ width: 32 }}
                preview={{
                  mask: '查看大图'
                }}
                onError={() => true} // 图片加载错误时不显示默认断裂图标
              />
            }
            size={32} 
          />
        ) : (
          <Avatar icon={<UserOutlined />} size={32} />
        )
      ),
    },
    {
      title: '用户名',
      dataIndex: 'username',
      key: 'username',
    },
    {
      title: '账号',
      dataIndex: 'userAccount',
      key: 'userAccount',
    },
    {
      title: '性别',
      dataIndex: 'gender',
      key: 'gender',
      render: (gender: number) => (gender === 0 ? '女' : gender === 1 ? '男' : '未知'),
    },
    {
      title: '电话',
      dataIndex: 'phone',
      key: 'phone',
    },
    {
      title: '邮箱',
      dataIndex: 'email',
      key: 'email',
    },
    {
      title: '状态',
      dataIndex: 'userStatus',
      key: 'userStatus',
      render: (status: number) => {
        if (status === 0) {
          return <Tag color="success">正常</Tag>;
        } else if (status === 1) {
          return <Tag color="default">已停用</Tag>;
        }
        return <Tag color="default">未知</Tag>;
      },
    },
    {
      title: '封禁状态',
      dataIndex: 'isBanned',
      key: 'isBanned',
      render: (isBanned: number, record: UserType) => {
        if (isBanned === 1) {
          const unbanDate = record.unbanDate;
          
          // 计算剩余封禁时间
          let remainingDays = 0;
          let isPastDue = false;
          
          if (unbanDate && unbanDate !== "null" && unbanDate !== "") {
            const now = new Date();
            const unbanDateTime = new Date(unbanDate);
            if (!isNaN(unbanDateTime.getTime())) {
              remainingDays = Math.ceil((unbanDateTime.getTime() - now.getTime()) / (1000 * 3600 * 24));
              isPastDue = remainingDays <= 0;
            }
          }
          
          // 确定封禁类型
          const isUnbanDateEmpty = !unbanDate || unbanDate === "null" || unbanDate === "";
          
          // 记录日志
          console.log(`用户${record.id}封禁状态:`, {
            isBanned,
            unbanDate原始值: record.unbanDate,
            unbanDate类型: typeof unbanDate,
            isUnbanDateEmpty,
            remainingDays,
            isPastDue,
            banReason: record.banReason
          });
          
          // 三种情况：永久封禁、临时封禁生效中、封禁已过期
          if (isUnbanDateEmpty) {
            // 永久封禁
            return (
              <Tooltip title={`原因: ${record.banReason || '未提供'}`}>
                <Tag color="#f50" style={{fontWeight: 'bold'}}>永久封禁</Tag>
              </Tooltip>
            );
          } else if (!isPastDue) {
            // 临时封禁且未过期
            return (
              <Tooltip title={`解封日期: ${formatDateTime(unbanDate)}\n剩余天数: ${remainingDays}天\n原因: ${record.banReason || '未提供'}`}>
                <Tag color="orange">临时封禁 ({remainingDays}天)</Tag>
              </Tooltip>
            );
          } else {
            // 已过期但未自动解封
            return (
              <Tooltip title={`封禁已过期，将在下次登录时自动解封\n原因: ${record.banReason || '未提供'}`}>
                <Tag color="gold">封禁已过期</Tag>
              </Tooltip>
            );
          }
        }
        return <Tag color="success">正常</Tag>;
      },
    },
    {
      title: '角色',
      dataIndex: 'userRole',
      key: 'userRole',
      render: (role: number) => (role === 0 ? '普通用户' : '管理员'),
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
      render: (createTime: string) => formatDateTime(createTime),
    },
    {
      title: '操作',
      key: 'action',
      render: (_: any, record: UserType) => (
        <Space size="middle">
          <Button 
            type="primary" 
            icon={<EditOutlined />} 
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          {record.isBanned === 1 ? (
            <Button
              type="primary"
              icon={<UnlockOutlined />}
              onClick={() => handleUnban(record.id)}
            >
              解封
            </Button>
          ) : record.userRole === 1 ? (
            <Tooltip title="管理员无法被封禁">
              <Button
                type="primary"
                danger
                icon={<LockOutlined />}
                disabled
              >
                封禁
              </Button>
            </Tooltip>
          ) : (
            <Button
              type="primary"
              danger
              icon={<LockOutlined />}
              onClick={() => handleBan(record)}
            >
              封禁
            </Button>
          )}
          <Popconfirm
            title="确定要删除该用户吗?"
            onConfirm={() => handleDelete(record.id)}
            okText="确定"
            cancelText="取消"
          >
            <Button type="primary" danger icon={<DeleteOutlined />}>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div className="p-4">
      <div className="mb-4 flex items-center space-x-4">
        <Input
          placeholder="搜索账号"
          value={userAccount}
          onChange={(e) => setUserAccount(e.target.value)}
          prefix={<SearchOutlined />}
          style={{ width: 200 }}
        />
        <Select
          placeholder="角色筛选"
          allowClear
          style={{ width: 200 }}
          onChange={(value) => setUserRole(value)}
          value={userRole}
        >
          <Option value={0}>普通用户</Option>
          <Option value={1}>管理员</Option>
        </Select>
        <Button type="primary" onClick={handleSearch}>
          搜索
        </Button>
      </div>

      <Table
        columns={columns}
        dataSource={users}
        rowKey="id"
        pagination={{
          current,
          pageSize,
          total,
          onChange: (page) => setCurrent(page),
          onShowSizeChange: (_, size) => setPageSize(size),
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (total) => `共 ${total} 条数据`,
        }}
        loading={loading}
      />
      
      <UserEditModal
        visible={editModalVisible}
        onCancel={() => setEditModalVisible(false)}
        onSuccess={handleEditSuccess}
        user={currentUser}
      />
      
      <Modal
        title="封禁用户"
        open={banModalVisible}
        onOk={handleBanSubmit}
        onCancel={() => setBanModalVisible(false)}
      >
        <Form form={banForm} layout="vertical">
          <Form.Item
            name="banDays"
            label="封禁天数 (0为永久封禁)"
            rules={[{ required: true, message: '请输入封禁天数' }]}
          >
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item
            name="reason"
            label="封禁原因"
            rules={[{ required: true, message: '请输入封禁原因' }]}
          >
            <TextArea rows={4} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default UserManagement; 