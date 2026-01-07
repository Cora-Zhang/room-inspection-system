'use client';

import { useState } from 'react';
import MainLayout from '@/components/Layout/MainLayout';

interface Schedule {
  id: string;
  date: string;
  shift: 'day' | 'night';
  staffId: string;
  staffName: string;
  status: 'scheduled' | 'completed' | 'missed';
}

const shifts = [
  { key: 'day', label: '白班', time: '08:00-17:00', color: 'from-cyan-500 to-blue-600' },
  { key: 'night', label: '夜班', time: '18:00-07:00', color: 'from-purple-500 to-pink-600' },
];

export default function RosterPage() {
  const [currentDate, setCurrentDate] = useState(new Date());
  const [schedules, setSchedules] = useState<Schedule[]>([
    { id: '1', date: '2025-01-07', shift: 'day', staffId: 'STF-001', staffName: '张三', status: 'completed' },
    { id: '2', date: '2025-01-07', shift: 'night', staffId: 'STF-002', staffName: '李四', status: 'scheduled' },
    { id: '3', date: '2025-01-08', shift: 'day', staffId: 'STF-003', staffName: '王五', status: 'scheduled' },
    { id: '4', date: '2025-01-08', shift: 'night', staffId: 'STF-001', staffName: '张三', status: 'scheduled' },
  ]);

  const [showModal, setShowModal] = useState(false);
  const [selectedDate, setSelectedDate] = useState<string>('');
  const [selectedShift, setSelectedShift] = useState<keyof typeof shifts>('day');
  const [formData, setFormData] = useState({
    staffId: '',
    staffName: '',
  });

  // 获取当月天数
  const getDaysInMonth = (date: Date) => {
    return new Date(date.getFullYear(), date.getMonth() + 1, 0).getDate();
  };

  // 获取当月第一天是星期几
  const getFirstDayOfMonth = (date: Date) => {
    return new Date(date.getFullYear(), date.getMonth(), 1).getDay();
  };

  // 格式化日期
  const formatDate = (date: Date) => {
    return date.toISOString().split('T')[0];
  };

  // 获取某天的排班
  const getScheduleForDay = (date: string, shift: string) => {
    return schedules.find((s) => s.date === date && s.shift === shift);
  };

  // 上个月
  const prevMonth = () => {
    setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() - 1, 1));
  };

  // 下个月
  const nextMonth = () => {
    setCurrentDate(new Date(currentDate.getFullYear(), currentDate.getMonth() + 1, 1));
  };

  // 点击日期班次
  const handleScheduleClick = (date: string, shift: keyof typeof shifts) => {
    setSelectedDate(date);
    setSelectedShift(shift);
    const schedule = getScheduleForDay(date, shift);
    setFormData({
      staffId: schedule?.staffId || '',
      staffName: schedule?.staffName || '',
    });
    setShowModal(true);
  };

  // 保存排班
  const handleSave = () => {
    const existingIndex = schedules.findIndex(
      (s) => s.date === selectedDate && s.shift === selectedShift
    );
    if (existingIndex >= 0) {
      const updated = [...schedules];
      updated[existingIndex] = {
        ...updated[existingIndex],
        staffId: formData.staffId,
        staffName: formData.staffName,
      };
      setSchedules(updated);
    } else {
      setSchedules([
        ...schedules,
        {
          id: String(schedules.length + 1),
          date: selectedDate,
          shift: selectedShift,
          staffId: formData.staffId,
          staffName: formData.staffName,
          status: 'scheduled',
        },
      ]);
    }
    setShowModal(false);
  };

  // 清除排班
  const handleClear = () => {
    setSchedules(schedules.filter((s) => !(s.date === selectedDate && s.shift === selectedShift)));
    setShowModal(false);
  };

  // 批量排班
  const handleBatchSchedule = () => {
    // 简化实现：为整月创建轮班
    const newSchedules: Schedule[] = [];
    const daysInMonth = getDaysInMonth(currentDate);
    const staffList = [
      { id: 'STF-001', name: '张三' },
      { id: 'STF-002', name: '李四' },
      { id: 'STF-003', name: '王五' },
    ];

    for (let day = 1; day <= daysInMonth; day++) {
      const dateStr = `${currentDate.getFullYear()}-${String(currentDate.getMonth() + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
      const staffIndex = (day - 1) % staffList.length;

      shifts.forEach((shift) => {
        const existing = schedules.find(
          (s) => s.date === dateStr && s.shift === shift.key
        );
        if (!existing) {
          newSchedules.push({
            id: `batch_${dateStr}_${shift.key}`,
            date: dateStr,
            shift: shift.key,
            staffId: staffList[staffIndex].id,
            staffName: staffList[staffIndex].name,
            status: 'scheduled',
          });
        }
      });
    }

    setSchedules([...schedules, ...newSchedules]);
  };

  // 导出排班表
  const handleExport = () => {
    const csv = [
      ['日期', '班次', '时间', '值班人员', '工号', '状态'],
      ...schedules.map((s) => {
        const shiftInfo = shifts.find((sh) => sh.key === s.shift);
        return [
          s.date,
          shiftInfo?.label || '',
          shiftInfo?.time || '',
          s.staffName,
          s.staffId,
          s.status === 'scheduled' ? '已排班' : s.status === 'completed' ? '已完成' : '缺勤',
        ];
      }),
    ]
      .map((row) => row.join(','))
      .join('\n');

    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = `值班排班表_${currentDate.getFullYear()}${String(currentDate.getMonth() + 1).padStart(2, '0')}.csv`;
    link.click();
  };

  // 生成日历
  const renderCalendar = () => {
    const daysInMonth = getDaysInMonth(currentDate);
    const firstDay = getFirstDayOfMonth(currentDate);
    const days = [];

    // 空白填充
    for (let i = 0; i < firstDay; i++) {
      days.push(<div key={`empty-${i}`} className="h-32"></div>);
    }

    // 日期
    for (let day = 1; day <= daysInMonth; day++) {
      const dateStr = `${currentDate.getFullYear()}-${String(currentDate.getMonth() + 1).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
      const isToday = formatDate(new Date()) === dateStr;

      days.push (
        <div
          key={day}
          className={`h-32 border border-gray-700/50 p-2 hover:bg-gray-700/30 transition-colors cursor-pointer ${
            isToday ? 'bg-cyan-500/10 border-cyan-500/50' : ''
          }`}
        >
          <div className="text-center mb-2">
            <span className={`text-sm font-medium ${isToday ? 'text-cyan-400' : 'text-gray-300'}`}>
              {day}
            </span>
          </div>
          <div className="space-y-1">
            {shifts.map((shift) => {
              const schedule = getScheduleForDay(dateStr, shift.key);
              return (
                <div
                  key={shift.key}
                  onClick={(e) => {
                    e.stopPropagation();
                    handleScheduleClick(dateStr, shift.key);
                  }}
                  className={`p-1 rounded text-xs cursor-pointer hover:opacity-80 transition-opacity ${
                    schedule
                      ? `bg-gradient-to-r ${shift.color} text-white`
                      : 'bg-gray-700/50 text-gray-500'
                  }`}
                >
                  {schedule ? schedule.staffName : shift.label}
                </div>
              );
            })}
          </div>
        </div>
      );
    }

    return days;
  };

  return (
    <MainLayout>
      <div className="space-y-6">
        {/* 页面标题 */}
        <div>
          <h1 className="text-2xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-cyan-400 to-blue-500 mb-2">
            值班排班表
          </h1>
          <p className="text-gray-400">
            管理每日值班安排，实行7×24小时值班制度，白班08:00-17:00，夜班18:00-次日07:00。
            夜班值班人员在22:00后可睡觉休息，次日早上离开。
          </p>
        </div>

        {/* 统计卡片 */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          {[
            { label: '本月班次', value: schedules.length, color: 'from-cyan-500 to-blue-600' },
            {
              label: '已完成',
              value: schedules.filter((s) => s.status === 'completed').length,
              color: 'from-green-500 to-emerald-600',
            },
            {
              label: '已排班',
              value: schedules.filter((s) => s.status === 'scheduled').length,
              color: 'from-purple-500 to-pink-600',
            },
            {
              label: '缺勤',
              value: schedules.filter((s) => s.status === 'missed').length,
              color: 'from-red-500 to-orange-600',
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

        {/* 操作按钮栏 */}
        <div className="flex gap-4">
          <button
            onClick={handleBatchSchedule}
            className="flex items-center gap-2 px-6 py-2 bg-gradient-to-r from-cyan-500 to-blue-600 rounded-lg text-white font-medium hover:from-cyan-400 hover:to-blue-500 transition-all"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
            </svg>
            自动排班
          </button>
          <button
            onClick={handleExport}
            className="flex items-center gap-2 px-6 py-2 bg-gradient-to-r from-purple-500 to-pink-600 rounded-lg text-white font-medium hover:from-purple-400 hover:to-pink-500 transition-all"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
            </svg>
            导出排班表
          </button>
        </div>

        {/* 月份导航 */}
        <div
          className="flex items-center justify-between p-4 rounded-xl bg-gradient-to-br from-gray-800/50 to-gray-900/50 border border-cyan-500/20 backdrop-blur-sm"
          style={{
            boxShadow: '0 4px 30px rgba(6, 182, 212, 0.1)',
          }}
        >
          <button
            onClick={prevMonth}
            className="p-2 rounded-lg hover:bg-cyan-500/10 text-cyan-400 hover:text-cyan-300 transition-colors"
          >
            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
            </svg>
          </button>
          <h2 className="text-xl font-bold text-white">
            {currentDate.getFullYear()}年{currentDate.getMonth() + 1}月
          </h2>
          <button
            onClick={nextMonth}
            className="p-2 rounded-lg hover:bg-cyan-500/10 text-cyan-400 hover:text-cyan-300 transition-colors"
          >
            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
            </svg>
          </button>
        </div>

        {/* 星期表头 */}
        <div className="grid grid-cols-7 gap-px bg-gray-700/50 rounded-t-lg overflow-hidden">
          {['周日', '周一', '周二', '周三', '周四', '周五', '周六'].map((day) => (
            <div key={day} className="bg-gray-800/50 p-2 text-center text-sm font-medium text-gray-400">
              {day}
            </div>
          ))}
        </div>

        {/* 日历 */}
        <div
          className="grid grid-cols-7 gap-px bg-gray-700/50 rounded-b-lg overflow-hidden"
          style={{
            boxShadow: '0 4px 30px rgba(6, 182, 212, 0.1)',
          }}
        >
          {renderCalendar()}
        </div>

        {/* 班次说明 */}
        <div
          className="flex items-center justify-center gap-6 p-4 rounded-xl bg-gradient-to-br from-gray-800/50 to-gray-900/50 border border-cyan-500/20 backdrop-blur-sm"
          style={{
            boxShadow: '0 4px 30px rgba(6, 182, 212, 0.1)',
          }}
        >
          {shifts.map((shift) => (
            <div key={shift.key} className="flex items-center gap-2">
              <div className={`w-4 h-4 rounded bg-gradient-to-r ${shift.color}`}></div>
              <span className="text-sm text-gray-400">
                {shift.label} ({shift.time})
              </span>
            </div>
          ))}
        </div>
      </div>

      {/* 排班模态框 */}
      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center" style={{ backgroundColor: 'rgba(0,0,0,0.7)' }}>
          <div
            className="relative rounded-2xl bg-gradient-to-br from-gray-800 to-gray-900 border border-cyan-500/30 p-6 w-full max-w-md"
            style={{
              boxShadow: '0 0 60px rgba(6, 182, 212, 0.2)',
            }}
          >
            <h2 className="text-xl font-bold text-white mb-6">
              设置值班人员
            </h2>
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-400 mb-2">日期</label>
                <input
                  type="text"
                  value={selectedDate}
                  disabled
                  className="w-full px-4 py-2 bg-gray-900/50 border border-gray-700 rounded-lg text-gray-300 disabled:opacity-50"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-400 mb-2">班次</label>
                <input
                  type="text"
                  value={shifts.find((s) => s.key === selectedShift)?.label}
                  disabled
                  className="w-full px-4 py-2 bg-gray-900/50 border border-gray-700 rounded-lg text-gray-300 disabled:opacity-50"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-400 mb-2">值班人员</label>
                <select
                  value={formData.staffId}
                  onChange={(e) => {
                    const selectedOption = e.target.options[e.target.selectedIndex];
                    setFormData({
                      staffId: e.target.value,
                      staffName: selectedOption.text,
                    });
                  }}
                  className="w-full px-4 py-2 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white"
                >
                  <option value="">请选择值班人员</option>
                  <option value="STF-001">张三 (STF-001)</option>
                  <option value="STF-002">李四 (STF-002)</option>
                  <option value="STF-003">王五 (STF-003)</option>
                  <option value="STF-004">赵六 (STF-004)</option>
                  <option value="STF-005">孙七 (STF-005)</option>
                </select>
              </div>
            </div>
            <div className="flex gap-4 mt-6">
              <button
                onClick={handleClear}
                className="flex-1 px-4 py-2 bg-red-500/20 text-red-400 rounded-lg hover:bg-red-500/30 transition-colors"
              >
                清除排班
              </button>
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
    </MainLayout>
  );
}
