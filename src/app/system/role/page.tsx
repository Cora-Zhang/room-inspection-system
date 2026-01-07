'use client';

import { useState, useEffect } from 'react';
import MainLayout from '@/components/Layout/MainLayout';
import type { Role, Permission } from '@/types';

export default function RolePage() {
  const [roles, setRoles] = useState<Role[]>([]);
  const [permissions, setPermissions] = useState<Permission[]>([]);
  const [selectedRole, setSelectedRole] = useState<Role | null>(null);
  const [selectedPermissions, setSelectedPermissions] = useState<number[]>([]);
  const [loading, setLoading] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [modalType, setModalType] = useState<'create' | 'edit'>('create');
  const [formData, setFormData] = useState({
    name: '',
    code: '',
    description: '',
    status: 'active' as 'active' | 'inactive',
  });

  const fetchRoles = async () => {
    try {
      setLoading(true);
      const res = await fetch('/api/roles');
      const json = await res.json();
      if (json.code === 200) {
        setRoles(json.data.list);
        if (json.data.list.length > 0 && !selectedRole) {
          setSelectedRole(json.data.list[0]);
        }
      }
    } catch (error) {
      console.error('获取角色失败:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchPermissions = async () => {
    try {
      const res = await fetch('/api/permissions');
      const json = await res.json();
      if (json.code === 200) {
        setPermissions(json.data);
      }
    } catch (error) {
      console.error('获取权限失败:', error);
    }
  };

  const fetchRolePermissions = async (roleId: number) => {
    try {
      const res = await fetch(`/api/role-permissions?roleId=${roleId}`);
      const json = await res.json();
      if (json.code === 200) {
        setSelectedPermissions(json.data);
      }
    } catch (error) {
      console.error('获取角色权限失败:', error);
    }
  };

  useEffect(() => {
    fetchRoles();
    fetchPermissions();
  }, []);

  useEffect(() => {
    if (selectedRole) {
      fetchRolePermissions(selectedRole.id);
    }
  }, [selectedRole]);

  const handleCreate = () => {
    setModalType('create');
    setFormData({ name: '', code: '', description: '', status: 'active' });
    setShowModal(true);
  };

  const handleSubmit = async () => {
    try {
      const res = await fetch('/api/roles', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData),
      });
      const json = await res.json();
      if (json.code === 200) {
        setShowModal(false);
        fetchRoles();
        alert('创建成功');
      }
    } catch (error) {
      console.error('创建失败:', error);
      alert('创建失败');
    }
  };

  const handleSavePermissions = async () => {
    if (!selectedRole) return;

    try {
      const res = await fetch('/api/role-permissions', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          roleId: selectedRole.id,
          permissionIds: selectedPermissions,
        }),
      });
      const json = await res.json();
      if (json.code === 200) {
        alert('权限设置成功');
      }
    } catch (error) {
      console.error('设置权限失败:', error);
      alert('设置权限失败');
    }
  };

  const handlePermissionChange = (permissionId: number, checked: boolean) => {
    if (checked) {
      setSelectedPermissions([...selectedPermissions, permissionId]);
    } else {
      setSelectedPermissions(selectedPermissions.filter((id) => id !== permissionId));
    }
  };

  const renderPermissionTree = (permissions: Permission[], level: number = 0) => {
    return permissions.map((permission) => (
      <div key={permission.id} style={{ marginLeft: `${level * 20}px` }}>
        <div className="flex items-center gap-3 py-2 hover:bg-gray-700/30 px-2 rounded">
          <input
            type="checkbox"
            id={`perm-${permission.id}`}
            checked={selectedPermissions.includes(permission.id)}
            onChange={(e) => handlePermissionChange(permission.id, e.target.checked)}
            className="w-4 h-4 rounded border-gray-600 text-cyan-500 focus:ring-cyan-500"
          />
          <label
            htmlFor={`perm-${permission.id}`}
            className={`text-sm ${
              permission.type === 'menu' ? 'text-white font-medium' : 'text-gray-400'
            }`}
          >
            {permission.name}
          </label>
          {permission.type === 'menu' && (
            <span className="text-xs px-2 py-0.5 rounded bg-cyan-500/20 text-cyan-400">
              菜单
            </span>
          )}
          {permission.type === 'button' && (
            <span className="text-xs px-2 py-0.5 rounded bg-purple-500/20 text-purple-400">
              按钮
            </span>
          )}
          {permission.type === 'api' && (
            <span className="text-xs px-2 py-0.5 rounded bg-green-500/20 text-green-400">
              API
            </span>
          )}
        </div>
        {permission.children && permission.children.length > 0 && (
          <div>{renderPermissionTree(permission.children, level + 1)}</div>
        )}
      </div>
    ));
  };

  return (
    <MainLayout>
      <div className="space-y-6">
        {/* 页面标题 */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-cyan-400 to-blue-500 mb-2">
              角色权限管理
            </h1>
            <p className="text-gray-400">管理系统角色和权限配置</p>
          </div>
          <button
            onClick={handleCreate}
            className="px-6 py-2 bg-gradient-to-r from-cyan-500 to-blue-600 rounded-lg text-white font-medium hover:from-cyan-400 hover:to-blue-500 transition-all"
          >
            新建角色
          </button>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* 左侧：角色列表 */}
          <div
            className="relative rounded-2xl bg-gradient-to-br from-gray-800/50 to-gray-900/50 border border-cyan-500/20 backdrop-blur-sm p-4"
            style={{
              boxShadow: '0 4px 30px rgba(6, 182, 212, 0.1)',
            }}
          >
            <h3 className="text-lg font-semibold text-white mb-4">角色列表</h3>
            <div className="space-y-2 max-h-[600px] overflow-y-auto custom-scrollbar">
              {roles.map((role) => (
                <button
                  key={role.id}
                  onClick={() => setSelectedRole(role)}
                  className={`
                    w-full text-left p-4 rounded-lg transition-all
                    ${selectedRole?.id === role.id
                      ? 'bg-cyan-500/20 border border-cyan-500/30'
                      : 'hover:bg-gray-700/30 border border-transparent'
                    }
                  `}
                >
                  <div className="flex items-center justify-between">
                    <span className="font-medium text-white">{role.name}</span>
                    <span
                      className={`text-xs px-2 py-1 rounded ${
                        role.status === 'active'
                          ? 'bg-green-500/20 text-green-400'
                          : 'bg-gray-500/20 text-gray-400'
                      }`}
                    >
                      {role.status === 'active' ? '启用' : '禁用'}
                    </span>
                  </div>
                  <p className="text-xs text-gray-500 mt-1">{role.code}</p>
                  <p className="text-xs text-gray-400 mt-1">{role.description}</p>
                </button>
              ))}
            </div>
          </div>

          {/* 右侧：权限配置 */}
          <div>
            {selectedRole ? (
              <div
                className="relative rounded-2xl bg-gradient-to-br from-gray-800/50 to-gray-900/50 border border-cyan-500/20 backdrop-blur-sm p-6"
                style={{
                  boxShadow: '0 4px 30px rgba(6, 182, 212, 0.1)',
                }}
              >
                <div className="flex items-center justify-between mb-6">
                  <div>
                    <h3 className="text-lg font-semibold text-white">{selectedRole.name}</h3>
                    <p className="text-sm text-gray-400">{selectedRole.description}</p>
                  </div>
                  <button
                    onClick={handleSavePermissions}
                    className="px-4 py-2 bg-cyan-500 text-white rounded-lg hover:bg-cyan-400 transition-all"
                  >
                    保存权限
                  </button>
                </div>

                {/* 权限图例 */}
                <div className="flex gap-4 mb-4 text-xs">
                  <div className="flex items-center gap-2">
                    <div className="w-2 h-2 rounded-full bg-cyan-500"></div>
                    <span className="text-gray-400">菜单权限</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <div className="w-2 h-2 rounded-full bg-purple-500"></div>
                    <span className="text-gray-400">按钮权限</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <div className="w-2 h-2 rounded-full bg-green-500"></div>
                    <span className="text-gray-400">数据权限</span>
                  </div>
                </div>

                {/* 权限树 */}
                <div className="max-h-[500px] overflow-y-auto custom-scrollbar border border-gray-700 rounded-lg p-4">
                  {renderPermissionTree(permissions)}
                </div>
              </div>
            ) : (
              <div
                className="relative rounded-2xl bg-gradient-to-br from-gray-800/50 to-gray-900/50 border border-cyan-500/20 backdrop-blur-sm p-12 text-center"
                style={{
                  boxShadow: '0 4px 30px rgba(6, 182, 212, 0.1)',
                }}
              >
                <p className="text-gray-400">请选择左侧角色配置权限</p>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* 角色模态框 */}
      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm">
          <div className="relative w-full max-w-md rounded-2xl bg-gray-900 border border-cyan-500/30 p-6">
            <h3 className="text-lg font-semibold text-white mb-4">新建角色</h3>
            <div className="space-y-4">
              <div>
                <label className="block text-sm text-gray-400 mb-2">角色名称</label>
                <input
                  type="text"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  className="w-full px-4 py-2 bg-gray-800 border border-gray-700 rounded-lg focus:ring-2 focus:ring-cyan-500/50 focus:border-cyan-500 text-white"
                />
              </div>
              <div>
                <label className="block text-sm text-gray-400 mb-2">角色编码</label>
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
    </MainLayout>
  );
}
