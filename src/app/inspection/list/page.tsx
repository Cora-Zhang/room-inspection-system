'use client';

import { useState } from 'react';
import MainLayout from '@/components/Layout/MainLayout';

interface Inspection {
  id: string;
  room: string;
  inspector: string;
  inspectorId: string;
  time: string;
  status: 'completed' | 'processing' | 'pending';
  shift?: 'day' | 'night';
}

export default function InspectionListPage() {
  const [inspections, setInspections] = useState<Inspection[]>([
    { id: 'INS-2025001', room: '机房A-101', inspector: '张三', inspectorId: 'STF-001', time: '2025-01-07 14:30', status: 'completed', shift: 'day' },
    { id: 'INS-2025002', room: '机房A-102', inspector: '李四', inspectorId: 'STF-002', time: '2025-01-07 13:15', status: 'completed', shift: 'day' },
    { id: 'INS-2025003', room: '机房B-203', inspector: '王五', inspectorId: 'STF-003', time: '2025-01-07 11:00', status: 'completed', shift: 'day' },
    { id: 'INS-2025004', room: '机房C-305', inspector: '张三', inspectorId: 'STF-001', time: '2025-01-07 19:30', status: 'processing', shift: 'night' },
    { id: 'INS-2025005', room: '机房A-103', inspector: '李四', inspectorId: 'STF-002', time: '2025-01-07 09:45', status: 'pending', shift: 'day' },
  ]);

  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const [roomFilter, setRoomFilter] = useState('all');
  const [showModal, setShowModal] = useState(false);
  const [editingInspection, setEditingInspection] = useState<Inspection | null>(null);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState<string | null>(null);
  const [formData, setFormData] = useState({
    room: '',
    inspector: '',
    inspectorId: '',
    status: 'pending' as 'completed' | 'processing' | 'pending',
    shift: 'day' as 'day' | 'night',
  });

  const filteredInspections = inspections.filter((item) => {
    const matchSearch = item.id.toLowerCase().includes(searchTerm.toLowerCase()) ||
                       item.room.toLowerCase().includes(searchTerm.toLowerCase()) ||
                       item.inspector.toLowerCase().includes(searchTerm.toLowerCase());
    const matchStatus = statusFilter === 'all' || item.status === statusFilter;
    const matchRoom = roomFilter === 'all' || item.room === roomFilter;
    return matchSearch && matchStatus && matchRoom;
  });

  const handleAdd = () => {
    setEditingInspection(null);
    setFormData({
      room: '',
      inspector: '',
      inspectorId: '',
      status: 'pending',
      shift: 'morning',
    });
    setShowModal(true);
  };

  const handleEdit = (inspection: Inspection) => {
    setEditingInspection(inspection);
    setFormData({
      room: inspection.room,
      inspector: inspection.inspector,
      inspectorId: inspection.inspectorId,
      status: inspection.status,
      shift: inspection.shift || 'morning',
    });
    setShowModal(true);
  };

  const handleDelete = (id: string) => {
    setDeleteTarget(id);
    setShowDeleteModal(true);
  };

  const handleSave = () => {
    if (editingInspection) {
      // 编辑
      setInspections(inspections.map((item) =>
        item.id === editingInspection.id
          ? {
              ...item,
              ...formData,
              time: new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-'),
            }
          : item
      ));
    } else {
      // 新增
      const newId = `INS-202500${inspections.length + 1}`;
      setInspections([
        ...inspections,
        {
          id: newId,
          ...formData,
          time: new Date().toLocaleString('zh-CN', { hour12: false }).replace(/\//g, '-'),
        },
      ]);
    }
    setShowModal(false);
  };

  const handleConfirmDelete = () => {
    if (deleteTarget) {
      setInspections(inspections.filter((item) => item.id !== deleteTarget));
      setShowDeleteModal(false);
      setDeleteTarget(null);
    }
  };

  const handleExport = () => {
    const csv = [
      ['巡检ID', '机房名称', '巡检人', '巡检时间', '状态'],
      ...filteredInspections.map((item) => [
        item.id,
        item.room,
        item.inspector,
        item.time,
        item.status === 'completed' ? '已完成' : item.status === 'processing' ? '进行中' : '待处理',
      ]),
    ]
      .map((row) => row.join(','))
      .join('\n');

    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = `巡检记录_${new Date().toISOString().split('T')[0]}.csv`;
    link.click();
  };

  const getStatusClass = (status: string) => {
    switch (status) {
      case 'completed':
        return 'bg-green-500/20 text-green-400 border border-green-500/30';
      case 'processing':
        return 'bg-blue-500/20 text-blue-400 border border-blue-500/30';
      case 'pending':
        return 'bg-yellow-500/20 text-yellow-400 border border-yellow-500/30';
      default:
        return 'bg-gray-500/20 text-gray-400 border border-gray-500/30';
    }
  };

  const getStatusText = (status: string) => {
    switch (status) {
      case 'completed':
        return '已完成';
      case 'processing':
        return '进行中';
      case 'pending':
        return '待处理';
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
            巡检记录
          </h1>
          <p className="text-gray-400">查看和管理所有巡检记录</p>
        </div>

        {/* 搜索和筛选栏 */}
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
                placeholder="搜索巡检记录..."
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
              <option value="completed">已完成</option>
              <option value="processing">进行中</option>
              <option value="pending">待处理</option>
            </select>
            <select
              value={roomFilter}
              onChange={(e) => setRoomFilter(e.target.value)}
              className="px-4 py-2 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white"
            >
              <option value="all">全部机房</option>
              <option value="机房A-101">机房A-101</option>
              <option value="机房A-102">机房A-102</option>
              <option value="机房B-203">机房B-203</option>
              <option value="机房C-305">机房C-305</option>
              <option value="机房A-103">机房A-103</option>
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
            新建巡检
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

        {/* 数据表格 */}
        <div
          className="relative rounded-2xl bg-gradient-to-br from-gray-800/50 to-gray-900/50 border border-cyan-500/20 backdrop-blur-sm overflow-hidden"
          style={{
            boxShadow: '0 4px 30px rgba(6, 182, 212, 0.1)',
          }}
        >
          <table className="w-full">
            <thead>
              <tr className="border-b border-gray-700">
                <th className="px-6 py-4 text-left text-sm font-medium text-gray-400">巡检ID</th>
                <th className="px-6 py-4 text-left text-sm font-medium text-gray-400">机房名称</th>
                <th className="px-6 py-4 text-left text-sm font-medium text-gray-400">班次</th>
                <th className="px-6 py-4 text-left text-sm font-medium text-gray-400">巡检人</th>
                <th className="px-6 py-4 text-left text-sm font-medium text-gray-400">巡检时间</th>
                <th className="px-6 py-4 text-left text-sm font-medium text-gray-400">状态</th>
                <th className="px-6 py-4 text-left text-sm font-medium text-gray-400">操作</th>
              </tr>
            </thead>
            <tbody>
              {filteredInspections.map((item, index) => (
                <tr key={index} className="border-b border-gray-700/50 hover:bg-gray-700/30 transition-colors">
                  <td className="px-6 py-4 text-sm text-white font-mono">{item.id}</td>
                  <td className="px-6 py-4 text-sm text-white">{item.room}</td>
                  <td className="px-6 py-4">
                    <span className={`inline-flex px-2 py-1 text-xs rounded-full ${
                      item.shift === 'day' ? 'bg-cyan-500/20 text-cyan-400 border border-cyan-500/30' :
                      'bg-indigo-500/20 text-indigo-400 border border-indigo-500/30'
                    }`}>
                      {item.shift === 'day' ? '白班' : '夜班'}
                    </span>
                  </td>
                  <td className="px-6 py-4 text-sm text-white">{item.inspector}</td>
                  <td className="px-6 py-4 text-sm text-gray-400 font-mono">{item.time}</td>
                  <td className="px-6 py-4">
                    <span className={`inline-flex px-3 py-1 text-xs rounded-full ${getStatusClass(item.status)}`}>
                      {getStatusText(item.status)}
                    </span>
                  </td>
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
              {filteredInspections.length === 0 && (
                <tr>
                  <td colSpan={6} className="px-6 py-12 text-center text-gray-400">
                    暂无数据
                  </td>
                </tr>
              )}
            </tbody>
          </table>

          {/* 分页 */}
          <div className="px-6 py-4 border-t border-gray-700 flex items-center justify-between">
            <p className="text-sm text-gray-400">共 {filteredInspections.length} 条记录</p>
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
              {editingInspection ? '编辑巡检' : '新建巡检'}
            </h2>
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-400 mb-2">班次</label>
                <select
                  value={formData.shift}
                  onChange={(e) => setFormData({ ...formData, shift: e.target.value as any })}
                  className="w-full px-4 py-2 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white"
                >
                  <option value="day">白班</option>
                  <option value="night">夜班</option>
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-400 mb-2">机房名称</label>
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
                  <option value="机房A-103">机房A-103</option>
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-400 mb-2">巡检人</label>
                <select
                  value={formData.inspectorId}
                  onChange={(e) => {
                    const selectedOption = e.target.options[e.target.selectedIndex];
                    setFormData({
                      ...formData,
                      inspectorId: e.target.value,
                      inspector: selectedOption.text,
                    });
                  }}
                  className="w-full px-4 py-2 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white"
                >
                  <option value="">请选择巡检人（从值班人员中选择）</option>
                  <option value="STF-001">张三 (STF-001)</option>
                  <option value="STF-002">李四 (STF-002)</option>
                  <option value="STF-003">王五 (STF-003)</option>
                  <option value="STF-004">赵六 (STF-004)</option>
                  <option value="STF-005">孙七 (STF-005)</option>
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-400 mb-2">状态</label>
                <select
                  value={formData.status}
                  onChange={(e) => setFormData({ ...formData, status: e.target.value as any })}
                  className="w-full px-4 py-2 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white"
                >
                  <option value="pending">待处理</option>
                  <option value="processing">进行中</option>
                  <option value="completed">已完成</option>
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
            <p className="text-gray-400 mb-6">确定要删除这条巡检记录吗？此操作无法撤销。</p>
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
