'use client';

import MainLayout from '@/components/Layout/MainLayout';

export default function DeviceListPage() {
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
            { label: '设备总数', value: 856, color: 'cyan' },
            { label: '在线设备', value: 844, color: 'green' },
            { label: '离线设备', value: 12, color: 'red' },
            { label: '告警设备', value: 3, color: 'orange' },
            { label: '维护设备', value: 5, color: 'purple' },
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
              {[
                { id: 'DEV-001', name: '服务器-001', room: '机房A-101', type: '服务器', status: 'online', lastUpdate: '2025-01-07 14:30' },
                { id: 'DEV-002', name: '服务器-002', room: '机房A-101', type: '服务器', status: 'online', lastUpdate: '2025-01-07 14:28' },
                { id: 'DEV-003', name: '路由器-001', room: '机房A-102', type: '网络设备', status: 'online', lastUpdate: '2025-01-07 14:25' },
                { id: 'DEV-004', name: '空调机组-001', room: '机房B-203', type: '环境设备', status: 'warning', lastUpdate: '2025-01-07 14:15' },
                { id: 'DEV-005', name: '交换机-001', room: '机房A-101', type: '网络设备', status: 'offline', lastUpdate: '2025-01-07 13:00' },
                { id: 'DEV-006', name: 'UPS-001', room: '机房C-305', type: '电源设备', status: 'maintenance', lastUpdate: '2025-01-07 10:00' },
              ].map((item, index) => (
                <tr key={index} className="border-b border-gray-700/50 hover:bg-gray-700/30 transition-colors">
                  <td className="px-6 py-4 text-sm text-white">{item.id}</td>
                  <td className="px-6 py-4 text-sm text-white font-medium">{item.name}</td>
                  <td className="px-6 py-4 text-sm text-gray-300">{item.room}</td>
                  <td className="px-6 py-4 text-sm text-gray-300">{item.type}</td>
                  <td className="px-6 py-4">
                    <span
                      className={`inline-flex px-2 py-1 text-xs rounded-full ${
                        item.status === 'online'
                          ? 'bg-green-500/20 text-green-400 border border-green-500/30'
                          : item.status === 'warning'
                          ? 'bg-yellow-500/20 text-yellow-400 border border-yellow-500/30'
                          : item.status === 'offline'
                          ? 'bg-red-500/20 text-red-400 border border-red-500/30'
                          : 'bg-gray-500/20 text-gray-400 border border-gray-500/30'
                      }`}
                    >
                      {item.status === 'online'
                        ? '在线'
                        : item.status === 'warning'
                        ? '告警'
                        : item.status === 'offline'
                        ? '离线'
                        : '维护中'}
                    </span>
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-400">{item.lastUpdate}</td>
                  <td className="px-6 py-4">
                    <div className="flex gap-2">
                      <button className="text-cyan-400 hover:text-cyan-300 text-sm font-medium transition-colors">
                        详情
                      </button>
                      <span className="text-gray-600">|</span>
                      <button className="text-blue-400 hover:text-blue-300 text-sm font-medium transition-colors">
                        监控
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          {/* 分页 */}
          <div className="px-6 py-4 border-t border-gray-700 flex items-center justify-between">
            <p className="text-sm text-gray-400">共 6 条记录</p>
            <div className="flex gap-2">
              <button className="px-3 py-1 text-sm rounded bg-gray-700 text-gray-300 hover:bg-gray-600">上一页</button>
              <button className="px-3 py-1 text-sm rounded bg-cyan-500/20 text-cyan-400 border border-cyan-500/30">1</button>
              <button className="px-3 py-1 text-sm rounded bg-gray-700 text-gray-300 hover:bg-gray-600">下一页</button>
            </div>
          </div>
        </div>
      </div>
    </MainLayout>
  );
}
