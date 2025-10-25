const axios = require('axios');
const logger = require('../utils/logger');

async function testBinanceAPI() {
  try {
    logger.info('测试Binance API连接');
    
    // 测试BTCUSDT价格获取
    logger.info('测试获取BTCUSDT价格...');
    const priceResponse = await axios.get('https://api.binance.com/api/v3/ticker/price?symbol=BTCUSDT');
    logger.info(`BTC价格: ${priceResponse.data.price} USDT`);
    
    // 测试BTCUSDT 24小时统计
    logger.info('测试获取BTCUSDT 24小时统计...');
    const statsResponse = await axios.get('https://api.binance.com/api/v3/ticker/24hr?symbol=BTCUSDT');
    logger.info(`BTC 24小时价格变化: ${statsResponse.data.priceChangePercent}%`);
    logger.info(`BTC 24小时成交量: ${statsResponse.data.volume}`);
    
    // 测试ETHUSDT价格获取
    logger.info('测试获取ETHUSDT价格...');
    const ethPriceResponse = await axios.get('https://api.binance.com/api/v3/ticker/price?symbol=ETHUSDT');
    logger.info(`ETH价格: ${ethPriceResponse.data.price} USDT`);
    
    // 测试DOGEUSDT价格获取
    logger.info('测试获取DOGEUSDT价格...');
    const dogePriceResponse = await axios.get('https://api.binance.com/api/v3/ticker/price?symbol=DOGEUSDT');
    logger.info(`DOGE价格: ${dogePriceResponse.data.price} USDT`);
    
    logger.info('Binance API测试完成');
  } catch (error) {
    logger.error('Binance API测试失败', error);
  } finally {
    process.exit(0);
  }
}

// 执行测试
testBinanceAPI();