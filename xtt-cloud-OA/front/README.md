# OA系统前端项目

基于Vue 3 + Vite + Element Plus的现代化前端项目，包含完整的登录系统和仪表板界面。

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
front/
├── public/                 # 静态资源
├── src/                    # 源代码
│   ├── api/               # API接口
│   ├── assets/            # 资源文件
│   ├── components/        # 公共组件
│   ├── router/            # 路由配置
│   ├── stores/            # 状态管理
│   ├── utils/             # 工具函数
│   ├── views/             # 页面组件
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
cd front
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

### 生产环境构建
```bash
npm run build
# 或
yarn build
```

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

### 2. 仪表板
- 数据统计展示
- 最近活动时间线
- 响应式布局
- 侧边栏导航

### 3. 路由管理
- 动态路由加载
- 权限控制
- 404页面处理

### 4. 状态管理
- 用户认证状态
- 全局状态管理
- 本地存储同步

## 🔧 配置说明

### 开发环境配置
- 开发服务器端口：3000
- API代理：/api -> http://localhost:8080
- 热更新：支持

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
- 数据展示组件（卡片、时间线等）
- 反馈组件（消息提示、对话框等）

## 🔐 安全特性

- 路由守卫保护
- Token自动刷新
- 请求/响应拦截
- 错误处理机制

## 🚀 部署说明

### 构建项目
```bash
npm run build
```

### 部署到服务器
将`dist`目录下的文件部署到Web服务器即可。

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
        proxy_pass http://your-backend-server:8080;
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

## 📞 技术支持

如有问题，请联系开发团队或查看项目文档。

## 📄 许可证

MIT License
