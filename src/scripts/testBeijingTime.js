const databaseService = require('../services/databaseService');
const logger = require('../utils/logger');

async function testBeijingTime() {
  try {
    logger.info('测试北京时间转换功能');
    
    // 测试时间转换方法
    const utcTime = '2025-10-25 08:00:00';
    const beijingTime = databaseService.convertToBeijingTime(utcTime);
    logger.info(`UTC时间: ${utcTime}`);
    logger.info(`北京时间: ${beijingTime}`);
    
    // 测试当前时间转换
    const nowUtc = new Date().toISOString().replace('T', ' ').substring(0, 19);
    const nowBeijing = databaseService.convertToBeijingTime(nowUtc);
    logger.info(`当前UTC时间: ${nowUtc}`);
    logger.info(`当前北京时间: ${nowBeijing}`);
    
    // 测试获取最新市场数据（应该包含北京时间）
    logger.info('测试获取最新市场数据...');
    const latestData = await databaseService.getLatestMarketData(3);
    if (latestData.length > 0) {
      logger.info('最新数据时间:');
      latestData.forEach((item, index) => {
        logger.info(`${index + 1}. ${item.name}: ${item.timestamp}`);
      });
    }
    
    logger.info('北京时间转换测试完成');
  } catch (error) {
    logger.error('测试过程中发生错误', error);
  } finally {
    process.exit(0);
  }
}

// 执行测试
testBeijingTime();