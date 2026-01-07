'use client';

import { useState } from 'react';
import MainLayout from '@/components/Layout/MainLayout';

interface Room {
  name: string;
  location: string;
  status: 'online' | 'offline' | 'maintenance';
  temperature: number;
  humidity: number;
  devices: number;
}

export default function RoomListPage() {
  const [rooms, setRooms] = useState<Room[]>([
    { name: '机房A-101', location: '一号楼 一层', status: 'online', temperature: 22, humidity: 45, devices: 156 },
    { name: '机房A-102', location: '一号楼 一层', status: 'online', temperature: 23, humidity: 48, devices: 142 },
    { name: '机房A-103', location: '一号楼 二层', status: 'online', temperature: 21, humidity: 46, devices: 128 },
    { name: '机房B-201', location: '二号楼 一层', status: 'online', temperature: 24, humidity: 50, devices: 98 },
    { name: '机房B-203', location: '二号楼 二层', status: 'warning', temperature: 28, humidity: 65, devices: 85 },
    { name: '机房C-305', location: '三号楼 三层', status: 'offline', temperature: 0, humidity: 0, devices: 72 },
  ]);

  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [showModal, setShowModal] = useState(false);
  const [editingRoom, setEditingRoom] = useState<Room | null>(null);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState<string | null>(null);
  const [formData, setFormData] = useState({
    name: '',
    location: '',
    status: 'online' as 'online' | 'offline' | 'maintenance',
    temperature: 0,
    humidity: 0,
    devices: 0,
  });

  const filteredRooms = rooms.filter((item) => {
    const matchSearch =
      item.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      item.location.toLowerCase().includes(searchTerm.toLowerCase());
    const matchStatus = statusFilter === 'all' || item.status === statusFilter;
    return matchSearch && matchStatus;
  });

  const handleAdd = () => {
    setEditingRoom(null);
    setFormData({
      name: '',
      location: '',
      status: 'online',
      temperature: 0,
      humidity: 0,
      devices: 0,
    });
    setShowModal(true);
  };

  const handleEdit = (room: Room) => {
    setEditingRoom(room);
    setFormData({
      name: room.name,
      location: room.location,
      status: room.status,
      temperature: room.temperature,
      humidity: room.humidity,
      devices: room.devices,
    });
    setShowModal(true);
  };

  const handleDelete = (name: string) => {
    setDeleteTarget(name);
    setShowDeleteModal(true);
  };

  const handleSave = () => {
    if (editingRoom) {
      setRooms(
        rooms.map((item) =>
          item.name === editingRoom.name ? { ...item, ...formData } : item
        )
      );
    } else {
      setRooms([...rooms, { ...formData }]);
    }
    setShowModal(false);
  };

  const handleConfirmDelete = () => {
    if (deleteTarget) {
      setRooms(rooms.filter((item) => item.name !== deleteTarget));
      setShowDeleteModal(false);
      setDeleteTarget(null);
    }
  };

  const handleExport = () => {
    const csv = [
      ['机房名称', '位置', '状态', '温度', '湿度', '设备数'],
      ...filteredRooms.map((item) => [
        item.name,
        item.location,
        item.status === 'online'
          ? '在线'
          : item.status === 'offline'
          ? '离线'
          : '维护中',
        item.temperature,
        item.humidity,
        item.devices,
      ]),
    ]
      .map((row) => row.join(','))
      .join('\n');

    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = `机房数据_${new Date().toISOString().split('T')[0]}.csv`;
    link.click();
  };

  const getStatusClass = (status: string) => {
    switch (status) {
      case 'online':
        return 'bg-green-500/20 text-green-400 border border-green-500/30';
      case 'offline':
        return 'bg-red-500/20 text-red-400 border border-red-500/30';
      case 'maintenance':
        return 'bg-gray-500/20 text-gray-400 border border-gray-500/30';
      case 'warning':
        return 'bg-yellow-500/20 text-yellow-400 border border-yellow-500/30';
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
      case 'maintenance':
        return '维护中';
      case 'warning':
        return '警告';
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
            机房管理
          </h1>
          <p className="text-gray-400">查看和管理所有机房信息</p>
        </div>

        {/* 统计卡片 */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          {[
            { label: '总机房数', value: rooms.length, color: 'from-cyan-500 to-blue-600' },
            {
              label: '在线机房',
              value: rooms.filter((r) => r.status === 'online').length,
              color: 'from-green-500 to-emerald-600',
            },
            {
              label: '离线机房',
              value: rooms.filter((r) => r.status === 'offline').length,
              color: 'from-red-500 to-pink-600',
            },
            {
              label: '维护中',
              value: rooms.filter((r) => r.status === 'maintenance').length,
              color: 'from-orange-500 to-amber-600',
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
                placeholder="搜索机房..."
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
              <option value="maintenance">维护中</option>
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
            新建机房
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

        {/* 机房卡片列表 */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredRooms.map((item, index) => (
            <div
              key={index}
              className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-gray-800/50 to-gray-900/50 border border-cyan-500/20 backdrop-blur-sm p-6 hover:scale-[1.02] transition-all duration-300"
              style={{
                boxShadow: '0 4px 30px rgba(6, 182, 212, 0.1)',
              }}
            >
              {/* 状态指示器 */}
              <div
                className={`absolute top-4 right-4 w-3 h-3 rounded-full ${
                  item.status === 'online'
                    ? 'bg-green-400 shadow-[0_0_8px_rgba(74,222,128,0.8)]'
                    : item.status === 'warning'
                    ? 'bg-yellow-400 shadow-[0_0_8px_rgba(250,204,21,0.8)]'
                    : 'bg-gray-400'
                }`}
              ></div>

              <h3 className="text-lg font-semibold text-white mb-2">{item.name}</h3>
              <p className="text-sm text-gray-400 mb-4">{item.location}</p>

              {item.status === 'maintenance' || item.status === 'offline' ? (
                <div className="py-8 text-center">
                  <p className="text-gray-400">
                    {item.status === 'maintenance' ? '机房维护中' : '机房已离线'}
                  </p>
                  <p className="text-xs text-gray-500 mt-2">
                    {item.status === 'maintenance'
                      ? '预计恢复时间：2025-01-10'
                      : '最后在线：2025-01-07 10:30'}
                  </p>
                </div>
              ) : (
                <div className="space-y-3">
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-gray-400">温度</span>
                    <span
                      className={`text-sm font-medium ${
                        item.temperature > 25 ? 'text-red-400' : 'text-cyan-400'
                      }`}
                    >
                      {item.temperature}°C
                    </span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-gray-400">湿度</span>
                    <span
                      className={`text-sm font-medium ${
                        item.humidity > 60 ? 'text-yellow-400' : 'text-cyan-400'
                      }`}
                    >
                      {item.humidity}%
                    </span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-gray-400">设备数</span>
                    <span className="text-sm font-medium text-white">{item.devices}</span>
                  </div>
                </div>
              )}

              {/* 底部按钮 */}
              <div className="mt-4 pt-4 border-t border-gray-700/50 flex gap-2">
                <button
                  onClick={() => handleEdit(item)}
                  className="flex-1 px-4 py-2 bg-cyan-500/20 text-cyan-400 rounded-lg text-sm hover:bg-cyan-500/30 transition-colors"
                >
                  编辑
                </button>
                <button
                  onClick={() => handleDelete(item.name)}
                  className="flex-1 px-4 py-2 bg-red-500/20 text-red-400 rounded-lg text-sm hover:bg-red-500/30 transition-colors"
                >
                  删除
                </button>
              </div>
            </div>
          ))}
          {filteredRooms.length === 0 && (
            <div className="col-span-full py-12 text-center text-gray-400">
              暂无数据
            </div>
          )}
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
              {editingRoom ? '编辑机房' : '新建机房'}
            </h2>
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-400 mb-2">机房名称</label>
                <input
                  type="text"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  placeholder="请输入机房名称"
                  className="w-full px-4 py-2 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white placeholder-gray-500"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-400 mb-2">位置</label>
                <input
                  type="text"
                  value={formData.location}
                  onChange={(e) => setFormData({ ...formData, location: e.target.value })}
                  placeholder="请输入机房位置"
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
                  <option value="online">在线</option>
                  <option value="offline">离线</option>
                  <option value="maintenance">维护中</option>
                </select>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-400 mb-2">温度(°C)</label>
                  <input
                    type="number"
                    value={formData.temperature}
                    onChange={(e) => setFormData({ ...formData, temperature: Number(e.target.value) })}
                    placeholder="0"
                    className="w-full px-4 py-2 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white placeholder-gray-500"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-400 mb-2">湿度(%)</label>
                  <input
                    type="number"
                    value={formData.humidity}
                    onChange={(e) => setFormData({ ...formData, humidity: Number(e.target.value) })}
                    placeholder="0"
                    className="w-full px-4 py-2 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white placeholder-gray-500"
                  />
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-400 mb-2">设备数</label>
                <input
                  type="number"
                  value={formData.devices}
                  onChange={(e) => setFormData({ ...formData, devices: Number(e.target.value) })}
                  placeholder="0"
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
            <p className="text-gray-400 mb-6">确定要删除这个机房吗？此操作无法撤销。</p>
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
