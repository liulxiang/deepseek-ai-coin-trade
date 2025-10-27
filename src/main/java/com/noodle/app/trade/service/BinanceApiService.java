package com.noodle.app.trade.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.noodle.app.trade.model.CryptoCurrency;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
public class BinanceApiService {
    
    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
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
}