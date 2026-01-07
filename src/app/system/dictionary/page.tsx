'use client';

import { useState, useEffect } from 'react';
import MainLayout from '@/components/Layout/MainLayout';
import type { Dictionary, DictionaryItem } from '@/types';

export default function DictionaryPage() {
  const [dictionaries, setDictionaries] = useState<Dictionary[]>([]);
  const [items, setItems] = useState<DictionaryItem[]>([]);
  const [selectedDictionary, setSelectedDictionary] = useState<Dictionary | null>(null);
  const [loading, setLoading] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [showItemModal, setShowItemModal] = useState(false);
  const [modalType, setModalType] = useState<'create' | 'edit'>('create');
  const [formData, setFormData] = useState({
    name: '',
    code: '',
    description: '',
    status: 'active' as 'active' | 'inactive',
  });
  const [itemFormData, setItemFormData] = useState({
    label: '',
    value: '',
    sort: 0,
    status: 'active' as 'active' | 'inactive',
    extra: '',
  });

  const fetchDictionaries = async () => {
    try {
      setLoading(true);
      const res = await fetch('/api/dictionaries');
      const json = await res.json();
      if (json.code === 200) {
        setDictionaries(json.data.list);
        if (json.data.list.length > 0 && !selectedDictionary) {
          setSelectedDictionary(json.data.list[0]);
        }
      }
    } catch (error) {
      console.error('获取数据字典失败:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchItems = async (dictionaryId: number) => {
    try {
      const res = await fetch(`/api/dictionary-items?dictionaryId=${dictionaryId}`);
      const json = await res.json();
      if (json.code === 200) {
        setItems(json.data.list);
      }
    } catch (error) {
      console.error('获取字典项失败:', error);
    }
  };

  useEffect(() => {
    fetchDictionaries();
  }, []);

  useEffect(() => {
    if (selectedDictionary) {
      fetchItems(selectedDictionary.id);
    }
  }, [selectedDictionary]);

  const handleCreate = () => {
    setModalType('create');
    setFormData({ name: '', code: '', description: '', status: 'active' });
    setShowModal(true);
  };

  const handleEdit = (dict: Dictionary) => {
    setModalType('edit');
    setFormData({
      name: dict.name,
      code: dict.code,
      description: dict.description || '',
      status: dict.status,
    });
    setShowModal(true);
  };

  const handleSubmit = async () => {
    try {
      const res = await fetch('/api/dictionaries', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData),
      });
      const json = await res.json();
      if (json.code === 200) {
        setShowModal(false);
        fetchDictionaries();
        alert('创建成功');
      }
    } catch (error) {
      console.error('创建失败:', error);
      alert('创建失败');
    }
  };

  const handleCreateItem = () => {
    setItemFormData({ label: '', value: '', sort: 0, status: 'active', extra: '' });
    setShowItemModal(true);
  };

  const handleItemSubmit = async () => {
    if (!selectedDictionary) return;

    try {
      const res = await fetch('/api/dictionary-items', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          ...itemFormData,
          dictionaryId: selectedDictionary.id,
        }),
      });
      const json = await res.json();
      if (json.code === 200) {
        setShowItemModal(false);
        fetchItems(selectedDictionary.id);
        alert('创建成功');
      }
    } catch (error) {
      console.error('创建失败:', error);
      alert('创建失败');
    }
  };

  return (
    <MainLayout>
      <div className="space-y-6">
        {/* 页面标题 */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-cyan-400 to-blue-500 mb-2">
              数据字典管理
            </h1>
            <p className="text-gray-400">管理系统的基础数据字典配置</p>
          </div>
          <button
            onClick={handleCreate}
            className="px-6 py-2 bg-gradient-to-r from-cyan-500 to-blue-600 rounded-lg text-white font-medium hover:from-cyan-400 hover:to-blue-500 transition-all"
          >
            新建字典
          </button>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* 左侧：字典列表 */}
          <div
            className="relative rounded-2xl bg-gradient-to-br from-gray-800/50 to-gray-900/50 border border-cyan-500/20 backdrop-blur-sm p-4"
            style={{
              boxShadow: '0 4px 30px rgba(6, 182, 212, 0.1)',
            }}
          >
            <h3 className="text-lg font-semibold text-white mb-4">字典列表</h3>
            <div className="space-y-2 max-h-[600px] overflow-y-auto custom-scrollbar">
              {dictionaries.map((dict) => (
                <button
                  key={dict.id}
                  onClick={() => setSelectedDictionary(dict)}
                  className={`
                    w-full text-left p-4 rounded-lg transition-all
                    ${selectedDictionary?.id === dict.id
                      ? 'bg-cyan-500/20 border border-cyan-500/30'
                      : 'hover:bg-gray-700/30 border border-transparent'
                    }
                  `}
                >
                  <div className="flex items-center justify-between">
                    <span className="font-medium text-white">{dict.name}</span>
                    <span
                      className={`text-xs px-2 py-1 rounded ${
                        dict.status === 'active'
                          ? 'bg-green-500/20 text-green-400'
                          : 'bg-gray-500/20 text-gray-400'
                      }`}
                    >
                      {dict.status === 'active' ? '启用' : '禁用'}
                    </span>
                  </div>
                  <p className="text-xs text-gray-500 mt-1">{dict.code}</p>
                </button>
              ))}
            </div>
          </div>

          {/* 右侧：字典项列表 */}
          <div className="lg:col-span-2">
            {selectedDictionary ? (
              <div
                className="relative rounded-2xl bg-gradient-to-br from-gray-800/50 to-gray-900/50 border border-cyan-500/20 backdrop-blur-sm p-6"
                style={{
                  boxShadow: '0 4px 30px rgba(6, 182, 212, 0.1)',
                }}
              >
                <div className="flex items-center justify-between mb-6">
                  <div>
                    <h3 className="text-lg font-semibold text-white">{selectedDictionary.name}</h3>
                    <p className="text-sm text-gray-400">{selectedDictionary.description}</p>
                  </div>
                  <button
                    onClick={handleCreateItem}
                    className="px-4 py-2 bg-cyan-500/20 text-cyan-400 rounded-lg border border-cyan-500/30 hover:bg-cyan-500/30 transition-all"
                  >
                    新增字典项
                  </button>
                </div>

                {/* 字典项列表 */}
                <div className="space-y-3">
                  {items.map((item) => (
                    <div
                      key={item.id}
                      className="flex items-center justify-between p-4 rounded-lg bg-gray-700/30 border border-gray-700 hover:border-cyan-500/30 transition-all"
                    >
                      <div className="flex items-center gap-4">
                        <span className="text-sm font-medium text-white">{item.label}</span>
                        <span className="text-xs text-gray-400">{item.value}</span>
                        {item.extra && <span className="text-xs text-gray-500">{item.extra}</span>}
                      </div>
                      <div className="flex items-center gap-2">
                        <span
                          className={`text-xs px-2 py-1 rounded ${
                            item.status === 'active'
                              ? 'bg-green-500/20 text-green-400'
                              : 'bg-gray-500/20 text-gray-400'
                          }`}
                        >
                          {item.status === 'active' ? '启用' : '禁用'}
                        </span>
                      </div>
                    </div>
                  ))}
                </div>

                {items.length === 0 && (
                  <div className="text-center py-12 text-gray-400">
                    暂无字典项，点击右上角按钮添加
                  </div>
                )}
              </div>
            ) : (
              <div
                className="relative rounded-2xl bg-gradient-to-br from-gray-800/50 to-gray-900/50 border border-cyan-500/20 backdrop-blur-sm p-12 text-center"
                style={{
                  boxShadow: '0 4px 30px rgba(6, 182, 212, 0.1)',
                }}
              >
                <p className="text-gray-400">请选择左侧字典查看详情</p>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* 字典模态框 */}
      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
          <div className="relative w-full max-w-md rounded-2xl bg-gray-900 border border-cyan-500/30 p-6">
            <h3 className="text-lg font-semibold text-white mb-4">
              {modalType === 'create' ? '新建字典' : '编辑字典'}
            </h3>
            <div className="space-y-4">
              <div>
                <label className="block text-sm text-gray-400 mb-2">字典名称</label>
                <input
                  type="text"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  className="w-full px-4 py-2 bg-gray-800 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white"
                />
              </div>
              <div>
                <label className="block text-sm text-gray-400 mb-2">字典编码</label>
                <input
                  type="text"
                  value={formData.code}
                  onChange={(e) => setFormData({ ...formData, code: e.target.value })}
                  className="w-full px-4 py-2 bg-gray-800 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white"
                />
              </div>
              <div>
                <label className="block text-sm text-gray-400 mb-2">描述</label>
                <textarea
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  className="w-full px-4 py-2 bg-gray-800 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white"
                  rows={3}
                />
              </div>
            </div>
            <div className="flex gap-3 mt-6">
              <button
                onClick={() => setShowModal(false)}
                className="flex-1 px-4 py-2 bg-gray-700 text-white rounded-lg hover:bg-gray-600 transition-all"
              >
                取消
              </button>
              <button
                onClick={handleSubmit}
                className="flex-1 px-4 py-2 bg-cyan-500 text-white rounded-lg hover:bg-cyan-400 transition-all"
              >
                确定
              </button>
            </div>
          </div>
        </div>
      )}

      {/* 字典项模态框 */}
      {showItemModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
          <div className="relative w-full max-w-md rounded-2xl bg-gray-900 border border-cyan-500/30 p-6">
            <h3 className="text-lg font-semibold text-white mb-4">新增字典项</h3>
            <div className="space-y-4">
              <div>
                <label className="block text-sm text-gray-400 mb-2">标签名称</label>
                <input
                  type="text"
                  value={itemFormData.label}
                  onChange={(e) => setItemFormData({ ...itemFormData, label: e.target.value })}
                  className="w-full px-4 py-2 bg-gray-800 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white"
                />
              </div>
              <div>
                <label className="block text-sm text-gray-400 mb-2">值</label>
                <input
                  type="text"
                  value={itemFormData.value}
                  onChange={(e) => setItemFormData({ ...itemFormData, value: e.target.value })}
                  className="w-full px-4 py-2 bg-gray-800 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white"
                />
              </div>
              <div>
                <label className="block text-sm text-gray-400 mb-2">排序</label>
                <input
                  type="number"
                  value={itemFormData.sort}
                  onChange={(e) => setItemFormData({ ...itemFormData, sort: parseInt(e.target.value) })}
                  className="w-full px-4 py-2 bg-gray-800 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white"
                />
              </div>
              <div>
                <label className="block text-sm text-gray-400 mb-2">扩展信息</label>
                <input
                  type="text"
                  value={itemFormData.extra}
                  onChange={(e) => setItemFormData({ ...itemFormData, extra: e.target.value })}
                  className="w-full px-4 py-2 bg-gray-800 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white"
                  placeholder="如: color: blue"
                />
              </div>
            </div>
            <div className="flex gap-3 mt-6">
              <button
                onClick={() => setShowItemModal(false)}
                className="flex-1 px-4 py-2 bg-gray-700 text-white rounded-lg hover:bg-gray-600 transition-all"
              >
                取消
              </button>
              <button
                onClick={handleItemSubmit}
                className="flex-1 px-4 py-2 bg-cyan-500 text-white rounded-lg hover:bg-cyan-400 transition-all"
              >
                确定
              </button>
            </div>
          </div>
        </div>
      )}
    </MainLayout>
  );
}
