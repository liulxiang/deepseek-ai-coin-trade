// 全局变量
let accountData = null;
let marketData = null;
let latestMarketData = null;

// DOM元素
const coinSelect = document.getElementById('coin-select');
const refreshDataBtn = document.getElementById('refresh-data');
const accountDataDiv = document.getElementById('account-data');
const marketDataDiv = document.getElementById('market-data');
const executeTradeBtn = document.getElementById('execute-trade');
const aiTradeBtn = document.getElementById('ai-trade');
const aiAnalysisDiv = document.getElementById('ai-analysis');
const startSimulationBtn = document.getElementById('start-simulation');
const stopSimulationBtn = document.getElementById('stop-simulation');
const tradeHistoryDiv = document.getElementById('trade-history');
const latestMarketDataDiv = document.getElementById('latest-market-data');

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', function() {
    // 初始化数据
    loadAccountData();
    loadMarketData(coinSelect.value);
    loadLatestMarketData(); // 加载最新市场数据
    
    // 设置事件监听器
    setupEventListeners();
});

// 设置事件监听器
function setupEventListeners() {
    // 选择加密货币
    coinSelect.addEventListener('change', function() {
        loadMarketData(this.value);
    });
    
    // 刷新数据
    refreshDataBtn.addEventListener('click', function() {
        loadMarketData(coinSelect.value);
        loadLatestMarketData(); // 同时刷新最新市场数据
    });
    
    // 执行交易
    executeTradeBtn.addEventListener('click', function() {
        executeManualTrade();
    });
    
    // AI交易
    aiTradeBtn.addEventListener('click', function() {
        executeAITrade();
    });
    
    // 开始模拟
    startSimulationBtn.addEventListener('click', function() {
        startSimulation();
    });
    
    // 停止模拟
    stopSimulationBtn.addEventListener('click', function() {
        stopSimulation();
    });
}

// 加载账户数据
async function loadAccountData() {
    try {
        const response = await fetch('/api/account');
        if (response.ok) {
            accountData = await response.json();
            renderAccountData(accountData);
        } else {
            throw new Error('获取账户数据失败');
        }
    } catch (error) {
        console.error('加载账户数据失败:', error);
        accountDataDiv.innerHTML = '<p>加载账户数据失败</p>';
    }
}

// 渲染账户数据
function renderAccountData(data) {
    let html = `
        <div class="account-balance">
            <h3>账户余额: $${data.balance.toFixed(2)}</h3>
        </div>
        <div class="account-positions">
            <h3>持仓情况:</h3>
    `;
    
    if (Object.keys(data.positions).length === 0) {
        html += '<p>暂无持仓</p>';
    } else {
        for (const symbol in data.positions) {
            const position = data.positions[symbol];
            html += `
                <div class="position-item">
                    <span>${symbol}:</span>
                    <span>数量: ${position.quantity.toFixed(6)}</span>
                </div>
            `;
        }
    }
    
    html += '</div>';
    accountDataDiv.innerHTML = html;
}

// 加载市场数据
async function loadMarketData(coinId) {
    try {
        marketDataDiv.innerHTML = '<p>加载中...</p>';
        
        const response = await fetch(`/api/market-data/${coinId}`);
        if (response.ok) {
            marketData = await response.json();
            renderMarketData(marketData);
        } else {
            throw new Error('获取市场数据失败');
        }
    } catch (error) {
        console.error('加载市场数据失败:', error);
        marketDataDiv.innerHTML = '<p>加载市场数据失败</p>';
    }
}

