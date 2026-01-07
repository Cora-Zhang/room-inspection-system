'use client';

import { useState } from 'react';
import MainLayout from '@/components/Layout/MainLayout';

interface Staff {
  id: string;
  name: string;
  department: string;
  phone: string;
  email: string;
  status: 'active' | 'inactive' | 'on_leave';
  joinedDate: string;
}

export default function StaffManagementPage() {
  const [staff, setStaff] = useState<Staff[]>([
    { id: 'STF-001', name: '张三', department: '运维一部', phone: '13800138001', email: 'zhangsan@example.com', status: 'active', joinedDate: '2024-01-15' },
    { id: 'STF-002', name: '李四', department: '运维一部', phone: '13800138002', email: 'lisi@example.com', status: 'active', joinedDate: '2024-02-20' },
    { id: 'STF-003', name: '王五', department: '运维二部', phone: '13800138003', email: 'wangwu@example.com', status: 'active', joinedDate: '2024-03-10' },
    { id: 'STF-004', name: '赵六', department: '运维二部', phone: '13800138004', email: 'zhaoliu@example.com', status: 'on_leave', joinedDate: '2024-04-05' },
    { id: 'STF-005', name: '孙七', department: '运维一部', phone: '13800138005', email: 'sunqi@example.com', status: 'inactive', joinedDate: '2023-12-01' },
  ]);

  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [departmentFilter, setDepartmentFilter] = useState('all');
  const [showModal, setShowModal] = useState(false);
  const [editingStaff, setEditingStaff] = useState<Staff | null>(null);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState<string | null>(null);
  const [formData, setFormData] = useState({
    id: '',
    name: '',
    department: '',
    phone: '',
    email: '',
    status: 'active' as 'active' | 'inactive' | 'on_leave',
    joinedDate: '',
  });

  const filteredStaff = staff.filter((item) => {
    const matchSearch =
      item.id.toLowerCase().includes(searchTerm.toLowerCase()) ||
      item.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      item.phone.includes(searchTerm);
    const matchStatus = statusFilter === 'all' || item.status === statusFilter;
    const matchDepartment = departmentFilter === 'all' || item.department === departmentFilter;
    return matchSearch && matchStatus && matchDepartment;
  });

  const handleAdd = () => {
    setEditingStaff(null);
    setFormData({
      id: '',
      name: '',
      department: '',
      phone: '',
      email: '',
      status: 'active',
      joinedDate: new Date().toISOString().split('T')[0],
    });
    setShowModal(true);
  };

  const handleEdit = (person: Staff) => {
    setEditingStaff(person);
    setFormData({ ...person });
    setShowModal(true);
  };

  const handleDelete = (id: string) => {
    setDeleteTarget(id);
    setShowDeleteModal(true);
  };

  const handleSave = () => {
    if (editingStaff) {
      setStaff(staff.map((item) => (item.id === editingStaff.id ? { ...item, ...formData } : item)));
    } else {
      const newId = `STF-${String(staff.length + 1).padStart(3, '0')}`;
      setStaff([...staff, { ...formData, id: newId }]);
    }
    setShowModal(false);
  };

  const handleConfirmDelete = () => {
    if (deleteTarget) {
      setStaff(staff.filter((item) => item.id !== deleteTarget));
      setShowDeleteModal(false);
      setDeleteTarget(null);
    }
  };

  const handleExport = () => {
    const csv = [
      ['员工ID', '姓名', '部门', '联系电话', '邮箱', '状态', '入职日期'],
      ...filteredStaff.map((item) => [
        item.id,
        item.name,
        item.department,
        item.phone,
        item.email,
        item.status === 'active'
          ? '在职'
          : item.status === 'on_leave'
          ? '请假中'
          : '离职',
        item.joinedDate,
      ]),
    ]
      .map((row) => row.join(','))
      .join('\n');

    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = `值班人员表_${new Date().toISOString().split('T')[0]}.csv`;
    link.click();
  };

  const getStatusClass = (status: string) => {
    switch (status) {
      case 'active':
        return 'bg-green-500/20 text-green-400 border border-green-500/30';
      case 'inactive':
        return 'bg-red-500/20 text-red-400 border border-red-500/30';
      case 'on_leave':
        return 'bg-yellow-500/20 text-yellow-400 border border-yellow-500/30';
      default:
        return 'bg-gray-500/20 text-gray-400 border border-gray-500/30';
    }
  };

  const getStatusText = (status: string) => {
    switch (status) {
      case 'active':
        return '在职';
      case 'inactive':
        return '离职';
      case 'on_leave':
        return '请假中';
      default:
        return '未知';
    }
  };

  return (
    <MainLayout>
      <div className="space-y-6">
        {/* 页面标题 */}
        <div>
          <h1 className="text-2xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-cyan-400 to-blue-500 mb-2">
            值班人员管理
          </h1>
          <p className="text-gray-400">管理值班人员信息，用于排班和巡检分配</p>
        </div>

        {/* 统计卡片 */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          {[
            {
              label: '总人数',
              value: staff.length,
              color: 'from-cyan-500 to-blue-600',
            },
            {
              label: '在职人员',
              value: staff.filter((s) => s.status === 'active').length,
              color: 'from-green-500 to-emerald-600',
            },
            {
              label: '请假中',
              value: staff.filter((s) => s.status === 'on_leave').length,
              color: 'from-yellow-500 to-amber-600',
            },
            {
              label: '已离职',
              value: staff.filter((s) => s.status === 'inactive').length,
              color: 'from-red-500 to-pink-600',
            },
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

        {/* 搜索栏 */}
        <div
          className="relative rounded-2xl bg-gradient-to-br from-gray-800/50 to-gray-900/50 border border-cyan-500/20 backdrop-blur-sm p-6"
          style={{
            boxShadow: '0 4px 30px rgba(6, 182, 212, 0.1)',
          }}
        >
          <div className="flex flex-col md:flex-row gap-4">
            <div className="flex-1">
              <input
                type="text"
                placeholder="搜索人员（姓名/电话/工号）..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full px-4 py-2 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white placeholder-gray-500"
              />
            </div>
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="px-4 py-2 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white"
            >
              <option value="all">全部状态</option>
              <option value="active">在职</option>
              <option value="on_leave">请假中</option>
              <option value="inactive">已离职</option>
            </select>
            <select
              value={departmentFilter}
              onChange={(e) => setDepartmentFilter(e.target.value)}
              className="px-4 py-2 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white"
            >
              <option value="all">全部部门</option>
              <option value="运维一部">运维一部</option>
              <option value="运维二部">运维二部</option>
            </select>
            <button className="px-6 py-2 bg-gradient-to-r from-cyan-500 to-blue-600 rounded-lg text-white font-medium hover:from-cyan-400 hover:to-blue-500 transition-all">
              查询
            </button>
          </div>
        </div>

        {/* 操作按钮栏 */}
        <div className="flex gap-4">
          <button
            onClick={handleAdd}
            className="flex items-center gap-2 px-6 py-2 bg-gradient-to-r from-cyan-500 to-blue-600 rounded-lg text-white font-medium hover:from-cyan-400 hover:to-blue-500 transition-all"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
            </svg>
            新增人员
          </button>
          <button
            onClick={handleExport}
            className="flex items-center gap-2 px-6 py-2 bg-gradient-to-r from-purple-500 to-pink-600 rounded-lg text-white font-medium hover:from-purple-400 hover:to-pink-500 transition-all"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
            </svg>
            导出数据
          </button>
        </div>

        {/* 人员列表 */}
        <div
          className="relative rounded-2xl bg-gradient-to-br from-gray-800/50 to-gray-900/50 border border-cyan-500/20 backdrop-blur-sm overflow-hidden"
          style={{
            boxShadow: '0 4px 30px rgba(6, 182, 212, 0.1)',
          }}
        >
          <table className="w-full">
            <thead>
              <tr className="border-b border-gray-700">
                <th className="px-6 py-4 text-left text-sm font-medium text-gray-400">工号</th>
                <th className="px-6 py-4 text-left text-sm font-medium text-gray-400">姓名</th>
                <th className="px-6 py-4 text-left text-sm font-medium text-gray-400">部门</th>
                <th className="px-6 py-4 text-left text-sm font-medium text-gray-400">联系电话</th>
                <th className="px-6 py-4 text-left text-sm font-medium text-gray-400">邮箱</th>
                <th className="px-6 py-4 text-left text-sm font-medium text-gray-400">状态</th>
                <th className="px-6 py-4 text-left text-sm font-medium text-gray-400">入职日期</th>
                <th className="px-6 py-4 text-left text-sm font-medium text-gray-400">操作</th>
              </tr>
            </thead>
            <tbody>
              {filteredStaff.map((item, index) => (
                <tr key={index} className="border-b border-gray-700/50 hover:bg-gray-700/30 transition-colors">
                  <td className="px-6 py-4 text-sm text-white font-mono">{item.id}</td>
                  <td className="px-6 py-4 text-sm text-white font-medium">{item.name}</td>
                  <td className="px-6 py-4 text-sm text-gray-300">{item.department}</td>
                  <td className="px-6 py-4 text-sm text-gray-300">{item.phone}</td>
                  <td className="px-6 py-4 text-sm text-gray-300">{item.email}</td>
                  <td className="px-6 py-4">
                    <span className={`inline-flex px-3 py-1 text-xs rounded-full ${getStatusClass(item.status)}`}>
                      {getStatusText(item.status)}
                    </span>
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-400 font-mono">{item.joinedDate}</td>
                  <td className="px-6 py-4">
                    <div className="flex gap-2">
                      <button
                        onClick={() => handleEdit(item)}
                        className="text-cyan-400 hover:text-cyan-300 text-sm font-medium transition-colors"
                      >
                        编辑
                      </button>
                      <button
                        onClick={() => handleDelete(item.id)}
                        className="text-red-400 hover:text-red-300 text-sm font-medium transition-colors"
                      >
                        删除
                      </button>
                      <button className="text-blue-400 hover:text-blue-300 text-sm font-medium transition-colors">
                        值班记录
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
              {filteredStaff.length === 0 && (
                <tr>
                  <td colSpan={8} className="px-6 py-12 text-center text-gray-400">
                    暂无数据
                  </td>
                </tr>
              )}
            </tbody>
          </table>

          {/* 分页 */}
          <div className="px-6 py-4 border-t border-gray-700 flex items-center justify-between">
            <p className="text-sm text-gray-400">共 {filteredStaff.length} 条记录</p>
            <div className="flex gap-2">
              <button className="px-3 py-1 text-sm rounded bg-gray-700 text-gray-300 hover:bg-gray-600 transition-colors">
                上一页
              </button>
              <button className="px-3 py-1 text-sm rounded bg-cyan-500/20 text-cyan-400 border border-cyan-500/30">
                1
              </button>
              <button className="px-3 py-1 text-sm rounded bg-gray-700 text-gray-300 hover:bg-gray-600 transition-colors">
                下一页
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* 新增/编辑模态框 */}
      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center" style={{ backgroundColor: 'rgba(0,0,0,0.7)' }}>
          <div
            className="relative rounded-2xl bg-gradient-to-br from-gray-800 to-gray-900 border border-cyan-500/30 p-6 w-full max-w-md"
            style={{
              boxShadow: '0 0 60px rgba(6, 182, 212, 0.2)',
            }}
          >
            <h2 className="text-xl font-bold text-white mb-6">
              {editingStaff ? '编辑人员信息' : '新增人员'}
            </h2>
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-400 mb-2">工号</label>
                <input
                  type="text"
                  value={formData.id}
                  onChange={(e) => setFormData({ ...formData, id: e.target.value })}
                  placeholder="请输入工号"
                  disabled={!!editingStaff}
                  className="w-full px-4 py-2 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white placeholder-gray-500 disabled:opacity-50 disabled:cursor-not-allowed"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-400 mb-2">姓名</label>
                <input
                  type="text"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  placeholder="请输入姓名"
                  className="w-full px-4 py-2 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white placeholder-gray-500"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-400 mb-2">部门</label>
                <select
                  value={formData.department}
                  onChange={(e) => setFormData({ ...formData, department: e.target.value })}
                  className="w-full px-4 py-2 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white"
                >
                  <option value="">请选择部门</option>
                  <option value="运维一部">运维一部</option>
                  <option value="运维二部">运维二部</option>
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-400 mb-2">联系电话</label>
                <input
                  type="text"
                  value={formData.phone}
                  onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                  placeholder="请输入联系电话"
                  className="w-full px-4 py-2 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white placeholder-gray-500"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-400 mb-2">邮箱</label>
                <input
                  type="email"
                  value={formData.email}
                  onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                  placeholder="请输入邮箱"
                  className="w-full px-4 py-2 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white placeholder-gray-500"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-400 mb-2">状态</label>
                <select
                  value={formData.status}
                  onChange={(e) => setFormData({ ...formData, status: e.target.value as any })}
                  className="w-full px-4 py-2 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white"
                >
                  <option value="active">在职</option>
                  <option value="on_leave">请假中</option>
                  <option value="inactive">已离职</option>
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-400 mb-2">入职日期</label>
                <input
                  type="date"
                  value={formData.joinedDate}
                  onChange={(e) => setFormData({ ...formData, joinedDate: e.target.value })}
                  className="w-full px-4 py-2 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white placeholder-gray-500"
                />
              </div>
            </div>
            <div className="flex gap-4 mt-6">
              <button
                onClick={() => setShowModal(false)}
                className="flex-1 px-4 py-2 bg-gray-700 text-white rounded-lg hover:bg-gray-600 transition-colors"
              >
                取消
              </button>
              <button
                onClick={handleSave}
                className="flex-1 px-4 py-2 bg-gradient-to-r from-cyan-500 to-blue-600 text-white rounded-lg hover:from-cyan-400 hover:to-blue-500 transition-all"
              >
                保存
              </button>
            </div>
          </div>
        </div>
      )}

      {/* 删除确认模态框 */}
      {showDeleteModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center" style={{ backgroundColor: 'rgba(0,0,0,0.7)' }}>
          <div
            className="relative rounded-2xl bg-gradient-to-br from-gray-800 to-gray-900 border border-red-500/30 p-6 w-full max-w-md"
            style={{
              boxShadow: '0 0 60px rgba(239, 68, 68, 0.2)',
            }}
          >
            <h2 className="text-xl font-bold text-white mb-4">确认删除</h2>
            <p className="text-gray-400 mb-6">确定要删除这个人员信息吗？此操作无法撤销。</p>
            <div className="flex gap-4">
              <button
                onClick={() => {
                  setShowDeleteModal(false);
                  setDeleteTarget(null);
                }}
                className="flex-1 px-4 py-2 bg-gray-700 text-white rounded-lg hover:bg-gray-600 transition-colors"
              >
                取消
              </button>
              <button
                onClick={handleConfirmDelete}
                className="flex-1 px-4 py-2 bg-gradient-to-r from-red-500 to-pink-600 text-white rounded-lg hover:from-red-400 hover:to-pink-500 transition-all"
              >
                确认删除
              </button>
            </div>
          </div>
        </div>
      )}
    </MainLayout>
  );
}
