const db = require('../config/database');
const MarketData = require('../models/MarketData');
const logger = require('../utils/logger');

// 数据库服务类
class DatabaseService {
  // 保存市场数据
  saveMarketData(marketData) {
    return new Promise((resolve, reject) => {
      try {
        const data = new MarketData(marketData);
        const dbObject = data.toDatabaseObject();
        
        logger.debug('准备保存市场数据:', JSON.stringify(dbObject));
        
        const stmt = db.prepare(`INSERT INTO market_data 
          (coin_id, symbol, name, price, market_cap, volume_24h, price_change_percentage_24h) 
          VALUES (?, ?, ?, ?, ?, ?, ?)`);
        
        stmt.run([
          dbObject.coin_id,
          dbObject.symbol,
          dbObject.name,
          dbObject.price,
          dbObject.market_cap,
          dbObject.volume_24h,
          dbObject.price_change_percentage_24h
        ], function(err) {
          if (err) {
            logger.error('保存市场数据失败', err);
            reject(err);
          } else {
            logger.info(`市场数据保存成功，ID: ${this.lastID}`);
            resolve(this.lastID);
          }
        });
        
        stmt.finalize();
      } catch (error) {
        logger.error('保存市场数据时发生异常', error);
        reject(error);
      }
    });
  }

  // 保存历史价格数据
  saveHistoricalPrice(coinId, currency, price) {
    return new Promise((resolve, reject) => {
      try {
        const stmt = db.prepare(`INSERT INTO historical_prices 
          (coin_id, currency, price) 
          VALUES (?, ?, ?)`);
        
        stmt.run([coinId, currency, price], function(err) {
          if (err) {
            logger.error('保存历史价格数据失败', err);
            reject(err);
          } else {
            logger.info(`历史价格数据保存成功，ID: ${this.lastID}`);
            resolve(this.lastID);
          }
        });
        
        stmt.finalize();
      } catch (error) {
        logger.error('保存历史价格数据时发生异常', error);
        reject(error);
      }
    });
  }

  // 保存加密货币列表
  saveCoins(coins) {
    return new Promise((resolve, reject) => {
      try {
        logger.debug(`开始保存${coins.length}个加密货币`);
        
        // 清空现有数据
        db.run('DELETE FROM coins', (err) => {
          if (err) {
            logger.error('清空coins表失败', err);
            reject(err);
            return;
          }
          
          logger.debug('coins表清空成功');
          
          // 如果没有数据要插入，直接返回
          if (!coins || coins.length === 0) {
            logger.debug('没有加密货币数据需要保存');
            resolve(0);
            return;
          }
          
          // 使用批处理方式插入数据，避免同时处理太多数据
          const batchSize = 100;
          let insertedCount = 0;
          let batchCount = 0;
          
          const insertBatch = (startIndex) => {
            if (startIndex >= coins.length) {
              logger.info(`加密货币列表保存完成，共${insertedCount}条记录`);
              resolve(insertedCount);
              return;
            }
            
            const endIndex = Math.min(startIndex + batchSize, coins.length);
            const batch = coins.slice(startIndex, endIndex);
            
            // 构建批量插入语句
            const placeholders = batch.map(() => '(?, ?, ?)').join(', ');
            const values = [];
            batch.forEach(coin => {
              values.push(coin.id, coin.symbol, coin.name);
            });
            
            db.run(`INSERT INTO coins (id, symbol, name) VALUES ${placeholders}`, values, (err) => {
              if (err) {
                logger.error(`保存加密货币批次${batchCount + 1}失败`, err);
                reject(err);
                return;
              }
              
              insertedCount += batch.length;
              batchCount++;
              logger.debug(`加密货币批次${batchCount}保存成功，共${batch.length}条记录`);
              
              // 处理下一个批次
              insertBatch(endIndex);
            });
          };
          
          // 开始处理第一个批次
          insertBatch(0);
        });
      } catch (error) {
        logger.error('保存加密货币列表时发生异常', error);
        reject(error);
      }
    });
  }

