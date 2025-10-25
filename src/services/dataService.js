const axios = require('axios');
const https = require('https');
const HttpsProxyAgent = require('https-proxy-agent');
const logger = require('../utils/logger');
const databaseService = require('./databaseService');
const { PRESET_COINS } = require('../config/coins');

// 模拟加密货币数据获取服务
class DataService {
  constructor() {
    this.baseApiUrl = 'https://api.binance.com/api/v3';
    this.presetCoins = PRESET_COINS;
    // Binance交易对映射
    this.binanceSymbols = {
      'bitcoin': 'BTCUSDT',
      'ethereum': 'ETHUSDT',
      'binancecoin': 'BNBUSDT',
      'solana': 'SOLUSDT',
      'ripple': 'XRPUSDT',
      'dogecoin': 'DOGEUSDT'
    };
    
    // 创建带代理支持的axios实例
    this.createAxiosInstance();
  }

  /**
   * 创建带代理支持的axios实例
   */
  createAxiosInstance() {
    // 检查环境变量中的代理设置
    const proxyUrl = process.env.HTTPS_PROXY || process.env.HTTP_PROXY;
    
    if (proxyUrl) {
      logger.info(`使用代理: ${proxyUrl}`);
      const agent = new HttpsProxyAgent(proxyUrl);
      
      this.axiosInstance = axios.create({
        httpsAgent: agent,
        timeout: 10000 // 10秒超时
      });
    } else {
      logger.info('未配置代理，使用直连');
      this.axiosInstance = axios.create({
        timeout: 10000 // 10秒超时
      });
    }
  }

  /**
   * 获取预设的加密货币列表
   */
  async getPresetCoinList() {
    try {
      // 直接返回预设的加密货币列表
      // 保存到数据库
      await databaseService.saveCoins(this.presetCoins);
      return this.presetCoins;
    } catch (error) {
      logger.error('获取预设加密货币列表失败', error);
      throw error;
    }
  }

  /**
   * 获取特定加密货币的市场数据
   * @param {string} coinId - 加密货币ID (如 bitcoin, ethereum)
   * @param {string} currency - 货币单位 (如 usd, eur) - 为了兼容性保留此参数
   */
  async getMarketData(coinId, currency = 'usdt') {
    try {
      // 检查是否是预设的加密货币
      const isPresetCoin = this.presetCoins.some(coin => coin.id === coinId);
      if (!isPresetCoin) {
        throw new Error(`不支持的加密货币: ${coinId}`);
      }

      // 获取Binance交易对符号
      const symbol = this.binanceSymbols[coinId];
      if (!symbol) {
        throw new Error(`不支持的加密货币: ${coinId}`);
      }

      // 从Binance API获取价格数据
      const response = await this.axiosInstance.get(`${this.baseApiUrl}/ticker/price?symbol=${symbol}`);
      
      // 从Binance API获取24小时统计信息
      const statsResponse = await this.axiosInstance.get(`${this.baseApiUrl}/ticker/24hr?symbol=${symbol}`);
      
      // 构造与之前格式兼容的数据对象
      const marketData = {
        id: coinId,
        symbol: coinId,
        name: this.presetCoins.find(coin => coin.id === coinId)?.name || coinId,
        market_data: {
          current_price: { usd: parseFloat(response.data.price) },
          price_change_percentage_24h: parseFloat(statsResponse.data.priceChangePercent),
          market_cap: { usd: parseFloat(statsResponse.data.quoteVolume) }, // 使用交易量作为市值的近似值
          total_volume: { usd: parseFloat(statsResponse.data.volume) }
        }
      };

      // 保存到数据库
      await databaseService.saveMarketData(marketData);
      
      return marketData;
    } catch (error) {
      logger.error(`获取${coinId}市场数据失败`, error);
      throw error;
    }
  }

  /**
   * 批量获取所有预设加密货币的市场数据
   */
  async getAllPresetMarketData(currency = 'usdt') {
    try {
      const marketDataList = [];
      
      // 并行获取所有预设加密货币的市场数据
      const promises = this.presetCoins.map(coin => 
        this.getMarketData(coin.id, currency)
      );
      
      const results = await Promise.allSettled(promises);
      
      results.forEach((result, index) => {
        if (result.status === 'fulfilled') {
          marketDataList.push(result.value);
        } else {
          logger.error(`获取${this.presetCoins[index].id}市场数据失败`, result.reason);
        }
      });
      
      return marketDataList;
    } catch (error) {
      logger.error('批量获取市场数据失败', error);
      throw error;
    }
  }

