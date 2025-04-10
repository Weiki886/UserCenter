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
    setUserToBan(user);
    setBanModalVisible(true);
    banForm.resetFields();
  };

  const handleBanSubmit = async () => {
    try {
      if (!userToBan) return;
      
      const values = await banForm.validateFields();
      await banUser({
        userId: userToBan.id,
        banDays: values.banDays,
        reason: values.reason,
      });
      
      message.success('用户封禁成功');
      setBanModalVisible(false);
      
      // 优化：本地立即更新状态
      const unbanTimestamp = values.banDays ? new Date(Date.now() + values.banDays * 24 * 60 * 60 * 1000).toISOString() : undefined;
      setUsers(prevUsers => 
        prevUsers.map(user => {
          if (user.id === userToBan.id) {
            return {
              ...user,
              isBanned: 1,
              banReason: values.reason,
              unbanDate: unbanTimestamp
            };
          }
          return user;
        })
      );
      
      // 然后在后台异步刷新完整数据
      fetchUsers();
    } catch (error: any) {
      message.error(error.message || '封禁用户失败');
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
          return unbanDate ? (
            <Tooltip title={`解封日期: ${formatDateTime(unbanDate)}\n原因: ${record.banReason}`}>
              <Tag color="error">临时封禁</Tag>
            </Tooltip>
          ) : (
            <Tooltip title={`原因: ${record.banReason}`}>
              <Tag color="error">永久封禁</Tag>
            </Tooltip>
          );
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