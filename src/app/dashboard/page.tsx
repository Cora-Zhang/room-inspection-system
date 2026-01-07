'use client';

import { useEffect, useState } from 'react';
import MainLayout from '@/components/Layout/MainLayout';
import DataCard from '@/components/Dashboard/DataCard';

export default function DashboardPage() {
  const [currentTime, setCurrentTime] = useState(new Date());

  useEffect(() => {
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

  const formatDate = (date: Date) => {
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      weekday: 'long',
    });
  };

  return (
    <MainLayout>
      <div className="space-y-6">
        {/* 顶部欢迎栏和时间 */}
        <div
          className="relative overflow-hidden rounded-2xl bg-gradient-to-r from-cyan-500/20 via-blue-500/20 to-purple-500/20 border border-cyan-500/30 backdrop-blur-sm"
          style={{
            boxShadow: '0 4px 30px rgba(6, 182, 212, 0.2)',
          }}
        >
          {/* 扫描线效果 */}
          <div className="absolute inset-0 overflow-hidden">
            <div
              className="absolute inset-0 animate-scanline"
              style={{
                background: 'linear-gradient(180deg, transparent, rgba(6, 182, 212, 0.1), transparent)',
              }}
            ></div>
          </div>

          <div className="relative p-8 flex items-center justify-between">
            <div>
              <h1 className="text-3xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-cyan-400 to-blue-500 mb-2">
                欢迎访问集团机房巡检系统
              </h1>
              <p className="text-gray-400">
                智能监控 · 实时预警 · 高效运维
              </p>
            </div>

            <div className="text-right">
              <div className="text-4xl font-mono font-bold text-cyan-400 mb-2">
                {formatTime(currentTime)}
              </div>
              <div className="text-sm text-gray-400">{formatDate(currentTime)}</div>
            </div>
          </div>

          {/* 装饰性元素 */}
          <div className="absolute bottom-0 left-0 right-0 h-1 bg-gradient-to-r from-cyan-500 via-blue-500 to-purple-500"></div>
        </div>

        {/* 数据卡片网格 */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <DataCard
            title="待巡检机房"
            value={12}
            unit="个"
            trend={{ value: 8.5, isPositive: true }}
            color="cyan"
            delay={0}
            icon={
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"
                />
              </svg>
            }
          />

          <DataCard
            title="今日已巡检"
            value={5}
            unit="个"
            trend={{ value: 12.3, isPositive: true }}
            color="green"
            delay={100}
            icon={
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"
                />
              </svg>
            }
          />

          <DataCard
            title="异常告警"
            value={2}
            unit="条"
            trend={{ value: 15.2, isPositive: false }}
            color="red"
            delay={200}
            icon={
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
                />
              </svg>
            }
          />

          <DataCard
            title="巡检完成率"
            value={42}
            unit="%"
            trend={{ value: 5.7, isPositive: true }}
            color="purple"
            delay={300}
            icon={
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"
                />
              </svg>
            }
          />
        </div>

        {/* 更多统计卡片 */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <DataCard
            title="在线设备"
            value={856}
            unit="台"
            trend={{ value: 2.1, isPositive: true }}
            color="orange"
            delay={400}
            icon={
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M9 3v2m6-2v2M9 19v2m6-2v2M5 9H3m2 6H3m18-6h-2m2 6h-2M7 19h10a2 2 0 002-2V7a2 2 0 00-2-2H7a2 2 0 00-2 2v10a2 2 0 002 2zM9 9h6v6H9V9z"
                />
              </svg>
            }
          />

          <DataCard
            title="离线设备"
            value={12}
            unit="台"
            trend={{ value: 8.3, isPositive: false }}
            color="red"
            delay={500}
            icon={
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M18.364 18.364A9 9 0 005.636 5.636m12.728 12.728A9 9 0 015.636 5.636m12.728 12.728L5.636 5.636"
                />
              </svg>
            }
          />

          <DataCard
            title="巡检任务"
            value={28}
            unit="个"
            trend={{ value: 10.2, isPositive: true }}
            color="cyan"
            delay={600}
            icon={
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"
                />
              </svg>
            }
          />

          <DataCard
            title="本月巡检"
            value={156}
            unit="次"
            trend={{ value: 18.5, isPositive: true }}
            color="green"
            delay={700}
            icon={
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"
                />
              </svg>
            }
          />
        </div>

        {/* 图表区域 */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* 巡检趋势图 */}
          <div
            className="relative rounded-2xl bg-gradient-to-br from-gray-800/50 to-gray-900/50 border border-cyan-500/20 backdrop-blur-sm p-6"
            style={{
              boxShadow: '0 4px 30px rgba(6, 182, 212, 0.1)',
            }}
          >
            <div className="flex items-center justify-between mb-6">
              <h3 className="text-lg font-semibold text-white">巡检趋势</h3>
              <div className="flex gap-2">
                <button className="px-3 py-1 text-xs rounded bg-cyan-500/20 text-cyan-400 border border-cyan-500/30">
                  周视图
                </button>
                <button className="px-3 py-1 text-xs rounded text-gray-400 hover:bg-cyan-500/10">
                  月视图
                </button>
              </div>
            </div>

            {/* 模拟图表 */}
            <div className="h-64 flex items-end justify-between gap-2 px-4">
              {[45, 62, 58, 71, 65, 82, 78].map((value, index) => (
                <div key={index} className="flex-1 flex flex-col items-center gap-2">
                  <div
                    className="w-full bg-gradient-to-t from-cyan-500 to-blue-500 rounded-t-lg transition-all duration-500 hover:from-cyan-400 hover:to-blue-400 cursor-pointer"
                    style={{
                      height: `${value}%`,
                      boxShadow: `0 0 20px rgba(6, 182, 212, ${value / 150})`,
                    }}
                  ></div>
                  <span className="text-xs text-gray-400">
                    {['周一', '周二', '周三', '周四', '周五', '周六', '周日'][index]}
                  </span>
                </div>
              ))}
            </div>
          </div>

          {/* 告警类型分布 */}
          <div
            className="relative rounded-2xl bg-gradient-to-br from-gray-800/50 to-gray-900/50 border border-cyan-500/20 backdrop-blur-sm p-6"
            style={{
              boxShadow: '0 4px 30px rgba(6, 182, 212, 0.1)',
            }}
          >
            <div className="flex items-center justify-between mb-6">
              <h3 className="text-lg font-semibold text-white">告警类型分布</h3>
              <div className="flex items-center gap-2 text-cyan-400">
                <div className="w-2 h-2 rounded-full bg-cyan-400 animate-pulse"></div>
                实时更新
              </div>
            </div>

            {/* 告警列表 */}
            <div className="space-y-4">
              {[
                { type: '温度异常', count: 5, percentage: 35, color: 'red' },
                { type: '湿度异常', count: 3, percentage: 21, color: 'orange' },
                { type: '设备离线', count: 2, percentage: 14, color: 'purple' },
                { type: '电力波动', count: 2, percentage: 14, color: 'cyan' },
                { type: '其他告警', count: 2, percentage: 16, color: 'gray' },
              ].map((item, index) => (
                <div key={index} className="space-y-2">
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-gray-300">{item.type}</span>
                    <span className="text-sm text-white font-medium">{item.count} 次</span>
                  </div>
                  <div className="h-2 bg-gray-700 rounded-full overflow-hidden">
                    <div
                      className="h-full rounded-full transition-all duration-1000"
                      style={{
                        width: `${item.percentage}%`,
                        background:
                          item.color === 'red'
                            ? 'linear-gradient(90deg, #ef4444, #f97316)'
                            : item.color === 'orange'
                            ? 'linear-gradient(90deg, #f97316, #eab308)'
                            : item.color === 'purple'
                            ? 'linear-gradient(90deg, #a855f7, #8b5cf6)'
                            : item.color === 'cyan'
                            ? 'linear-gradient(90deg, #06b6d4, #3b82f6)'
                            : 'linear-gradient(90deg, #6b7280, #9ca3af)',
                        boxShadow: `0 0 10px ${
                          item.color === 'red'
                            ? 'rgba(239, 68, 68, 0.5)'
                            : item.color === 'orange'
                            ? 'rgba(249, 115, 22, 0.5)'
                            : item.color === 'purple'
                            ? 'rgba(168, 85, 247, 0.5)'
                            : item.color === 'cyan'
                            ? 'rgba(6, 182, 212, 0.5)'
                            : 'rgba(107, 114, 128, 0.5)'
                        }`,
                      }}
                    ></div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* 最近活动和快捷操作 */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* 最近活动 */}
          <div className="lg:col-span-2">
            <div
              className="relative rounded-2xl bg-gradient-to-br from-gray-800/50 to-gray-900/50 border border-cyan-500/20 backdrop-blur-sm p-6"
              style={{
                boxShadow: '0 4px 30px rgba(6, 182, 212, 0.1)',
              }}
            >
              <h3 className="text-lg font-semibold text-white mb-6">最近活动</h3>
              <div className="space-y-4">
                {[
                  {
                    type: 'success',
                    title: '机房A-101 巡检完成',
                    time: '2025-01-07 14:30',
                    user: '张三',
                  },
                  {
                    type: 'warning',
                    title: '机房B-203 温度异常',
                    time: '2025-01-07 12:15',
                    user: '系统',
                  },
                  {
                    type: 'info',
                    title: '新增机房 C-305',
                    time: '2025-01-07 10:00',
                    user: '李四',
                  },
                  {
                    type: 'success',
                    title: '机房A-102 巡检完成',
                    time: '2025-01-07 09:20',
                    user: '王五',
                  },
                  {
                    type: 'info',
                    title: '创建巡检任务 #1024',
                    time: '2025-01-07 08:45',
                    user: '系统',
                  },
                ].map((item, index) => (
                  <div
                    key={index}
                    className="flex items-start gap-4 p-4 rounded-lg hover:bg-gray-700/30 transition-colors duration-200"
                  >
                    <div
                      className={`w-2 h-2 rounded-full mt-2 ${
                        item.type === 'success'
                          ? 'bg-green-400 shadow-[0_0_8px_rgba(74,222,128,0.8)]'
                          : item.type === 'warning'
                          ? 'bg-yellow-400 shadow-[0_0_8px_rgba(250,204,21,0.8)]'
                          : 'bg-blue-400 shadow-[0_0_8px_rgba(96,165,250,0.8)]'
                      }`}
                    ></div>
                    <div className="flex-1">
                      <p className="text-sm text-white">{item.title}</p>
                      <p className="text-xs text-gray-500 mt-1">
                        {item.time} · {item.user}
                      </p>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* 快捷操作 */}
          <div>
            <div
              className="relative rounded-2xl bg-gradient-to-br from-gray-800/50 to-gray-900/50 border border-cyan-500/20 backdrop-blur-sm p-6"
              style={{
                boxShadow: '0 4px 30px rgba(6, 182, 212, 0.1)',
              }}
            >
              <h3 className="text-lg font-semibold text-white mb-6">快捷操作</h3>
              <div className="grid grid-cols-2 gap-3">
                {[
                  { icon: 'M12 4v16m8-8H4', label: '新建巡检', color: 'cyan' },
                  { icon: 'M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2', label: '巡检记录', color: 'green' },
                  { icon: 'M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4', label: '机房管理', color: 'purple' },
                  { icon: 'M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z', label: '数据报表', color: 'orange' },
                ].map((item, index) => (
                  <button
                    key={index}
                    className={`
                      flex flex-col items-center justify-center p-4 rounded-xl
                      bg-gradient-to-br ${
                        item.color === 'cyan'
                          ? 'from-cyan-500/20 to-blue-500/20 border-cyan-500/30'
                          : item.color === 'green'
                          ? 'from-green-500/20 to-emerald-500/20 border-green-500/30'
                          : item.color === 'purple'
                          ? 'from-purple-500/20 to-violet-500/20 border-purple-500/30'
                          : 'from-orange-500/20 to-amber-500/20 border-orange-500/30'
                      } border hover:scale-105 transition-all duration-300 group
                    `}
                  >
                    <svg
                      className={`w-6 h-6 mb-2 ${
                        item.color === 'cyan'
                          ? 'text-cyan-400'
                          : item.color === 'green'
                          ? 'text-green-400'
                          : item.color === 'purple'
                          ? 'text-purple-400'
                          : 'text-orange-400'
                      } group-hover:scale-110 transition-transform duration-300`}
                      fill="none"
                      stroke="currentColor"
                      viewBox="0 0 24 24"
                    >
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d={item.icon} />
                    </svg>
                    <span className="text-xs text-white">{item.label}</span>
                  </button>
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>

      <style jsx>{`
        @keyframes scanline {
          0% {
            transform: translateY(-100%);
          }
          100% {
            transform: translateY(100%);
          }
        }
        .animate-scanline {
          animation: scanline 3s linear infinite;
        }
      `}</style>
    </MainLayout>
  );
}
