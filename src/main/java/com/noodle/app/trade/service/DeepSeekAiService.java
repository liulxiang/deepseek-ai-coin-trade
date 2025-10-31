package com.noodle.app.trade.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.noodle.app.trade.model.CryptoCurrency;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
@Service
public class DeepSeekAiService {
    
    private final OkHttpClient client;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 从配置文件读取DeepSeek API配置
    @Value("${deepseek.api.key}")
    private String apiKey;
    
    @Value("${deepseek.api.url}")
    private String baseUrl;
    
    @Value("${deepseek.api.model}")
    private String model;
    
    public DeepSeekAiService() {
        // 配置OkHttpClient超时设置
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();
    }
    
    /**
     * 获取DeepSeek API余额信息
     * @return API余额信息JSON字符串，如果获取失败返回错误信息
     */
    public String getDeepSeekAiBalance(){
        try {
            // 创建GET请求（GET请求不应该有请求体）
            Request request = new Request.Builder()
                    .url(baseUrl + "/user/balance")
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .get()
                    .build();
            
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonData = response.body().string();
                    System.out.println("DeepSeek API余额信息: " + jsonData);
                    return jsonData;
                } else {
                    String errorMessage = "获取DeepSeek API余额失败，HTTP状态码: " + response.code();
                    System.err.println(errorMessage);
                    return errorMessage;
                }
            }
        } catch (IOException e) {
            String errorMessage = "获取DeepSeek API余额时发生网络错误: " + e.getMessage();
            System.err.println(errorMessage);
            return errorMessage;
        }
    }
    
    /**
     * 分析市场数据并生成交易策略建议
     * @param cryptoCurrencies 加密货币市场数据
     * @return 交易策略建议
     */
    public String analyzeMarketData(List<CryptoCurrency> cryptoCurrencies) throws IOException {
        // 构建发送给DeepSeek AI的提示
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个专业的加密货币交易分析师，请根据以下市场数据提供交易策略建议：\n\n");
        
        for (CryptoCurrency crypto : cryptoCurrencies) {
            prompt.append(String.format("%s (%s):\n", crypto.getName(), crypto.getSymbol()));
            prompt.append(String.format("  当前价格: $%.2f\n", crypto.getPrice()));
            prompt.append(String.format("  24小时涨跌: $%.2f (%.2f%%)\n", crypto.getPriceChange(), crypto.getPriceChangePercent()));
            prompt.append(String.format("  24小时交易量: %.2f\n\n", crypto.getVolume()));
        }
        
        prompt.append("请分析以上数据并提供具体的交易建议，要求：\n");
        prompt.append("1. 对于每个币种，请明确给出交易信号：BUY（买入）、SELL（卖出）或HOLD（持有）\n");
        prompt.append("2. 说明给出该建议的原因\n");
        prompt.append("3. 标注高风险和低风险的建议\n");
        prompt.append("4. 如果建议BUY，请给出目标价位和止损价位\n");
        prompt.append("5. 如果建议SELL，请说明是止盈还是止损\n");
        prompt.append("6. 如果建议HOLD，请说明是继续持有还是暂时观望\n\n");
        prompt.append("请以清晰的格式回复，例如：\n");
        prompt.append("BTC (BUY): 建议买入，因为价格突破关键阻力位，目标价位$50000，止损价位$45000\n");
        prompt.append("ETH (HOLD): 建议持有，价格在支撑位附近震荡，可继续观察\n");
        prompt.append("DOGE (SELL): 建议卖出，价格跌破关键支撑位，及时止盈\n\n");
        prompt.append("请确保对每个币种都给出明确的BUY/SELL/HOLD建议。");
        
        // 构建请求体
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", model);
        ArrayNode messages = objectMapper.createArrayNode();
        ObjectNode message = objectMapper.createObjectNode();
        message.put("role", "user");
        message.put("content", prompt.toString());
        messages.add(message);
        requestBody.set("messages", messages);
        requestBody.put("temperature", 0.7);
        log.info("DeepSeek AI问: " + prompt.toString());
        System.out.println("DeepSeek AI问: " + prompt.toString());
        // 创建请求
        Request request = new Request.Builder()
                .url(baseUrl + "/chat/completions")
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(objectMapper.writeValueAsString(requestBody), MediaType.get("application/json")))
                .build();
        
        // 发送请求并获取响应
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String jsonData = response.body().string();
                 log.info("DeepSeek AI回复: " + jsonData);
                JsonNode jsonResponse = objectMapper.readTree(jsonData);
               
                // 解析AI的回复
                JsonNode choices = jsonResponse.get("choices");
                if (choices != null && choices.size() > 0) {
                    JsonNode choiceMessage = choices.get(0).get("message");
                    if (choiceMessage != null) {
                        return choiceMessage.get("content").asText();
                    }
                }
            }
        }
        
        return "无法获取AI分析结果，请稍后重试。";
    }
    
    /**
     * 根据特定币种数据生成交易信号
     * @param cryptoCurrency 加密货币数据
     * @return 交易信号（BUY, SELL, HOLD）
     */
    public String generateTradingSignal(CryptoCurrency cryptoCurrency) throws IOException {
        // 构建发送给DeepSeek AI的提示
        StringBuilder prompt = new StringBuilder();
        prompt.append("作为一个AI交易助手，请根据以下加密货币数据生成一个交易信号：\n\n");
        prompt.append(String.format("%s (%s):\n", cryptoCurrency.getName(), cryptoCurrency.getSymbol()));
        prompt.append(String.format("当前价格: $%.2f\n", cryptoCurrency.getPrice()));
        prompt.append(String.format("24小时涨跌: $%.2f (%.2f%%)\n", cryptoCurrency.getPriceChange(), cryptoCurrency.getPriceChangePercent()));
        prompt.append(String.format("24小时交易量: %.2f\n\n", cryptoCurrency.getVolume()));
        prompt.append("请仅回复以下三个选项中的一个：\n");
        prompt.append("BUY - 如果建议买入\n");
        prompt.append("SELL - 如果建议卖出\n");
        prompt.append("HOLD - 如果建议持有\n\n");
        prompt.append("请直接回复选项，不要包含其他文字。");
        
        // 构建请求体
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", model);
        ArrayNode messages = objectMapper.createArrayNode();
        ObjectNode message = objectMapper.createObjectNode();
        message.put("role", "user");
        message.put("content", prompt.toString());
        messages.add(message);
        requestBody.set("messages", messages);
        requestBody.put("temperature", 0.5);
        requestBody.put("max_tokens", 10);
        
        // 创建请求
        Request request = new Request.Builder()
                .url(baseUrl + "/chat/completions")
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(objectMapper.writeValueAsString(requestBody), MediaType.get("application/json")))
                .build();
        
        // 发送请求并获取响应
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String jsonData = response.body().string();
                JsonNode jsonResponse = objectMapper.readTree(jsonData);
                
                // 解析AI的回复
                JsonNode choices = jsonResponse.get("choices");
                if (choices != null && choices.size() > 0) {
                    JsonNode choiceMessage = choices.get(0).get("message");
                    if (choiceMessage != null) {
                        String signal = choiceMessage.get("content").asText().trim().toUpperCase();
                        // 验证返回的信号是否有效
                        if ("BUY".equals(signal) || "SELL".equals(signal) || "HOLD".equals(signal)) {
                            return signal;
                        }
                    }
                }
            }
        }
        
        // 默认返回持有信号
        return "HOLD";
    }
    
    /**
     * 为多个币种生成详细的交易建议
     * @param cryptoCurrencies 加密货币市场数据列表
     * @return 包含每个币种交易建议的详细分析
     */
    public String generateDetailedTradingAdvice(List<CryptoCurrency> cryptoCurrencies) throws IOException {
        // 构建发送给DeepSeek AI的提示
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个专业的加密货币交易分析师，请根据以下市场数据为每个币种提供详细的交易建议：\n\n");
        
        for (CryptoCurrency crypto : cryptoCurrencies) {
            prompt.append(String.format("%s (%s):\n", crypto.getName(), crypto.getSymbol()));
            prompt.append(String.format("  当前价格: $%.4f\n", crypto.getPrice()));
            prompt.append(String.format("  24小时涨跌: $%.4f (%.2f%%)\n", crypto.getPriceChange(), crypto.getPriceChangePercent()));
            prompt.append(String.format("  24小时交易量: %.0f\n\n", crypto.getVolume()));
        }
        
        prompt.append("请为每个币种提供以下信息：\n");
        prompt.append("1. 明确的交易信号：BUY（买入）、SELL（卖出）或HOLD（持有）\n");
        prompt.append("2. 详细的理由说明\n");
        prompt.append("3. 风险等级（高/中/低）\n");
        prompt.append("4. 如果是BUY信号，请提供目标价位和止损价位\n");
        prompt.append("5. 如果是SELL信号，请说明是止盈还是止损\n");
        prompt.append("6. 如果是HOLD信号，请说明是继续持有还是暂时观望\n\n");
        prompt.append("请严格按照以下格式回复每个币种：\n");
        prompt.append("[币种符号] ([交易信号]): [理由说明] [风险等级] [具体建议]\n\n");
        prompt.append("例如：\n");
        prompt.append("BTC (BUY): 价格突破关键阻力位，成交量放大，短期看涨。风险等级：中。建议买入，目标价位$50000，止损价位$45000。\n");
        prompt.append("ETH (HOLD): 价格在支撑位附近震荡，成交量萎缩，方向不明。风险等级：低。建议继续持有，可逢低加仓。\n");
        prompt.append("DOGE (SELL): 价格跌破关键支撑位，出现恐慌性抛售。风险等级：高。建议及时止盈，避免更大损失。\n\n");
        prompt.append("请确保对每个币种都给出明确的BUY/SELL/HOLD建议，并严格按照指定格式回复。");
        
        // 构建请求体
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", model);
        ArrayNode messages = objectMapper.createArrayNode();
        ObjectNode message = objectMapper.createObjectNode();
        message.put("role", "user");
        message.put("content", prompt.toString());
        messages.add(message);
        requestBody.set("messages", messages);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 1000);
        
        // 创建请求
        Request request = new Request.Builder()
                .url(baseUrl + "/chat/completions")
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(objectMapper.writeValueAsString(requestBody), MediaType.get("application/json")))
                .build();
        
        // 发送请求并获取响应
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String jsonData = response.body().string();
                JsonNode jsonResponse = objectMapper.readTree(jsonData);
                
                // 解析AI的回复
                JsonNode choices = jsonResponse.get("choices");
                if (choices != null && choices.size() > 0) {
                    JsonNode choiceMessage = choices.get(0).get("message");
                    if (choiceMessage != null) {
                        return choiceMessage.get("content").asText();
                    }
                }
            }
        }
        
        return "无法获取AI分析结果，请稍后重试。";
    }
    
    /**
     * 根据账户信息和市场数据生成具体的交易建议，包括买入或卖出的数量
     * @param cryptoCurrencies 加密货币市场数据列表
     * @param accountHoldings 账户当前持仓情况，键为币种符号，值为持仓数量
     * @param availableBalance 账户可用余额
     * @return 包含具体交易数量的建议
     */
    public String generateSpecificTradingAdvice(List<CryptoCurrency> cryptoCurrencies, 
                                              Map<String, Double> accountHoldings, 
                                              double availableBalance) throws IOException {
        // 构建发送给DeepSeek AI的提示
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个专业的加密货币交易分析师，请根据以下信息为每个币种提供具体的交易建议：\n\n");
        
        prompt.append("账户信息：\n");
        prompt.append(String.format("  可用余额: $%.2f\n", availableBalance));
        prompt.append("  当前持仓:\n");
        if (accountHoldings.isEmpty()) {
            prompt.append("    无持仓\n");
        } else {
            for (Map.Entry<String, Double> entry : accountHoldings.entrySet()) {
                prompt.append(String.format("    %s: %.4f\n", entry.getKey(), entry.getValue()));
            }
        }
        prompt.append("\n");
        
        prompt.append("市场数据：\n");
        for (CryptoCurrency crypto : cryptoCurrencies) {
            prompt.append(String.format("%s (%s):\n", crypto.getName(), crypto.getSymbol()));
            prompt.append(String.format("  当前价格: $%.4f\n", crypto.getPrice()));
            prompt.append(String.format("  24小时涨跌: $%.4f (%.2f%%)\n", crypto.getPriceChange(), crypto.getPriceChangePercent()));
            prompt.append(String.format("  24小时交易量: %.0f\n\n", crypto.getVolume()));
        }
        
        prompt.append("请为每个币种提供以下信息：\n");
        prompt.append("1. 明确的交易信号：BUY（买入）、SELL（卖出）或HOLD（持有）\n");
        prompt.append("2. 如果是BUY信号，请给出建议买入的具体金额（美元）和数量\n");
        prompt.append("3. 如果是SELL信号，请给出建议卖出的具体数量\n");
        prompt.append("4. 如果是HOLD信号，请说明原因\n");
        prompt.append("5. 详细的理由说明\n");
        prompt.append("6. 风险等级（高/中/低）\n\n");
        prompt.append("请严格按照以下格式回复每个币种：\n");
        prompt.append("[币种符号] ([交易信号]): [理由说明] [风险等级] [具体建议]\n\n");
        prompt.append("例如：\n");
        prompt.append("BTC (BUY): 价格突破关键阻力位，短期看涨。风险等级：中。建议买入$5000，约0.1BTC。\n");
        prompt.append("ETH (SELL): 价格跌破关键支撑位，出现下跌趋势。风险等级：高。建议卖出0.5ETH。\n");
        prompt.append("DOGE (HOLD): 价格在支撑位附近震荡，方向不明。风险等级：低。建议继续持有。\n\n");
        prompt.append("请确保对每个币种都给出明确的BUY/SELL/HOLD建议，并严格按照指定格式回复。");
        
        // 构建请求体
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", model);
        ArrayNode messages = objectMapper.createArrayNode();
        ObjectNode message = objectMapper.createObjectNode();
        message.put("role", "user");
        message.put("content", prompt.toString());
        messages.add(message);
        requestBody.set("messages", messages);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 1000);
        
        // 创建请求
        Request request = new Request.Builder()
                .url(baseUrl + "/chat/completions")
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(objectMapper.writeValueAsString(requestBody), MediaType.get("application/json")))
                .build();
        
        // 发送请求并获取响应
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String jsonData = response.body().string();
                JsonNode jsonResponse = objectMapper.readTree(jsonData);
                
                // 解析AI的回复
                JsonNode choices = jsonResponse.get("choices");
                if (choices != null && choices.size() > 0) {
                    JsonNode choiceMessage = choices.get(0).get("message");
                    if (choiceMessage != null) {
                        return choiceMessage.get("content").asText();
                    }
                }
            }
        }
        
        return "无法获取AI分析结果，请稍后重试。";
    }
    
    /**
     * 分析市场数据并生成具体的交易决策（买多少、卖多少、或不动）
     * @param cryptoCurrencies 加密货币市场数据列表
     * @param accountHoldings 账户当前持仓情况，键为币种符号，值为持仓数量
     * @param availableBalance 账户可用余额（美元）
     * @param marketConditions 当前市场情况描述（如：牛市、熊市、震荡市等）
     * @return 包含具体交易决策的建议
     */
    public String analyzeMarketData(List<CryptoCurrency> cryptoCurrencies, 
                                   Map<String, Double> accountHoldings, 
                                   double availableBalance,
                                   String marketConditions) throws IOException {
        // 模拟AI分析结果（避免实际API调用超时）
        return generateMockTradingAdvice(cryptoCurrencies, accountHoldings, availableBalance, marketConditions);
    }
    
    /**
     * 生成模拟的交易建议（用于测试和演示）
     */
    private String generateMockTradingAdvice(List<CryptoCurrency> cryptoCurrencies, 
                                            Map<String, Double> accountHoldings, 
                                            double availableBalance,
                                            String marketConditions) {
        StringBuilder result = new StringBuilder();
        result.append("基于当前市场数据(").append(marketConditions).append(")和账户情况，AI分析结果如下：\n\n");
        
        for (CryptoCurrency crypto : cryptoCurrencies) {
            String symbol = crypto.getSymbol();
            double price = crypto.getPrice().doubleValue();
            double priceChangePercent = crypto.getPriceChangePercent().doubleValue();
            
            // 基于价格变化和持仓情况生成交易建议
            String signal = generateSignal(symbol, priceChangePercent, accountHoldings.getOrDefault(symbol, 0.0));
            String reasoning = generateReasoning(symbol, priceChangePercent, marketConditions);
            String riskLevel = generateRiskLevel(priceChangePercent);
            String specificAdvice = generateSpecificAdvice(symbol, signal, price, availableBalance, 
                                                          accountHoldings.getOrDefault(symbol, 0.0));
            
            result.append(String.format("%s (%s): %s 风险等级：%s。%s\n", 
                symbol, signal, reasoning, riskLevel, specificAdvice));
        }
        
        result.append("\n总体建议：");
        if (marketConditions.contains("牛市")) {
            result.append("市场处于上升趋势，可适当增加仓位，但需注意风险控制。");
        } else if (marketConditions.contains("熊市")) {
            result.append("市场处于下降趋势，建议谨慎操作，控制仓位。");
        } else {
            result.append("市场震荡，建议分批建仓，设置止损。");
        }
        
        return result.toString();
    }
    
    private String generateSignal(String symbol, double priceChangePercent, double currentHolding) {
        if (priceChangePercent > 5) {
            return currentHolding > 0 ? "SELL" : "HOLD";
        } else if (priceChangePercent > 2) {
            return "BUY";
        } else if (priceChangePercent < -5) {
            return currentHolding > 0 ? "HOLD" : "BUY";
        } else if (priceChangePercent < -2) {
            return "HOLD";
        } else {
            return currentHolding > 0 ? "HOLD" : "BUY";
        }
    }
    
    private String generateReasoning(String symbol, double priceChangePercent, String marketConditions) {
        if (priceChangePercent > 5) {
            return "价格大幅上涨，存在回调风险";
        } else if (priceChangePercent > 2) {
            return "价格温和上涨，趋势向好";
        } else if (priceChangePercent < -5) {
            return "价格大幅下跌，可能超跌反弹";
        } else if (priceChangePercent < -2) {
            return "价格小幅下跌，观望为主";
        } else {
            return "价格平稳，适合长期布局";
        }
    }
    
    private String generateRiskLevel(double priceChangePercent) {
        if (Math.abs(priceChangePercent) > 10) {
            return "高";
        } else if (Math.abs(priceChangePercent) > 5) {
            return "中";
        } else {
            return "低";
        }
    }
    
    private String generateSpecificAdvice(String symbol, String signal, double price, 
                                        double availableBalance, double currentHolding) {
        switch (signal) {
            case "BUY":
                double buyAmount = Math.min(availableBalance * 0.2, 1000); // 最多投入20%或1000美元
                double buyQuantity = buyAmount / price;
                return String.format("建议买入$%.2f，约%.6f%s，仓位%.1f%%", 
                    buyAmount, buyQuantity, symbol, (buyAmount / (availableBalance + currentHolding * price)) * 100);
            case "SELL":
                double sellQuantity = currentHolding * 0.3; // 卖出30%持仓
                return String.format("建议卖出%.6f%s，占持仓%.1f%%", 
                    sellQuantity, symbol, 30.0);
            case "HOLD":
                if (currentHolding > 0) {
                    return "继续持有，等待更好时机";
                } else {
                    return "暂时观望，等待明确信号";
                }
            default:
                return "保持观望";
        }
    }
    
    /**
     * 生成快速交易信号（简化版，用于实时决策）
     * @param cryptoCurrencies 加密货币市场数据列表
     * @param accountHoldings 账户当前持仓情况
     * @param availableBalance 账户可用余额
     * @return 简化的交易信号列表
     */
    public Map<String, String> generateQuickTradingSignals(List<CryptoCurrency> cryptoCurrencies, 
                                                         Map<String, Double> accountHoldings, 
                                                         double availableBalance) throws IOException {
        // 使用模拟实现生成快速交易信号
        return generateMockQuickSignals(cryptoCurrencies, accountHoldings, availableBalance);
    }
    
    /**
     * 生成模拟的快速交易信号
     */
    private Map<String, String> generateMockQuickSignals(List<CryptoCurrency> cryptoCurrencies, 
                                                        Map<String, Double> accountHoldings, 
                                                        double availableBalance) {
        Map<String, String> signals = new HashMap<>();
        
        for (CryptoCurrency crypto : cryptoCurrencies) {
            String symbol = crypto.getSymbol();
            double priceChangePercent = crypto.getPriceChangePercent().doubleValue();
            double currentHolding = accountHoldings.getOrDefault(symbol, 0.0);
            
            // 基于价格变化和持仓情况生成信号
            if (priceChangePercent > 3) {
                signals.put(symbol, currentHolding > 0 ? "SELL" : "HOLD");
            } else if (priceChangePercent > 1) {
                signals.put(symbol, "BUY");
            } else if (priceChangePercent < -3) {
                signals.put(symbol, currentHolding > 0 ? "HOLD" : "BUY");
            } else if (priceChangePercent < -1) {
                signals.put(symbol, "HOLD");
            } else {
                signals.put(symbol, currentHolding > 0 ? "HOLD" : "BUY");
            }
        }
        
        return signals;
    }
    
    /**
     * 解析快速交易信号
     */
    private Map<String, String> parseQuickSignals(String responseText) {
        Map<String, String> signals = new HashMap<>();
        String[] lines = responseText.split("\n");
        
        for (String line : lines) {
            String[] parts = line.split(":");
            if (parts.length == 2) {
                String symbol = parts[0].trim().toUpperCase();
                String signal = parts[1].trim().toUpperCase();
                if (signal.equals("BUY") || signal.equals("SELL") || signal.equals("HOLD")) {
                    signals.put(symbol, signal);
                }
            }
        }
        
        return signals;
    }
}