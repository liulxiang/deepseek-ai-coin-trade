const axios = require('axios');
const databaseService = require('../services/databaseService');
const MarketData = require('../models/MarketData');
const logger = require('../utils/logger');

async function debugDatabase() {
  try {
    logger.info('开始调试数据库写入功能');
    
    // 直接从CoinGecko API获取数据
    logger.info('从CoinGecko API获取比特币数据...');
    const response = await axios.get('https://api.coingecko.com/api/v3/coins/bitcoin', {
      params: {
        vs_currency: 'usd',
        include_market_cap: true,
        include_24hr_vol: true,
        include_24hr_change: true,
        include_last_updated_at: true
      }
    });
    
    logger.info('API响应数据结构:');
    logger.info(`ID: ${response.data.id}`);
    logger.info(`Symbol: ${response.data.symbol}`);
    logger.info(`Name: ${response.data.name}`);
    logger.info(`Price: ${response.data.market_data?.current_price?.usd}`);
    
    // 创建MarketData对象
    logger.info('创建MarketData对象...');
    const marketData = new MarketData(response.data);
    logger.info('MarketData对象创建成功');
    logger.info(`MarketData: ${JSON.stringify(marketData)}`);
    
    // 尝试保存到数据库
    logger.info('尝试保存到数据库...');
    const result = await databaseService.saveMarketData(response.data);
    logger.info(`数据保存成功，ID: ${result}`);
    
    // 查询数据
    logger.info('查询保存的数据...');
    const savedData = await databaseService.getLatestMarketData(1);
    logger.info(`查询到 ${savedData.length} 条数据`);
    if (savedData.length > 0) {
      logger.info(`保存的数据: ${JSON.stringify(savedData[0])}`);
    }
    
    logger.info('调试完成');
  } catch (error) {
    logger.error('调试过程中发生错误', error);
  } finally {
    process.exit(0);
  }
}

// 执行调试
debugDatabase();