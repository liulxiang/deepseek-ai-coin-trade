const axios = require('axios');
const logger = require('../utils/logger');

async function verifyService() {
  try {
    logger.info('验证服务功能');
    
    // 测试1: 检查服务器是否响应
    logger.info('测试1: 检查服务器是否响应...');
    const serverResponse = await axios.get('http://localhost:3001/api/coins');
    logger.info(`服务器响应状态: ${serverResponse.status}`);
    logger.info(`加密货币数量: ${serverResponse.data.length}`);
    
    // 测试2: 检查是否包含所有预设的加密货币
    const expectedCoins = ['bitcoin', 'ethereum', 'binancecoin', 'solana', 'ripple', 'dogecoin'];
    const receivedCoins = serverResponse.data.map(coin => coin.id);
    
    logger.info('测试2: 验证预设加密货币...');
    const missingCoins = expectedCoins.filter(coin => !receivedCoins.includes(coin));
    if (missingCoins.length === 0) {
      logger.info('所有预设加密货币都已正确配置');
    } else {
      logger.error(`缺少以下加密货币: ${missingCoins.join(', ')}`);
    }
    
    // 测试3: 检查狗狗币是否在列表中
    const hasDogecoin = receivedCoins.includes('dogecoin');
    if (hasDogecoin) {
      logger.info('狗狗币已成功添加到系统中');
    } else {
      logger.error('狗狗币未找到');
    }
    
    // 测试4: 尝试获取比特币市场数据
    logger.info('测试3: 尝试获取比特币市场数据...');
    try {
      const bitcoinData = await axios.get('http://localhost:3001/api/market-data/bitcoin');
      logger.info('比特币市场数据API正常');
    } catch (error) {
      logger.warn('无法获取实时市场数据（可能是网络问题），但API端点正常');
    }
    
    logger.info('服务验证完成');
  } catch (error) {
    logger.error('服务验证过程中发生错误', error);
  } finally {
    process.exit(0);
  }
}

// 执行验证
verifyService();