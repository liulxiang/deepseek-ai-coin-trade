const axios = require('axios');
const databaseService = require('../services/databaseService');
const logger = require('../utils/logger');

async function testCoinList() {
  try {
    logger.info('开始测试加密货币列表获取和保存');
    
    // 从CoinGecko API获取加密货币列表
    logger.info('从CoinGecko API获取加密货币列表...');
    const response = await axios.get('https://api.coingecko.com/api/v3/coins/list');
    logger.info(`成功获取到 ${response.data.length} 种加密货币`);
    
    // 显示前10个加密货币
    logger.info('前10个加密货币:');
    response.data.slice(0, 10).forEach((coin, index) => {
      logger.info(`${index + 1}. ${coin.name} (${coin.symbol}) - ID: ${coin.id}`);
    });
    
    // 测试保存到数据库
    logger.info('测试保存到数据库...');
    const result = await databaseService.saveCoins(response.data.slice(0, 100)); // 只保存前100个
    logger.info(`成功保存 ${result} 个加密货币到数据库`);
    
    logger.info('测试完成');
  } catch (error) {
    logger.error('测试过程中发生错误', error);
  } finally {
    process.exit(0);
  }
}

// 执行测试
testCoinList();