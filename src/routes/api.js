const express = require('express');
const router = express.Router();
const dataService = require('../services/dataService');
const databaseService = require('../services/databaseService');
const TradingService = require('../services/tradingService');
const logger = require('../utils/logger');

// 初始化交易服务
const tradingService = new TradingService();

/**
 * 获取预设的加密货币列表
 */
router.get('/coins', async (req, res) => {
  try {
    const coins = await dataService.getPresetCoinList();
    res.json(coins);
  } catch (error) {
    logger.error('获取加密货币列表失败', error);
    res.status(500).json({ error: '获取数据失败' });
  }
});

/**
 * 获取特定加密货币的市场数据
 */
router.get('/market-data/:coinId', async (req, res) => {
  try {
    const { coinId } = req.params;
    const { currency = 'usd' } = req.query;
    
    const marketData = await dataService.getMarketData(coinId, currency);
    res.json(marketData);
  } catch (error) {
    logger.error('获取市场数据失败', error);
    res.status(500).json({ error: '获取数据失败' });
  }
});

/**
 * 批量获取所有预设加密货币的市场数据
 */
router.get('/market-data-all', async (req, res) => {
  try {
    const { currency = 'usd' } = req.query;
    
    const marketDataList = await dataService.getAllPresetMarketData(currency);
    res.json(marketDataList);
  } catch (error) {
    logger.error('批量获取市场数据失败', error);
    res.status(500).json({ error: '获取数据失败' });
  }
});

/**
 * 获取每种加密货币的最新市场数据
 */
router.get('/latest-market-data', async (req, res) => {
  try {
    // 从数据库获取每种加密货币的最新数据
    const latestData = await databaseService.getLatestMarketDataForEachCoin();
    res.json(latestData);
  } catch (error) {
    logger.error('获取最新市场数据失败', error);
    res.status(500).json({ error: '获取数据失败' });
  }
});

/**
 * 获取账户信息
 */
router.get('/account', (req, res) => {
  try {
    const accountInfo = tradingService.getAccountInfo();
    res.json(accountInfo);
  } catch (error) {
    logger.error('获取账户信息失败', error);
    res.status(500).json({ error: '获取账户信息失败' });
  }
});

/**
 * 执行交易
 */
router.post('/trade', async (req, res) => {
  try {
    const { symbol, type, quantity, price } = req.body;
    
    if (!symbol || !type || !quantity || !price) {
      return res.status(400).json({ error: '缺少必要参数' });
    }
    
    const result = await tradingService.executeTrade(symbol, type, quantity, price);
    res.json(result);
  } catch (error) {
    logger.error('执行交易失败', error);
    res.status(500).json({ error: error.message || '执行交易失败' });
  }
});

/**
 * 启动模拟交易
 */
router.post('/simulation/start', async (req, res) => {
  try {
    const { symbol, interval } = req.body;
    
    if (!symbol) {
      return res.status(400).json({ error: '缺少交易对符号' });
    }
    
    await tradingService.startSimulation(symbol, interval);
    res.json({ message: '模拟交易已启动' });
  } catch (error) {
    logger.error('启动模拟交易失败', error);
    res.status(500).json({ error: '启动模拟交易失败' });
  }
});

/**
 * 停止模拟交易
 */
router.post('/simulation/stop', (req, res) => {
  try {
    tradingService.stopSimulation();
    res.json({ message: '模拟交易已停止' });
  } catch (error) {
    logger.error('停止模拟交易失败', error);
    res.status(500).json({ error: '停止模拟交易失败' });
  }
});

module.exports = router;