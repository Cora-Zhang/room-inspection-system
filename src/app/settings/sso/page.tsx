'use client';

import { useState, useEffect } from 'react';
import { http } from '@/lib/api';

interface SSOConfig {
  id: string;
  provider: string;
  name: string;
  type: string;
  enabled: boolean;
  config: string;
  description?: string;
  sort: number;
  createdAt: string;
  updatedAt: string;
}

interface OAuth2ConfigForm {
  appId?: string;
  appSecret?: string;
  clientId?: string;
  clientSecret?: string;
  authorizationUrl: string;
  tokenUrl: string;
  userInfoUrl: string;
  scope?: string;
  userMapping?: {
    userId?: string;
    username?: string;
    email?: string;
    realName?: string;
    phone?: string;
  };
  extraParams?: Record<string, string>;
}

export default function SSOConfigPage() {
  const [configs, setConfigs] = useState<SSOConfig[]>([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [editingConfig, setEditingConfig] = useState<SSOConfig | null>(null);
  const [formData, setFormData] = useState({
    provider: '',
    name: '',
    type: 'oauth2',
    enabled: false,
    description: '',
    sort: 0,
    config: {} as OAuth2ConfigForm,
  });

  useEffect(() => {
    fetchConfigs();
  }, []);

  const fetchConfigs = async () => {
    try {
      setLoading(true);
      const response = await http.get<SSOConfig[]>('/sso');
      if (response.success && response.data) {
        setConfigs(response.data);
      }
    } catch (error) {
      console.error('Failed to fetch SSO configs:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    setEditingConfig(null);
    setFormData({
      provider: '',
      name: '',
      type: 'oauth2',
      enabled: false,
      description: '',
      sort: 0,
      config: {
        authorizationUrl: '',
        tokenUrl: '',
        userInfoUrl: '',
        scope: 'openid profile email',
      } as OAuth2ConfigForm,
    });
    setShowModal(true);
  };

  const handleEdit = (config: SSOConfig) => {
    setEditingConfig(config);
    setFormData({
      provider: config.provider,
      name: config.name,
      type: config.type,
      enabled: config.enabled,
      description: config.description || '',
      sort: config.sort,
      config: JSON.parse(config.config) as OAuth2ConfigForm,
    });
    setShowModal(true);
  };

  const handleDelete = async (id: string) => {
    if (!confirm('确定要删除此SSO配置吗？')) {
      return;
    }

    try {
      const response = await http.delete(`/sso/${id}`);
      if (response.success) {
        fetchConfigs();
        alert('删除成功');
      }
    } catch (error) {
      console.error('Failed to delete SSO config:', error);
      alert('删除失败');
    }
  };

  const handleToggle = async (id: string, enabled: boolean) => {
    try {
      const response = await http.patch(`/sso/${id}/toggle`, { enabled });
      if (response.success) {
        fetchConfigs();
      }
    } catch (error) {
      console.error('Failed to toggle SSO config:', error);
      alert('操作失败');
    }
  };

  const handleSave = async () => {
    try {
      if (editingConfig) {
        await http.put(`/sso/${editingConfig.id}`, formData);
        alert('更新成功');
      } else {
        await http.post('/sso', formData);
        alert('创建成功');
      }
      setShowModal(false);
      fetchConfigs();
    } catch (error: any) {
      console.error('Failed to save SSO config:', error);
      alert(error.response?.data?.error?.message || '保存失败');
    }
  };

  const handleConfigChange = (path: string, value: any) => {
    setFormData((prev) => {
      const config = { ...prev.config };
      if (path === '') {
        return { ...prev, config: value };
      }
      const keys = path.split('.');
      let current: any = config;
      for (let i = 0; i < keys.length - 1; i++) {
        if (!current[keys[i]]) {
          current[keys[i]] = {};
        }
        current = current[keys[i]];
      }
      current[keys[keys.length - 1]] = value;
      return { ...prev, config };
    });
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-900 via-gray-900 to-blue-900/20 p-8">
      <div className="max-w-6xl mx-auto">
        {/* 标题 */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-white mb-2">SSO配置管理</h1>
          <p className="text-gray-400">配置单点登录认证协议，支持OAuth2.0、SAML、CAS等</p>
        </div>

        {/* 操作栏 */}
        <div className="flex justify-between items-center mb-6">
          <div className="text-gray-400">
            共 {configs.length} 条配置
          </div>
          <button
            onClick={handleCreate}
            className="px-6 py-2 bg-gradient-to-r from-cyan-500 to-blue-600 text-white rounded-lg hover:from-cyan-600 hover:to-blue-700 transition-all duration-200"
          >
            + 新增SSO配置
          </button>
        </div>

        {/* 配置列表 */}
        {loading ? (
          <div className="text-center text-gray-400 py-12">加载中...</div>
        ) : configs.length === 0 ? (
          <div className="bg-gray-800/50 rounded-xl p-12 text-center">
            <div className="text-gray-400 mb-4">
              <svg className="w-16 h-16 mx-auto mb-4 text-gray-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
              </svg>
              暂无SSO配置
            </div>
            <button
              onClick={handleCreate}
              className="px-6 py-2 bg-cyan-600 text-white rounded-lg hover:bg-cyan-700 transition-colors"
            >
              立即配置
            </button>
          </div>
        ) : (
          <div className="grid gap-4">
            {configs.map((config) => {
              const configData = JSON.parse(config.config);
              return (
                <div
                  key={config.id}
                  className="bg-gray-800/50 backdrop-blur-xl border border-cyan-500/20 rounded-xl p-6 hover:border-cyan-500/40 transition-all duration-200"
                >
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <div className="flex items-center gap-4 mb-3">
                        <div className="text-2xl font-bold text-white">{config.name}</div>
                        <span className={`px-3 py-1 rounded-full text-xs font-medium ${
                          config.enabled
                            ? 'bg-green-500/20 text-green-400'
                            : 'bg-gray-500/20 text-gray-400'
                        }`}>
                          {config.enabled ? '已启用' : '已禁用'}
                        </span>
                        <span className="px-3 py-1 rounded-full text-xs font-medium bg-cyan-500/20 text-cyan-400">
                          {config.type.toUpperCase()}
                        </span>
                      </div>
                      <div className="text-sm text-gray-400 mb-2">
                        Provider: <span className="text-cyan-400">{config.provider}</span>
                      </div>
                      {config.description && (
                        <div className="text-sm text-gray-400 mb-2">{config.description}</div>
                      )}
                      <div className="text-sm text-gray-500">
                        授权URL: {configData.authorizationUrl}
                      </div>
                    </div>
                    <div className="flex gap-2">
                      <button
                        onClick={() => handleToggle(config.id, !config.enabled)}
                        className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
                          config.enabled
                            ? 'bg-orange-500/20 text-orange-400 hover:bg-orange-500/30'
                            : 'bg-green-500/20 text-green-400 hover:bg-green-500/30'
                        }`}
                      >
                        {config.enabled ? '禁用' : '启用'}
                      </button>
                      <button
                        onClick={() => handleEdit(config)}
                        className="px-4 py-2 rounded-lg text-sm font-medium bg-blue-500/20 text-blue-400 hover:bg-blue-500/30 transition-colors"
                      >
                        编辑
                      </button>
                      <button
                        onClick={() => handleDelete(config.id)}
                        className="px-4 py-2 rounded-lg text-sm font-medium bg-red-500/20 text-red-400 hover:bg-red-500/30 transition-colors"
                      >
                        删除
                      </button>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* 配置模态框 */}
      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70">
          <div className="bg-gray-800 rounded-2xl w-full max-w-4xl max-h-[90vh] overflow-y-auto border border-cyan-500/30">
            <div className="p-6 border-b border-gray-700">
              <h2 className="text-2xl font-bold text-white">
                {editingConfig ? '编辑SSO配置' : '新增SSO配置'}
              </h2>
            </div>
            <div className="p-6 space-y-6">
              {/* 基本信息 */}
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-300 mb-2">
                    配置名称 <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    value={formData.name}
                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                    className="w-full px-4 py-3 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white"
                    placeholder="如: IAM认证"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-300 mb-2">
                    Provider标识 <span className="text-red-500">*</span>
                  </label>
                  <input
                    type="text"
                    value={formData.provider}
                    onChange={(e) => setFormData({ ...formData, provider: e.target.value })}
                    className="w-full px-4 py-3 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white"
                    placeholder="如: iam-oauth2"
                    disabled={!!editingConfig}
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-300 mb-2">
                  认证类型
                </label>
                <select
                  value={formData.type}
                  onChange={(e) => setFormData({ ...formData, type: e.target.value })}
                  className="w-full px-4 py-3 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white"
                  disabled={!!editingConfig}
                >
                  <option value="oauth2">OAuth2.0</option>
                  <option value="saml">SAML 2.0</option>
                  <option value="cas">CAS</option>
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-300 mb-2">
                  描述
                </label>
                <textarea
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  className="w-full px-4 py-3 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white"
                  rows={2}
                  placeholder="描述此SSO配置的用途"
                />
              </div>

              {/* OAuth2.0配置 */}
              {formData.type === 'oauth2' && (
                <div className="space-y-4 border-t border-gray-700 pt-6">
                  <h3 className="text-lg font-semibold text-cyan-400">OAuth2.0配置</h3>

                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-300 mb-2">
                        App ID / Client ID
                      </label>
                      <input
                        type="text"
                        value={formData.config.appId || formData.config.clientId || ''}
                        onChange={(e) => handleConfigChange('appId', e.target.value)}
                        className="w-full px-4 py-3 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white"
                        placeholder="应用ID或客户端ID"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-300 mb-2">
                        App Secret / Client Secret
                      </label>
                      <input
                        type="password"
                        value={formData.config.appSecret || formData.config.clientSecret || ''}
                        onChange={(e) => handleConfigChange('appSecret', e.target.value)}
                        className="w-full px-4 py-3 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white"
                        placeholder="应用密钥或客户端密钥"
                      />
                    </div>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-300 mb-2">
                      授权地址 (Authorization URL) <span className="text-red-500">*</span>
                    </label>
                    <input
                      type="url"
                      value={formData.config.authorizationUrl || ''}
                      onChange={(e) => handleConfigChange('authorizationUrl', e.target.value)}
                      className="w-full px-4 py-3 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white"
                      placeholder="https://sso.example.com/oauth/authorize"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-300 mb-2">
                      获取Token地址 (Token URL) <span className="text-red-500">*</span>
                    </label>
                    <input
                      type="url"
                      value={formData.config.tokenUrl || ''}
                      onChange={(e) => handleConfigChange('tokenUrl', e.target.value)}
                      className="w-full px-4 py-3 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white"
                      placeholder="https://sso.example.com/oauth/token"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-300 mb-2">
                      获取用户信息地址 (User Info URL) <span className="text-red-500">*</span>
                    </label>
                    <input
                      type="url"
                      value={formData.config.userInfoUrl || ''}
                      onChange={(e) => handleConfigChange('userInfoUrl', e.target.value)}
                      className="w-full px-4 py-3 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white"
                      placeholder="https://sso.example.com/oauth/userinfo"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-300 mb-2">
                      Scope
                    </label>
                    <input
                      type="text"
                      value={formData.config.scope || ''}
                      onChange={(e) => handleConfigChange('scope', e.target.value)}
                      className="w-full px-4 py-3 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white"
                      placeholder="openid profile email"
                    />
                  </div>

                  {/* 额外参数 */}
                  <div>
                    <label className="block text-sm font-medium text-gray-300 mb-2">
                      额外参数 (API Key等)
                    </label>
                    <div className="space-y-2">
                      {Object.entries(formData.config.extraParams || {}).map(([key, value]) => (
                        <div key={key} className="flex gap-2">
                          <input
                            type="text"
                            value={key}
                            readOnly
                            className="flex-1 px-4 py-2 bg-gray-900/50 border border-gray-700 rounded-lg text-gray-400"
                          />
                          <input
                            type="text"
                            value={value as string}
                            onChange={(e) => {
                              const newParams = { ...formData.config.extraParams };
                              newParams[key] = e.target.value;
                              handleConfigChange('extraParams', newParams);
                            }}
                            className="flex-1 px-4 py-2 bg-gray-900/50 border border-gray-700 rounded-lg text-white"
                          />
                        </div>
                      ))}
                      <button
                        type="button"
                        onClick={() => {
                          const newParams = { ...formData.config.extraParams };
                          newParams[`param_${Date.now()}`] = '';
                          handleConfigChange('extraParams', newParams);
                        }}
                        className="px-4 py-2 bg-gray-700 text-white rounded-lg hover:bg-gray-600 transition-colors"
                      >
                        + 添加参数
                      </button>
                    </div>
                  </div>
                </div>
              )}

              {/* 启用开关 */}
              <div className="flex items-center gap-4">
                <label className="flex items-center gap-2 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={formData.enabled}
                    onChange={(e) => setFormData({ ...formData, enabled: e.target.checked })}
                    className="w-5 h-5 rounded bg-gray-900/50 border-gray-700 text-cyan-500 focus:ring-2 focus:ring-cyan-500/50"
                  />
                  <span className="text-sm font-medium text-gray-300">启用此配置</span>
                </label>
              </div>
            </div>

            {/* 按钮栏 */}
            <div className="p-6 border-t border-gray-700 flex justify-end gap-4">
              <button
                onClick={() => setShowModal(false)}
                className="px-6 py-2 bg-gray-700 text-white rounded-lg hover:bg-gray-600 transition-colors"
              >
                取消
              </button>
              <button
                onClick={handleSave}
                className="px-6 py-2 bg-gradient-to-r from-cyan-500 to-blue-600 text-white rounded-lg hover:from-cyan-600 hover:to-blue-700 transition-all"
              >
                保存
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
