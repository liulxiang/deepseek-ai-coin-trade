const axios = require('axios');
const logger = require('../utils/logger');

async function checkCoinData() {
  try {
    logger.info('检查CoinGecko API返回的数据结构');
    
    // 从CoinGecko API获取加密货币列表
    logger.info('从CoinGecko API获取加密货币列表...');
    const response = await axios.get('https://api.coingecko.com/api/v3/coins/list');
    logger.info(`成功获取到 ${response.data.length} 种加密货币`);
    
    // 显示前5个加密货币的详细信息
    logger.info('前5个加密货币的详细信息:');
    response.data.slice(0, 5).forEach((coin, index) => {
      logger.info(`${index + 1}. ${JSON.stringify(coin)}`);
    });
    
    // 检查是否有重复的ID
    const ids = response.data.map(coin => coin.id);
    const uniqueIds = [...new Set(ids)];
    logger.info(`总数量: ${ids.length}, 唯一ID数量: ${uniqueIds.length}`);
    
    if (ids.length !== uniqueIds.length) {
      logger.warn('发现重复的ID');
    }
    
    logger.info('数据检查完成');
  } catch (error) {
    logger.error('数据检查过程中发生错误', error);
  } finally {
    process.exit(0);
  }
}

// 执行检查
checkCoinData();