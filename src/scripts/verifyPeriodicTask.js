const dataService = require('../services/dataService');
const logger = require('../utils/logger');

async function verifyPeriodicTask() {
  try {
    logger.info('验证定时任务功能');
    
    // 检查数据服务中的方法是否存在
    if (typeof dataService.startPeriodicDataFetch === 'function') {
      logger.info('startPeriodicDataFetch 方法存在');
    } else {
      logger.error('startPeriodicDataFetch 方法不存在');
      return;
    }
    
    if (typeof dataService.stopPeriodicDataFetch === 'function') {
      logger.info('stopPeriodicDataFetch 方法存在');
    } else {
      logger.error('stopPeriodicDataFetch 方法不存在');
      return;
    }
    
    if (typeof dataService.fetchAndStoreAllMarketData === 'function') {
      logger.info('fetchAndStoreAllMarketData 方法存在');
    } else {
      logger.error('fetchAndStoreAllMarketData 方法不存在');
      return;
    }
    
    // 测试启动定时任务
    logger.info('测试启动定时任务...');
    const interval = dataService.startPeriodicDataFetch(0.1); // 6秒间隔（用于测试）
    logger.info('定时任务已启动');
    
    // 等待一段时间后停止
    setTimeout(() => {
      logger.info('停止定时任务');
      dataService.stopPeriodicDataFetch();
      clearInterval(interval);
      logger.info('验证完成');
      process.exit(0);
    }, 20000); // 20秒后停止
    
  } catch (error) {
    logger.error('验证过程中发生错误', error);
    process.exit(1);
  }
}

// 执行验证
verifyPeriodicTask();