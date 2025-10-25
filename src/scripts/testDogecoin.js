const dataService = require('../services/dataService');
const logger = require('../utils/logger');

async function testDogecoin() {
  try {
    logger.info('测试狗狗币配置');
    
    // 获取预设加密货币列表
    const coins = await dataService.getPresetCoinList();
    logger.info(`预设加密货币数量: ${coins.length}`);
    
    // 查找狗狗币
    const dogecoin = coins.find(coin => coin.id === 'dogecoin');
    if (dogecoin) {
      logger.info('狗狗币已成功添加到预设列表:');
      logger.info(`ID: ${dogecoin.id}`);
      logger.info(`符号: ${dogecoin.symbol}`);
      logger.info(`名称: ${dogecoin.name}`);
    } else {
      logger.error('狗狗币未找到在预设列表中');
    }
    
    // 显示所有预设加密货币
    logger.info('所有预设加密货币:');
    coins.forEach((coin, index) => {
      logger.info(`${index + 1}. ${coin.name} (${coin.symbol}) - ID: ${coin.id}`);
    });
    
    logger.info('测试完成');
  } catch (error) {
    logger.error('测试过程中发生错误', error);
  } finally {
    process.exit(0);
  }
}

// 执行测试
testDogecoin();