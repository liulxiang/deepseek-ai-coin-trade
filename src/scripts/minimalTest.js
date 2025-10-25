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

// 测试基本插入
db.serialize(() => {
  logger.info('开始测试基本插入');
  
  // 创建测试表
  db.run(`CREATE TABLE IF NOT EXISTS test_table (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL
  )`, (err) => {
    if (err) {
      logger.error('创建test_table表失败', err);
    } else {
      logger.info('test_table表创建成功或已存在');
    }
  });
  
  // 插入测试数据
  const stmt = db.prepare('INSERT INTO test_table (name) VALUES (?)');
  stmt.run(['测试数据'], function(err) {
    if (err) {
      logger.error('插入测试数据失败', err);
    } else {
      logger.info(`插入测试数据成功，ID: ${this.lastID}`);
    }
  });
  stmt.finalize();
  
  // 查询测试数据
  db.all('SELECT * FROM test_table', (err, rows) => {
    if (err) {
      logger.error('查询测试数据失败', err);
    } else {
      logger.info('查询结果:', rows);
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