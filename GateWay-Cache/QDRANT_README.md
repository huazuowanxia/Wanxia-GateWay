# Qdrant 向量数据库集成指南

## 📦 已创建的文件

```
GateWay-Cache/
├── src/main/java/com/wanxia/gateway/cache/vector/
│   ├── QdrantProvider.java              # Qdrant 提供者实现
│   └── QdrantProviderInitializer.java   # Qdrant 初始化器
├── src/test/java/com/wanxia/gateway/cache/vector/
│   └── QdrantExample.java               # 使用示例
└── pom.xml                              # 已添加 Apache HttpClient 依赖
```

---

## 🚀 快速开始

### 1. 启动 Qdrant

```bash
# 使用 Docker 启动 Qdrant
docker run -d \
  --name qdrant \
  --restart=always \
  -p 6333:6333 \
  -v ~/qdrant_data:/qdrant/storage \
  qdrant/qdrant

# 验证启动
curl http://localhost:6333
open http://localhost:6333/dashboard
```

### 2. 创建集合

```bash
curl -X PUT 'http://localhost:6333/collections/test_collection' \
  -H 'Content-Type: application/json' \
  -d '{
    "vectors": {
      "size": 4,
      "distance": "Cosine"
    }
  }'
```

### 3. 运行测试

```bash
# 在项目根目录执行
cd /Users/zyl/IdeaProjects/Wanxia-GateWay

# 运行所有测试
mvn test -pl GateWay-Cache -Dtest=QdrantExample

# 运行单个测试
mvn test -pl GateWay-Cache -Dtest=QdrantExample#testUploadAnswerAndEmbedding
```

---

## 💻 代码示例

### 基本使用

```java
// 1. 创建配置
ProviderConfig config = new ProviderConfig();
config.setType("qdrant");
config.setServiceName("localhost");
config.setServicePort(6333);
config.setCollectionId("test_collection");
config.setTopK(3);

// 2. 验证配置
config.validate();

// 3. 创建提供者
Provider provider = VectorProviderFactory.createProvider(config);

// 4. 上传向量和答案
AnswerAndEmbeddingUploader uploader = (AnswerAndEmbeddingUploader) provider;
uploader.uploadAnswerAndEmbedding(
    "什么是 Java？",
    new double[]{0.1, 0.2, 0.3, 0.4},
    "Java 是一种面向对象的编程语言",
    error -> {
        if (error == null) {
            System.out.println("上传成功");
        }
    }
);

// 5. 查询相似向量
EmbeddingQuerier querier = (EmbeddingQuerier) provider;
querier.queryEmbedding(
    new double[]{0.12, 0.22, 0.32, 0.42},
    (results, error) -> {
        if (error == null) {
            results.forEach(result -> {
                System.out.println("问题: " + result.getText());
                System.out.println("答案: " + result.getAnswer());
                System.out.println("相似度: " + result.getScore());
            });
        }
    }
);
```

### 从 JSON 配置创建

```java
String jsonConfig = """
{
  "type": "qdrant",
  "serviceName": "localhost",
  "servicePort": 6333,
  "collectionID": "test_collection",
  "topK": 5
}
""";

ObjectMapper mapper = new ObjectMapper();
JsonNode json = mapper.readTree(jsonConfig);

ProviderConfig config = new ProviderConfig();
config.fromJson(json);
config.validate();

Provider provider = VectorProviderFactory.createProvider(config);
```

---

## 🔧 配置说明

| 配置项 | 类型 | 必填 | 默认值 | 说明 |
|--------|------|------|--------|------|
| type | String | ✅ | - | 固定为 "qdrant" |
| serviceName | String | ✅ | - | 服务名称 |
| serviceHost | String | ❌ | serviceName | 服务地址 |
| servicePort | int | ❌ | 443 | 服务端口 |
| collectionId | String | ✅ | - | 集合 ID |
| topK | int | ❌ | 1 | 返回结果数量 |
| timeout | int | ❌ | 10000 | 超时时间（毫秒） |
| apiKey | String | ❌ | - | API 密钥（可选） |

---

## 📚 API 说明

### AnswerAndEmbeddingUploader 接口

```java
void uploadAnswerAndEmbedding(
    String queryString,      // 问题文本
    double[] queryEmbedding, // 问题向量
    String answer,           // 答案文本
    Consumer<Exception> callback // 回调函数
);
```

### EmbeddingQuerier 接口

```java
void queryEmbedding(
    double[] embedding,      // 查询向量
    BiConsumer<List<QueryResult>, Exception> callback // 回调函数
);
```

### QueryResult 结构

```java
public class QueryResult {
    private String text;      // 相似的文本（问题）
    private double[] embedding; // 向量
    private double score;     // 相似度分数
    private String answer;    // 答案
}
```

---

## 🧪 测试用例

### 1. 上传向量和答案
```bash
mvn test -pl GateWay-Cache -Dtest=QdrantExample#testUploadAnswerAndEmbedding
```

### 2. 查询相似向量
```bash
mvn test -pl GateWay-Cache -Dtest=QdrantExample#testQueryEmbedding
```

### 3. 完整 RAG 流程
```bash
mvn test -pl GateWay-Cache -Dtest=QdrantExample#testRAGWorkflow
```

### 4. 从 JSON 配置创建
```bash
mvn test -pl GateWay-Cache -Dtest=QdrantExample#testCreateFromJson
```

---

## 🔍 常见问题

### Q1: 连接失败
```
Error: Connection refused
```
**解决方案**：确保 Qdrant 已启动
```bash
docker ps | grep qdrant
curl http://localhost:6333
```

### Q2: 集合不存在
```
Error: Collection not found
```
**解决方案**：创建集合
```bash
curl -X PUT 'http://localhost:6333/collections/test_collection' \
  -H 'Content-Type: application/json' \
  -d '{"vectors": {"size": 4, "distance": "Cosine"}}'
```

### Q3: 向量维度不匹配
```
Error: Vector dimension mismatch
```
**解决方案**：确保向量维度与集合配置一致
```java
// 集合配置 size=4，向量也必须是 4 维
double[] embedding = {0.1, 0.2, 0.3, 0.4}; // ✅ 正确
double[] embedding = {0.1, 0.2, 0.3};      // ❌ 错误
```

---

## 📊 性能建议

1. **批量上传**：一次上传多个向量可以提高性能
2. **连接池**：生产环境建议配置 HTTP 连接池
3. **异步处理**：使用回调函数进行异步处理
4. **缓存结果**：对于频繁查询的向量，可以缓存结果

---

## 🔗 相关资源

- [Qdrant 官方文档](https://qdrant.tech/documentation/)
- [Qdrant REST API](https://qdrant.github.io/qdrant/redoc/index.html)
- [Qdrant Web UI](http://localhost:6333/dashboard)

---

## 📝 更新日志

### 2024-10-17
- ✅ 实现 QdrantProvider
- ✅ 实现 QdrantProviderInitializer
- ✅ 添加完整测试示例
- ✅ 添加 Apache HttpClient 依赖
- ✅ 注册到 VectorProviderInitializerRegistry

---

## 🎯 下一步

1. 实现其他向量数据库提供者（Chroma、Milvus 等）
2. 添加批量操作支持
3. 添加过滤条件支持
4. 优化 HTTP 连接池配置
5. 添加重试机制

---

**作者**: CatPaw AI Assistant
**日期**: 2024-10-17

