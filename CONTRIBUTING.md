# 贡献指南

感谢您对DeepSeek AI驱动的加密货币交易模拟系统的关注！我们欢迎任何形式的贡献。

## 开发环境设置

1. Fork 本仓库
2. 克隆到本地：
   ```bash
   git clone https://github.com/your-username/crypto-trading-simulator.git
   ```
3. 安装依赖：
   ```bash
   npm install
   ```
4. 创建功能分支：
   ```bash
   git checkout -b feature/your-feature-name
   ```

## 代码规范

### JavaScript/Node.js 规范
- 使用ESLint进行代码检查
- 遵循Airbnb JavaScript风格指南
- 函数和变量命名使用驼峰命名法
- 类名使用帕斯卡命名法
- 常量使用大写字母和下划线分隔

### 提交信息规范
- 使用清晰、简洁的提交信息
- 以动词开头，使用现在时
- 首字母小写
- 示例：
  - `add new trading strategy`
  - `fix market data display bug`
  - `update documentation`

## 开发流程

1. 创建Issue描述要解决的问题或新增的功能
2. 从`main`分支创建功能分支
3. 编写代码和测试
4. 确保所有测试通过：
   ```bash
   npm test
   ```
5. 提交更改并推送分支
6. 创建Pull Request

## 测试

### 单元测试
- 为新功能编写单元测试
- 确保代码覆盖率不低于80%
- 使用Jest作为测试框架

### 集成测试
- 对API接口进行集成测试
- 确保各模块间协作正常

运行测试：
```bash
# 运行所有测试
npm test

# 运行测试并监听文件变化
npm run test:watch

# 生成覆盖率报告
npm run test:coverage
```

## 文档

- 为新功能更新README.md
- 在CHANGELOG.md中记录重要变更
- 保持注释清晰、准确

## Pull Request 指南

1. PR标题应简洁明了
2. PR描述应详细说明变更内容和解决的问题
3. 关联相关的Issue
4. 确保CI检查通过
5. 等待代码审查

## 问题报告

如果您发现bug或有功能建议，请：
1. 搜索是否已有相关Issue
2. 创建新的Issue
3. 提供详细的复现步骤和环境信息

## 代码审查

所有PR都需要至少一名维护者审查通过后才能合并。

## 许可证

通过贡献代码，您同意您的贡献遵循项目的MIT许可证。