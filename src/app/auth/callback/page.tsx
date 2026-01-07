'use client';

import { useEffect, useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { http } from '@/lib/api';

export default function OAuth2CallbackPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [status, setStatus] = useState<'loading' | 'success' | 'error'>('loading');
  const [message, setMessage] = useState('');

  useEffect(() => {
    const handleCallback = async () => {
      const code = searchParams.get('code');
      const state = searchParams.get('state');
      const provider = searchParams.get('provider') || localStorage.getItem('sso_provider');

      if (!code) {
        setStatus('error');
        setMessage('未获取到授权码');
        setTimeout(() => router.push('/login'), 3000);
        return;
      }

      if (!provider) {
        setStatus('error');
        setMessage('无法确定SSO提供商');
        setTimeout(() => router.push('/login'), 3000);
        return;
      }

      try {
        setStatus('loading');
        setMessage('正在登录...');

        const response = await http.post<any>(`/auth/sso/callback/${provider}`, {
          code,
          state,
        });

        if (response.success && response.data) {
          // 保存登录信息
          localStorage.setItem('token', response.data.token);
          localStorage.setItem('refreshToken', response.data.refreshToken);
          localStorage.setItem('user', JSON.stringify(response.data.user));
          
          setStatus('success');
          setMessage('登录成功，正在跳转...');
          
          // 跳转到仪表板
          setTimeout(() => {
            const redirect = localStorage.getItem('login_redirect') || '/dashboard';
            localStorage.removeItem('login_redirect');
            router.push(redirect);
          }, 1000);
        }
      } catch (error: any) {
        console.error('OAuth2 callback error:', error);
        setStatus('error');
        setMessage(error.response?.data?.error?.message || '登录失败，请重试');
        
        setTimeout(() => {
          router.push('/login');
        }, 3000);
      }
    };

    handleCallback();
  }, [searchParams, router]);

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-gray-900 via-gray-900 to-blue-900/20">
      <div className="text-center">
        {status === 'loading' && (
          <div className="space-y-4">
            <div className="w-16 h-16 border-4 border-cyan-500/30 border-t-cyan-500 rounded-full animate-spin mx-auto"></div>
            <p className="text-xl text-white">{message}</p>
          </div>
        )}

        {status === 'success' && (
          <div className="space-y-4">
            <div className="w-16 h-16 bg-green-500 rounded-full flex items-center justify-center mx-auto">
              <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
              </svg>
            </div>
            <p className="text-xl text-white">{message}</p>
          </div>
        )}

        {status === 'error' && (
          <div className="space-y-4">
            <div className="w-16 h-16 bg-red-500 rounded-full flex items-center justify-center mx-auto">
              <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </div>
            <p className="text-xl text-white">{message}</p>
            <p className="text-sm text-gray-400">3秒后自动跳转回登录页...</p>
          </div>
        )}
      </div>
    </div>
  );
}
