'use client';

import { useState } from 'react';
import MainLayout from '@/components/Layout/MainLayout';

interface Device {
  id: string;
  name: string;
  room: string;
  type: string;
  status: 'online' | 'offline' | 'warning' | 'maintenance';
  lastUpdate: string;
}

export default function DeviceListPage() {
  const [devices, setDevices] = useState<Device[]>([
    { id: 'DEV-001', name: '服务器-001', room: '机房A-101', type: '服务器', status: 'online', lastUpdate: '2025-01-07 14:30' },
    { id: 'DEV-002', name: '服务器-002', room: '机房A-101', type: '服务器', status: 'online', lastUpdate: '2025-01-07 14:28' },
    { id: 'DEV-003', name: '路由器-001', room: '机房A-102', type: '网络设备', status: 'online', lastUpdate: '2025-01-07 14:25' },
    { id: 'DEV-004', name: '空调机组-001', room: '机房B-203', type: '环境设备', status: 'warning', lastUpdate: '2025-01-07 14:15' },
    { id: 'DEV-005', name: '交换机-001', room: '机房A-101', type: '网络设备', status: 'offline', lastUpdate: '2025-01-07 13:00' },
    { id: 'DEV-006', name: 'UPS-001', room: '机房C-305', type: '电源设备', status: 'maintenance', lastUpdate: '2025-01-07 10:00' },
  ]);

  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [typeFilter, setTypeFilter] = useState('all');
  const [showModal, setShowModal] = useState(false);
  const [editingDevice, setEditingDevice] = useState<Device | null>(null);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState<string | null>(null);
  const [formData, setFormData] = useState({
    id: '',
    name: '',
    room: '',
    type: '',
    status: 'online' as 'online' | 'offline' | 'warning' | 'maintenance',
  });

  const filteredDevices = devices.filter((item) => {
    const matchSearch =
      item.id.toLowerCase().includes(searchTerm.toLowerCase()) ||
      item.name.toLowerCase().includes(searchTerm.toLowerCase());
    const matchStatus = statusFilter === 'all' || item.status === statusFilter;
    const matchType = typeFilter === 'all' || item.type === typeFilter;
    return matchSearch && matchStatus && matchType;
  });

  const handleAdd = () => {
    setEditingDevice(null);
    setFormData({
      id: '',
      name: '',
      room: '',
      type: '',
      status: 'online',
    });
    setShowModal(true);
  };

  const handleEdit = (device: Device) => {
    setEditingDevice(device);
    setFormData({
      id: device.id,
      name: device.name,
      room: device.room,
      type: device.type,
      status: device.status,
    });
    setShowModal(true);
  };

  const handleDelete = (id: string) => {
    setDeleteTarget(id);
    setShowDeleteModal(true);
  };

  const handleSave = () => {
    if (editingDevice) {
      setDevices(
        devices.map((item) =>
          item.id === editingDevice.id
            ? { ...item, ...formData, lastUpdate: new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-') }
            : item
        )
      );
    } else {
      const newId = `DEV-${String(devices.length + 1).padStart(3, '0')}`;
      setDevices([
        ...devices,
        {
          ...formData,
          id: newId,
          lastUpdate: new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-'),
        },
      ]);
    }
    setShowModal(false);
  };

  const handleConfirmDelete = () => {
    if (deleteTarget) {
      setDevices(devices.filter((item) => item.id !== deleteTarget));
      setShowDeleteModal(false);
      setDeleteTarget(null);
    }
  };

  const handleExport = () => {
    const csv = [
      ['设备ID', '设备名称', '所属机房', '类型', '状态', '最后更新'],
      ...filteredDevices.map((item) => [
        item.id,
        item.name,
        item.room,
        item.type,
        item.status === 'online'
          ? '在线'
          : item.status === 'offline'
          ? '离线'
          : item.status === 'warning'
          ? '告警'
          : '维护中',
        item.lastUpdate,
      ]),
    ]
      .map((row) => row.join(','))
      .join('\n');

    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = `设备数据_${new Date().toISOString().split('T')[0]}.csv`;
    link.click();
  };

  const getStatusClass = (status: string) => {
    switch (status) {
      case 'online':
        return 'bg-green-500/20 text-green-400 border border-green-500/30';
      case 'offline':
        return 'bg-red-500/20 text-red-400 border border-red-500/30';
      case 'warning':
        return 'bg-yellow-500/20 text-yellow-400 border border-yellow-500/30';
      case 'maintenance':
        return 'bg-gray-500/20 text-gray-400 border border-gray-500/30';
      default:
        return 'bg-gray-500/20 text-gray-400 border border-gray-500/30';
    }
  };

  const getStatusText = (status: string) => {
    switch (status) {
      case 'online':
        return '在线';
      case 'offline':
        return '离线';
      case 'warning':
        return '告警';
      case 'maintenance':
        return '维护中';
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
            设备管理
          </h1>
          <p className="text-gray-400">查看和管理所有设备信息</p>
        </div>

        {/* 统计概览 */}
        <div className="grid grid-cols-1 md:grid-cols-5 gap-4">
          {[
            { label: '设备总数', value: devices.length, color: 'cyan' },
            {
              label: '在线设备',
              value: devices.filter((d) => d.status === 'online').length,
              color: 'green',
            },
            {
              label: '离线设备',
              value: devices.filter((d) => d.status === 'offline').length,
              color: 'red',
            },
            {
              label: '告警设备',
              value: devices.filter((d) => d.status === 'warning').length,
              color: 'orange',
            },
            {
              label: '维护设备',
              value: devices.filter((d) => d.status === 'maintenance').length,
              color: 'purple',
            },
          ].map((item, index) => (
            <div
              key={index}
              className="relative overflow-hidden rounded-xl bg-gradient-to-br from-gray-800/50 to-gray-900/50 border border-cyan-500/20 backdrop-blur-sm p-4 hover:scale-[1.02] transition-all duration-300"
              style={{
                boxShadow: '0 4px 30px rgba(6, 182, 212, 0.1)',
              }}
            >
              <div
                className={`absolute top-0 right-0 w-16 h-16 rounded-bl-full opacity-20 ${
                  item.color === 'cyan'
                    ? 'bg-cyan-500'
                    : item.color === 'green'
                    ? 'bg-green-500'
                    : item.color === 'red'
                    ? 'bg-red-500'
                    : item.color === 'orange'
                    ? 'bg-orange-500'
                    : 'bg-purple-500'
                }`}
              ></div>
              <p className="text-xs text-gray-400 mb-1">{item.label}</p>
              <p
                className={`text-2xl font-bold ${
                  item.color === 'cyan'
                    ? 'text-cyan-400'
                    : item.color === 'green'
                    ? 'text-green-400'
                    : item.color === 'red'
                    ? 'text-red-400'
                    : item.color === 'orange'
                    ? 'text-orange-400'
                    : 'text-purple-400'
                }`}
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
                placeholder="搜索设备..."
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
              <option value="online">在线</option>
              <option value="offline">离线</option>
              <option value="warning">告警</option>
              <option value="maintenance">维护中</option>
            </select>
            <select
              value={typeFilter}
              onChange={(e) => setTypeFilter(e.target.value)}
              className="px-4 py-2 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white"
            >
              <option value="all">全部类型</option>
              <option value="服务器">服务器</option>
              <option value="网络设备">网络设备</option>
              <option value="环境设备">环境设备</option>
              <option value="电源设备">电源设备</option>
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
            新建设备
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

        {/* 设备列表 */}
        <div
          className="relative rounded-2xl bg-gradient-to-br from-gray-800/50 to-gray-900/50 border border-cyan-500/20 backdrop-blur-sm overflow-hidden"
          style={{
            boxShadow: '0 4px 30px rgba(6, 182, 212, 0.1)',
          }}
        >
          <table className="w-full">
            <thead>
              <tr className="border-b border-gray-700">
                <th className="px-6 py-4 text-left text-sm font-medium text-gray-400">设备ID</th>
                <th className="px-6 py-4 text-left text-sm font-medium text-gray-400">设备名称</th>
                <th className="px-6 py-4 text-left text-sm font-medium text-gray-400">所属机房</th>
                <th className="px-6 py-4 text-left text-sm font-medium text-gray-400">类型</th>
                <th className="px-6 py-4 text-left text-sm font-medium text-gray-400">状态</th>
                <th className="px-6 py-4 text-left text-sm font-medium text-gray-400">最后更新</th>
                <th className="px-6 py-4 text-left text-sm font-medium text-gray-400">操作</th>
              </tr>
            </thead>
            <tbody>
              {filteredDevices.map((item, index) => (
                <tr key={index} className="border-b border-gray-700/50 hover:bg-gray-700/30 transition-colors">
                  <td className="px-6 py-4 text-sm text-white font-mono">{item.id}</td>
                  <td className="px-6 py-4 text-sm text-white font-medium">{item.name}</td>
                  <td className="px-6 py-4 text-sm text-gray-300">{item.room}</td>
                  <td className="px-6 py-4 text-sm text-gray-300">{item.type}</td>
                  <td className="px-6 py-4">
                    <span className={`inline-flex px-3 py-1 text-xs rounded-full ${getStatusClass(item.status)}`}>
                      {getStatusText(item.status)}
                    </span>
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-400 font-mono">{item.lastUpdate}</td>
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
                        详情
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
              {filteredDevices.length === 0 && (
                <tr>
                  <td colSpan={7} className="px-6 py-12 text-center text-gray-400">
                    暂无数据
                  </td>
                </tr>
              )}
            </tbody>
          </table>

          {/* 分页 */}
          <div className="px-6 py-4 border-t border-gray-700 flex items-center justify-between">
            <p className="text-sm text-gray-400">共 {filteredDevices.length} 条记录</p>
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
              {editingDevice ? '编辑设备' : '新建设备'}
            </h2>
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-400 mb-2">设备ID</label>
                <input
                  type="text"
                  value={formData.id}
                  onChange={(e) => setFormData({ ...formData, id: e.target.value })}
                  placeholder="请输入设备ID"
                  disabled={!!editingDevice}
                  className="w-full px-4 py-2 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white placeholder-gray-500 disabled:opacity-50 disabled:cursor-not-allowed"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-400 mb-2">设备名称</label>
                <input
                  type="text"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  placeholder="请输入设备名称"
                  className="w-full px-4 py-2 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white placeholder-gray-500"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-400 mb-2">所属机房</label>
                <select
                  value={formData.room}
                  onChange={(e) => setFormData({ ...formData, room: e.target.value })}
                  className="w-full px-4 py-2 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white"
                >
                  <option value="">请选择机房</option>
                  <option value="机房A-101">机房A-101</option>
                  <option value="机房A-102">机房A-102</option>
                  <option value="机房B-203">机房B-203</option>
                  <option value="机房C-305">机房C-305</option>
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-400 mb-2">设备类型</label>
                <select
                  value={formData.type}
                  onChange={(e) => setFormData({ ...formData, type: e.target.value })}
                  className="w-full px-4 py-2 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white"
                >
                  <option value="">请选择类型</option>
                  <option value="服务器">服务器</option>
                  <option value="网络设备">网络设备</option>
                  <option value="环境设备">环境设备</option>
                  <option value="电源设备">电源设备</option>
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-400 mb-2">状态</label>
                <select
                  value={formData.status}
                  onChange={(e) => setFormData({ ...formData, status: e.target.value as any })}
                  className="w-full px-4 py-2 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white"
                >
                  <option value="online">在线</option>
                  <option value="offline">离线</option>
                  <option value="warning">告警</option>
                  <option value="maintenance">维护中</option>
                </select>
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
            <p className="text-gray-400 mb-6">确定要删除这个设备吗？此操作无法撤销。</p>
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
