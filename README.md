# DeepSeek AI 驱动的加密货币交易模拟系统

一个基于DeepSeek AI的加密货币交易模拟器，允许用户测试交易策略并模拟真实市场条件下的交易。

## 功能特点

- 实时加密货币市场数据获取
- DeepSeek AI 驱动的交易策略分析
- 模拟交易执行和账户管理
- 交易历史记录和性能分析
- 可视化图表展示
- 支持多币种交易（Bitcoin, Ethereum, Binance Coin, Solana, Ripple, Dogecoin）
- 定时数据更新（每30分钟自动获取最新市场数据）
- 北京时间显示支持

## 技术栈

- **后端**: Node.js + Express.js
- **前端**: HTML5 + CSS3 + Vanilla JavaScript
- **数据库**: SQLite3
- **AI服务**: DeepSeek AI API
- **实时通信**: WebSocket
- **测试框架**: Jest
- **构建工具**: npm

## 系统架构

```
┌─────────────────┐    ┌──────────────────┐    ┌──────────────────┐
│   前端界面      │◄──►│   API服务层      │◄──►│   数据存储层     │
│  (public/)      │    │  (src/routes/)   │    │  (SQLite数据库)  │
└─────────────────┘    └──────────────────┘    └──────────────────┘
                              │                         ▲
                              ▼                         │
                    ┌──────────────────┐               │
                    │   业务逻辑层     │◄──────────────┘
                    │  (src/services/) │    外部API调用
                    └──────────────────┘    (Binance API)
                              │
                              ▼
                    ┌──────────────────┐
                    │   AI分析服务     │
                    │  (DeepSeek API)  │
                    └──────────────────┘
```

## 安装和设置

### 环境要求
- Node.js >= 14.0.0
- npm >= 6.0.0

### 安装步骤

1. 克隆项目:
   ```bash
   git clone <repository-url>
   cd dog_ai
   ```

2. 安装依赖:
   ```bash
   npm install
   ```

3. 配置环境变量:
   ```bash
   cp .env.example .env
   ```
   编辑 `.env` 文件，填写必要的配置信息

4. 启动服务:
   ```bash
   # 开发模式
   npm run dev
   
   # 生产模式
   npm start
   ```

5. 访问应用:
   打开浏览器访问 `http://localhost:3001`

## 配置说明

### 环境变量配置
```env
# DeepSeek AI 配置
DEEPSEEK_API_KEY=your_deepseek_api_key_here
DEEPSEEK_API_URL=https://api.deepseek.com/v1

# 服务器配置
PORT=3001
NODE_ENV=development

# 交易配置
INITIAL_BALANCE=10000
TRADING_FEE=0.001

# 代理配置 (可选)
HTTPS_PROXY=http://proxy.example.com:8080
HTTP_PROXY=http://proxy.example.com:8080
```

### 代理配置

如果遇到网络连接问题，可以通过设置代理来解决:

1. 在 `.env` 文件中添加代理配置:
   ```env
   HTTPS_PROXY=http://proxy.example.com:8080
   HTTP_PROXY=http://proxy.example.com:8080
   ```

2. 或者在启动时设置环境变量:
   ```bash
   HTTPS_PROXY=http://proxy.example.com:8080 npm start
   ```

## 项目结构

```
.
├── public/                 # 前端静态文件
│   ├── index.html         # 主页面
│   ├── styles.css         # 样式文件
│   └── script.js          # 前端JavaScript
├── src/                   # 后端源代码
│   ├── config/            # 配置文件
│   │   ├── app.js         # 应用配置
│   │   ├── coins.js       # 加密货币配置
│   │   └── database.js    # 数据库配置
│   ├── models/            # 数据模型
│   │   ├── MarketData.js  # 市场数据模型
│   │   ├── Portfolio.js   # 投资组合模型
│   │   └── Trade.js       # 交易模型
│   ├── routes/            # API路由
│   │   └── api.js         # API路由定义
│   ├── services/          # 核心服务
│   │   ├── aiService.js   # AI服务
│   │   ├── dataService.js # 数据服务
│   │   ├── databaseService.js # 数据库服务
│   │   └── tradingService.js # 交易服务
│   ├── utils/             # 工具函数
│   │   └── logger.js      # 日志工具
│   ├── scripts/           # 脚本文件
│   ├── __tests__/         # 测试文件
│   └── index.js           # 应用入口
├── data/                  # 数据文件
│   └── market_data.db     # SQLite数据库文件
├── .env                   # 环境变量配置文件
├── .gitignore             # Git忽略文件
├── package.json           # npm配置文件
└── README.md              # 项目说明文档
```

## 核心功能说明

### 市场数据获取
系统通过Binance API获取以下加密货币的实时市场数据：
- Bitcoin (BTC)
- Ethereum (ETH)
- Binance Coin (BNB)
- Solana (SOL)
- Ripple (XRP)
- Dogecoin (DOGE)

数据每30分钟自动更新并存储到本地SQLite数据库中。

### AI交易分析
系统集成DeepSeek AI API，提供以下功能：
- 市场趋势分析
- 交易策略建议
- 风险评估

### 模拟交易
- 支持买入/卖出操作
- 实时账户余额和持仓管理
- 交易手续费计算
- 交易历史记录

### 数据可视化
- 实时市场数据展示
- 账户信息展示
- 交易历史记录
- 性能分析图表

## API 接口

### 获取加密货币列表
```http
GET /api/coins
```

### 获取特定加密货币市场数据
```http
GET /api/market-data/:symbol
```

### 获取所有加密货币最新市场数据
```http
GET /api/market-data-all
```

### 获取每种加密货币最新市场数据
```http
GET /api/latest-market-data
```

### 获取账户信息
```http
GET /api/account
```

### 执行交易
```http
POST /api/trade
Content-Type: application/json

{
  "symbol": "bitcoin",
  "type": "buy",
  "quantity": 0.1,
  "price": 50000
}
```

### 启动模拟交易
```http
POST /api/simulation/start
Content-Type: application/json

{
  "symbol": "bitcoin",
  "interval": 60000
}
```

### 停止模拟交易
```http
POST /api/simulation/stop
```

## 定时任务

系统配置了定时任务，每30分钟自动执行以下操作：
1. 获取所有预设加密货币的最新市场数据
2. 将数据存储到SQLite数据库
3. 更新前端显示

## 时间处理

所有时间显示均采用北京时间（UTC+8），确保用户看到的时间与本地时区一致。

## 测试

运行单元测试：
```bash
npm test
```

## 部署

### 生产环境部署
```bash
npm start
```

### 开发环境部署
```bash
npm run dev
```

## 故障排除

### 常见问题

1. **无法获取市场数据**
   - 检查网络连接
   - 配置代理设置
   - 检查API密钥是否正确

2. **数据库连接失败**
   - 检查data目录权限
   - 确保SQLite3已正确安装

3. **端口被占用**
   - 修改.env文件中的PORT配置
   - 或停止占用端口的其他进程

## 许可证

MIT

## 联系方式

如有问题，请联系项目维护者。

## 风险提示与免责声明

⚠️ **炒币有风险，投资需谨慎**

本项目为加密货币交易模拟系统，仅供学习和研究使用：

- 加密货币市场具有极高的波动性，价格可能大幅上涨或下跌
- 数字货币交易存在技术风险、市场风险和监管风险
- 过往表现不代表未来收益，任何投资都可能导致损失
- 用户应根据自身财务状况和风险承受能力谨慎决策
- 本项目不对任何投资损失承担法律责任

请理性投资，避免因盲目跟风造成财产损失.
