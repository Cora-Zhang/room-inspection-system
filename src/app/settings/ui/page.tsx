'use client';

import { useState, useEffect } from 'react';
import MainLayout from '@/components/Layout/MainLayout';
import type { UIConfig, ThemeConfig } from '@/types';

export default function UIConfigPage() {
  const [configs, setConfigs] = useState<UIConfig[]>([]);
  const [themes, setThemes] = useState<ThemeConfig[]>([]);
  const [loading, setLoading] = useState(false);
  const [showUploadModal, setShowUploadModal] = useState(false);
  const [selectedType, setSelectedType] = useState<'logo' | 'background'>('logo');
  const [selectedConfig, setSelectedConfig] = useState<UIConfig | null>(null);

  const fetchConfigs = async () => {
    try {
      setLoading(true);
      const res = await fetch('/api/ui-configs');
      const json = await res.json();
      if (json.code === 200) {
        setConfigs(json.data);
      }
    } catch (error) {
      console.error('获取UI配置失败:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchThemes = async () => {
    try {
      const res = await fetch('/api/ui-configs');
      const json = await res.json();
      // 这里需要返回主题数据，暂时使用空数组
      const mockThemes: ThemeConfig[] = [
        {
          id: 1,
          name: '默认科幻蓝',
          primaryColor: '#06b6d4',
          secondaryColor: '#3b82f6',
          backgroundColor: '#0f172a',
          textColor: '#ffffff',
          accentColor: '#8b5cf6',
          isDefault: true,
        },
        {
          id: 2,
          name: '深邃紫',
          primaryColor: '#8b5cf6',
          secondaryColor: '#a855f7',
          backgroundColor: '#1a1025',
          textColor: '#ffffff',
          accentColor: '#ec4899',
          isDefault: false,
        },
      ];
      setThemes(mockThemes);
    } catch (error) {
      console.error('获取主题失败:', error);
    }
  };

  useEffect(() => {
    fetchConfigs();
    fetchThemes();
  }, []);

  const handleUpload = () => {
    setSelectedConfig(null);
    setShowUploadModal(true);
  };

  const handleEditConfig = (config: UIConfig) => {
    setSelectedConfig(config);
    setSelectedType(config.type as 'logo' | 'background');
    setShowUploadModal(true);
  };

  const groupedConfigs = configs.reduce((acc, config) => {
    if (!acc[config.category]) {
      acc[config.category] = [];
    }
    acc[config.category].push(config);
    return acc;
  }, {} as Record<string, UIConfig[]>);

  return (
    <MainLayout>
      <div className="space-y-6">
        {/* 页面标题 */}
        <div>
          <h1 className="text-2xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-cyan-400 to-blue-500 mb-2">
            UI配置管理
          </h1>
          <p className="text-gray-400">自定义系统UI元素和主题</p>
        </div>

        {/* 主题配置 */}
        <div
          className="relative rounded-2xl bg-gradient-to-br from-gray-800/50 to-gray-900/50 border border-cyan-500/20 backdrop-blur-sm p-6"
          style={{
            boxShadow: '0 4px 30px rgba(6, 182, 212, 0.1)',
          }}
        >
          <div className="flex items-center justify-between mb-6">
            <div>
              <h3 className="text-lg font-semibold text-white">主题配置</h3>
              <p className="text-sm text-gray-400">选择系统主题颜色方案</p>
            </div>
            <button className="px-4 py-2 bg-cyan-500/20 text-cyan-400 rounded-lg border border-cyan-500/30 hover:bg-cyan-500/30 transition-all">
              新建主题
            </button>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {themes.map((theme) => (
              <div
                key={theme.id}
                className={`
                  relative rounded-xl border-2 p-4 cursor-pointer transition-all
                  ${theme.isDefault ? 'border-cyan-500 bg-cyan-500/10' : 'border-gray-700 bg-gray-800/50 hover:border-gray-600'}
                `}
              >
                {theme.isDefault && (
                  <div className="absolute top-2 right-2">
                    <span className="text-xs px-2 py-1 rounded bg-cyan-500 text-white">当前</span>
                  </div>
                )}
                <h4 className="text-white font-medium mb-3">{theme.name}</h4>
                <div className="space-y-2">
                  <div className="flex items-center gap-2">
                    <div
                      className="w-8 h-8 rounded border border-gray-600"
                      style={{ backgroundColor: theme.primaryColor }}
                    ></div>
                    <span className="text-xs text-gray-400">主色</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <div
                      className="w-8 h-8 rounded border border-gray-600"
                      style={{ backgroundColor: theme.secondaryColor }}
                    ></div>
                    <span className="text-xs text-gray-400">辅色</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <div
                      className="w-8 h-8 rounded border border-gray-600"
                      style={{ backgroundColor: theme.accentColor }}
                    ></div>
                    <span className="text-xs text-gray-400">强调色</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* UI元素配置 */}
        <div
          className="relative rounded-2xl bg-gradient-to-br from-gray-800/50 to-gray-900/50 border border-cyan-500/20 backdrop-blur-sm p-6"
          style={{
            boxShadow: '0 4px 30px rgba(6, 182, 212, 0.1)',
          }}
        >
          <div className="flex items-center justify-between mb-6">
            <div>
              <h3 className="text-lg font-semibold text-white">UI元素配置</h3>
              <p className="text-sm text-gray-400">管理系统LOGO、背景等UI元素</p>
            </div>
            <button
              onClick={handleUpload}
              className="px-4 py-2 bg-gradient-to-r from-cyan-500 to-blue-600 rounded-lg text-white font-medium hover:from-cyan-400 hover:to-blue-500 transition-all"
            >
              上传元素
            </button>
          </div>

          {/* 品牌 */}
          <div className="mb-6">
            <h4 className="text-white font-medium mb-4">品牌</h4>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {groupedConfigs.brand?.map((config) => (
                <div
                  key={config.id}
                  className="flex items-center justify-between p-4 rounded-lg bg-gray-700/30 border border-gray-700 hover:border-cyan-500/30 transition-all"
                >
                  <div>
                    <p className="text-white font-medium">{config.key}</p>
                    <p className="text-xs text-gray-400 mt-1">{config.description}</p>
                  </div>
                  <div className="flex items-center gap-2">
                    {config.type === 'logo' && (
                      <div className="w-12 h-12 rounded-lg bg-gray-800 flex items-center justify-center">
                        <svg className="w-6 h-6 text-cyan-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                        </svg>
                      </div>
                    )}
                    <button
                      onClick={() => handleEditConfig(config)}
                      className="px-3 py-1 text-xs rounded bg-cyan-500/20 text-cyan-400 border border-cyan-500/30 hover:bg-cyan-500/30 transition-all"
                    >
                      编辑
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* 背景 */}
          <div className="mb-6">
            <h4 className="text-white font-medium mb-4">背景</h4>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {groupedConfigs.background?.map((config) => (
                <div
                  key={config.id}
                  className="flex items-center justify-between p-4 rounded-lg bg-gray-700/30 border border-gray-700 hover:border-cyan-500/30 transition-all"
                >
                  <div>
                    <p className="text-white font-medium">{config.key}</p>
                    <p className="text-xs text-gray-400 mt-1">{config.description}</p>
                  </div>
                  <button
                    onClick={() => handleEditConfig(config)}
                    className="px-3 py-1 text-xs rounded bg-cyan-500/20 text-cyan-400 border border-cyan-500/30 hover:bg-cyan-500/30 transition-all"
                  >
                    编辑
                  </button>
                </div>
              ))}
            </div>
          </div>

          {/* 通用 */}
          <div>
            <h4 className="text-white font-medium mb-4">通用</h4>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {groupedConfigs.general?.map((config) => (
                <div
                  key={config.id}
                  className="flex items-center justify-between p-4 rounded-lg bg-gray-700/30 border border-gray-700 hover:border-cyan-500/30 transition-all"
                >
                  <div>
                    <p className="text-white font-medium">{config.key}</p>
                    <p className="text-xs text-gray-400 mt-1">{config.description}</p>
                  </div>
                  <button
                    onClick={() => handleEditConfig(config)}
                    className="px-3 py-1 text-xs rounded bg-cyan-500/20 text-cyan-400 border border-cyan-500/30 hover:bg-cyan-500/30 transition-all"
                  >
                    编辑
                  </button>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* 上传模态框 */}
      {showUploadModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
          <div className="relative w-full max-w-lg rounded-2xl bg-gray-900 border border-cyan-500/30 p-6">
            <h3 className="text-lg font-semibold text-white mb-4">
              {selectedConfig ? '编辑UI元素' : '上传UI元素'}
            </h3>

            {/* 拖拽上传区 */}
            <div className="border-2 border-dashed border-gray-600 rounded-lg p-8 text-center hover:border-cyan-500/50 transition-all">
              <svg className="mx-auto w-12 h-12 text-gray-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
              </svg>
              <p className="text-gray-400 mb-2">点击或拖拽文件到此处上传</p>
              <p className="text-xs text-gray-500">支持 PNG、JPG、GIF 格式，建议尺寸 {selectedType === 'logo' ? '200x200' : '1920x1080'}</p>
            </div>

            <div className="flex gap-3 mt-6">
              <button
                onClick={() => setShowUploadModal(false)}
                className="flex-1 px-4 py-2 bg-gray-700 text-white rounded-lg hover:bg-gray-600 transition-all"
              >
                取消
              </button>
              <button className="flex-1 px-4 py-2 bg-cyan-500 text-white rounded-lg hover:bg-cyan-400 transition-all">
                确定
              </button>
            </div>
          </div>
        </div>
      )}
    </MainLayout>
  );
}
