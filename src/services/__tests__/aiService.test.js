const aiService = require('../aiService');

// Mock axios for testing
jest.mock('axios');
const axios = require('axios');

describe('AIService', () => {
  beforeEach(() => {
    // Clear all mocks before each test
    jest.clearAllMocks();
  });

  describe('analyzeTradingOpportunity', () => {
    it('should call DeepSeek API and return analysis result', async () => {
      // Mock the API response
      const mockResponse = {
        data: {
          choices: [{
            message: {
              content: "建议买入比特币。当前价格处于支撑位，技术指标显示上涨趋势。"
            }
          }]
        }
      };
      
      axios.post.mockResolvedValue(mockResponse);
      
      const marketData = { price: 50000, trend: "up" };
      const accountData = { balance: 10000, positions: [] };
      const prompt = "分析当前是否适合买入比特币";
      
      const result = await aiService.analyzeTradingOpportunity(marketData, accountData, prompt);
      
      expect(result).toContain("建议买入");
      expect(axios.post).toHaveBeenCalled();
    });
  });

  describe('generateTradingStrategy', () => {
    it('should generate a trading strategy', async () => {
      // Mock the API response
      const mockResponse = {
        data: {
          choices: [{
            message: {
              content: "建议采用均线交叉策略。当短期均线上穿长期均线时买入，反之卖出。"
            }
          }]
        }
      };
      
      axios.post.mockResolvedValue(mockResponse);
      
      const historicalData = [{ price: 49000 }, { price: 50000 }, { price: 51000 }];
      const strategyType = "moving_average";
      
      const result = await aiService.generateTradingStrategy(historicalData, strategyType);
      
      expect(result).toContain("均线交叉策略");
      expect(axios.post).toHaveBeenCalled();
    });
  });
});