const dataService = require('../services/dataService');
const databaseService = require('../services/databaseService');
const logger = require('../utils/logger');

async function testDatabase() {
  try {
    logger.info('开始测试数据库功能');
    
    // 获取并保存加密货币列表
    logger.info('获取加密货币列表...');
    const coins = await dataService.getCoinList();
    logger.info(`获取到 ${coins.length} 种加密货币`);
    
    // 获取并保存比特币市场数据
    logger.info('获取比特币市场数据...');
    const bitcoinData = await dataService.getMarketData('bitcoin');
    logger.info(`比特币当前价格: $${bitcoinData.market_data.current_price.usd}`);
    
    // 获取并保存以太坊市场数据
    logger.info('获取以太坊市场数据...');
    const ethereumData = await dataService.getMarketData('ethereum');
    logger.info(`以太坊当前价格: $${ethereumData.market_data.current_price.usd}`);
    
    // 查询保存的数据
    logger.info('查询保存的市场数据...');
    const latestData = await databaseService.getLatestMarketData(5);
    logger.info(`查询到 ${latestData.length} 条市场数据`);
    
    // 查询比特币数据
    logger.info('查询比特币历史数据...');
    const bitcoinHistory = await databaseService.getMarketDataByCoinId('bitcoin', 3);
    logger.info(`查询到 ${bitcoinHistory.length} 条比特币数据`);
    
    logger.info('数据库测试完成');
  } catch (error) {
    logger.error('数据库测试失败', error);
  } finally {
    // 不在这里关闭数据库连接，因为应用可能还需要使用
    // await databaseService.close();
    logger.info('测试脚本执行完毕');
    process.exit(0);
  }
}

// 执行测试
testDatabase();