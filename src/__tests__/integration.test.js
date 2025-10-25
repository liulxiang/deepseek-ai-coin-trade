const request = require('supertest');
const app = require('../index');

// 增加超时时间
jest.setTimeout(10000);

describe('API Integration Tests', () => {
  describe('GET /api/coins', () => {
    it('should return a list of coins', async () => {
      const response = await request(app)
        .get('/api/coins')
        .expect(200);
      
      expect(response.body).toBeInstanceOf(Array);
    }, 10000); // 设置单个测试的超时时间
  });

  describe('GET /api/account', () => {
    it('should return account information', async () => {
      const response = await request(app)
        .get('/api/account')
        .expect(200);
      
      expect(response.body).toHaveProperty('balance');
      expect(response.body).toHaveProperty('positions');
      expect(response.body).toHaveProperty('history');
    });
  });

  describe('POST /api/trade', () => {
    it('should execute a trade successfully', async () => {
      const tradeData = {
        symbol: 'bitcoin',
        type: 'buy',
        quantity: 0.1,
        price: 50000
      };

      const response = await request(app)
        .post('/api/trade')
        .send(tradeData)
        .expect(200);
      
      expect(response.body).toHaveProperty('success', true);
    });

    it('should return error for invalid trade data', async () => {
      const invalidTradeData = {
        symbol: 'bitcoin',
        type: 'buy'
        // 缺少 quantity 和 price
      };

      const response = await request(app)
        .post('/api/trade')
        .send(invalidTradeData)
        .expect(400);
    });
  });
});