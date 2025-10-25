const dataService = require('../services/dataService');
const logger = require('../utils/logger');

// 模拟定时任务执行
async function simulatePeriodicTask() {
  logger.info('模拟定时任务执行');
  
  try {
    // 调用获取并存储所有市场数据的方法
    await dataService.fetchAndStoreAllMarketData();
    logger.info('定时任务执行完成');
  } catch (error) {
    logger.error('定时任务执行失败', error);
  }
}

// 执行模拟
simulatePeriodicTask();