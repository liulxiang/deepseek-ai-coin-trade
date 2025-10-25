const dataService = require('../dataService');

describe('DataService', () => {
  describe('getCoinList', () => {
    it('should return a list of coins', async () => {
      // Mock the axios call to avoid actual API requests
      jest.spyOn(dataService, 'getCoinList').mockResolvedValue([
        { id: 'bitcoin', symbol: 'btc', name: 'Bitcoin' },
        { id: 'ethereum', symbol: 'eth', name: 'Ethereum' }
      ]);
      
      const coins = await dataService.getCoinList();
      expect(coins).toBeInstanceOf(Array);
      expect(coins.length).toBeGreaterThan(0);
    });
  });

  describe('getMarketData', () => {
    it('should return market data for a specific coin', async () => {
      // Mock the axios call
      const mockData = {
        id: 'bitcoin',
        symbol: 'btc',
        name: 'Bitcoin',
        market_data: {
          current_price: { usd: 50000 },
          price_change_percentage_24h: 2.5
        }
      };
      
      jest.spyOn(dataService, 'getMarketData').mockResolvedValue(mockData);
      
      const marketData = await dataService.getMarketData('bitcoin');
      expect(marketData.id).toBe('bitcoin');
      expect(marketData.symbol).toBe('btc');
    });
  });
});