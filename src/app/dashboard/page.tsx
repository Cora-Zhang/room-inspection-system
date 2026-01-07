'use client';

import { useState, useEffect } from 'react';

export default function DashboardPage() {
  const [currentTime, setCurrentTime] = useState(new Date());
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
    const timer = setInterval(() => {
      setCurrentTime(new Date());
    }, 1000);
    return () => clearInterval(timer);
  }, []);

  const formatTime = (date: Date) => {
    return date.toLocaleTimeString('zh-CN', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: false,
    });
  };

  const stats = [
    {
      title: '机房总数',
      value: 12,
      unit: '个',
      icon: 'M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4',
      color: 'from-cyan-500 to-blue-600',
      trend: '+2',
    },
    {
      title: '设备总数',
      value: 328,
      unit: '台',
      icon: 'M9 3v2m6-2v2M9 19v2m6-2v2M5 9H3m2 6H3m18-6h-2m2 6h-2M7 19h10a2 2 0 002-2V7a2 2 0 00-2-2H7a2 2 0 00-2 2v10a2 2 0 002 2zM9 9h6v6H9V9z',
      color: 'from-purple-500 to-pink-600',
      trend: '+15',
    },
    {
      title: '今日巡检',
      value: 24,
      unit: '次',
      icon: 'M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z',
      color: 'from-green-500 to-teal-600',
      trend: '+8',
    },
    {
      title: '告警数量',
      value: 5,
      unit: '条',
      icon: 'M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z',
      color: 'from-red-500 to-orange-600',
      trend: '-3',
    },
  ];

  const recentInspections = [
    { id: 1, room: '机房A-01', inspector: '张三', time: '10:30', status: 'normal' },
    { id: 2, room: '机房B-03', inspector: '李四', time: '09:45', status: 'warning' },
    { id: 3, room: '机房C-02', inspector: '王五', time: '09:15', status: 'normal' },
    { id: 4, room: '机房A-02', inspector: '赵六', time: '08:50', status: 'normal' },
    { id: 5, room: '机房B-01', inspector: '钱七', time: '08:20', status: 'error' },
  ];

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'normal':
        return 'bg-green-500/20 text-green-400 border-green-500/30';
      case 'warning':
        return 'bg-yellow-500/20 text-yellow-400 border-yellow-500/30';
      case 'error':
        return 'bg-red-500/20 text-red-400 border-red-500/30';
      default:
        return 'bg-gray-500/20 text-gray-400 border-gray-500/30';
    }
  };

  const getStatusText = (status: string) => {
    switch (status) {
      case 'normal':
        return '正常';
      case 'warning':
        return '警告';
      case 'error':
        return '异常';
      default:
        return '未知';
    }
  };

  return (
    <div className="min-h-screen">
      {/* 顶部标题栏 */}
      <div className="mb-6">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-white mb-2">数据中心看板</h1>
            <p className="text-gray-400 text-sm">实时监控机房运行状态</p>
          </div>
          <div className="flex items-center gap-6">
            <div className="text-right">
              {mounted && (
                <div className="text-4xl font-mono font-bold text-cyan-400 mb-1">
                  {formatTime(currentTime)}
                </div>
              )}
              {mounted && (
                <div className="text-xs text-gray-400">
                  {currentTime.toLocaleDateString('zh-CN', {
                    year: 'numeric',
                    month: '2-digit',
                    day: '2-digit',
                    weekday: 'long',
                  })}
                </div>
              )}
            </div>
            <div className="flex items-center gap-2 px-4 py-2 rounded-lg bg-green-500/10 border border-green-500/30">
              <div className="w-2 h-2 rounded-full bg-green-400 animate-pulse shadow-[0_0_8px_rgba(74,222,128,0.8)]"></div>
              <span className="text-green-400 text-sm font-medium">系统运行正常</span>
            </div>
          </div>
        </div>
      </div>

      {/* 统计卡片 */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-6">
        {stats.map((stat, index) => (
          <div
            key={index}
            className="relative rounded-xl bg-gradient-to-br from-gray-800/80 to-gray-900/80 backdrop-blur-xl border border-cyan-500/30 p-6 overflow-hidden group hover:scale-105 transition-transform duration-300"
            style={{
              boxShadow: '0 0 40px rgba(6, 182, 212, 0.1)',
            }}
          >
            {/* 背景光效 */}
            <div
              className={`absolute inset-0 bg-gradient-to-br ${stat.color} opacity-0 group-hover:opacity-10 transition-opacity duration-300`}
            ></div>

            {/* 内容 */}
            <div className="relative z-10">
              <div className="flex items-center justify-between mb-4">
                <div
                  className={`w-12 h-12 rounded-xl bg-gradient-to-br ${stat.color} flex items-center justify-center shadow-lg shadow-cyan-500/30`}
                >
                  <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d={stat.icon} />
                  </svg>
                </div>
                <div className={`text-sm font-medium ${stat.trend.startsWith('+') ? 'text-green-400' : 'text-red-400'}`}>
                  {stat.trend}
                </div>
              </div>
              <h3 className="text-gray-400 text-sm mb-2">{stat.title}</h3>
              <div className="flex items-baseline gap-1">
                <span className="text-3xl font-bold text-white">{stat.value}</span>
                <span className="text-gray-400 text-sm">{stat.unit}</span>
              </div>
            </div>

            {/* 底部光效线 */}
            <div
              className={`absolute bottom-0 left-0 right-0 h-1 bg-gradient-to-r ${stat.color} opacity-50`}
            ></div>
          </div>
        ))}
      </div>

      {/* 主内容区 */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* 左侧：巡检记录 */}
        <div className="lg:col-span-2">
          <div
            className="rounded-xl bg-gradient-to-br from-gray-800/80 to-gray-900/80 backdrop-blur-xl border border-cyan-500/30 p-6"
            style={{
              boxShadow: '0 0 40px rgba(6, 182, 212, 0.1)',
            }}
          >
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-xl font-bold text-white flex items-center gap-2">
                <div className="w-1 h-6 rounded-full bg-gradient-to-b from-cyan-500 to-blue-600"></div>
                最近巡检记录
              </h2>
              <button className="px-4 py-2 rounded-lg bg-cyan-500/10 text-cyan-400 hover:bg-cyan-500/20 transition-colors text-sm">
                查看全部
              </button>
            </div>

            <div className="space-y-3">
              {recentInspections.map((item) => (
                <div
                  key={item.id}
                  className="flex items-center justify-between p-4 rounded-lg bg-gray-900/50 border border-gray-700/50 hover:border-cyan-500/30 transition-colors"
                >
                  <div className="flex items-center gap-4">
                    <div className="w-10 h-10 rounded-lg bg-gradient-to-br from-cyan-500/20 to-blue-600/20 flex items-center justify-center">
                      <svg className="w-5 h-5 text-cyan-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4" />
                      </svg>
                    </div>
                    <div>
                      <p className="text-white font-medium">{item.room}</p>
                      <p className="text-gray-400 text-sm">巡检人：{item.inspector}</p>
                    </div>
                  </div>
                  <div className="flex items-center gap-4">
                    <span className="text-gray-400 text-sm">{item.time}</span>
                    <span
                      className={`px-3 py-1 rounded-full text-xs font-medium border ${getStatusColor(item.status)}`}
                    >
                      {getStatusText(item.status)}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* 右侧：告警信息 */}
        <div className="lg:col-span-1">
          <div
            className="rounded-xl bg-gradient-to-br from-gray-800/80 to-gray-900/80 backdrop-blur-xl border border-cyan-500/30 p-6"
            style={{
              boxShadow: '0 0 40px rgba(6, 182, 212, 0.1)',
            }}
          >
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-xl font-bold text-white flex items-center gap-2">
                <div className="w-1 h-6 rounded-full bg-gradient-to-b from-red-500 to-orange-600"></div>
                实时告警
              </h2>
              <div className="relative">
                <svg className="w-6 h-6 text-red-400 animate-pulse" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
                </svg>
                <span className="absolute -top-1 -right-1 w-4 h-4 bg-red-500 rounded-full text-white text-xs flex items-center justify-center">
                  5
                </span>
              </div>
            </div>

            <div className="space-y-3">
              {[
                { title: '温度过高', room: '机房B-01', time: '2分钟前', level: 'error' },
                { title: '湿度异常', room: '机房A-02', time: '15分钟前', level: 'warning' },
                { title: '网络中断', room: '机房C-01', time: '30分钟前', level: 'error' },
                { title: '设备离线', room: '机房A-03', time: '1小时前', level: 'warning' },
                { title: '电压波动', room: '机房B-02', time: '2小时前', level: 'warning' },
              ].map((alert, index) => (
                <div
                  key={index}
                  className="p-4 rounded-lg bg-gray-900/50 border border-gray-700/50 hover:border-red-500/30 transition-colors"
                >
                  <div className="flex items-start justify-between mb-2">
                    <p className="text-white font-medium">{alert.title}</p>
                    <div
                      className={`w-2 h-2 rounded-full ${
                        alert.level === 'error'
                          ? 'bg-red-500 animate-pulse'
                          : 'bg-yellow-500'
                      }`}
                    ></div>
                  </div>
                  <p className="text-gray-400 text-sm mb-1">{alert.room}</p>
                  <p className="text-gray-500 text-xs">{alert.time}</p>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* 底部图表区域 */}
      <div className="mt-6 grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* 巡检趋势 */}
        <div
          className="rounded-xl bg-gradient-to-br from-gray-800/80 to-gray-900/80 backdrop-blur-xl border border-cyan-500/30 p-6"
          style={{
            boxShadow: '0 0 40px rgba(6, 182, 212, 0.1)',
          }}
        >
          <h2 className="text-xl font-bold text-white mb-6 flex items-center gap-2">
            <div className="w-1 h-6 rounded-full bg-gradient-to-b from-purple-500 to-pink-600"></div>
            巡检趋势分析
          </h2>
          <div className="h-64 flex items-end justify-around gap-2 px-4">
            {[65, 80, 45, 90, 70, 85, 60, 75, 95, 80, 70, 85].map((value, index) => (
              <div key={index} className="flex flex-col items-center gap-2 flex-1">
                <div className="w-full relative group">
                  <div
                    className="w-full rounded-t-lg bg-gradient-to-t from-cyan-500 to-blue-500 transition-all duration-300 group-hover:from-cyan-400 group-hover:to-blue-400"
                    style={{
                      height: `${value}%`,
                      boxShadow: '0 0 20px rgba(6, 182, 212, 0.3)',
                    }}
                  ></div>
                  <div className="absolute inset-0 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
                    <span className="text-white text-xs font-bold bg-black/50 px-2 py-1 rounded">
                      {value}%
                    </span>
                  </div>
                </div>
                <span className="text-gray-400 text-xs">
                  {index + 1}月
                </span>
              </div>
            ))}
          </div>
        </div>

        {/* 设备状态分布 */}
        <div
          className="rounded-xl bg-gradient-to-br from-gray-800/80 to-gray-900/80 backdrop-blur-xl border border-cyan-500/30 p-6"
          style={{
            boxShadow: '0 0 40px rgba(6, 182, 212, 0.1)',
          }}
        >
          <h2 className="text-xl font-bold text-white mb-6 flex items-center gap-2">
            <div className="w-1 h-6 rounded-full bg-gradient-to-b from-green-500 to-teal-600"></div>
            设备状态分布
          </h2>
          <div className="flex items-center justify-center gap-8 h-64">
            {/* 中心圆 */}
            <div className="relative w-48 h-48">
              {/* 外圈 */}
              <div className="absolute inset-0 rounded-full border-4 border-gray-700/30"></div>
              {/* 绿色正常 */}
              <div className="absolute inset-0 rounded-full border-4 border-green-500/50 border-t-transparent border-l-transparent" style={{ transform: 'rotate(0deg)' }}></div>
              {/* 黄色警告 */}
              <div className="absolute inset-0 rounded-full border-4 border-yellow-500/50 border-t-transparent border-l-transparent" style={{ transform: 'rotate(216deg)' }}></div>
              {/* 红色异常 */}
              <div className="absolute inset-0 rounded-full border-4 border-red-500/50 border-t-transparent border-l-transparent" style={{ transform: 'rotate(288deg)' }}></div>
              {/* 中心内容 */}
              <div className="absolute inset-0 flex flex-col items-center justify-center">
                <span className="text-3xl font-bold text-white">328</span>
                <span className="text-sm text-gray-400">设备总数</span>
              </div>
            </div>

            {/* 图例 */}
            <div className="space-y-4">
              <div className="flex items-center gap-2">
                <div className="w-3 h-3 rounded-full bg-green-500 shadow-[0_0_8px_rgba(34,197,94,0.8)]"></div>
                <span className="text-gray-400 text-sm">正常 85%</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="w-3 h-3 rounded-full bg-yellow-500 shadow-[0_0_8px_rgba(234,179,8,0.8)]"></div>
                <span className="text-gray-400 text-sm">警告 12%</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="w-3 h-3 rounded-full bg-red-500 shadow-[0_0_8px_rgba(239,68,68,0.8)]"></div>
                <span className="text-gray-400 text-sm">异常 3%</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
