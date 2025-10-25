// 应用配置
module.exports = {
  port: process.env.PORT || 3000,
  env: process.env.NODE_ENV || 'development',
  deepSeek: {
    apiKey: process.env.DEEPSEEK_API_KEY,
    apiUrl: process.env.DEEPSEEK_API_URL || 'https://api.deepseek.com/v1'
  },
  trading: {
    initialBalance: parseFloat(process.env.INITIAL_BALANCE) || 10000,
    fee: parseFloat(process.env.TRADING_FEE) || 0.001
  }
};