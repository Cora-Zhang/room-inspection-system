# 将项目推送到 GitHub - 详细操作步骤

## 📋 推送信息

- **GitHub 用户名**: `Cora-Zhang`
- **仓库名称**: `room-inspection-system`
- **仓库地址**: https://github.com/Cora-Zhang/room-inspection-system

---

## 🚀 方法一：在沙箱环境配置认证后推送（推荐）

### 步骤1：创建GitHub Personal Access Token

1. 登录GitHub：https://github.com
2. 点击右上角头像 → `Settings`
3. 左侧菜单 → `Developer settings`
4. `Personal access tokens` → `Tokens (classic)`
5. 点击 `Generate new token (classic)`
6. 填写信息：
   - **Note**: `Room Inspection System - Cora Zhang`
   - **Expiration**: 选择有效期（推荐 `90 days` 或 `No expiration`）
   - **Scopes**: 勾选 `repo`（Full control of private repositories）
7. 点击 `Generate token`
8. **⚠️ 重要**：立即复制生成的Token（格式类似：`ghp_xxxxxxxxxxxxxxxxxxxx`），这只显示一次！

### 步骤2：在沙箱环境配置认证

在当前沙箱环境中执行以下命令（将 `YOUR_TOKEN_HERE` 替换为刚才复制的Token）：

```bash
# 配置Git凭据存储
git config --global credential.helper store

# 使用Token推送到GitHub（替换YOUR_TOKEN_HERE）
echo "https://Cora-Zhang:YOUR_TOKEN_HERE@github.com" > ~/.git-credentials

# 推送代码
git push -u origin main
```

**示例**（假设你的Token是 `ghp_abc123def456xyz789`）：
```bash
echo "https://Cora-Zhang:ghp_abc123def456xyz789@github.com" > ~/.git-credentials
git push -u origin main
```

### 步骤3：验证推送成功

推送成功后，你会看到类似输出：
```
Enumerating objects: xxx, done.
Counting objects: 100% (xxx/xxx), done.
Delta compression using up to 4 threads
Compressing objects: 100% (xxx/xxx), done.
Writing objects: 100% (xxx/xxx), 10.00 MiB | 5.00 MiB/s, done.
Total xxx (delta 0), reused 0 (delta 0), pack-reused 0
To https://github.com/Cora-Zhang/room-inspection-system.git
 * [new branch]      main -> main
```

---

## 💻 方法二：在本地MacBook上完成推送（最简单）

### 步骤1：在GitHub上创建仓库

1. 访问 https://github.com/new
2. 填写仓库信息：
   - **Repository name**: `room-inspection-system`
   - **Description**: `机房巡检系统 - 企业级智能监控与运维管理平台`
   - **Public/Private**: 根据需要选择
   - **⚠️ 不要勾选** "Initialize this repository with" 的任何选项
3. 点击 `Create repository`

### 步骤2：下载项目到本地

在MacBook终端执行：

```bash
# 从沙箱下载项目（如果提供HTTP下载）
# 或使用SCP/SFTP从沙箱传输

# 或者直接使用git clone（如果已推送到其他位置）
# git clone https://your-temp-repo.com/room-inspection-system.git
cd room-inspection-system
```

### 步骤3：在MacBook上配置Git

```bash
# 配置Git用户信息（如果尚未配置）
git config --global user.name "Cora Zhang"
git config --global user.email "your.email@example.com"
```

### 步骤4：在MacBook上推送到GitHub

```bash
# 检查当前状态
git status

# 查看远程仓库
git remote -v

# 如果没有远程仓库，添加
git remote add origin https://github.com/Cora-Zhang/room-inspection-system.git

# 推送到GitHub
git push -u origin main

# 输入GitHub用户名和密码
# Username: Cora-Zhang
# Password: （输入刚才创建的Personal Access Token）
```

---

## 🔐 方法三：使用SSH方式推送（如果你已配置SSH密钥）

### 步骤1：检查SSH密钥

在MacBook终端执行：

```bash
# 检查是否已有SSH密钥
ls -la ~/.ssh/id_rsa.pub
ls -la ~/.ssh/id_ed25519.pub
```

### 步骤2：生成SSH密钥（如果没有）

```bash
# 生成ED25519密钥（推荐）
ssh-keygen -t ed25519 -C "your.email@example.com"

# 或生成RSA密钥
ssh-keygen -t rsa -b 4096 -C "your.email@example.com"

# 启动SSH代理
eval "$(ssh-agent -s)"

# 添加密钥到代理
ssh-add ~/.ssh/id_ed25519
```

### 步骤3：添加SSH密钥到GitHub

```bash
# 复制公钥
cat ~/.ssh/id_ed25519.pub

# 或使用pbcopy（macOS）
cat ~/.ssh/id_ed25519.pub | pbcopy
```

