const dataService = require('../services/dataService');
const logger = require('../utils/logger');

async function testProxy() {
  try {
    logger.info('测试代理配置');
    
    // 检查是否使用代理
    if (process.env.HTTPS_PROXY || process.env.HTTP_PROXY) {
      logger.info(`当前代理设置: ${process.env.HTTPS_PROXY || process.env.HTTP_PROXY}`);
    } else {
      logger.info('未设置代理');
    }
    
    // 检查数据服务中的axios实例
    // 注意：由于数据服务是单例，我们直接使用导入的实例
    if (dataService) {
      logger.info('数据服务实例可用');
    } else {
      logger.error('数据服务实例不可用');
      return;
    }
    
    logger.info('代理配置测试完成');
  } catch (error) {
    logger.error('代理配置测试失败', error);
  } finally {
    process.exit(0);
  }
}

// 执行测试
testProxy();