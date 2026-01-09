# GitHub权限问题解决方案

## ⚠️ 当前问题

推送失败，错误信息：
```
remote: Permission to Cora-Zhang/room-inspection-system.git denied to Cora-Zhang.
fatal: unable to access 'https://github.com/Cora-Zhang/room-inspection-system.git/': The requested URL returned error: 403
```

**原因**：当前的Personal Access Token缺少 `repo` 权限。

---

## 🔧 解决方案

### 步骤1：检查现有Token权限

1. 访问：https://github.com/settings/tokens
2. 找到名为 "Room Inspection System" 的Token
3. 点击查看详情

**检查以下权限是否已勾选**：
- ✅ **repo**（Full control of private repositories） - **必须！**
- ✅ **repo:status**
- ✅ **repo_deployment**
- ✅ **public_repo**（如果是公开仓库）

### 步骤2：更新Token权限（如果可能）

某些Token可以更新权限：
- 如果Token详情页有编辑按钮，点击编辑
- 勾选 `repo` 权限
- 保存更新

### 步骤3：或重新创建Token（推荐）

如果无法更新权限，创建一个新的Token：

1. 访问：https://github.com/settings/tokens
2. 点击 `Generate new token` → `Generate new token (classic)`
3. 填写信息：
   - **Note**: `Room Inspection System - Fixed Permissions`
   - **Expiration**: 选择有效期（推荐 `90 days` 或 `No expiration`）
4. **⚠️ 关键**：勾选以下权限：
   - ✅ **repo**（Full control of private repositories）← 这个最重要！
   - ✅ **workflow**（如果要使用GitHub Actions）
   - ✅ **delete_repo**（如果需要删除仓库）
5. 点击 `Generate token`
6. **立即复制新Token**（格式：`ghp_xxxxxxxxxxxxxxxxxxxx`）

### 步骤4：提供新Token

将新Token发送给我，格式如下：

```
新的GitHub Token: ghp_xxxxxxxxxxxxxxxxxxxx
```

---

## 🎯 推送权限说明

### 推送代码到GitHub必需的权限

| 权限 | 说明 | 是否必需 |
|-----|------|---------|
| **repo** | 完整的私有仓库控制权限 | ✅ 必需 |
| repo:status | 访问提交状态 | 推荐 |
| public_repo | 访问公开仓库（如果是公开仓库） | 推荐 |
| workflow | GitHub Actions | 可选 |

### 不需要的权限

- ❌ user（用户信息）
- ❌ admin:org（组织管理）
- ❌ delete_repo（除非要删除仓库）
- ❌ gist（代码片段）

---

## 📝 Token安全提示

1. ⚠️ **不要将Token分享给他人**
2. ⚠️ **不要将Token提交到Git仓库**
3. ⚠️ **定期更换Token**
4. ⚠️ **设置合理的过期时间**

---

## 🔄 临时方案：使用SSH

如果Token问题持续存在，可以使用SSH方式推送：

### 步骤1：生成SSH密钥

在MacBook或沙箱环境执行：

```bash
# 生成ED25519密钥
ssh-keygen -t ed25519 -C "your.email@example.com"

# 启动SSH代理
eval "$(ssh-agent -s)"

# 添加密钥
ssh-add ~/.ssh/id_ed25519

# 复制公钥
cat ~/.ssh/id_ed25519.pub
```

### 步骤2：添加SSH密钥到GitHub

1. 访问：https://github.com/settings/keys
2. 点击 `New SSH key`
3. **Title**: `MacBook Air M1 - Room Inspection System`
4. **Key**: 粘贴公钥内容
5. 点击 `Add SSH key`

### 步骤3：修改远程仓库地址

```bash
# 删除HTTPS远程仓库
git remote remove origin

# 添加SSH远程仓库
git remote add origin git@github.com:Cora-Zhang/room-inspection-system.git

# 测试连接
ssh -T git@github.com

# 推送（无需密码）
git push -u origin main
```

---

## ✅ 下一步

1. **方案一**：更新或重新创建有 `repo` 权限的Token
2. **方案二**：使用SSH方式推送（更安全，无需输入密码）

请选择一种方案并执行，然后告诉我结果。

---

**当前状态**：等待新的Token或切换到SSH方式
