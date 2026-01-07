'use client';

import { useEffect, useState, useRef } from 'react';

interface DataCardProps {
  title: string;
  value: number;
  unit?: string;
  icon: React.ReactNode;
  trend?: {
    value: number;
    isPositive: boolean;
  };
  color?: 'cyan' | 'green' | 'red' | 'purple' | 'orange';
  delay?: number;
}

export default function DataCard({
  title,
  value,
  unit = '',
  icon,
  trend,
  color = 'cyan',
  delay = 0,
}: DataCardProps) {
  const [displayValue, setDisplayValue] = useState(0);
  const [isVisible, setIsVisible] = useState(false);
  const cardRef = useRef<HTMLDivElement>(null);

  const colorClasses = {
    cyan: {
      bg: 'from-cyan-500/20 to-blue-500/20',
      border: 'border-cyan-500/30',
      iconBg: 'from-cyan-500 to-blue-600',
      shadow: 'shadow-cyan-500/20',
    },
    green: {
      bg: 'from-green-500/20 to-emerald-500/20',
      border: 'border-green-500/30',
      iconBg: 'from-green-500 to-emerald-600',
      shadow: 'shadow-green-500/20',
    },
    red: {
      bg: 'from-red-500/20 to-pink-500/20',
      border: 'border-red-500/30',
      iconBg: 'from-red-500 to-pink-600',
      shadow: 'shadow-red-500/20',
    },
    purple: {
      bg: 'from-purple-500/20 to-violet-500/20',
      border: 'border-purple-500/30',
      iconBg: 'from-purple-500 to-violet-600',
      shadow: 'shadow-purple-500/20',
    },
    orange: {
      bg: 'from-orange-500/20 to-amber-500/20',
      border: 'border-orange-500/30',
      iconBg: 'from-orange-500 to-amber-600',
      shadow: 'shadow-orange-500/20',
    },
  };

  const colors = colorClasses[color];

  useEffect(() => {
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          setIsVisible(true);
          observer.disconnect();
        }
      },
      { threshold: 0.1 }
    );

    if (cardRef.current) {
      observer.observe(cardRef.current);
    }

    return () => observer.disconnect();
  }, []);

  useEffect(() => {
    if (!isVisible) return;

    const timer = setTimeout(() => {
      let start = 0;
      const end = value;
      const duration = 2000;
      const incrementTime = 16;

      const animate = () => {
        start += incrementTime;
        const progress = Math.min(start / duration, 1);
        const easeOutQuart = 1 - Math.pow(1 - progress, 4);
        setDisplayValue(Math.floor(easeOutQuart * end));

        if (progress < 1) {
          requestAnimationFrame(animate);
        }
      };

      animate();
    }, delay);

    return () => clearTimeout(timer);
  }, [isVisible, value, delay]);

  return (
    <div
      ref={cardRef}
      className={`
        relative overflow-hidden rounded-xl bg-gradient-to-br ${colors.bg}
        border ${colors.border} backdrop-blur-sm
        transition-all duration-500 hover:scale-[1.02] hover:shadow-2xl ${colors.shadow}
        group
      `}
    >
      {/* 角落装饰 */}
      <div className="absolute top-0 left-0 w-2 h-2 border-t-2 border-l-2 border-cyan-400"></div>
      <div className="absolute top-0 right-0 w-2 h-2 border-t-2 border-r-2 border-cyan-400"></div>
      <div className="absolute bottom-0 left-0 w-2 h-2 border-b-2 border-l-2 border-cyan-400"></div>
      <div className="absolute bottom-0 right-0 w-2 h-2 border-b-2 border-r-2 border-cyan-400"></div>

      {/* 光效 */}
      <div
        className="absolute inset-0 opacity-0 group-hover:opacity-100 transition-opacity duration-500"
        style={{
          background: 'radial-gradient(circle at center, rgba(6, 182, 212, 0.1) 0%, transparent 70%)',
        }}
      ></div>

      <div className="relative p-6">
        <div className="flex items-start justify-between">
          {/* 左侧：标题和数值 */}
          <div className="flex-1">
            <p className="text-sm text-gray-400 mb-2 font-medium">{title}</p>
            <div className="flex items-baseline gap-2">
              <span
                className={`
                  text-3xl font-bold text-transparent bg-clip-text
                  bg-gradient-to-r ${colors.iconBg}
                  drop-shadow-lg
                `}
              >
                {displayValue.toLocaleString()}
              </span>
              {unit && <span className="text-sm text-gray-400">{unit}</span>}
            </div>

            {/* 趋势指示器 */}
            {trend && (
              <div className="flex items-center gap-1 mt-3">
                <svg
                  className={`w-4 h-4 ${trend.isPositive ? 'text-green-400' : 'text-red-400'}`}
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth={2}
                    d={trend.isPositive ? 'M5 10l7-7m0 0l7 7m-7-7v18' : 'M19 14l-7 7m0 0l-7-7m7 7V3'}
                  />
                </svg>
                <span
                  className={`text-sm font-medium ${trend.isPositive ? 'text-green-400' : 'text-red-400'}`}
                >
                  {trend.isPositive ? '+' : ''}
                  {trend.value}%
                </span>
                <span className="text-xs text-gray-500 ml-1">较昨日</span>
              </div>
            )}
          </div>

          {/* 右侧：图标 */}
          <div
            className={`
              w-14 h-14 rounded-xl bg-gradient-to-br ${colors.iconBg}
              flex items-center justify-center shadow-lg ${colors.shadow}
              group-hover:scale-110 transition-transform duration-300
            `}
          >
            <div className="text-white">{icon}</div>
          </div>
        </div>
      </div>
    </div>
  );
}
