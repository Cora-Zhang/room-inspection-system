'use client';

import { useState, useEffect } from 'react';
import MainLayout from '@/components/Layout/MainLayout';
import type { User } from '@/types';

export default function UserPage() {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [showPasswordModal, setShowPasswordModal] = useState(false);
  const [modalType, setModalType] = useState<'create' | 'edit'>('create');
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [formData, setFormData] = useState({
    username: '',
    name: '',
    email: '',
    phone: '',
    status: 'active' as 'active' | 'inactive' | 'locked',
    departmentId: 1,
    position: '',
  });
  const [passwordForm, setPasswordForm] = useState({
    oldPassword: '',
    newPassword: '',
    confirmPassword: '',
  });

  const fetchUsers = async () => {
    try {
      setLoading(true);
      const res = await fetch('/api/users');
      const json = await res.json();
      if (json.code === 200) {
        setUsers(json.data.list);
      }
    } catch (error) {
      console.error('获取用户列表失败:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  const handleCreate = () => {
    setModalType('create');
    setFormData({
      username: '',
      name: '',
      email: '',
      phone: '',
      status: 'active',
      departmentId: 1,
      position: '',
    });
    setShowModal(true);
  };

  const handleEdit = (user: User) => {
    setModalType('edit');
    setSelectedUser(user);
    setFormData({
      username: user.username,
      name: user.name,
      email: user.email || '',
      phone: user.phone || '',
      status: user.status,
      departmentId: user.departmentId || 1,
      position: user.position || '',
    });
    setShowModal(true);
  };

  const handleSubmit = async () => {
    try {
      const res = await fetch('/api/users', {
        method: modalType === 'create' ? 'POST' : 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(
          modalType === 'create'
            ? { ...formData, password: '123456' }
            : { ...formData, id: selectedUser?.id }
        ),
      });
      const json = await res.json();
      if (json.code === 200) {
        setShowModal(false);
        fetchUsers();
        alert('操作成功');
      }
    } catch (error) {
      console.error('操作失败:', error);
      alert('操作失败');
    }
  };

  const handleDelete = async (userId: number, username: string) => {
    if (username === 'admin') {
      alert('不允许删除管理员账号');
      return;
    }

    if (!confirm('确认删除该用户？')) return;

    try {
      const res = await fetch(`/api/users?id=${userId}`, {
        method: 'DELETE',
      });
      const json = await res.json();
      if (json.code === 200) {
        fetchUsers();
        alert('删除成功');
      }
    } catch (error) {
      console.error('删除失败:', error);
      alert('删除失败');
    }
  };

  const handleResetPassword = (user: User) => {
    setSelectedUser(user);
    setPasswordForm({
      oldPassword: '',
      newPassword: '',
      confirmPassword: '',
    });
    setShowPasswordModal(true);
  };

  const handlePasswordSubmit = async () => {
    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      alert('两次密码不一致');
      return;
    }

    try {
      const res = await fetch('/api/users/reset-password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          userId: selectedUser?.id,
          newPassword: passwordForm.newPassword,
        }),
      });

      // Mock response
      setShowPasswordModal(false);
      alert('密码重置成功');
    } catch (error) {
      console.error('密码重置失败:', error);
      alert('密码重置失败');
    }
  };

  const getStatusBadge = (status: string) => {
    const statusMap: Record<string, { text: string; bg: string; border: string }> = {
      active: { text: '正常', bg: 'bg-green-500/20', border: 'border-green-500/30' },
      inactive: { text: '禁用', bg: 'bg-gray-500/20', border: 'border-gray-500/30' },
      locked: { text: '锁定', bg: 'bg-red-500/20', border: 'border-red-500/30' },
    };
    const config = statusMap[status] || statusMap.active;
    return (
      <span className={`inline-flex px-2 py-1 text-xs rounded-full ${config.bg} ${config.border} text-white`}>
        {config.text}
      </span>
    );
  };

  return (
    <MainLayout>
      <div className="space-y-6">
        {/* 页面标题 */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-cyan-400 to-blue-500 mb-2">
              账号管理
            </h1>
            <p className="text-gray-400">管理系统用户账号和权限</p>
          </div>
          <button
            onClick={handleCreate}
            className="px-6 py-2 bg-gradient-to-r from-cyan-500 to-blue-600 rounded-lg text-white font-medium hover:from-cyan-400 hover:to-blue-500 transition-all"
          >
            新建用户
          </button>
        </div>

        {/* 统计卡片 */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          {[
            { label: '总用户数', value: users.length, color: 'from-cyan-500 to-blue-600' },
            { label: '正常用户', value: users.filter((u) => u.status === 'active').length, color: 'from-green-500 to-emerald-600' },
            { label: '禁用用户', value: users.filter((u) => u.status === 'inactive').length, color: 'from-gray-500 to-slate-600' },
            { label: '锁定用户', value: users.filter((u) => u.status === 'locked').length, color: 'from-red-500 to-pink-600' },
          ].map((item, index) => (
            <div
              key={index}
              className="relative overflow-hidden rounded-xl bg-gradient-to-br from-gray-800/50 to-gray-900/50 border border-cyan-500/20 backdrop-blur-sm p-4"
              style={{
                boxShadow: '0 4px 30px rgba(6, 182, 212, 0.1)',
              }}
            >
              <p className="text-sm text-gray-400 mb-2">{item.label}</p>
              <p
                className={`text-2xl font-bold text-transparent bg-clip-text bg-gradient-to-r ${item.color}`}
              >
                {item.value}
              </p>
            </div>
          ))}
        </div>

        {/* 用户列表 */}
        <div
          className="relative rounded-2xl bg-gradient-to-br from-gray-800/50 to-gray-900/50 border border-cyan-500/20 backdrop-blur-sm overflow-hidden"
          style={{
            boxShadow: '0 4px 30px rgba(6, 182, 212, 0.1)',
          }}
        >
          <table className="w-full">
            <thead>
              <tr className="border-b border-gray-700">
                <th className="px-6 py-4 text-left text-sm font-medium text-gray-400">用户信息</th>
                <th className="px-6 py-4 text-left text-sm font-medium text-gray-400">联系方式</th>
                <th className="px-6 py-4 text-left text-sm font-medium text-gray-400">部门/职位</th>
                <th className="px-6 py-4 text-left text-sm font-medium text-gray-400">状态</th>
                <th className="px-6 py-4 text-left text-sm font-medium text-gray-400">最后登录</th>
                <th className="px-6 py-4 text-left text-sm font-medium text-gray-400">操作</th>
              </tr>
            </thead>
            <tbody>
              {users.map((user) => (
                <tr key={user.id} className="border-b border-gray-700/50 hover:bg-gray-700/30 transition-colors">
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-3">
                      <div className="w-10 h-10 rounded-full bg-gradient-to-br from-cyan-500 to-blue-600 flex items-center justify-center text-white font-medium">
                        {user.name?.charAt(0) || 'U'}
                      </div>
                      <div>
                        <p className="text-white font-medium">{user.name}</p>
                        <p className="text-xs text-gray-400">{user.username}</p>
                      </div>
                    </div>
                  </td>
                  <td className="px-6 py-4">
                    <p className="text-sm text-gray-300">{user.email || '-'}</p>
                    <p className="text-xs text-gray-500">{user.phone || '-'}</p>
                  </td>
                  <td className="px-6 py-4">
                    <p className="text-sm text-gray-300">IT部门</p>
                    <p className="text-xs text-gray-500">{user.position || '-'}</p>
                  </td>
                  <td className="px-6 py-4">{getStatusBadge(user.status)}</td>
                  <td className="px-6 py-4 text-sm text-gray-400">{user.lastLoginTime || '-'}</td>
                  <td className="px-6 py-4">
                    <div className="flex gap-2">
                      <button
                        onClick={() => handleEdit(user)}
                        className="text-cyan-400 hover:text-cyan-300 text-sm font-medium transition-colors"
                      >
                        编辑
                      </button>
                      <span className="text-gray-600">|</span>
                      <button
                        onClick={() => handleResetPassword(user)}
                        className="text-blue-400 hover:text-blue-300 text-sm font-medium transition-colors"
                      >
                        重置密码
                      </button>
                      {user.username !== 'admin' && (
                        <>
                          <span className="text-gray-600">|</span>
                          <button
                            onClick={() => handleDelete(user.id, user.username)}
                            className="text-red-400 hover:text-red-300 text-sm font-medium transition-colors"
                          >
                            删除
                          </button>
                        </>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          {/* 分页 */}
          <div className="px-6 py-4 border-t border-gray-700 flex items-center justify-between">
            <p className="text-sm text-gray-400">共 {users.length} 条记录</p>
            <div className="flex gap-2">
              <button className="px-3 py-1 text-sm rounded bg-gray-700 text-gray-300 hover:bg-gray-600">上一页</button>
              <button className="px-3 py-1 text-sm rounded bg-cyan-500/20 text-cyan-400 border border-cyan-500/30">1</button>
              <button className="px-3 py-1 text-sm rounded bg-gray-700 text-gray-300 hover:bg-gray-600">下一页</button>
            </div>
          </div>
        </div>
      </div>

      {/* 用户模态框 */}
      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
          <div className="relative w-full max-w-lg rounded-2xl bg-gray-900 border border-cyan-500/30 p-6">
            <h3 className="text-lg font-semibold text-white mb-4">
              {modalType === 'create' ? '新建用户' : '编辑用户'}
            </h3>
            <div className="space-y-4 max-h-[500px] overflow-y-auto custom-scrollbar">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm text-gray-400 mb-2">用户名</label>
                  <input
                    type="text"
                    value={formData.username}
                    onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                    disabled={modalType === 'edit'}
                    className="w-full px-4 py-2 bg-gray-800 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white disabled:opacity-50"
                  />
                </div>
                <div>
                  <label className="block text-sm text-gray-400 mb-2">姓名</label>
                  <input
                    type="text"
                    value={formData.name}
                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                    className="w-full px-4 py-2 bg-gray-800 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white"
                  />
                </div>
              </div>
              <div>
                <label className="block text-sm text-gray-400 mb-2">邮箱</label>
                <input
                  type="email"
                  value={formData.email}
                  onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                  className="w-full px-4 py-2 bg-gray-800 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white"
                />
              </div>
              <div>
                <label className="block text-sm text-gray-400 mb-2">手机号</label>
                <input
                  type="tel"
                  value={formData.phone}
                  onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                  className="w-full px-4 py-2 bg-gray-800 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white"
                />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm text-gray-400 mb-2">部门</label>
                  <select
                    value={formData.departmentId}
                    onChange={(e) => setFormData({ ...formData, departmentId: parseInt(e.target.value) })}
                    className="w-full px-4 py-2 bg-gray-800 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white"
                  >
                    <option value={1}>IT部门</option>
                    <option value={2}>巡检部门</option>
                    <option value={3}>技术部门</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm text-gray-400 mb-2">职位</label>
                  <input
                    type="text"
                    value={formData.position}
                    onChange={(e) => setFormData({ ...formData, position: e.target.value })}
                    className="w-full px-4 py-2 bg-gray-800 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white"
                  />
                </div>
              </div>
              <div>
                <label className="block text-sm text-gray-400 mb-2">状态</label>
                <select
                  value={formData.status}
                  onChange={(e) => setFormData({ ...formData, status: e.target.value as any })}
                  className="w-full px-4 py-2 bg-gray-800 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white"
                >
                  <option value="active">正常</option>
                  <option value="inactive">禁用</option>
                  <option value="locked">锁定</option>
                </select>
              </div>
            </div>
            <div className="flex gap-3 mt-6">
              <button
                onClick={() => setShowModal(false)}
                className="flex-1 px-4 py-2 bg-gray-700 text-white rounded-lg hover:bg-gray-600 transition-all"
              >
                取消
              </button>
              <button
                onClick={handleSubmit}
                className="flex-1 px-4 py-2 bg-cyan-500 text-white rounded-lg hover:bg-cyan-400 transition-all"
              >
                确定
              </button>
            </div>
          </div>
        </div>
      )}

      {/* 重置密码模态框 */}
      {showPasswordModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
          <div className="relative w-full max-w-md rounded-2xl bg-gray-900 border border-cyan-500/30 p-6">
            <h3 className="text-lg font-semibold text-white mb-4">重置密码</h3>
            <div className="space-y-4">
              <div>
                <label className="block text-sm text-gray-400 mb-2">新密码</label>
                <input
                  type="password"
                  value={passwordForm.newPassword}
                  onChange={(e) => setPasswordForm({ ...passwordForm, newPassword: e.target.value })}
                  className="w-full px-4 py-2 bg-gray-800 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white"
                />
              </div>
              <div>
                <label className="block text-sm text-gray-400 mb-2">确认密码</label>
                <input
                  type="password"
                  value={passwordForm.confirmPassword}
                  onChange={(e) => setPasswordForm({ ...passwordForm, confirmPassword: e.target.value })}
                  className="w-full px-4 py-2 bg-gray-800 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white"
                />
              </div>
            </div>
            <div className="flex gap-3 mt-6">
              <button
                onClick={() => setShowPasswordModal(false)}
                className="flex-1 px-4 py-2 bg-gray-700 text-white rounded-lg hover:bg-gray-600 transition-all"
              >
                取消
              </button>
              <button
                onClick={handlePasswordSubmit}
                className="flex-1 px-4 py-2 bg-cyan-500 text-white rounded-lg hover:bg-cyan-400 transition-all"
              >
                确定
              </button>
            </div>
          </div>
        </div>
      )}
    </MainLayout>
  );
}