  // 获取最新的市场数据
  getLatestMarketData(limit = 10) {
    return new Promise((resolve, reject) => {
      db.all(`SELECT * FROM market_data 
        ORDER BY timestamp DESC 
        LIMIT ?`, [limit], (err, rows) => {
        if (err) {
          logger.error('获取市场数据失败', err);
          reject(err);
        } else {
          // 将时间转换为北京时间
          const beijingTimeRows = rows.map(row => {
            return {
              ...row,
              timestamp: this.convertToBeijingTime(row.timestamp)
            };
          });
          resolve(beijingTimeRows);
        }
      });
    });
  }

  // 根据加密货币ID获取市场数据
  getMarketDataByCoinId(coinId, limit = 10) {
    return new Promise((resolve, reject) => {
      db.all(`SELECT * FROM market_data 
        WHERE coin_id = ? 
        ORDER BY timestamp DESC 
        LIMIT ?`, [coinId, limit], (err, rows) => {
        if (err) {
          logger.error(`获取${coinId}市场数据失败`, err);
          reject(err);
        } else {
          // 将时间转换为北京时间
          const beijingTimeRows = rows.map(row => {
            return {
              ...row,
              timestamp: this.convertToBeijingTime(row.timestamp)
            };
          });
          resolve(beijingTimeRows);
        }
      });
    });
  }

  // 获取每种加密货币的最新市场数据
  getLatestMarketDataForEachCoin() {
    return new Promise((resolve, reject) => {
      // 使用子查询获取每种加密货币的最新数据
      const query = `
        SELECT m1.* 
        FROM market_data m1
        INNER JOIN (
          SELECT coin_id, MAX(timestamp) as max_timestamp
          FROM market_data
          GROUP BY coin_id
        ) m2 ON m1.coin_id = m2.coin_id AND m1.timestamp = m2.max_timestamp
        ORDER BY m1.coin_id
      `;
      
      db.all(query, (err, rows) => {
        if (err) {
          logger.error('获取每种加密货币的最新市场数据失败', err);
          reject(err);
        } else {
          // 将时间转换为北京时间
          const beijingTimeRows = rows.map(row => {
            return {
              ...row,
              timestamp: this.convertToBeijingTime(row.timestamp)
            };
          });
          resolve(beijingTimeRows);
        }
      });
    });
  }

  // 将UTC时间转换为北京时间
  convertToBeijingTime(utcTime) {
    try {
      // 如果时间已经是 YYYY-MM-DD HH:MM:SS 格式，假设它已经是北京时间
      if (typeof utcTime === 'string' && /^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/.test(utcTime)) {
        return utcTime;
      }
      
      // 创建UTC时间的Date对象
      const utcDate = new Date(utcTime);
      
      // 转换为北京时间（UTC+8）
      const beijingTime = new Date(utcDate.getTime() + 8 * 60 * 60 * 1000);
      
      // 返回格式化的北京时间字符串
      return beijingTime.getFullYear() + '-' +
             String(beijingTime.getMonth() + 1).padStart(2, '0') + '-' +
             String(beijingTime.getDate()).padStart(2, '0') + ' ' +
             String(beijingTime.getHours()).padStart(2, '0') + ':' +
             String(beijingTime.getMinutes()).padStart(2, '0') + ':' +
             String(beijingTime.getSeconds()).padStart(2, '0');
    } catch (error) {
      logger.error('时间转换失败', error);
      return utcTime; // 如果转换失败，返回原始时间
    }
  }

  // 关闭数据库连接
  close() {
    return new Promise((resolve, reject) => {
      db.close((err) => {
        if (err) {
          logger.error('关闭数据库连接失败', err);
          reject(err);
        } else {
          logger.info('数据库连接已关闭');
          resolve();
        }
      });
    });
  }
}

module.exports = new DatabaseService();