// 渲染市场数据
function renderMarketData(data) {
    const price = data.market_data.current_price.usd;
    const priceChange24h = data.market_data.price_change_percentage_24h;
    const marketCap = data.market_data.market_cap.usd;
    const volume24h = data.market_data.total_volume.usd;
    
    const priceChangeClass = priceChange24h >= 0 ? 'positive' : 'negative';
    const priceChangeSymbol = priceChange24h >= 0 ? '+' : '';
    
    const html = `
        <div class="market-data-item">
            <span>当前价格:</span>
            <span>$${price.toLocaleString()}</span>
        </div>
        <div class="market-data-item">
            <span>24小时涨跌幅:</span>
            <span class="${priceChangeClass}">${priceChangeSymbol}${priceChange24h.toFixed(2)}%</span>
        </div>
        <div class="market-data-item">
            <span>市值:</span>
            <span>$${(marketCap / 1000000000).toFixed(2)}B</span>
        </div>
        <div class="market-data-item">
            <span>24小时交易量:</span>
            <span>$${(volume24h / 1000000).toFixed(2)}M</span>
        </div>
    `;
    
    marketDataDiv.innerHTML = html;
}

// 执行手动交易
async function executeManualTrade() {
    const symbol = document.getElementById('trade-symbol').value;
    const type = document.getElementById('trade-type').value;
    const quantity = parseFloat(document.getElementById('trade-quantity').value);
    const price = parseFloat(document.getElementById('trade-price').value);
    
    if (!quantity || !price) {
        alert('请输入数量和价格');
        return;
    }
    
    try {
        const response = await fetch('/api/trade', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ symbol, type, quantity, price })
        });
        
        if (response.ok) {
            const result = await response.json();
            alert('交易执行成功');
            // 重新加载账户数据
            loadAccountData();
        } else {
            const error = await response.json();
            alert(`交易失败: ${error.error}`);
        }
    } catch (error) {
        console.error('执行交易失败:', error);
        alert('执行交易失败');
    }
}

// 执行AI交易
async function executeAITrade() {
    if (!marketData) {
        alert('请先加载市场数据');
        return;
    }
    
    aiTradeBtn.disabled = true;
    aiTradeBtn.textContent = 'AI分析中...';
    aiAnalysisDiv.innerHTML = '<p>AI正在分析市场数据...</p>';
    
    try {
        // 这里应该调用一个专门的AI分析端点
        // 为了演示，我们模拟AI分析结果
        const analysis = await simulateAIAnalysis();
        aiAnalysisDiv.innerHTML = `
            <h4>AI分析结果:</h4>
            <p>${analysis}</p>
        `;
        
        // 模拟执行建议的交易
        setTimeout(() => {
            alert('AI建议的交易已执行');
            loadAccountData();
            aiTradeBtn.disabled = false;
            aiTradeBtn.textContent = 'AI 分析并交易';
        }, 2000);
    } catch (error) {
        console.error('AI分析失败:', error);
        aiAnalysisDiv.innerHTML = '<p>AI分析失败</p>';
        aiTradeBtn.disabled = false;
        aiTradeBtn.textContent = 'AI 分析并交易';
    }
}

// 模拟AI分析（实际应用中应调用后端AI服务）
async function simulateAIAnalysis() {
    return new Promise((resolve) => {
        setTimeout(() => {
            const actions = [
                "建议买入: 技术指标显示价格可能上涨，当前是良好的买入时机。",
                "建议持有: 市场波动较小，建议继续持有现有仓位。",
                "建议卖出: 价格已达到阻力位，建议部分获利了结。"
            ];
            const randomAction = actions[Math.floor(Math.random() * actions.length)];
            resolve(randomAction);
        }, 3000);
    });
}

// 开始模拟交易
async function startSimulation() {
    const symbol = coinSelect.value;
    const interval = parseInt(document.getElementById('simulation-interval').value) * 1000;
    
    try {
        const response = await fetch('/api/simulation/start', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ symbol, interval })
        });
        
        if (response.ok) {
            const result = await response.json();
            alert(result.message);
            startSimulationBtn.disabled = true;
            stopSimulationBtn.disabled = false;
        } else {
            const error = await response.json();
            alert(`启动模拟失败: ${error.error}`);
        }
    } catch (error) {
        console.error('启动模拟失败:', error);
        alert('启动模拟失败');
    }
}

