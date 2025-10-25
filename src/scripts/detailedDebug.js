const sqlite3 = require('sqlite3').verbose();
const path = require('path');
const logger = require('../utils/logger');

// 创建数据库连接
const dbPath = path.join(__dirname, '../../data/market_data.db');
const db = new sqlite3.Database(dbPath, (err) => {
  if (err) {
    logger.error('连接SQLite数据库失败', err);
  } else {
    logger.info('连接SQLite数据库成功');
  }
});

// 测试复杂插入
db.serialize(() => {
  logger.info('开始测试复杂插入');
  
  // 插入复杂数据
  const testData = {
    coin_id: 'bitcoin',
    symbol: 'btc',
    name: 'Bitcoin',
    price: 50000.0,
    market_cap: 900000000000.0,
    volume_24h: 30000000000.0,
    price_change_percentage_24h: 2.5
  };
  
  logger.info('准备插入数据:', testData);
  
  const stmt = db.prepare(`INSERT INTO market_data 
    (coin_id, symbol, name, price, market_cap, volume_24h, price_change_percentage_24h) 
    VALUES (?, ?, ?, ?, ?, ?, ?)`);
  
  stmt.run([
    testData.coin_id,
    testData.symbol,
    testData.name,
    testData.price,
    testData.market_cap,
    testData.volume_24h,
    testData.price_change_percentage_24h
  ], function(err) {
    if (err) {
      logger.error('插入复杂数据失败', err);
    } else {
      logger.info(`插入复杂数据成功，ID: ${this.lastID}`);
    }
  });
  
  stmt.finalize();
  
  // 查询数据
  db.all('SELECT * FROM market_data ORDER BY id DESC LIMIT 1', (err, rows) => {
    if (err) {
      logger.error('查询数据失败', err);
    } else {
      logger.info('最新插入的数据:', rows);
    }
    
    // 关闭数据库
    db.close((err) => {
      if (err) {
        logger.error('关闭数据库连接失败', err);
      } else {
        logger.info('数据库连接已关闭');
      }
      process.exit(0);
    });
  });
});