'use client';

import { useState } from 'react';
import MainLayout from '@/components/Layout/MainLayout';

interface Handover {
  id: string;
  date: string;
  shift: 'morning' | 'afternoon' | 'night';
  outgoingStaff: string;
  incomingStaff: string;
  outgoingTime: string;
  incomingTime: string;
  tasks: string[];
  issues: string[];
  equipmentStatus: string;
  notes: string;
}

const shifts = [
  { key: 'morning', label: '早班', time: '08:00-16:00' },
  { key: 'afternoon', label: '中班', time: '16:00-00:00' },
  { key: 'night', label: '晚班', time: '00:00-08:00' },
];

export default function HandoverPage() {
  const [handovers, setHandovers] = useState<Handover[]>([
    {
      id: 'HND-001',
      date: '2025-01-07',
      shift: 'morning',
      outgoingStaff: '张三',
      incomingStaff: '李四',
      outgoingTime: '16:00',
      incomingTime: '16:05',
      tasks: ['完成机房A-101巡检', '完成机房A-102巡检', '更新告警记录'],
      issues: ['机房B-203温度偏高，已通知', '服务器DEV-005需要维护'],
      equipmentStatus: '设备运行正常，无异常',
      notes: '交接完成，一切正常',
    },
    {
      id: 'HND-002',
      date: '2025-01-06',
      shift: 'night',
      outgoingStaff: '赵六',
      incomingStaff: '张三',
      outgoingTime: '08:00',
      incomingTime: '08:02',
      tasks: ['完成夜间巡检', '监控告警', '处理紧急事件2起'],
      issues: [],
      equipmentStatus: '夜间运行稳定',
      notes: '交接顺利',
    },
  ]);

  const [searchTerm, setSearchTerm] = useState('');
  const [shiftFilter, setShiftFilter] = useState('all');
  const [dateRange, setDateRange] = useState({ start: '', end: '' });
  const [showModal, setShowModal] = useState(false);
  const [editingHandover, setEditingHandover] = useState<Handover | null>(null);
  const [showDetailModal, setShowDetailModal] = useState(false);
  const [selectedHandover, setSelectedHandover] = useState<Handover | null>(null);
  const [formData, setFormData] = useState({
    date: '',
    shift: 'morning' as 'morning' | 'afternoon' | 'night',
    outgoingStaff: '',
    incomingStaff: '',
    outgoingTime: '',
    incomingTime: '',
    equipmentStatus: '',
    notes: '',
  });
  const [tasks, setTasks] = useState<string[]>([]);
  const [issues, setIssues] = useState<string[]>([]);
  const [newTask, setNewTask] = useState('');
  const [newIssue, setNewIssue] = useState('');

  const filteredHandovers = handovers.filter((item) => {
    const matchSearch =
      item.id.toLowerCase().includes(searchTerm.toLowerCase()) ||
      item.outgoingStaff.includes(searchTerm) ||
      item.incomingStaff.includes(searchTerm);
    const matchShift = shiftFilter === 'all' || item.shift === shiftFilter;
    const matchDateStart = !dateRange.start || item.date >= dateRange.start;
    const matchDateEnd = !dateRange.end || item.date <= dateRange.end;
    return matchSearch && matchShift && matchDateStart && matchDateEnd;
  });

  const handleAdd = () => {
    setEditingHandover(null);
    setFormData({
      date: new Date().toISOString().split('T')[0],
      shift: 'morning',
      outgoingStaff: '',
      incomingStaff: '',
      outgoingTime: '',
      incomingTime: '',
      equipmentStatus: '',
      notes: '',
    });
    setTasks([]);
    setIssues([]);
    setShowModal(true);
  };

  const handleEdit = (handover: Handover) => {
    setEditingHandover(handover);
    setFormData({
      date: handover.date,
      shift: handover.shift,
      outgoingStaff: handover.outgoingStaff,
      incomingStaff: handover.incomingStaff,
      outgoingTime: handover.outgoingTime,
      incomingTime: handover.incomingTime,
      equipmentStatus: handover.equipmentStatus,
      notes: handover.notes,
    });
    setTasks([...handover.tasks]);
    setIssues([...handover.issues]);
    setShowModal(true);
  };

  const handleView = (handover: Handover) => {
    setSelectedHandover(handover);
    setShowDetailModal(true);
  };

  const handleSave = () => {
    if (editingHandover) {
      setHandovers(
        handovers.map((item) =>
          item.id === editingHandover.id
            ? { ...item, ...formData, tasks, issues }
            : item
        )
      );
    } else {
      const newId = `HND-${String(handovers.length + 1).padStart(3, '0')}`;
      setHandovers([
        ...handovers,
        { ...formData, id: newId, tasks, issues },
      ]);
    }
    setShowModal(false);
  };

  const handleAddTask = () => {
    if (newTask.trim()) {
      setTasks([...tasks, newTask.trim()]);
      setNewTask('');
    }
  };

  const handleRemoveTask = (index: number) => {
    setTasks(tasks.filter((_, i) => i !== index));
  };

  const handleAddIssue = () => {
    if (newIssue.trim()) {
      setIssues([...issues, newIssue.trim()]);
      setNewIssue('');
    }
  };

  const handleRemoveIssue = (index: number) => {
    setIssues(issues.filter((_, i) => i !== index));
  };

  const handleExport = () => {
    const csv = [
      ['交接ID', '日期', '班次', '交班人员', '接班人员', '交班时间', '接班时间', '任务数', '问题数', '备注'],
      ...filteredHandovers.map((h) => {
        const shiftInfo = shifts.find((s) => s.key === h.shift);
        return [
          h.id,
          h.date,
          shiftInfo?.label || '',
          h.outgoingStaff,
          h.incomingStaff,
          h.outgoingTime,
          h.incomingTime,
          h.tasks.length,
          h.issues.length,
          h.notes,
        ];
      }),
    ]
      .map((row) => row.join(','))
      .join('\n');

    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = `值班交接记录_${new Date().toISOString().split('T')[0]}.csv`;
    link.click();
  };

  const getShiftClass = (shift: string) => {
    switch (shift) {
      case 'morning':
        return 'bg-cyan-500/20 text-cyan-400 border border-cyan-500/30';
      case 'afternoon':
        return 'bg-purple-500/20 text-purple-400 border border-purple-500/30';
      case 'night':
        return 'bg-indigo-500/20 text-indigo-400 border border-indigo-500/30';
      default:
        return 'bg-gray-500/20 text-gray-400 border border-gray-500/30';
    }
  };

  const getShiftText = (shift: string) => {
    return shifts.find((s) => s.key === shift)?.label || '未知';
  };

  return (
    <MainLayout>
      <div className="space-y-6">
        {/* 页面标题 */}
        <div>
          <h1 className="text-2xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-cyan-400 to-blue-500 mb-2">
            值班交接记录
          </h1>
          <p className="text-gray-400">记录值班交接内容、任务完成情况和异常信息</p>
        </div>

        {/* 统计卡片 */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          {[
            { label: '交接总数', value: handovers.length, color: 'from-cyan-500 to-blue-600' },
            {
              label: '本月交接',
              value: handovers.filter((h) => h.date.startsWith(new Date().toISOString().slice(0, 7))).length,
              color: 'from-green-500 to-emerald-600',
            },
            {
              label: '待处理问题',
              value: handovers.reduce((sum, h) => sum + h.issues.length, 0),
              color: 'from-orange-500 to-red-600',
            },
            {
              label: '完成任务',
              value: handovers.reduce((sum, h) => sum + h.tasks.length, 0),
              color: 'from-purple-500 to-pink-600',
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
                placeholder="搜索交接记录（ID/交班人/接班人）..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full px-4 py-2 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white placeholder-gray-500"
              />
            </div>
            <select
              value={shiftFilter}
              onChange={(e) => setShiftFilter(e.target.value)}
              className="px-4 py-2 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white"
            >
              <option value="all">全部班次</option>
              <option value="morning">早班</option>
              <option value="afternoon">中班</option>
              <option value="night">晚班</option>
            </select>
            <input
              type="date"
              value={dateRange.start}
              onChange={(e) => setDateRange({ ...dateRange, start: e.target.value })}
              className="px-4 py-2 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white"
            />
            <input
              type="date"
              value={dateRange.end}
              onChange={(e) => setDateRange({ ...dateRange, end: e.target.value })}
              className="px-4 py-2 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white"
            />
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
            新建交接
          </button>
          <button
            onClick={handleExport}
            className="flex items-center gap-2 px-6 py-2 bg-gradient-to-r from-purple-500 to-pink-600 rounded-lg text-white font-medium hover:from-purple-400 hover:to-pink-500 transition-all"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
            </svg>
            导出记录
          </button>
        </div>

        {/* 交接记录列表 */}
        <div
          className="relative rounded-2xl bg-gradient-to-br from-gray-800/50 to-gray-900/50 border border-cyan-500/20 backdrop-blur-sm overflow-hidden"
          style={{
            boxShadow: '0 4px 30px rgba(6, 182, 212, 0.1)',
          }}
        >
          <table className="w-full">
            <thead>
              <tr className="border-b border-gray-700">
                <th className="px-6 py-4 text-left text-sm font-medium text-gray-400">交接ID</th>
                <th className="px-6 py-4 text-left text-sm font-medium text-gray-400">日期</th>
                <th className="px-6 py-4 text-left text-sm font-medium text-gray-400">班次</th>
                <th className="px-6 py-4 text-left text-sm font-medium text-gray-400">交班人</th>
                <th className="px-6 py-4 text-left text-sm font-medium text-gray-400">接班人</th>
                <th className="px-6 py-4 text-left text-sm font-medium text-gray-400">任务数</th>
                <th className="px-6 py-4 text-left text-sm font-medium text-gray-400">问题数</th>
                <th className="px-6 py-4 text-left text-sm font-medium text-gray-400">备注</th>
                <th className="px-6 py-4 text-left text-sm font-medium text-gray-400">操作</th>
              </tr>
            </thead>
            <tbody>
              {filteredHandovers.map((item) => (
                <tr key={item.id} className="border-b border-gray-700/50 hover:bg-gray-700/30 transition-colors">
                  <td className="px-6 py-4 text-sm text-white font-mono">{item.id}</td>
                  <td className="px-6 py-4 text-sm text-gray-300">{item.date}</td>
                  <td className="px-6 py-4">
                    <span className={`inline-flex px-3 py-1 text-xs rounded-full ${getShiftClass(item.shift)}`}>
                      {getShiftText(item.shift)}
                    </span>
                  </td>
                  <td className="px-6 py-4 text-sm text-white">{item.outgoingStaff}</td>
                  <td className="px-6 py-4 text-sm text-white">{item.incomingStaff}</td>
                  <td className="px-6 py-4">
                    <span className="inline-flex px-2 py-1 text-xs rounded-full bg-green-500/20 text-green-400 border border-green-500/30">
                      {item.tasks.length}
                    </span>
                  </td>
                  <td className="px-6 py-4">
                    {item.issues.length > 0 ? (
                      <span className="inline-flex px-2 py-1 text-xs rounded-full bg-red-500/20 text-red-400 border border-red-500/30">
                        {item.issues.length}
                      </span>
                    ) : (
                      <span className="inline-flex px-2 py-1 text-xs rounded-full bg-gray-500/20 text-gray-400 border border-gray-500/30">
                        0
                      </span>
                    )}
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-400 truncate max-w-xs">{item.notes}</td>
                  <td className="px-6 py-4">
                    <div className="flex gap-2">
                      <button
                        onClick={() => handleView(item)}
                        className="text-cyan-400 hover:text-cyan-300 text-sm font-medium transition-colors"
                      >
                        详情
                      </button>
                      <button
                        onClick={() => handleEdit(item)}
                        className="text-blue-400 hover:text-blue-300 text-sm font-medium transition-colors"
                      >
                        编辑
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
              {filteredHandovers.length === 0 && (
                <tr>
                  <td colSpan={9} className="px-6 py-12 text-center text-gray-400">
                    暂无数据
                  </td>
                </tr>
              )}
            </tbody>
          </table>

          {/* 分页 */}
          <div className="px-6 py-4 border-t border-gray-700 flex items-center justify-between">
            <p className="text-sm text-gray-400">共 {filteredHandovers.length} 条记录</p>
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
            className="relative rounded-2xl bg-gradient-to-br from-gray-800 to-gray-900 border border-cyan-500/30 p-6 w-full max-w-2xl max-h-[90vh] overflow-y-auto"
            style={{
              boxShadow: '0 0 60px rgba(6, 182, 212, 0.2)',
            }}
          >
            <h2 className="text-xl font-bold text-white mb-6">
              {editingHandover ? '编辑交接记录' : '新建交接记录'}
            </h2>
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-400 mb-2">日期</label>
                  <input
                    type="date"
                    value={formData.date}
                    onChange={(e) => setFormData({ ...formData, date: e.target.value })}
                    className="w-full px-4 py-2 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-400 mb-2">班次</label>
                  <select
                    value={formData.shift}
                    onChange={(e) => setFormData({ ...formData, shift: e.target.value as any })}
                    className="w-full px-4 py-2 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white"
                  >
                    <option value="morning">早班</option>
                    <option value="afternoon">中班</option>
                    <option value="night">晚班</option>
                  </select>
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-400 mb-2">交班人</label>
                  <select
                    value={formData.outgoingStaff}
                    onChange={(e) => setFormData({ ...formData, outgoingStaff: e.target.value })}
                    className="w-full px-4 py-2 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white"
                  >
                    <option value="">请选择交班人</option>
                    <option value="张三">张三</option>
                    <option value="李四">李四</option>
                    <option value="王五">王五</option>
                    <option value="赵六">赵六</option>
                    <option value="孙七">孙七</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-400 mb-2">接班人</label>
                  <select
                    value={formData.incomingStaff}
                    onChange={(e) => setFormData({ ...formData, incomingStaff: e.target.value })}
                    className="w-full px-4 py-2 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white"
                  >
                    <option value="">请选择接班人</option>
                    <option value="张三">张三</option>
                    <option value="李四">李四</option>
                    <option value="王五">王五</option>
                    <option value="赵六">赵六</option>
                    <option value="孙七">孙七</option>
                  </select>
                </div>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-400 mb-2">交班时间</label>
                  <input
                    type="time"
                    value={formData.outgoingTime}
                    onChange={(e) => setFormData({ ...formData, outgoingTime: e.target.value })}
                    className="w-full px-4 py-2 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-400 mb-2">接班时间</label>
                  <input
                    type="time"
                    value={formData.incomingTime}
                    onChange={(e) => setFormData({ ...formData, incomingTime: e.target.value })}
                    className="w-full px-4 py-2 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white"
                  />
                </div>
              </div>

              {/* 任务列表 */}
              <div>
                <label className="block text-sm font-medium text-gray-400 mb-2">完成任务</label>
                <div className="flex gap-2 mb-2">
                  <input
                    type="text"
                    value={newTask}
                    onChange={(e) => setNewTask(e.target.value)}
                    placeholder="输入任务内容"
                    onKeyPress={(e) => e.key === 'Enter' && handleAddTask()}
                    className="flex-1 px-4 py-2 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white placeholder-gray-500"
                  />
                  <button
                    onClick={handleAddTask}
                    className="px-4 py-2 bg-cyan-500/20 text-cyan-400 rounded-lg hover:bg-cyan-500/30 transition-colors"
                  >
                    添加
                  </button>
                </div>
                <div className="space-y-2">
                  {tasks.map((task, index) => (
                    <div
                      key={index}
                      className="flex items-center justify-between p-2 bg-gray-900/50 rounded-lg"
                    >
                      <span className="text-sm text-gray-300">{task}</span>
                      <button
                        onClick={() => handleRemoveTask(index)}
                        className="text-red-400 hover:text-red-300 text-sm"
                      >
                        删除
                      </button>
                    </div>
                  ))}
                </div>
              </div>

              {/* 问题列表 */}
              <div>
                <label className="block text-sm font-medium text-gray-400 mb-2">待处理问题</label>
                <div className="flex gap-2 mb-2">
                  <input
                    type="text"
                    value={newIssue}
                    onChange={(e) => setNewIssue(e.target.value)}
                    placeholder="输入问题内容"
                    onKeyPress={(e) => e.key === 'Enter' && handleAddIssue()}
                    className="flex-1 px-4 py-2 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white placeholder-gray-500"
                  />
                  <button
                    onClick={handleAddIssue}
                    className="px-4 py-2 bg-orange-500/20 text-orange-400 rounded-lg hover:bg-orange-500/30 transition-colors"
                  >
                    添加
                  </button>
                </div>
                <div className="space-y-2">
                  {issues.map((issue, index) => (
                    <div
                      key={index}
                      className="flex items-center justify-between p-2 bg-gray-900/50 rounded-lg"
                    >
                      <span className="text-sm text-orange-300">{issue}</span>
                      <button
                        onClick={() => handleRemoveIssue(index)}
                        className="text-red-400 hover:text-red-300 text-sm"
                      >
                        删除
                      </button>
                    </div>
                  ))}
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-400 mb-2">设备状态</label>
                <textarea
                  value={formData.equipmentStatus}
                  onChange={(e) => setFormData({ ...formData, equipmentStatus: e.target.value })}
                  placeholder="描述设备运行状态"
                  rows={2}
                  className="w-full px-4 py-2 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white placeholder-gray-500 resize-none"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-400 mb-2">备注</label>
                <textarea
                  value={formData.notes}
                  onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
                  placeholder="其他需要说明的内容"
                  rows={2}
                  className="w-full px-4 py-2 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white placeholder-gray-500 resize-none"
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

      {/* 详情模态框 */}
      {showDetailModal && selectedHandover && (
        <div className="fixed inset-0 z-50 flex items-center justify-center" style={{ backgroundColor: 'rgba(0,0,0,0.7)' }}>
          <div
            className="relative rounded-2xl bg-gradient-to-br from-gray-800 to-gray-900 border border-cyan-500/30 p-6 w-full max-w-3xl max-h-[90vh] overflow-y-auto"
            style={{
              boxShadow: '0 0 60px rgba(6, 182, 212, 0.2)',
            }}
          >
            <h2 className="text-xl font-bold text-white mb-6">交接详情</h2>
            <div className="space-y-6">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="text-sm text-gray-400">交接ID</label>
                  <p className="text-white font-mono">{selectedHandover.id}</p>
                </div>
                <div>
                  <label className="text-sm text-gray-400">日期</label>
                  <p className="text-white">{selectedHandover.date}</p>
                </div>
                <div>
                  <label className="text-sm text-gray-400">班次</label>
                  <span className={`inline-flex px-3 py-1 text-xs rounded-full ${getShiftClass(selectedHandover.shift)}`}>
                    {getShiftText(selectedHandover.shift)}
                  </span>
                </div>
                <div>
                  <label className="text-sm text-gray-400">交接时间</label>
                  <p className="text-white">{selectedHandover.outgoingTime} → {selectedHandover.incomingTime}</p>
                </div>
                <div>
                  <label className="text-sm text-gray-400">交班人</label>
                  <p className="text-white">{selectedHandover.outgoingStaff}</p>
                </div>
                <div>
                  <label className="text-sm text-gray-400">接班人</label>
                  <p className="text-white">{selectedHandover.incomingStaff}</p>
                </div>
              </div>

              <div>
                <label className="text-sm text-gray-400 mb-2 block">完成任务</label>
                <div className="space-y-2">
                  {selectedHandover.tasks.map((task, index) => (
                    <div key={index} className="p-3 bg-green-500/10 border border-green-500/30 rounded-lg">
                      <span className="text-green-400">✓</span> <span className="text-gray-300">{task}</span>
                    </div>
                  ))}
                  {selectedHandover.tasks.length === 0 && (
                    <p className="text-gray-500">无完成任务</p>
                  )}
                </div>
              </div>

              <div>
                <label className="text-sm text-gray-400 mb-2 block">待处理问题</label>
                <div className="space-y-2">
                  {selectedHandover.issues.map((issue, index) => (
                    <div key={index} className="p-3 bg-red-500/10 border border-red-500/30 rounded-lg">
                      <span className="text-red-400">!</span> <span className="text-gray-300">{issue}</span>
                    </div>
                  ))}
                  {selectedHandover.issues.length === 0 && (
                    <p className="text-gray-500">无待处理问题</p>
                  )}
                </div>
              </div>

              <div>
                <label className="text-sm text-gray-400 mb-2 block">设备状态</label>
                <p className="text-gray-300">{selectedHandover.equipmentStatus || '无记录'}</p>
              </div>

              <div>
                <label className="text-sm text-gray-400 mb-2 block">备注</label>
                <p className="text-gray-300">{selectedHandover.notes || '无备注'}</p>
              </div>
            </div>
            <div className="mt-6">
              <button
                onClick={() => {
                  setShowDetailModal(false);
                  setSelectedHandover(null);
                }}
                className="w-full px-4 py-2 bg-gradient-to-r from-cyan-500 to-blue-600 text-white rounded-lg hover:from-cyan-400 hover:to-blue-500 transition-all"
              >
                关闭
              </button>
            </div>
          </div>
        </div>
      )}
    </MainLayout>
  );
}
