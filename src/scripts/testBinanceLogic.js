const dataService = require('../services/dataService');
const logger = require('../utils/logger');

// 模拟网络请求的测试
async function testBinanceLogic() {
  try {
    logger.info('测试Binance数据服务逻辑');
    
    // 检查Binance符号映射
    logger.info('检查Binance符号映射:');
    Object.keys(dataService.binanceSymbols).forEach(coinId => {
      logger.info(`  ${coinId} -> ${dataService.binanceSymbols[coinId]}`);
    });
    
    // 检查预设加密货币
    logger.info('检查预设加密货币:');
    dataService.presetCoins.forEach(coin => {
      logger.info(`  ${coin.name} (${coin.symbol}) - ID: ${coin.id}`);
    });
    
    // 测试获取单个加密货币数据的方法（模拟）
    logger.info('测试获取市场数据方法:');
    const coinId = 'bitcoin';
    logger.info(`准备获取 ${coinId} 的市场数据`);
    logger.info(`对应的Binance符号: ${dataService.binanceSymbols[coinId]}`);
    
    logger.info('Binance数据服务逻辑测试完成');
  } catch (error) {
    logger.error('测试过程中发生错误', error);
  } finally {
    process.exit(0);
  }
}

// 执行测试
testBinanceLogic();