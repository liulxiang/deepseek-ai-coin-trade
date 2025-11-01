# 代理配置说明

本项目支持通过环境变量或配置文件配置HTTP/HTTPS代理，以帮助在受限网络环境中访问外部服务。

## 配置选项

在 `application.yml` 中，我们提供了以下代理配置选项：

```yaml
# 代理配置
proxy:
  # HTTP代理配置
  http-host: ${HTTP_PROXY_HOST:}
  http-port: ${HTTP_PROXY_PORT:0}
  # HTTPS代理配置
  https-host: ${HTTPS_PROXY_HOST:}
  https-port: ${HTTPS_PROXY_PORT:0}
  # 代理认证配置
  username: ${PROXY_USERNAME:}
  password: ${PROXY_PASSWORD:}
```

## 使用方法

### 1. 通过环境变量配置（推荐）

```bash
# 设置HTTP代理
export HTTP_PROXY_HOST=your.proxy.host
export HTTP_PROXY_PORT=8080

# 设置HTTPS代理
export HTTPS_PROXY_HOST=your.proxy.host
export HTTPS_PROXY_PORT=8080

# 如果需要认证
export PROXY_USERNAME=your_username
export PROXY_PASSWORD=your_password
```

### 2. 通过application.yml直接配置

```yaml
# 代理配置
proxy:
  # HTTP代理配置
  http-host: your.proxy.host
  http-port: 8080
  # HTTPS代理配置
  https-host: your.proxy.host
  https-port: 8080
  # 代理认证配置
  username: your_username
  password: your_password
```

### 3. 通过命令行参数配置

```bash
java -jar your-app.jar \
  --proxy.https-host=your.proxy.host \
  --proxy.https-port=8080 \
  --proxy.username=your_username \
  --proxy.password=your_password
```

## 支持的外部服务

代理配置将应用于以下外部服务：

1. **Binance API** - 用于获取加密货币市场数据
2. **DeepSeek AI API** - 用于AI交易分析

## 注意事项

1. 如果未配置代理，应用将直接连接外部服务
2. 代理配置支持HTTP和HTTPS代理
3. 支持基本认证（用户名/密码）
4. 代理配置对所有网络请求生效

## 测试代理连接

启动应用后，可以通过以下方式验证代理是否正常工作：

1. 检查应用日志中是否有代理连接相关的消息
2. 观察市场数据是否能正常获取
3. 测试AI分析功能是否正常工作