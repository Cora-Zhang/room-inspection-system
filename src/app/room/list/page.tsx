'use client';

import MainLayout from '@/components/Layout/MainLayout';

export default function RoomListPage() {
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
            { label: '总机房数', value: 12, color: 'from-cyan-500 to-blue-600' },
            { label: '在线机房', value: 11, color: 'from-green-500 to-emerald-600' },
            { label: '离线机房', value: 1, color: 'from-red-500 to-pink-600' },
            { label: '维护中', value: 2, color: 'from-orange-500 to-amber-600' },
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

        {/* 机房卡片列表 */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {[
            {
              name: '机房A-101',
              location: '一号楼 一层',
              status: 'online',
              temperature: 22,
              humidity: 45,
              devices: 156,
            },
            {
              name: '机房A-102',
              location: '一号楼 一层',
              status: 'online',
              temperature: 23,
              humidity: 48,
              devices: 142,
            },
            {
              name: '机房A-103',
              location: '一号楼 二层',
              status: 'online',
              temperature: 21,
              humidity: 46,
              devices: 128,
            },
            {
              name: '机房B-201',
              location: '二号楼 一层',
              status: 'online',
              temperature: 24,
              humidity: 50,
              devices: 98,
            },
            {
              name: '机房B-203',
              location: '二号楼 二层',
              status: 'warning',
              temperature: 28,
              humidity: 65,
              devices: 85,
            },
            {
              name: '机房C-305',
              location: '三号楼 三层',
              status: 'maintenance',
              temperature: 0,
              humidity: 0,
              devices: 72,
            },
          ].map((item, index) => (
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

              {item.status === 'maintenance' ? (
                <div className="py-8 text-center">
                  <p className="text-gray-400">机房维护中</p>
                  <p className="text-xs text-gray-500 mt-2">预计恢复时间：2025-01-10</p>
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
                <button className="flex-1 px-4 py-2 bg-cyan-500/20 text-cyan-400 rounded-lg text-sm hover:bg-cyan-500/30 transition-colors">
                  查看详情
                </button>
                <button className="flex-1 px-4 py-2 bg-blue-500/20 text-blue-400 rounded-lg text-sm hover:bg-blue-500/30 transition-colors">
                  开始巡检
                </button>
              </div>
            </div>
          ))}
        </div>
      </div>
    </MainLayout>
  );
}
