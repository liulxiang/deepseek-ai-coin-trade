const dataService = require('../services/dataService');
const logger = require('../utils/logger');

async function testPeriodicFetch() {
  try {
    logger.info('测试定时数据获取功能');
    
    // 启动定时任务（每1分钟，仅用于测试）
    const interval = dataService.startPeriodicDataFetch(1);
    logger.info('定时任务已启动，间隔1分钟');
    
    // 运行5分钟后停止
    setTimeout(() => {
      logger.info('停止定时任务');
      dataService.stopPeriodicDataFetch();
      clearInterval(interval);
      process.exit(0);
    }, 5 * 60 * 1000); // 5分钟
    
  } catch (error) {
    logger.error('测试过程中发生错误', error);
    process.exit(1);
  }
}

// 执行测试
testPeriodicFetch();