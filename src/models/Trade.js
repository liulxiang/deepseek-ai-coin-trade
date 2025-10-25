// 交易模型
class Trade {
  constructor(symbol, type, quantity, price, timestamp = new Date()) {
    this.id = this.generateId();
    this.symbol = symbol;          // 交易对符号
    this.type = type;              // 交易类型: buy/sell
    this.quantity = quantity;      // 数量
    this.price = price;            // 价格
    this.timestamp = timestamp;    // 时间戳
    this.status = 'pending';       // 状态: pending/executed/cancelled
  }

  generateId() {
    return Math.random().toString(36).substr(2, 9) + Date.now().toString(36);
  }

  execute() {
    this.status = 'executed';
    return this;
  }

  cancel() {
    this.status = 'cancelled';
    return this;
  }

  getValue() {
    return this.quantity * this.price;
  }

  toJSON() {
    return {
      id: this.id,
      symbol: this.symbol,
      type: this.type,
      quantity: this.quantity,
      price: this.price,
      timestamp: this.timestamp,
      status: this.status,
      value: this.getValue()
    };
  }
}

module.exports = Trade;