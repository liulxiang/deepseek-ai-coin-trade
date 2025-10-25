const databaseService = require('../services/databaseService');
const logger = require('../utils/logger');

async function testLatestData() {
  try {
    logger.info('测试获取每种加密货币的最新市场数据');
    
    // 获取每种加密货币的最新市场数据
    const latestData = await databaseService.getLatestMarketDataForEachCoin();
    logger.info(`获取到 ${latestData.length} 种加密货币的最新数据`);
    
    if (latestData.length > 0) {
      logger.info('最新市场数据:');
      latestData.forEach((data, index) => {
        logger.info(`${index + 1}. ${data.name} (${data.symbol}): 
          价格: $${data.price ? data.price.toFixed(2) : 'N/A'}
          24小时涨跌幅: ${data.price_change_percentage_24h ? data.price_change_percentage_24h.toFixed(2) : 'N/A'}%
          市值: $${data.market_cap ? (data.market_cap / 1000000000).toFixed(2) : 'N/A'}B
          24小时交易量: $${data.volume_24h ? (data.volume_24h / 1000000).toFixed(2) : 'N/A'}M
          更新时间: ${data.timestamp}`);
      });
    } else {
      logger.info('暂无市场数据');
    }
    
    logger.info('测试完成');
  } catch (error) {
    logger.error('测试过程中发生错误', error);
  } finally {
    process.exit(0);
  }
}

// 执行测试
testLatestData();