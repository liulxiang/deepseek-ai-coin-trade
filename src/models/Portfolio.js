// 投资组合模型
class Portfolio {
  constructor(initialBalance = 10000) {
    this.balance = initialBalance;            // 现金余额
    this.positions = {};                      // 持仓 {symbol: {quantity, avgPrice}}
    this.history = [];                        // 交易历史
    this.initialBalance = initialBalance;     // 初始资金
  }

  /**
   * 买入资产
   * @param {string} symbol - 资产符号
   * @param {number} quantity - 数量
   * @param {number} price - 价格
   * @param {number} fee - 手续费比例
   */
  buy(symbol, quantity, price, fee = 0.001) {
    const cost = quantity * price;
    const feeAmount = cost * fee;
    const totalCost = cost + feeAmount;

    if (this.balance < totalCost) {
      throw new Error('余额不足');
    }

    this.balance -= totalCost;

    if (!this.positions[symbol]) {
      this.positions[symbol] = { quantity: 0, avgPrice: 0 };
    }

    // 更新平均价格
    const currentPositionValue = this.positions[symbol].avgPrice * this.positions[symbol].quantity;
    const newPositionValue = cost;
    const totalQuantity = this.positions[symbol].quantity + quantity;
    
    this.positions[symbol].avgPrice = (currentPositionValue + newPositionValue) / totalQuantity;
    this.positions[symbol].quantity += quantity;

    return {
      success: true,
      balance: this.balance,
      position: {...this.positions[symbol]}
    };
  }

  /**
   * 卖出资产
   * @param {string} symbol - 资产符号
   * @param {number} quantity - 数量
   * @param {number} price - 价格
   * @param {number} fee - 手续费比例
   */
  sell(symbol, quantity, price, fee = 0.001) {
    if (!this.positions[symbol] || this.positions[symbol].quantity < quantity) {
      throw new Error('持仓不足');
    }

    const revenue = quantity * price;
    const feeAmount = revenue * fee;
    const netRevenue = revenue - feeAmount;

    this.balance += netRevenue;
    this.positions[symbol].quantity -= quantity;

    // 如果持仓为0，删除该持仓
    if (this.positions[symbol].quantity === 0) {
      delete this.positions[symbol];
    }

    return {
      success: true,
      balance: this.balance,
      position: this.positions[symbol] || null
    };
  }

  /**
   * 获取持仓价值
   * @param {Object} currentPrices - 当前价格 {symbol: price}
   */
  getPortfolioValue(currentPrices) {
    let positionsValue = 0;
    
    for (const symbol in this.positions) {
      if (currentPrices[symbol]) {
        positionsValue += this.positions[symbol].quantity * currentPrices[symbol];
      }
    }
    
    return this.balance + positionsValue;
  }

  /**
   * 获取收益率
   * @param {Object} currentPrices - 当前价格 {symbol: price}
   */
  getReturnRate(currentPrices) {
    const currentValue = this.getPortfolioValue(currentPrices);
    return ((currentValue - this.initialBalance) / this.initialBalance) * 100;
  }

  /**
   * 添加交易到历史记录
   * @param {Object} trade - 交易对象
   */
  addTradeToHistory(trade) {
    this.history.push(trade);
  }

  /**
   * 获取持仓详情
   */
  getPositions() {
    return {...this.positions};
  }

  /**
   * 获取账户信息
   */
  getAccountInfo() {
    return {
      balance: this.balance,
      positions: this.getPositions(),
      history: [...this.history]
    };
  }
}

module.exports = Portfolio;