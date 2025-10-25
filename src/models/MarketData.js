// 市场数据模型
class MarketData {
  constructor(data) {
    this.coin_id = data.id || '';
    this.symbol = data.symbol || '';
    this.name = data.name || '';
    this.price = data.market_data?.current_price?.usd || 0;
    this.market_cap = data.market_data?.market_cap?.usd || 0;
    this.volume_24h = data.market_data?.total_volume?.usd || 0;
    this.price_change_percentage_24h = data.market_data?.price_change_percentage_24h || 0;
    this.timestamp = new Date().toISOString();
  }

  // 转换为数据库插入对象
  toDatabaseObject() {
    return {
      coin_id: this.coin_id,
      symbol: this.symbol,
      name: this.name,
      price: this.price,
      market_cap: this.market_cap,
      volume_24h: this.volume_24h,
      price_change_percentage_24h: this.price_change_percentage_24h
    };
  }
}

module.exports = MarketData;