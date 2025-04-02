'use client';

import { useState, useEffect } from 'react';
import { Table, Button, Space, Input, Select, message, Popconfirm } from 'antd';
import { SearchOutlined, DeleteOutlined } from '@ant-design/icons';
import { getUserPage, deleteUser, UserType, PageVO } from '@/services/userService';

const { Option } = Select;

const UserManagement = () => {
  const [loading, setLoading] = useState(false);
  const [users, setUsers] = useState<UserType[]>([]);
  const [total, setTotal] = useState(0);
  const [current, setCurrent] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [userAccount, setUserAccount] = useState('');
  const [userRole, setUserRole] = useState<number | undefined>(undefined);

  const fetchUsers = async () => {
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
  };

  useEffect(() => {
    fetchUsers();
  }, [current, pageSize]);

  // 当筛选条件变化时不自动触发，而是等用户点击搜索按钮
  const handleSearch = () => {
    setCurrent(1); // 重置到第一页
    fetchUsers();
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteUser(id);
      message.success('删除用户成功');
      fetchUsers();
    } catch (error: any) {
      // 增强错误处理，显示更具体的错误信息
      const errorMsg = error.message || '删除用户失败，请检查网络或权限';
      message.error(errorMsg);
      console.error('删除用户失败:', error);
    }
  };

  const columns = [
    {
      title: 'ID',
      dataIndex: 'id',
      key: 'id',
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
      render: (status: number) => (status === 0 ? '正常' : '封禁'),
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
    },
    {
      title: '操作',
      key: 'action',
      render: (_: any, record: UserType) => (
        <Space size="middle">
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
    </div>
  );
};

export default UserManagement; 