  /**
   * 获取加密货币历史价格数据
   * @param {string} coinId - 加密货币ID
   * @param {string} currency - 货币单位
   * @param {number} days - 天数
   */
  async getHistoricalPrices(coinId, currency = 'usdt', days = 7) {
    try {
      // 检查是否是预设的加密货币
      const isPresetCoin = this.presetCoins.some(coin => coin.id === coinId);
      if (!isPresetCoin) {
        throw new Error(`不支持的加密货币: ${coinId}`);
      }

      // 获取Binance交易对符号
      const symbol = this.binanceSymbols[coinId];
      if (!symbol) {
        throw new Error(`不支持的加密货币: ${coinId}`);
      }

      // 注意：Binance API的K线数据接口需要不同的参数格式
      // 这里我们简化实现，只返回当前价格作为历史数据的一部分
      const response = await this.axiosInstance.get(`${this.baseApiUrl}/klines?symbol=${symbol}&interval=1d&limit=${days}`);
      
      // 解析K线数据
      const prices = response.data.map(kline => [
        kline[0], // 时间戳
        parseFloat(kline[4]) // 收盘价
      ]);

      // 保存历史价格到数据库
      for (const priceData of prices) {
        const timestamp = new Date(priceData[0]);
        const price = priceData[1];
        await databaseService.saveHistoricalPrice(coinId, currency, price);
      }
      
      return {
        prices: prices
      };
    } catch (error) {
      logger.error(`获取${coinId}历史价格数据失败`, error);
      throw error;
    }
  }

  /**
   * 获取市场趋势数据
   */
  async getMarketTrends(currency = 'usdt') {
    try {
      // 获取所有预设加密货币的24小时统计信息
      const trends = [];
      
      for (const coin of this.presetCoins) {
        const symbol = this.binanceSymbols[coin.id];
        if (symbol) {
          try {
            const response = await this.axiosInstance.get(`${this.baseApiUrl}/ticker/24hr?symbol=${symbol}`);
            trends.push({
              id: coin.id,
              symbol: symbol,
              name: coin.name,
              priceChangePercent: parseFloat(response.data.priceChangePercent),
              volume: parseFloat(response.data.volume)
            });
          } catch (error) {
            logger.error(`获取${coin.id}趋势数据失败`, error);
          }
        }
      }
      
      // 按价格变化百分比排序
      trends.sort((a, b) => b.priceChangePercent - a.priceChangePercent);
      
      return {
        categories: ['trending'],
        coins: trends.slice(0, 5) // 返回前5个趋势币种
      };
    } catch (error) {
      logger.error('获取市场趋势数据失败', error);
      throw error;
    }
  }

  /**
   * 模拟实时价格更新
   * @param {string} coinId - 加密货币ID
   * @param {Function} callback - 回调函数
   */
  simulateRealTimePrices(coinId, callback) {
    // 模拟每5秒更新一次价格
    const interval = setInterval(async () => {
      try {
        const data = await this.getMarketData(coinId);
        callback(null, data);
      } catch (error) {
        callback(error, null);
      }
    }, 5000);

    return interval;
  }

  /**
   * 启动定时任务，定期获取所有预设加密货币的市场数据
   * @param {number} intervalMinutes - 间隔时间（分钟）
   */
  startPeriodicDataFetch(intervalMinutes = 30) {
    logger.info(`启动定时数据获取任务，间隔: ${intervalMinutes}分钟`);
    
    // 立即执行一次
    this.fetchAndStoreAllMarketData();
    
    // 设置定时任务
    this.periodicFetchInterval = setInterval(async () => {
      try {
        await this.fetchAndStoreAllMarketData();
      } catch (error) {
        logger.error('定时数据获取任务执行失败', error);
      }
    }, intervalMinutes * 60 * 1000); // 转换为毫秒
    
    return this.periodicFetchInterval;
  }

  /**
   * 停止定时数据获取任务
   */
  stopPeriodicDataFetch() {
    if (this.periodicFetchInterval) {
      clearInterval(this.periodicFetchInterval);
      logger.info('定时数据获取任务已停止');
    }
  }

  /**
   * 获取并存储所有预设加密货币的市场数据
   */
  async fetchAndStoreAllMarketData() {
    logger.info('开始获取并存储所有预设加密货币的市场数据');
    
    try {
      // 获取所有预设加密货币的市场数据
      const marketDataList = await this.getAllPresetMarketData();
      
      logger.info(`成功获取 ${marketDataList.length} 种加密货币的市场数据`);
      
      // 记录成功获取数据的加密货币
      const coinNames = marketDataList.map(data => data.name).join(', ');
      logger.info(`获取到的加密货币: ${coinNames}`);
      
      return marketDataList;
    } catch (error) {
      logger.error('获取并存储市场数据失败', error);
      throw error;
    }
  }
}

module.exports = new DataService();