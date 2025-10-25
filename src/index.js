const express = require('express');
const dotenv = require('dotenv');
const path = require('path');
const logger = require('./utils/logger');
const apiRoutes = require('./routes/api');
const dataService = require('./services/dataService'); // 添加这一行

// 加载环境变量
dotenv.config();

// 创建Express应用
const app = express();
const PORT = process.env.PORT || 3000;

// 中间件
app.use(express.json());
app.use(express.static(path.join(__dirname, '../public')));

// API路由
app.use('/api', apiRoutes);

// 主页路由
app.get('/', (req, res) => {
  res.sendFile(path.join(__dirname, '../public/index.html'));
});

// 启动服务器
app.listen(PORT, () => {
  logger.info(`服务器运行在端口 ${PORT}`);
  
  // 启动定时数据获取任务（每30分钟）
  dataService.startPeriodicDataFetch(30);
});

// 错误处理中间件
app.use((err, req, res, next) => {
  logger.error('未处理的错误', err);
  res.status(500).json({ error: '服务器内部错误' });
});

// 处理未捕获的异常
process.on('uncaughtException', (err) => {
  logger.error('未捕获的异常', err);
  // 停止定时任务
  dataService.stopPeriodicDataFetch();
  process.exit(1);
});

// 处理未处理的Promise拒绝
process.on('unhandledRejection', (reason, promise) => {
  logger.error('未处理的Promise拒绝', { reason, promise });
  // 停止定时任务
  dataService.stopPeriodicDataFetch();
  process.exit(1);
});

// 应用关闭时的清理工作
process.on('SIGINT', () => {
  logger.info('收到SIGINT信号，正在关闭应用...');
  // 停止定时任务
  dataService.stopPeriodicDataFetch();
  process.exit(0);
});

process.on('SIGTERM', () => {
  logger.info('收到SIGTERM信号，正在关闭应用...');
  // 停止定时任务
  dataService.stopPeriodicDataFetch();
  process.exit(0);
});

module.exports = app;