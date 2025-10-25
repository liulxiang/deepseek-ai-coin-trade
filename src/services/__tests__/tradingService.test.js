const TradingService = require('../tradingService');
const Trade = require('../../models/Trade');
const Portfolio = require('../../models/Portfolio');

describe('TradingService', () => {
  let tradingService;

  beforeEach(() => {
    tradingService = new TradingService(10000);
  });

  describe('executeTrade', () => {
    it('should execute a buy trade successfully', async () => {
      const result = await tradingService.executeTrade('bitcoin', 'buy', 0.1, 50000);
      
      expect(result.success).toBe(true);
      expect(result.balance).toBeLessThan(10000);
      expect(tradingService.getAccountInfo().positions.bitcoin).toBeDefined();
    });

    it('should execute a sell trade successfully', async () => {
      // First buy some bitcoin
      await tradingService.executeTrade('bitcoin', 'buy', 0.1, 50000);
      
      // Then sell it
      const result = await tradingService.executeTrade('bitcoin', 'sell', 0.05, 51000);
      
      expect(result.success).toBe(true);
      expect(result.balance).toBeGreaterThan(0);
    });

    it('should throw an error when buying with insufficient funds', async () => {
      await expect(tradingService.executeTrade('bitcoin', 'buy', 1000, 50000))
        .rejects
        .toThrow('余额不足');
    });
  });

  describe('getAccountInfo', () => {
    it('should return account information', () => {
      const accountInfo = tradingService.getAccountInfo();
      
      expect(accountInfo).toHaveProperty('balance');
      expect(accountInfo).toHaveProperty('positions');
      expect(accountInfo).toHaveProperty('history');
      expect(accountInfo.balance).toBe(10000);
    });
  });

  describe('getPortfolioValue', () => {
    it('should calculate portfolio value correctly', async () => {
      // Buy some bitcoin
      await tradingService.executeTrade('bitcoin', 'buy', 0.1, 50000);
      
      const currentPrices = { bitcoin: 51000 };
      const portfolioValue = tradingService.getPortfolioValue(currentPrices);
      
      expect(portfolioValue).toBeGreaterThan(10000);
    });
  });
});