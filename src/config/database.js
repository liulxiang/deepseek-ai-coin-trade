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

// 初始化数据库表
db.serialize(() => {
  // 创建市场数据表
  db.run(`CREATE TABLE IF NOT EXISTS market_data (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    coin_id TEXT NOT NULL,
    symbol TEXT NOT NULL,
    name TEXT NOT NULL,
    price REAL,
    market_cap REAL,
    volume_24h REAL,
    price_change_percentage_24h REAL,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
  )`, (err) => {
    if (err) {
      logger.error('创建market_data表失败', err);
    } else {
      logger.info('market_data表创建成功或已存在');
    }
  });

  // 创建历史价格表
  db.run(`CREATE TABLE IF NOT EXISTS historical_prices (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    coin_id TEXT NOT NULL,
    currency TEXT NOT NULL,
    price REAL NOT NULL,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
  )`, (err) => {
    if (err) {
      logger.error('创建historical_prices表失败', err);
    } else {
      logger.info('historical_prices表创建成功或已存在');
    }
  });

  // 创建交易对表
  db.run(`CREATE TABLE IF NOT EXISTS coins (
    id TEXT PRIMARY KEY,
    symbol TEXT NOT NULL,
    name TEXT NOT NULL
  )`, (err) => {
    if (err) {
      logger.error('创建coins表失败', err);
    } else {
      logger.info('coins表创建成功或已存在');
    }
  });
});

module.exports = db;