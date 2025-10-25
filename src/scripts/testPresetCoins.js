const dataService = require('../services/dataService');
const databaseService = require('../services/databaseService');
const logger = require('../utils/logger');

async function testPresetCoins() {
  try {
    logger.info('开始测试预设加密货币功能');
    
    // 测试1: 获取预设加密货币列表
    logger.info('测试1: 获取预设加密货币列表...');
    const coins = await dataService.getPresetCoinList();
    logger.info(`成功获取 ${coins.length} 种预设加密货币`);
    coins.forEach((coin, index) => {
      logger.info(`${index + 1}. ${coin.name} (${coin.symbol}) - ID: ${coin.id}`);
    });
    
    // 测试2: 获取单个加密货币的市场数据
    logger.info('测试2: 获取比特币市场数据...');
    const bitcoinData = await dataService.getMarketData('bitcoin');
    logger.info(`比特币当前价格: $${bitcoinData.market_data.current_price.usd}`);
    
    // 测试3: 批量获取所有预设加密货币的市场数据
    logger.info('测试3: 批量获取所有预设加密货币市场数据...');
    const allMarketData = await dataService.getAllPresetMarketData();
    logger.info(`成功获取 ${allMarketData.length} 种加密货币的市场数据`);
    
    // 测试4: 查询数据库中的数据
    logger.info('测试4: 查询数据库中的市场数据...');
    const marketData = await databaseService.getLatestMarketData(10);
    logger.info(`数据库中有 ${marketData.length} 条市场数据`);
    
    if (marketData.length > 0) {
      logger.info('最新市场数据:');
      marketData.forEach(data => {
        logger.info(`  ${data.name} (${data.symbol}): $${data.price}`);
      });
    }
    
    logger.info('预设加密货币功能测试完成');
  } catch (error) {
    logger.error('测试过程中发生错误', error);
  } finally {
    process.exit(0);
  }
}

// 执行测试
testPresetCoins();