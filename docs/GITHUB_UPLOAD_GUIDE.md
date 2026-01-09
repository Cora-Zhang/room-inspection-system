# 机房巡检系统 - GitHub上传指南

## 📋 前提条件

- ✅ 已有GitHub账号
- ✅ 本地已安装Git工具
- ✅ 项目已开发完成

---

## 🚀 步骤一：在GitHub上创建仓库

### 1. 登录GitHub

访问 [https://github.com](https://github.com) 并登录你的账号

### 2. 创建新仓库

1. 点击右上角的 `+` 按钮，选择 `New repository`
2. 填写仓库信息：
   - **Repository name**: `room-inspection-system`（建议）
   - **Description**: `机房巡检系统 - 企业级智能监控与运维管理平台`
   - **Public/Private**: 根据需要选择（推荐Private）
   - **Initialize this repository**: ⚠️ **不要勾选**任何选项（Add README、.gitignore、license等）
3. 点击 `Create repository` 按钮

### 3. 记录仓库地址

创建成功后，GitHub会显示仓库地址，有两种格式：
- **HTTPS**: `https://github.com/your-username/room-inspection-system.git`
- **SSH**: `git@github.com:your-username/room-inspection-system.git`

📌 **推荐使用SSH**（如果已配置SSH密钥）

---

## 🔧 步骤二：本地Git初始化

### 1. 配置Git用户信息（首次使用）

```bash
# 设置用户名
git config --global user.name "Your Name"

# 设置邮箱
git config --global user.email "your.email@example.com"
```

### 2. 进入项目目录

```bash
cd /workspace/projects/room-inspection-system
```

### 3. 初始化Git仓库

```bash
git init
```

### 4. 查看当前状态

```bash
git status
```

---

## 📦 步骤三：配置.gitignore

项目已包含 `.gitignore` 文件，已配置好需要忽略的文件和目录。

**.gitignore 已排除**：
- node_modules
- dist、.next 等构建产物
- 日志文件
- 环境变量文件（.env）
- IDE配置文件
- 操作系统文件

---

## ➕ 步骤四：添加文件到暂存区

### 1. 添加所有文件

```bash
git add .
```

### 2. 查看暂存状态

```bash
git status
```

你应该看到大量文件被添加到暂存区（绿色显示）。

---

## 💾 步骤五：提交代码

### 1. 首次提交

```bash
git commit -m "feat: 初始化机房巡检系统

- 完成前后端核心功能开发
- 支持用户权限管理、巡检管理、设备管理、机房管理
- 集成OAuth2.0单点登录、RBAC权限模型
- 支持SNMP、Modbus、BMS等监控协议
- 支持海康、大华门禁系统对接
- 支持钉钉、邮件、短信多渠道告警
- 科幻风格UI设计（深色主题、霓虹光效）
- 支持Docker一键部署
- 完整文档和技术方案"
```

### 2. 查看提交记录

```bash
git log --oneline
```

---

## 🔗 步骤六：关联远程仓库

### 1. 添加远程仓库地址

```bash
# 方式一：使用HTTPS（适合未配置SSH的情况）
git remote add origin https://github.com/your-username/room-inspection-system.git

# 方式二：使用SSH（推荐，如果已配置SSH密钥）
git remote add origin git@github.com:your-username/room-inspection-system.git
```

### 2. 验证远程仓库

```bash
git remote -v
```

应该显示：
```
origin  https://github.com/your-username/room-inspection-system.git (fetch)
origin  https://github.com/your-username/room-inspection-system.git (push)
```

---

## 📤 步骤七：推送到GitHub

### 1. 首次推送（设置主分支）

```bash
# 方法一：使用 -u 参数设置上游分支（推荐）
git push -u origin main

# 方法二：如果遇到分支名问题
git branch -M main
git push -u origin main
```

### 2. 输入凭证（如果使用HTTPS）

- **Username**: 你的GitHub用户名或邮箱
- **Password**: ⚠️ **不是登录密码**，而是 **Personal Access Token**

### 3. 创建Personal Access Token（如果使用HTTPS）

如果GitHub提示认证失败，需要创建Token：

1. 登录GitHub
2. 点击右上角头像 → `Settings`
3. 左侧菜单 → `Developer settings`
4. `Personal access tokens` → `Tokens (classic)`
5. `Generate new token (classic)`
6. 填写信息：
   - **Note**: `Room Inspection System`
   - **Expiration**: 选择有效期（推荐 `No expiration` 或 `90 days`）
   - **Scopes**: 勾选 `repo`（Full control of private repositories）
7. 点击 `Generate token`
8. **⚠️ 立即复制Token**（只显示一次！）

再次执行 `git push -u origin main`，输入Token作为密码。

---

## ✅ 步骤八：验证上传成功

### 1. 访问GitHub仓库

打开浏览器访问：`https://github.com/your-username/room-inspection-system`

### 2. 检查文件

确认以下内容已成功上传：
- ✅ `docs/` - 完整文档
- ✅ `java-backend/` - Java后端代码
- ✅ `src/` - Next.js前端代码
- ✅ `scripts/` - 部署脚本
- ✅ `docker-compose.full.yml` - Docker编排文件
- ✅ `.env.production.example` - 环境变量模板
- ✅ `Dockerfile.frontend` - 前端Dockerfile
- ✅ `package.json` - 前端依赖
- ✅ `pom.xml` - 后端Maven配置

### 3. 确认忽略内容正确

确认以下内容未被上传：
- ✅ `node_modules/`
- ✅ `.next/`
- ✅ `logs/`
- ✅ `.env`
- ✅ `*.log`

---

## 🔄 后续提交代码

### 添加、提交、推送

```bash
# 1. 查看修改的文件
git status

# 2. 添加修改的文件
git add .

# 3. 提交修改
git commit -m "fix: 修复某问题"

# 4. 推送到GitHub
git push
```

### 推送特定分支

```bash
# 推送当前分支
git push origin current-branch-name
```

---

## 🛠️ 常见问题

### 问题1：远程仓库地址错误

```bash
# 删除旧的远程仓库
git remote remove origin

# 添加新的远程仓库
git remote add origin https://github.com/your-username/room-inspection-system.git
```

### 问题2：推送失败（权限不足）

- 确认你有该仓库的写权限
- 检查Personal Access Token是否有效
- 确认Token是否有 `repo` 权限

### 问题3：文件太大

GitHub单个文件不能超过100MB，推荐使用 [Git LFS](https://git-lfs.github.com/)。

```bash
# 安装Git LFS（如果需要）
git lfs install

# 追踪大文件
git lfs track "*.zip"
git lfs track "*.tar.gz"
```

### 问题4：.gitignore不生效

如果文件已经被Git跟踪，.gitignore不会生效。需要先从Git中删除：

```bash
# 从Git中删除，但保留本地文件
git rm --cached -r node_modules
git commit -m "chore: 忽略node_modules"
git push
```

---

## 📚 完整命令清单

### 初始化并首次推送

```bash
# 1. 配置Git用户信息
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"

# 2. 初始化仓库
git init

# 3. 添加所有文件
git add .

# 4. 提交
git commit -m "feat: 初始化机房巡检系统"

# 5. 关联远程仓库
git remote add origin https://github.com/your-username/room-inspection-system.git

# 6. 推送
git push -u origin main
```

---

## 🎉 完成！

项目已成功上传到GitHub！🎊

下一步，请参考 [MacBook Air M1本地部署指南](./MACBOOK_M1_DEPLOYMENT.md)，在您的MacBook上完成本地部署。

---

**文档版本**: v1.0.0  
**最后更新**: 2024-01  
**适用平台**: GitHub
