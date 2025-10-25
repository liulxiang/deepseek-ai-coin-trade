const Trade = require('../models/Trade');
const Portfolio = require('../models/Portfolio');
const logger = require('../utils/logger');
const aiService = require('./aiService');
const dataService = require('./dataService');

// 交易服务
class TradingService {
  constructor(initialBalance) {
    this.portfolio = new Portfolio(initialBalance);
    this.isSimulationRunning = false;
  }

  /**
   * 执行交易
   * @param {string} symbol - 交易对符号
   * @param {string} type - 交易类型 (buy/sell)
   * @param {number} quantity - 数量
   * @param {number} price - 价格
   */
  async executeTrade(symbol, type, quantity, price) {
    try {
      const trade = new Trade(symbol, type, quantity, price);
      
      if (type === 'buy') {
        const result = this.portfolio.buy(symbol, quantity, price);
        trade.execute();
        this.portfolio.addTradeToHistory(trade.toJSON());
        logger.info(`执行买入交易: ${symbol}, 数量: ${quantity}, 价格: ${price}`);
        return result;
      } else if (type === 'sell') {
        const result = this.portfolio.sell(symbol, quantity, price);
        trade.execute();
        this.portfolio.addTradeToHistory(trade.toJSON());
        logger.info(`执行卖出交易: ${symbol}, 数量: ${quantity}, 价格: ${price}`);
        return result;
      } else {
        throw new Error('无效的交易类型');
      }
    } catch (error) {
      logger.error('执行交易失败', error);
      throw error;
    }
  }

  /**
   * 基于AI建议执行交易
   * @param {string} symbol - 交易对符号
   * @param {Object} marketData - 市场数据
   */
  async executeAITrade(symbol, marketData) {
    try {
      // 获取AI分析结果
      const analysis = await aiService.analyzeTradingOpportunity(
        marketData, 
        this.portfolio.getAccountInfo(),
        `分析${symbol}的交易机会`
      );

      logger.info(`AI分析结果: ${analysis}`);

      // 简单解析AI建议（实际应用中需要更复杂的解析逻辑）
      const shouldBuy = analysis.toLowerCase().includes('买入') || analysis.toLowerCase().includes('buy');
      const shouldSell = analysis.toLowerCase().includes('卖出') || analysis.toLowerCase().includes('sell');
      
      if (shouldBuy) {
        // 简单策略：使用账户余额的10%买入
        const investmentAmount = this.portfolio.balance * 0.1;
        const quantity = investmentAmount / marketData.market_data.current_price.usd;
        
        if (quantity > 0) {
          return await this.executeTrade(
            symbol, 
            'buy', 
            quantity, 
            marketData.market_data.current_price.usd
          );
        }
      } else if (shouldSell) {
        // 如果有持仓则卖出
        if (this.portfolio.positions[symbol] && this.portfolio.positions[symbol].quantity > 0) {
          return await this.executeTrade(
            symbol, 
            'sell', 
            this.portfolio.positions[symbol].quantity, 
            marketData.market_data.current_price.usd
          );
        }
      }
      
      return { message: 'AI建议保持持仓不变' };
    } catch (error) {
      logger.error('执行AI交易失败', error);
      throw error;
    }
  }

  /**
   * 启动模拟交易
   * @param {string} symbol - 交易对符号
   * @param {number} interval - 交易间隔（毫秒）
   */
  async startSimulation(symbol, interval = 60000) {
    if (this.isSimulationRunning) {
      logger.warn('模拟交易已在运行中');
      return;
    }

    this.isSimulationRunning = true;
    logger.info(`启动${symbol}模拟交易，间隔${interval}毫秒`);

    // 定时执行交易决策
    this.simulationInterval = setInterval(async () => {
      try {
        const marketData = await dataService.getMarketData(symbol);
        await this.executeAITrade(symbol, marketData);
        
        // 记录当前投资组合价值
        const currentPrices = {};
        currentPrices[symbol] = marketData.market_data.current_price.usd;
        const portfolioValue = this.portfolio.getPortfolioValue(currentPrices);
        const returnRate = this.portfolio.getReturnRate(currentPrices);
        
        logger.info(`投资组合价值: $${portfolioValue.toFixed(2)}, 收益率: ${returnRate.toFixed(2)}%`);
      } catch (error) {
        logger.error('模拟交易执行失败', error);
      }
    }, interval);
  }

  /**
   * 停止模拟交易
   */
  stopSimulation() {
    if (this.simulationInterval) {
      clearInterval(this.simulationInterval);
      this.isSimulationRunning = false;
      logger.info('模拟交易已停止');
    }
  }

  /**
   * 获取账户信息
   */
  getAccountInfo() {
    return this.portfolio.getAccountInfo();
  }

  /**
   * 获取投资组合价值
   * @param {Object} currentPrices - 当前价格
   */
  getPortfolioValue(currentPrices) {
    return this.portfolio.getPortfolioValue(currentPrices);
  }

  /**
   * 获取收益率
   * @param {Object} currentPrices - 当前价格
   */
  getReturnRate(currentPrices) {
    return this.portfolio.getReturnRate(currentPrices);
  }
}

module.exports = TradingService;