// 停止模拟交易
async function stopSimulation() {
    try {
        const response = await fetch('/api/simulation/stop', {
            method: 'POST'
        });
        
        if (response.ok) {
            const result = await response.json();
            alert(result.message);
            startSimulationBtn.disabled = false;
            stopSimulationBtn.disabled = true;
        } else {
            const error = await response.json();
            alert(`停止模拟失败: ${error.error}`);
        }
    } catch (error) {
        console.error('停止模拟失败:', error);
        alert('停止模拟失败');
    }
}

// 加载最新市场数据
async function loadLatestMarketData() {
    try {
        latestMarketDataDiv.innerHTML = '<p>加载中...</p>';
        
        const response = await fetch('/api/latest-market-data');
        if (response.ok) {
            latestMarketData = await response.json();
            renderLatestMarketData(latestMarketData);
        } else {
            throw new Error('获取最新市场数据失败');
        }
    } catch (error) {
        console.error('加载最新市场数据失败:', error);
        latestMarketDataDiv.innerHTML = '<p class="no-data">加载最新市场数据失败</p>';
    }
}

// 将UTC时间转换为北京时间
function convertToBeijingTime(utcTime) {
    try {
        // 如果时间已经是 YYYY-MM-DD HH:MM:SS 格式，假设它已经是北京时间
        // if (typeof utcTime === 'string' && /^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/.test(utcTime)) {
        //     return utcTime;
        // }
        
        // 创建UTC时间的Date对象
        const utcDate = new Date(utcTime);
        
        // 转换为北京时间（UTC+8）
        const beijingTime = new Date(utcDate.getTime() + 8 * 60 * 60 * 1000);
        
        // 返回格式化的北京时间字符串
        return beijingTime.toLocaleString('zh-CN', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit',
            hour: '2-digit',
            minute: '2-digit',
            second: '2-digit',
            hour12: false
        }).replace(/\//g, '-');
    } catch (error) {
        console.error('时间转换失败:', error);
        return utcTime; // 如果转换失败，返回原始时间
    }
}

// 渲染最新市场数据
function renderLatestMarketData(data) {
    if (!data || data.length === 0) {
        latestMarketDataDiv.innerHTML = '<p class="no-data">暂无市场数据</p>';
        return;
    }
    
    let html = `
        <table>
            <thead>
                <tr>
                    <th>加密货币</th>
                    <th>当前价格 (USD)</th>
                    <th>24小时涨跌幅</th>
                    <th>市值 (USD)</th>
                    <th>24小时交易量 (USD)</th>
                    <th>更新时间</th>
                </tr>
            </thead>
            <tbody>
    `;
    
    data.forEach(item => {
        const priceChangeClass = item.price_change_percentage_24h >= 0 ? 'positive' : 'negative';
        const priceChangeSymbol = item.price_change_percentage_24h >= 0 ? '+' : '';
        
        // 转换时间为北京时间
        const beijingTime = convertToBeijingTime(item.timestamp);
        
        // 格式化数值
        const formattedPrice = item.price ? item.price.toLocaleString(undefined, { minimumFractionDigits: 2, maximumFractionDigits: 2 }) : 'N/A';
        const formattedMarketCap = item.market_cap ? (item.market_cap / 1000000000).toFixed(2) + 'B' : 'N/A';
        const formattedVolume = item.volume_24h ? (item.volume_24h / 1000000).toFixed(2) + 'M' : 'N/A';
        
        html += `
            <tr>
                <td>${item.name} (${item.symbol.toUpperCase()})</td>
                <td>$${formattedPrice}</td>
                <td class="price-change ${priceChangeClass}">${priceChangeSymbol}${item.price_change_percentage_24h ? item.price_change_percentage_24h.toFixed(2) : 'N/A'}%</td>
                <td>$${formattedMarketCap}</td>
                <td>$${formattedVolume}</td>
                <td>${beijingTime}</td>
            </tr>
        `;
    });
    
    html += `
            </tbody>
        </table>
    `;
    
    latestMarketDataDiv.innerHTML = html;
}