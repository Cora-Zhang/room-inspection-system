'use client';

import MainLayout from '@/components/Layout/MainLayout';

export default function InspectionListPage() {
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
                className="w-full px-4 py-2 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white placeholder-gray-500"
              />
            </div>
            <select className="px-4 py-2 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white">
              <option>全部状态</option>
              <option>已完成</option>
              <option>进行中</option>
              <option>待处理</option>
            </select>
            <select className="px-4 py-2 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white">
              <option>全部机房</option>
              <option>机房A</option>
              <option>机房B</option>
              <option>机房C</option>
            </select>
            <button className="px-6 py-2 bg-gradient-to-r from-cyan-500 to-blue-600 rounded-lg text-white font-medium hover:from-cyan-400 hover:to-blue-500 transition-all">
              查询
            </button>
          </div>
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
                <th className="px-6 py-4 text-left text-sm font-medium text-gray-400">巡检人</th>
                <th className="px-6 py-4 text-left text-sm font-medium text-gray-400">巡检时间</th>
                <th className="px-6 py-4 text-left text-sm font-medium text-gray-400">状态</th>
                <th className="px-6 py-4 text-left text-sm font-medium text-gray-400">操作</th>
              </tr>
            </thead>
            <tbody>
              {[
                { id: 'INS-2025001', room: '机房A-101', inspector: '张三', time: '2025-01-07 14:30', status: 'completed' },
                { id: 'INS-2025002', room: '机房A-102', inspector: '李四', time: '2025-01-07 13:15', status: 'completed' },
                { id: 'INS-2025003', room: '机房B-203', inspector: '王五', time: '2025-01-07 11:00', status: 'completed' },
                { id: 'INS-2025004', room: '机房C-305', inspector: '赵六', time: '2025-01-07 10:30', status: 'processing' },
                { id: 'INS-2025005', room: '机房A-103', inspector: '孙七', time: '2025-01-07 09:45', status: 'pending' },
              ].map((item, index) => (
                <tr key={index} className="border-b border-gray-700/50 hover:bg-gray-700/30 transition-colors">
                  <td className="px-6 py-4 text-sm text-white">{item.id}</td>
                  <td className="px-6 py-4 text-sm text-white">{item.room}</td>
                  <td className="px-6 py-4 text-sm text-white">{item.inspector}</td>
                  <td className="px-6 py-4 text-sm text-gray-400">{item.time}</td>
                  <td className="px-6 py-4">
                    <span
                      className={`inline-flex px-2 py-1 text-xs rounded-full ${
                        item.status === 'completed'
                          ? 'bg-green-500/20 text-green-400 border border-green-500/30'
                          : item.status === 'processing'
                          ? 'bg-blue-500/20 text-blue-400 border border-blue-500/30'
                          : 'bg-yellow-500/20 text-yellow-400 border border-yellow-500/30'
                      }`}
                    >
                      {item.status === 'completed' ? '已完成' : item.status === 'processing' ? '进行中' : '待处理'}
                    </span>
                  </td>
                  <td className="px-6 py-4">
                    <button className="text-cyan-400 hover:text-cyan-300 text-sm font-medium transition-colors">
                      查看详情
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          {/* 分页 */}
          <div className="px-6 py-4 border-t border-gray-700 flex items-center justify-between">
            <p className="text-sm text-gray-400">共 5 条记录</p>
            <div className="flex gap-2">
              <button className="px-3 py-1 text-sm rounded bg-gray-700 text-gray-300 hover:bg-gray-600">上一页</button>
              <button className="px-3 py-1 text-sm rounded bg-cyan-500/20 text-cyan-400 border border-cyan-500/30">1</button>
              <button className="px-3 py-1 text-sm rounded bg-gray-700 text-gray-300 hover:bg-gray-600">2</button>
              <button className="px-3 py-1 text-sm rounded bg-gray-700 text-gray-300 hover:bg-gray-600">下一页</button>
            </div>
          </div>
        </div>
      </div>
    </MainLayout>
  );
}
