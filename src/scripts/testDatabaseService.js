const databaseService = require('../services/databaseService');
const logger = require('../utils/logger');

async function testDatabaseService() {
  try {
    logger.info('开始测试数据库服务');
    
    // 测试保存市场数据
    logger.info('测试保存市场数据...');
    const testData = {
      id: 'testcoin',
      symbol: 'tst',
      name: 'Test Coin',
      market_data: {
        current_price: { usd: 100.0 },
        market_cap: { usd: 1000000.0 },
        total_volume: { usd: 50000.0 },
        price_change_percentage_24h: 5.0
      }
    };
    
    const result = await databaseService.saveMarketData(testData);
    logger.info(`市场数据保存成功，ID: ${result}`);
    
    // 测试查询数据
    logger.info('测试查询市场数据...');
    const marketData = await databaseService.getLatestMarketData(5);
    logger.info(`查询到 ${marketData.length} 条市场数据`);
    
    // 测试保存加密货币列表
    logger.info('测试保存加密货币列表...');
    const coins = [
      { id: 'bitcoin', symbol: 'btc', name: 'Bitcoin' },
      { id: 'ethereum', symbol: 'eth', name: 'Ethereum' },
      { id: 'litecoin', symbol: 'ltc', name: 'Litecoin' }
    ];
    
    const coinCount = await databaseService.saveCoins(coins);
    logger.info(`加密货币列表保存成功，共${coinCount}条记录`);
    
    logger.info('数据库服务测试完成');
  } catch (error) {
    logger.error('数据库服务测试失败', error);
  } finally {
    process.exit(0);
  }
}

// 执行测试
testDatabaseService();