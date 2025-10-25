const databaseService = require('../services/databaseService');
const logger = require('../utils/logger');
const { PRESET_COINS } = require('../config/coins');

async function generateMockData() {
  try {
    logger.info('开始生成模拟市场数据');
    
    // 保存预设加密货币列表
    logger.info('保存预设加密货币列表...');
    await databaseService.saveCoins(PRESET_COINS);
    logger.info(`成功保存 ${PRESET_COINS.length} 种加密货币`);
    
    // 生成模拟市场数据
    const mockMarketData = [
      {
        id: 'bitcoin',
        symbol: 'btc',
        name: 'Bitcoin',
        market_data: {
          current_price: { usd: 50000 + Math.random() * 5000 },
          market_cap: { usd: 900000000000 + Math.random() * 100000000000 },
          total_volume: { usd: 30000000000 + Math.random() * 5000000000 },
          price_change_percentage_24h: (Math.random() - 0.5) * 10
        }
      },
      {
        id: 'ethereum',
        symbol: 'eth',
        name: 'Ethereum',
        market_data: {
          current_price: { usd: 3000 + Math.random() * 300 },
          market_cap: { usd: 300000000000 + Math.random() * 30000000000 },
          total_volume: { usd: 15000000000 + Math.random() * 2000000000 },
          price_change_percentage_24h: (Math.random() - 0.5) * 15
        }
      },
      {
        id: 'binancecoin',
        symbol: 'bnb',
        name: 'Binance Coin',
        market_data: {
          current_price: { usd: 300 + Math.random() * 30 },
          market_cap: { usd: 50000000000 + Math.random() * 5000000000 },
          total_volume: { usd: 2000000000 + Math.random() * 300000000 },
          price_change_percentage_24h: (Math.random() - 0.5) * 12
        }
      },
      {
        id: 'solana',
        symbol: 'sol',
        name: 'Solana',
        market_data: {
          current_price: { usd: 100 + Math.random() * 20 },
          market_cap: { usd: 40000000000 + Math.random() * 4000000000 },
          total_volume: { usd: 3000000000 + Math.random() * 500000000 },
          price_change_percentage_24h: (Math.random() - 0.5) * 20
        }
      },
      {
        id: 'ripple',
        symbol: 'xrp',
        name: 'Ripple',
        market_data: {
          current_price: { usd: 0.5 + Math.random() * 0.1 },
          market_cap: { usd: 25000000000 + Math.random() * 2500000000 },
          total_volume: { usd: 2000000000 + Math.random() * 300000000 },
          price_change_percentage_24h: (Math.random() - 0.5) * 8
        }
      }
    ];
    
    // 保存模拟市场数据到数据库
    logger.info('保存模拟市场数据...');
    for (const data of mockMarketData) {
      const result = await databaseService.saveMarketData(data);
      logger.info(`保存 ${data.name} 数据成功，ID: ${result}`);
    }
    
    // 查询保存的数据
    logger.info('查询保存的市场数据...');
    const marketData = await databaseService.getLatestMarketData(10);
    logger.info(`数据库中有 ${marketData.length} 条市场数据`);
    
    if (marketData.length > 0) {
      logger.info('最新市场数据:');
      marketData.forEach(data => {
        logger.info(`  ${data.name} (${data.symbol}): $${data.price.toFixed(2)} (24h: ${data.price_change_percentage_24h.toFixed(2)}%)`);
      });
    }
    
    logger.info('模拟数据生成完成');
  } catch (error) {
    logger.error('生成模拟数据过程中发生错误', error);
  } finally {
    process.exit(0);
  }
}

// 执行数据生成
generateMockData();