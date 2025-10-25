const db = require('../config/database');
const logger = require('../utils/logger');

// 测试基本的数据库写入
logger.info('开始简单数据库测试');

// 插入测试数据
db.run(`INSERT INTO coins (id, symbol, name) VALUES (?, ?, ?)`, 
  ['testcoin', 'tst', 'Test Coin'], 
  function(err) {
    if (err) {
      logger.error('插入测试数据失败', err);
    } else {
      logger.info(`插入测试数据成功，ID: ${this.lastID}`);
    }
    
    // 查询数据
    db.all(`SELECT * FROM coins WHERE id = ?`, ['testcoin'], (err, rows) => {
      if (err) {
        logger.error('查询测试数据失败', err);
      } else {
        logger.info('查询结果:', rows);
      }
      
      // 关闭数据库
      db.close((err) => {
        if (err) {
          logger.error('关闭数据库失败', err);
        } else {
          logger.info('数据库连接已关闭');
        }
        process.exit(0);
      });
    });
  }
);