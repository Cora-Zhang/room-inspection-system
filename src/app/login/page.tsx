'use client';

import { useState, FormEvent, useEffect } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { http } from '@/lib/api';

interface SSOProvider {
  provider: string;
  name: string;
  type: string;
}

export default function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [currentTime, setCurrentTime] = useState(new Date());
  const [mounted, setMounted] = useState(false);
  const [ssoProviders, setSsoProviders] = useState<SSOProvider[]>([]);
  const [loadingSSO, setLoadingSSO] = useState(false);

  const router = useRouter();
  const searchParams = useSearchParams();
  const ssoParam = searchParams.get('sso');

  // 根据URL参数判断登录类型
  const isSSOLogin = ssoParam !== null;

  useEffect(() => {
    setMounted(true);
    const timer = setInterval(() => {
      setCurrentTime(new Date());
    }, 1000);

    // 如果是SSO登录，获取SSO配置
    if (isSSOLogin) {
      fetchSSOProviders();
    }

    return () => {
      clearInterval(timer);
    };
  }, [isSSOLogin]);

  const fetchSSOProviders = async () => {
    try {
      setLoadingSSO(true);
      const response = await http.get<SSOProvider[]>('/auth/sso/providers');
      if (response.success && response.data) {
        setSsoProviders(response.data);
      }
    } catch (error) {
      console.error('Failed to fetch SSO providers:', error);
    } finally {
      setLoadingSSO(false);
    }
  };

  const formatTime = (date: Date) => {
    return date.toLocaleTimeString('zh-CN', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: false,
    });
  };

  const handleLocalLogin = async (e: FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError('');

    try {
      const result = await http.post<any>('/auth/login', {
        username,
        password,
      });

      if (result.success && result.data) {
        localStorage.setItem('token', result.data.token);
        localStorage.setItem('refreshToken', result.data.refreshToken);
        localStorage.setItem('user', JSON.stringify(result.data.user));
        router.push('/dashboard');
      }
    } catch (err: any) {
      setError(err.response?.data?.error?.message || '登录失败，请重试');
    } finally {
      setIsLoading(false);
    }
  };

  const handleSSOLogin = async (provider: string) => {
    try {
      setIsLoading(true);
      const response = await http.get<any>(`/auth/sso/authorize/${provider}`);

      if (response.success && response.data?.authUrl) {
        // 跳转到SSO授权页面
        window.location.href = response.data.authUrl;
      }
    } catch (err: any) {
      setError(err.response?.data?.error?.message || 'SSO登录失败，请重试');
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center relative overflow-hidden bg-gradient-to-br from-gray-900 via-gray-900 to-blue-900/20">
      {/* 网格背景 */}
      <div className="absolute inset-0 opacity-5 pointer-events-none">
        <div
          className="absolute inset-0"
          style={{
            backgroundImage: `
              linear-gradient(to right, rgba(6, 182, 212, 0.3) 1px, transparent 1px),
              linear-gradient(to bottom, rgba(6, 182, 212, 0.3) 1px, transparent 1px)
            `,
            backgroundSize: '50px 50px',
          }}
        ></div>
      </div>

      {/* 扫描线效果 */}
      <div
        className="absolute inset-0 overflow-hidden pointer-events-none"
        style={{
          background: 'radial-gradient(circle at center, transparent 0%, rgba(0,0,0,0.5) 100%)',
        }}
      >
        <div
          className="absolute inset-0 animate-scanline"
          style={{
            background: 'linear-gradient(180deg, transparent, rgba(6, 182, 212, 0.05), transparent)',
          }}
        ></div>
      </div>

      {/* 时间显示 */}
      <div className="absolute top-8 right-8 text-right">
        {mounted && (
          <div className="text-4xl font-mono font-bold text-cyan-400 mb-2">
            {formatTime(currentTime)}
          </div>
        )}
        {mounted && (
          <div className="text-sm text-gray-400">
            {currentTime.toLocaleDateString('zh-CN', {
              year: 'numeric',
              month: '2-digit',
              day: '2-digit',
              weekday: 'long',
            })}
          </div>
        )}
      </div>

      {/* 登录卡片 */}
      <div className="relative w-full max-w-md px-4">
        <div
          className="relative rounded-2xl bg-gradient-to-br from-gray-800/90 to-gray-900/90 backdrop-blur-xl border border-cyan-500/30 p-8"
          style={{
            boxShadow: '0 0 60px rgba(6, 182, 212, 0.15), inset 0 0 20px rgba(6, 182, 212, 0.05)',
          }}
        >
          {/* 角落装饰 */}
          <div className="absolute top-0 left-0 w-4 h-4 border-t-2 border-l-2 border-cyan-400"></div>
          <div className="absolute top-0 right-0 w-4 h-4 border-t-2 border-r-2 border-cyan-400"></div>
          <div className="absolute bottom-0 left-0 w-4 h-4 border-b-2 border-l-2 border-cyan-400"></div>
          <div className="absolute bottom-0 right-0 w-4 h-4 border-b-2 border-r-2 border-cyan-400"></div>

          {/* Logo 和标题 */}
          <div className="text-center mb-8">
            <div className="inline-flex items-center justify-center w-20 h-20 rounded-2xl bg-gradient-to-br from-cyan-500 to-blue-600 mb-6 shadow-lg shadow-cyan-500/30 hover:scale-110 transition-transform duration-300">
              <svg
                className="w-10 h-10 text-white"
                fill="none"
                stroke="currentColor"
                viewBox="0 0 24 24"
              >
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth={2}
                  d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"
                />
              </svg>
            </div>
            <h1 className="text-2xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-cyan-400 to-blue-500 mb-2">
              机房巡检系统
            </h1>
            <p className="text-sm text-gray-400">智能监控 · 实时预警 · 高效运维</p>
          </div>

          {/* 错误提示 */}
          {error && (
            <div className="mb-6 p-4 bg-red-500/20 border border-red-500/50 rounded-lg">
              <p className="text-sm text-red-400">{error}</p>
            </div>
          )}

          {/* 账密登录表单 */}
          {!isSSOLogin && (
            <form onSubmit={handleLocalLogin} className="space-y-6">
              {/* 用户名输入框 */}
              <div className="space-y-2">
                <label htmlFor="username" className="block text-sm font-medium text-gray-300">
                  用户名
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <svg className="w-5 h-5 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                    </svg>
                  </div>
                  <input
                    id="username"
                    type="text"
                    required
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    className="w-full pl-10 pr-4 py-3 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white placeholder-gray-500 transition-all duration-200"
                    placeholder="请输入用户名"
                  />
                </div>
              </div>

              {/* 密码输入框 */}
              <div className="space-y-2">
                <label htmlFor="password" className="block text-sm font-medium text-gray-300">
                  密码
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <svg className="w-5 h-5 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                    </svg>
                  </div>
                  <input
                    id="password"
                    type="password"
                    required
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    className="w-full pl-10 pr-4 py-3 bg-gray-900/50 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white placeholder-gray-500 transition-all duration-200"
                    placeholder="请输入密码"
                  />
                </div>
              </div>

              {/* 登录按钮 */}
              <button
                type="submit"
                disabled={isLoading}
                className="w-full py-3 px-4 bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-600 hover:to-blue-700 disabled:from-gray-600 disabled:to-gray-700 text-white font-medium rounded-lg shadow-lg shadow-cyan-500/30 hover:shadow-cyan-500/50 disabled:shadow-none transition-all duration-200"
              >
                {isLoading ? '登录中...' : '登录'}
              </button>
            </form>
          )}

          {/* SSO登录 */}
          {isSSOLogin && (
            <div className="space-y-4">
              {loadingSSO ? (
                <div className="text-center text-gray-400 py-8">加载中...</div>
              ) : ssoProviders.length === 0 ? (
                <div className="text-center text-gray-400 py-8">
                  暂无可用的单点登录配置
                </div>
              ) : (
                <div className="space-y-3">
                  {ssoProviders.map((provider) => (
                    <button
                      key={provider.provider}
                      onClick={() => handleSSOLogin(provider.provider)}
                      disabled={isLoading}
                      className="w-full py-4 px-6 bg-gray-900/50 border border-gray-700 hover:border-cyan-500/50 hover:bg-gray-800/50 rounded-lg text-white font-medium transition-all duration-200 flex items-center justify-center gap-3"
                    >
                      <svg className="w-6 h-6 text-cyan-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1" />
                      </svg>
                      <span>{provider.name}</span>
                      <span className="text-xs text-gray-500 bg-gray-800 px-2 py-1 rounded">
                        {provider.type.toUpperCase()}
                      </span>
                    </button>
                  ))}
                </div>
              )}

              <div className="text-center text-sm text-gray-500 pt-4">
                点击上方按钮将跳转至单点登录平台进行身份认证
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
