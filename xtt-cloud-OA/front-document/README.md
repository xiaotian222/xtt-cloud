# 文档管理系统前端项目

基于Vue 3 + Vite + Element Plus的现代化前端项目，用于文档管理和流程审批。

## 🚀 技术栈

- **Vue 3** - 渐进式JavaScript框架
- **Vite** - 下一代前端构建工具
- **Vue Router 4** - 官方路由管理器
- **Pinia** - Vue状态管理库
- **Element Plus** - 基于Vue 3的组件库
- **Axios** - HTTP客户端
- **CSS3** - 现代CSS特性

## 📁 项目结构

```
front-document/
├── public/                 # 静态资源
├── src/                    # 源代码
│   ├── api/               # API接口
│   │   ├── auth.js        # 认证相关接口
│   │   └── document.js    # 文档相关接口
│   ├── assets/            # 资源文件
│   ├── components/        # 公共组件
│   ├── config/            # 配置文件
│   │   └── api.config.js  # API配置
│   ├── directives/        # 自定义指令
│   │   └── permission.js  # 权限指令
│   ├── router/            # 路由配置
│   │   └── index.js       # 路由定义
│   ├── stores/            # 状态管理
│   │   └── auth.js        # 认证状态管理
│   ├── utils/             # 工具函数
│   │   ├── request.js     # Axios封装
│   │   └── token.js       # Token管理
│   ├── views/             # 页面组件
│   │   ├── layout/        # 布局组件
│   │   │   └── Index.vue  # 主布局
│   │   ├── document/      # 文档管理页面
│   │   │   └── Index.vue  # 文档列表
│   │   ├── Dashboard.vue  # 仪表板
│   │   ├── Login.vue      # 登录页
│   │   └── NotFound.vue   # 404页面
│   ├── App.vue            # 根组件
│   ├── main.js            # 入口文件
│   └── style.css          # 全局样式
├── index.html             # HTML模板
├── package.json           # 项目配置
├── vite.config.js         # Vite配置
└── README.md              # 项目说明
```

## 🛠️ 安装和运行

### 环境要求

- Node.js >= 16.0.0
- npm >= 8.0.0 或 yarn >= 1.22.0

### 安装依赖

```bash
cd front-document
npm install
# 或
yarn install
```

### 开发环境运行

```bash
npm run dev
# 或
yarn dev
```

开发服务器将在 `http://localhost:3001` 启动。

### 生产环境构建

```bash
npm run build
# 或
yarn build
```

构建产物将输出到 `dist` 目录。

### 预览构建结果

```bash
npm run preview
# 或
yarn preview
```

## 🌟 主要功能

### 1. 用户认证
- 用户登录/登出
- 表单验证
- 路由守卫
- Token管理
- 记住登录状态

### 2. 文档管理
- 文档列表展示
- 文档搜索
- 文档创建/编辑/删除
- 文档状态管理
- 分页功能

### 3. 仪表板
- 数据统计展示
- 最近文档列表
- 待办事项
- 响应式布局

### 4. 路由管理
- 动态路由加载
- 权限控制
- 404页面处理

### 5. 状态管理
- 用户认证状态
- 全局状态管理
- 本地存储同步

## 🔧 配置说明

### 开发环境配置

- 开发服务器端口：3001
- API代理：/api -> http://localhost:30010 (可通过环境变量 `VITE_GATEWAY_URL` 配置)
- 热更新：支持

### 环境变量

创建 `.env` 文件（参考 `.env.example`）：

```env
VITE_GATEWAY_URL=http://localhost:30010
```

### 生产环境配置

- 构建输出目录：dist/
- 资源压缩：启用
- Source Map：禁用

## 📱 响应式设计

项目采用移动优先的响应式设计，支持：
- 桌面端（>= 1200px）
- 平板端（>= 768px）
- 移动端（< 768px）

## 🎨 UI组件

使用Element Plus组件库，包含：
- 表单组件（输入框、按钮、复选框等）
- 导航组件（菜单、面包屑等）
- 数据展示组件（卡片、表格、标签等）
- 反馈组件（消息提示、对话框等）

## 🔐 安全特性

- 路由守卫保护
- Token自动刷新
- 请求/响应拦截
- 错误处理机制

## 📡 API接口

### 认证接口

- `POST /api/auth/login` - 用户登录
- `POST /api/auth/logout` - 用户登出
- `GET /api/auth/user` - 获取用户信息

### 文档接口

- `GET /api/document/list` - 获取文档列表
- `GET /api/document/:id` - 获取文档详情
- `POST /api/document` - 创建文档
- `PUT /api/document/:id` - 更新文档
- `DELETE /api/document/:id` - 删除文档
- `GET /api/document/flows` - 获取流程列表
- `POST /api/document/flows/start` - 启动流程

## 🚀 部署说明

### 构建项目

```bash
npm run build
```

### 部署到服务器

将 `dist` 目录下的文件部署到Web服务器即可。

### Nginx配置示例

```nginx
server {
    listen 80;
    server_name your-domain.com;
    root /path/to/dist;
    index index.html;
    
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    location /api {
        proxy_pass http://your-backend-server:30010;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 🤝 开发指南

### 代码规范

- 使用ESLint + Prettier进行代码格式化
- 遵循Vue 3 Composition API最佳实践
- 组件命名采用PascalCase
- 文件命名采用kebab-case

### 组件开发

- 优先使用组合式API
- 保持组件的单一职责
- 合理使用Props和Emits
- 添加必要的注释和文档

### 状态管理

- 使用Pinia进行状态管理
- 按功能模块划分Store
- 避免过度使用全局状态

### API调用

- 使用封装的 `request` 工具进行API调用
- 统一错误处理
- 合理使用loading状态

## 📞 技术支持

如有问题，请联系开发团队或查看项目文档。

## 📄 许可证

MIT License

