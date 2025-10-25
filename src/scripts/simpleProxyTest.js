const axios = require('axios');
const HttpsProxyAgent = require('https-proxy-agent');
const logger = require('../utils/logger');

async function simpleProxyTest() {
  try {
    logger.info('简单代理测试');
    
    // 测试不使用代理的请求
    logger.info('测试不使用代理的请求...');
    try {
      const response = await axios.get('https://api.binance.com/api/v3/ping', { timeout: 5000 });
      logger.info('不使用代理请求成功');
    } catch (error) {
      logger.warn('不使用代理请求失败:', error.message);
    }
    
    // 如果环境变量中有代理设置，测试使用代理的请求
    const proxyUrl = process.env.HTTPS_PROXY || process.env.HTTP_PROXY;
    if (proxyUrl) {
      logger.info(`测试使用代理的请求: ${proxyUrl}`);
      try {
        const agent = new HttpsProxyAgent(proxyUrl);
        const response = await axios.get('https://api.binance.com/api/v3/ping', {
          httpsAgent: agent,
          timeout: 5000
        });
        logger.info('使用代理请求成功');
      } catch (error) {
        logger.warn('使用代理请求失败:', error.message);
      }
    } else {
      logger.info('未配置代理，跳过代理测试');
    }
    
    logger.info('简单代理测试完成');
  } catch (error) {
    logger.error('简单代理测试失败', error);
  } finally {
    process.exit(0);
  }
}

// 执行测试
simpleProxyTest();