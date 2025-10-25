const axios = require('axios');
const logger = require('../utils/logger');
const config = require('../config/app');

// DeepSeek AI 服务
class AIService {
  constructor() {
    this.apiKey = config.deepSeek.apiKey;
    this.apiUrl = config.deepSeek.apiUrl;
    this.headers = {
      'Authorization': `Bearer ${this.apiKey}`,
      'Content-Type': 'application/json'
    };
  }

  /**
   * 调用 DeepSeek AI API 进行交易分析
   * @param {Object} marketData - 市场数据
   * @param {Object} accountData - 账户数据
   * @param {string} prompt - 分析提示
   */
  async analyzeTradingOpportunity(marketData, accountData, prompt) {
    try {
      const messages = [
        {
          role: "system",
          content: "你是一个专业的加密货币交易分析师。根据提供的市场数据和账户信息，给出交易建议。"
        },
        {
          role: "user",
          content: `
          市场数据: ${JSON.stringify(marketData)}
          账户数据: ${JSON.stringify(accountData)}
          分析提示: ${prompt}
          
          请提供以下信息：
          1. 交易建议（买入/卖出/持有）
          2. 建议理由
          3. 风险评估
          4. 建议交易量
          `
        }
      ];

      const response = await axios.post(
        `${this.apiUrl}/chat/completions`,
        {
          model: "deepseek-chat",
          messages: messages,
          temperature: 0.7,
          max_tokens: 500
        },
        {
          headers: this.headers
        }
      );

      return response.data.choices[0].message.content;
    } catch (error) {
      logger.error('DeepSeek AI 分析失败', error);
      throw error;
    }
  }

  /**
   * 使用 DeepSeek AI 生成交易策略
   * @param {Array} historicalData - 历史数据
   * @param {string} strategyType - 策略类型
   */
  async generateTradingStrategy(historicalData, strategyType) {
    try {
      const messages = [
        {
          role: "system",
          content: "你是一个专业的量化交易策略师。根据历史数据生成交易策略。"
        },
        {
          role: "user",
          content: `
          历史数据: ${JSON.stringify(historicalData)}
          策略类型: ${strategyType}
          
          请生成一个详细的交易策略，包括：
          1. 进入点条件
          2. 退出点条件
          3. 止损设置
          4. 止盈设置
          5. 风险管理规则
          `
        }
      ];

      const response = await axios.post(
        `${this.apiUrl}/chat/completions`,
        {
          model: "deepseek-chat",
          messages: messages,
          temperature: 0.8,
          max_tokens: 800
        },
        {
          headers: this.headers
        }
      );

      return response.data.choices[0].message.content;
    } catch (error) {
      logger.error('生成交易策略失败', error);
      throw error;
    }
  }

  /**
   * 风险评估分析
   * @param {Object} tradeData - 交易数据
   */
  async riskAssessment(tradeData) {
    try {
      const messages = [
        {
          role: "system",
          content: "你是一个专业的金融风险评估专家。对交易进行风险评估。"
        },
        {
          role: "user",
          content: `
          交易数据: ${JSON.stringify(tradeData)}
          
          请进行风险评估，包括：
          1. 风险等级（低/中/高）
          2. 主要风险因素
          3. 风险缓解建议
          4. 最大可能损失
          `
        }
      ];

      const response = await axios.post(
        `${this.apiUrl}/chat/completions`,
        {
          model: "deepseek-chat",
          messages: messages,
          temperature: 0.5,
          max_tokens: 400
        },
        {
          headers: this.headers
        }
      );

      return response.data.choices[0].message.content;
    } catch (error) {
      logger.error('风险评估失败', error);
      throw error;
    }
  }
}

module.exports = new AIService();