1. 访问 https://github.com/settings/keys
2. 点击 `New SSH key`
3. **Title**: 填写 `MacBook Air M1 - Cora Zhang`
4. **Key**: 粘贴刚才复制的公钥内容
5. 点击 `Add SSH key`

### 步骤4：测试SSH连接

```bash
# 测试SSH连接
ssh -T git@github.com
```

应该看到：
```
Hi Cora-Zhang! You've successfully authenticated, but GitHub does not provide shell access.
```

### 步骤5：修改远程仓库地址为SSH

```bash
# 删除旧的远程仓库
git remote remove origin

# 添加SSH远程仓库
git remote add origin git@github.com:Cora-Zhang/room-inspection-system.git

# 推送代码（无需密码）
git push -u origin main
```

---

## ✅ 验证推送成功

### 1. 在GitHub上查看

访问：https://github.com/Cora-Zhang/room-inspection-system

应该看到：
- ✅ 仓库已创建
- ✅ 所有文件已上传
- ✅ README.md 显示
- ✅ docs/ 目录包含所有文档
- ✅ java-backend/ 目录包含后端代码
- ✅ src/ 目录包含前端代码

### 2. 检查关键文件

确认以下文件已成功上传：

**文档**:
- ✅ docs/GITHUB_UPLOAD_GUIDE.md
- ✅ docs/MACBOOK_M1_DEPLOYMENT.md
- ✅ docs/QUICK_REFERENCE.md
- ✅ docs/FINAL_DEPLOYMENT_GUIDE.md
- ✅ docs/TECHNICAL_SOLUTION.md
- ✅ docs/PROJECT_SUMMARY.md

**配置**:
- ✅ docker-compose.full.yml
- ✅ .env.production.example
- ✅ Dockerfile.frontend
- ✅ .gitignore

**代码**:
- ✅ java-backend/src/main/resources/sql/init.sql
- ✅ java-backend/pom.xml
- ✅ src/app/page.tsx
- ✅ package.json
- ✅ scripts/deploy.sh

---

## 🔄 后续更新代码

### 在沙箱环境修改后推送

```bash
# 添加修改
git add .

# 提交修改
git commit -m "fix: 修复某问题"

# 推送到GitHub
git push
```

### 在MacBook上拉取最新代码

```bash
# 拉取最新代码
git pull origin main

# 或拉取并合并
git fetch origin
git merge origin/main
```

---

## 🛠️ 常见问题

### 问题1：推送失败 - Authentication failed

**错误信息**:
```
remote: Invalid username or password.
fatal: Authentication failed for 'https://github.com/...'
```

**解决方案**:
- 确认Personal Access Token是否正确
- 确认Token是否包含 `repo` 权限
- 确认Token是否已过期

### 问题2：推送失败 - Repository not found

**错误信息**:
```
remote: Repository not found.
fatal: repository 'https://github.com/Cora-Zhang/room-inspection-system.git/' not found
```

**解决方案**:
- 确认仓库名称是否正确
- 确认你是否是仓库的成员
- 检查仓库是否为Private且你没有权限

### 问题3：推送被拒绝

**错误信息**:
```
! [rejected] main -> main (fetch first)
error: failed to push some refs to 'https://github.com/...'
```

**解决方案**:
```bash
# 先拉取远程代码
git pull origin main --rebase

# 重新推送
git push origin main
```

### 问题4：文件太大

**错误信息**:
```
error: RPC failed; curl 56 SSL read: error:00000000:lib(0):func(0):reason(0)
```

**解决方案**:
```bash
# 增加缓冲区大小
git config --global http.postBuffer 524288000

# 使用Git LFS（如果需要）
git lfs install
git lfs track "*.zip"
git lfs track "*.tar.gz"
```

---

## 📝 推送清单

上传前确认：
- [ ] 已在GitHub创建仓库 `Cora-Zhang/room-inspection-system`
- [ ] 已创建Personal Access Token（包含repo权限）
- [ ] Git用户信息已配置
- [ ] 项目根目录在Git仓库中
- [ ] 所有文件已提交
- [ ] .gitignore 配置正确

上传后验证：
- [ ] GitHub仓库页面可访问
- [ ] 所有文件已上传
- [ ] 关键目录结构完整
- [ ] 文档可正常查看
- [ ] README 显示正确

---

## 🎉 完成！

项目已成功推送到你的GitHub账号！

**仓库地址**: https://github.com/Cora-Zhang/room-inspection-system

下一步，参考 [MacBook Air M1本地部署指南](./MACBOOK_M1_DEPLOYMENT.md)，在你的MacBook上克隆项目并完成本地部署：

```bash
git clone https://github.com/Cora-Zhang/room-inspection-system.git
cd room-inspection-system
cp .env.production.example .env.production
docker-compose -f docker-compose.full.yml up -d
```

---

**文档版本**: v1.0.0
**GitHub 用户**: Cora-Zhang
**仓库名称**: room-inspection-system
