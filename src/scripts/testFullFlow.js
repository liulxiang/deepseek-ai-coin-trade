const dataService = require('../services/dataService');
const databaseService = require('../services/databaseService');
const logger = require('../utils/logger');

async function testFullFlow() {
  try {
    logger.info('开始完整流程测试');
    
    // 测试1: 获取并保存加密货币列表
    logger.info('测试1: 获取加密货币列表...');
    const coins = await dataService.getCoinList();
    logger.info(`成功获取 ${coins.length} 种加密货币`);
    
    // 测试2: 获取并保存市场数据
    logger.info('测试2: 获取比特币市场数据...');
    const bitcoinData = await dataService.getMarketData('bitcoin');
    logger.info(`成功获取比特币数据，价格: $${bitcoinData.market_data.current_price.usd}`);
    
    // 测试3: 查询数据库中的数据
    logger.info('测试3: 查询数据库中的市场数据...');
    const marketData = await databaseService.getLatestMarketData(5);
    logger.info(`数据库中有 ${marketData.length} 条市场数据`);
    
    if (marketData.length > 0) {
      logger.info('最新市场数据:');
      marketData.forEach(data => {
        logger.info(`  ${data.name} (${data.symbol}): $${data.price}`);
      });
    }
    
    // 测试4: 查询特定加密货币数据
    logger.info('测试4: 查询比特币历史数据...');
    const bitcoinHistory = await databaseService.getMarketDataByCoinId('bitcoin', 3);
    logger.info(`找到 ${bitcoinHistory.length} 条比特币数据`);
    
    logger.info('完整流程测试完成');
  } catch (error) {
    logger.error('测试过程中发生错误', error);
  } finally {
    // 关闭数据库连接
    // 注意：在实际应用中，通常不会在每次操作后关闭数据库连接
    logger.info('测试完成');
    process.exit(0);
  }
}

// 执行测试
testFullFlow();