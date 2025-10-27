package com.noodle.app.trade.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.noodle.app.trade.config.BinanceConfig;
import com.noodle.app.trade.model.CryptoCurrency;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
public class BinanceApiService {
    
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired
    private BinanceConfig binanceConfig;
    
    // Binance API基础URL
    private static final String BASE_URL = "https://api.binance.com";
    
    /**
     * 获取指定币种的当前价格
     * @param symbol 币种符号，如 "DOGEUSDT"
     * @return 当前价格
     */
    public BigDecimal getCurrentPrice(String symbol) throws IOException {
        String url = BASE_URL + "/api/v3/ticker/price?symbol=" + symbol;
        Request request = new Request.Builder()
                .url(url)
                .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String jsonData = response.body().string();
                JsonNode jsonNode = objectMapper.readTree(jsonData);
                return new BigDecimal(jsonNode.get("price").asText());
            }
        }
        throw new IOException("无法获取币种 " + symbol + " 的当前价格");
    }
    
    /**
     * 获取指定币种的市场数据
     * @param symbols 币种符号列表，如 ["BTCUSDT", "ETHUSDT"]
     * @return 加密货币市场数据列表
     */
    public List<CryptoCurrency> getMarketData(List<String> symbols) throws IOException {
        List<CryptoCurrency> cryptoCurrencies = new ArrayList<>();
        
        for (String symbol : symbols) {
            String url = BASE_URL + "/api/v3/ticker/24hr?symbol=" + symbol;
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonData = response.body().string();
                    JsonNode jsonNode = objectMapper.readTree(jsonData);
                    
                    CryptoCurrency crypto = parseCryptoCurrency(jsonNode, symbol);
                    cryptoCurrencies.add(crypto);
                }
            }
        }
        
        return cryptoCurrencies;
    }
    
    /**
     * 获取所有支持币种的市场数据
     * @return 加密货币市场数据列表
     */
    public List<CryptoCurrency> getAllMarketData() throws IOException {
        List<String> symbols = new ArrayList<>();
        symbols.add("BTCUSDT");
        symbols.add("ETHUSDT");
        symbols.add("BNBUSDT");
        // symbols.add("SOLUSDT");
        symbols.add("XRPUSDT");
        symbols.add("DOGEUSDT");
        
        return getMarketData(symbols);
    }
    
    /**
     * 解析JSON数据为CryptoCurrency对象
     * @param jsonNode JSON数据节点
     * @param symbol 币种符号
     * @return CryptoCurrency对象
     */
    private CryptoCurrency parseCryptoCurrency(JsonNode jsonNode, String symbol) {
        String name = getCurrencyNameFromSymbol(symbol);
        BigDecimal price = new BigDecimal(jsonNode.get("lastPrice").asText());
        BigDecimal priceChange = new BigDecimal(jsonNode.get("priceChange").asText());
        BigDecimal priceChangePercent = new BigDecimal(jsonNode.get("priceChangePercent").asText());
        BigDecimal volume = new BigDecimal(jsonNode.get("volume").asText());
        LocalDateTime lastUpdated = LocalDateTime.now(ZoneId.of("Asia/Shanghai"));
        
        return new CryptoCurrency(
                symbol.replace("USDT", ""),
                name,
                price,
                priceChange,
                priceChangePercent,
                volume,
                lastUpdated
        );
    }
    
    /**
     * 根据币种符号获取币种名称
     * @param symbol 币种符号
     * @return 币种名称
     */
    private String getCurrencyNameFromSymbol(String symbol) {
        switch (symbol.replace("USDT", "")) {
            case "BTC": return "Bitcoin";
            case "ETH": return "Ethereum";
            case "BNB": return "Binance Coin";
            case "SOL": return "Solana";
            case "XRP": return "Ripple";
            case "DOGE": return "Dogecoin";
            default: return symbol;
        }
    }
    
    /**
     * 生成签名
     * @param data 需要签名的数据
     * @return 签名
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     */
    private String generateSignature(String data) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(binanceConfig.getSecret().getBytes(), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hash = mac.doFinal(data.getBytes());
        
        StringBuilder hashString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hashString.append('0');
            }
            hashString.append(hex);
        }
        
        return hashString.toString();
    }
    
    /**
     * 查询账户信息
     * @return 账户信息JSON字符串
     * @throws IOException
     */
    public String getAccountInfo() throws IOException {
        // 检查API密钥是否配置
        if (binanceConfig.getKey() == null || binanceConfig.getKey().isEmpty() || 
            binanceConfig.getSecret() == null || binanceConfig.getSecret().isEmpty()) {
            throw new IOException("Binance API密钥未配置");
        }
        
        long timestamp = System.currentTimeMillis();
        String params = "timestamp=" + timestamp;
        
        try {
            String signature = generateSignature(params);
            String url = binanceConfig.getUrl() + "/api/v3/account?" + params + "&signature=" + signature;
            
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("X-MBX-APIKEY", binanceConfig.getKey())
                    .build();
            
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    return response.body().string();
                } else if (response.body() != null) {
                    throw new IOException("Binance API错误: " + response.body().string());
                }
            }
        } catch (Exception e) {
            throw new IOException("获取账户信息失败: " + e.getMessage(), e);
        }
        
        throw new IOException("获取账户信息失败");
    }
    
    /**
     * 查询账户持仓
     * @return 持仓信息JSON字符串
     * @throws IOException
     */
    public String getAccountHoldings() throws IOException {
        return getAccountInfo();
    }
    
    /**
     * 查询账户交易历史
     * @param symbol 币种符号（可选）
     * @param limit 返回记录数限制（可选，默认500）
     * @return 交易历史记录JSON字符串
     * @throws IOException
     */
    public String getTradeHistory(String symbol, Integer limit) throws IOException {
        // 检查API密钥是否配置
        if (binanceConfig.getKey() == null || binanceConfig.getKey().isEmpty() || 
            binanceConfig.getSecret() == null || binanceConfig.getSecret().isEmpty()) {
            throw new IOException("Binance API密钥未配置");
        }
        
        long timestamp = System.currentTimeMillis();
        StringBuilder paramsBuilder = new StringBuilder();
        paramsBuilder.append("timestamp=").append(timestamp);
        
        if (symbol != null && !symbol.isEmpty()) {
            paramsBuilder.append("&symbol=").append(symbol);
        }
        
        if (limit != null && limit > 0) {
            paramsBuilder.append("&limit=").append(limit);
        }
        
        String params = paramsBuilder.toString();
        
        try {
            String signature = generateSignature(params);
            String url = binanceConfig.getUrl() + "/api/v3/myTrades?" + params + "&signature=" + signature;
            
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("X-MBX-APIKEY", binanceConfig.getKey())
                    .build();
            
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    return response.body().string();
                } else if (response.body() != null) {
                    throw new IOException("Binance API错误: " + response.body().string());
                }
            }
        } catch (Exception e) {
            throw new IOException("获取交易历史失败: " + e.getMessage(), e);
        }
        
        throw new IOException("获取交易历史失败");
    }
    
    /**
     * 下单交易（买入）
     * @param symbol 币种符号
     * @param quantity 购买数量
     * @param price 价格（市价单可为null）
     * @return 交易结果JSON字符串
     * @throws IOException
     */
    public String placeBuyOrder(String symbol, BigDecimal quantity, BigDecimal price) throws IOException {
        return placeOrder(symbol, "BUY", quantity, price);
    }
    
    /**
     * 下单交易（卖出）
     * @param symbol 币种符号
     * @param quantity 卖出数量
     * @param price 价格（市价单可为null）
     * @return 交易结果JSON字符串
     * @throws IOException
     */
    public String placeSellOrder(String symbol, BigDecimal quantity, BigDecimal price) throws IOException {
        return placeOrder(symbol, "SELL", quantity, price);
    }
    
    /**
     * 下单交易通用方法
     * @param symbol 币种符号
     * @param side 交易方向（BUY/SELL）
     * @param quantity 数量
     * @param price 价格（市价单可为null）
     * @return 交易结果JSON字符串
     * @throws IOException
     */
    private String placeOrder(String symbol, String side, BigDecimal quantity, BigDecimal price) throws IOException {
        // 检查API密钥是否配置
        if (binanceConfig.getKey() == null || binanceConfig.getKey().isEmpty() || 
            binanceConfig.getSecret() == null || binanceConfig.getSecret().isEmpty()) {
            throw new IOException("Binance API密钥未配置");
        }
        
        long timestamp = System.currentTimeMillis();
        StringBuilder paramsBuilder = new StringBuilder();
        paramsBuilder.append("symbol=").append(symbol);
        paramsBuilder.append("&side=").append(side);
        paramsBuilder.append("&type=").append(price == null ? "MARKET" : "LIMIT");
        paramsBuilder.append("&quantity=").append(quantity.toPlainString());
        
        if (price != null) {
            paramsBuilder.append("&price=").append(price.toPlainString());
            paramsBuilder.append("&timeInForce=GTC");
        }
        
        paramsBuilder.append("&timestamp=").append(timestamp);
        
        String params = paramsBuilder.toString();
        
        try {
            String signature = generateSignature(params);
            String url = binanceConfig.getUrl() + "/api/v3/order?" + params + "&signature=" + signature;
            
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("X-MBX-APIKEY", binanceConfig.getKey())
                    .post(RequestBody.create(new byte[0], MediaType.parse("application/json")))
                    .build();
            
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    return response.body().string();
                } else if (response.body() != null) {
                    throw new IOException("Binance API错误: " + response.body().string());
                }
            }
        } catch (Exception e) {
            throw new IOException("下单失败: " + e.getMessage(), e);
        }
        
        throw new IOException("下单失败");
    }
}