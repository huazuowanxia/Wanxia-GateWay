package com.wanxia.gateway.cache.vector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Qdrant 向量数据库使用示例
 *
 * 前置条件：
 * 1. 启动 Qdrant: docker run -d --name qdrant -p 6333:6333 qdrant/qdrant
 * 2. 创建集合（见下方 createCollection 方法）
 */
public class QdrantExample {

    private Provider provider;

    @Before
    public void setUp() {
        // 配置 Qdrant
        ProviderConfig config = new ProviderConfig();
        config.setType(VectorConstants.PROVIDER_TYPE_QDRANT);
        config.setServiceName("localhost");
        config.setServiceHost("127.0.0.1");
        config.setServicePort(6333);
        config.setCollectionId("test_collection");
        config.setTopK(3);
        config.setTimeout(10000);

        // 验证配置
        config.validate();

        // 创建提供者
        provider = VectorProviderFactory.createProvider(config);

        System.out.println("Qdrant Provider 初始化完成");
        System.out.println("提供者类型: " + provider.getProviderType());
    }

    /**
     * 示例0：创建集合（首次运行需要执行）
     */
    @Test
    public void createCollection() throws Exception {
        System.out.println("\n=== 创建 Qdrant 集合 ===");
        System.out.println("请在终端执行以下命令：");
        System.out.println();
        System.out.println("curl -X PUT 'http://localhost:6333/collections/test_collection' \\");
        System.out.println("  -H 'Content-Type: application/json' \\");
        System.out.println("  -d '{");
        System.out.println("    \"vectors\": {");
        System.out.println("      \"size\": 4,");
        System.out.println("      \"distance\": \"Cosine\"");
        System.out.println("    }");
        System.out.println("  }'");
        System.out.println();
        System.out.println("或者访问 Web UI 创建：http://localhost:6333/dashboard");
    }

    /**
     * 示例1：上传向量和答案
     */
    @Test
    public void testUploadAnswerAndEmbedding() throws InterruptedException {
        System.out.println("\n=== 示例1：上传向量和答案 ===");

        CountDownLatch latch = new CountDownLatch(3);

        // 上传第一条数据
        String question1 = "什么是 Java？";
        double[] embedding1 = {0.1, 0.2, 0.3, 0.4};
        String answer1 = "Java 是一种面向对象的编程语言，由 Sun Microsystems 开发。";

        AnswerAndEmbeddingUploader uploader = (AnswerAndEmbeddingUploader) provider;
        uploader.uploadAnswerAndEmbedding(question1, embedding1, answer1, error -> {
            if (error == null) {
                System.out.println("✓ 上传成功：" + question1);
            } else {
                System.out.println("✗ 上传失败：" + error.getMessage());
            }
            latch.countDown();
        });

        // 上传第二条数据
        String question2 = "什么是 Python？";
        double[] embedding2 = {0.15, 0.25, 0.35, 0.45};
        String answer2 = "Python 是一种高级编程语言，以其简洁的语法和强大的功能而闻名。";

        uploader.uploadAnswerAndEmbedding(question2, embedding2, answer2, error -> {
            if (error == null) {
                System.out.println("✓ 上传成功：" + question2);
            } else {
                System.out.println("✗ 上传失败：" + error.getMessage());
            }
            latch.countDown();
        });

        // 上传第三条数据
        String question3 = "什么是数据库？";
        double[] embedding3 = {0.9, 0.8, 0.7, 0.6};
        String answer3 = "数据库是用于存储和管理数据的系统，支持数据的增删改查操作。";

        uploader.uploadAnswerAndEmbedding(question3, embedding3, answer3, error -> {
            if (error == null) {
                System.out.println("✓ 上传成功：" + question3);
            } else {
                System.out.println("✗ 上传失败：" + error.getMessage());
            }
            latch.countDown();
        });

        latch.await(30, TimeUnit.SECONDS);
    }

    /**
     * 示例2：查询相似向量
     */
    @Test
    public void testQueryEmbedding() throws InterruptedException {
        System.out.println("\n=== 示例2：查询相似向量 ===");

        CountDownLatch latch = new CountDownLatch(1);

        // 查询向量（与 "什么是 Java？" 相似）
        double[] queryEmbedding = {0.12, 0.22, 0.32, 0.42};

        EmbeddingQuerier querier = (EmbeddingQuerier) provider;
        querier.queryEmbedding(queryEmbedding, (results, error) -> {
            if (error == null && results != null) {
                System.out.println("✓ 查询成功，找到 " + results.size() + " 条结果：");
                for (int i = 0; i < results.size(); i++) {
                    QueryResult result = results.get(i);
                    System.out.println("\n结果 " + (i + 1) + ":");
                    System.out.println("  问题: " + result.getText());
                    System.out.println("  答案: " + result.getAnswer());
                    System.out.println("  相似度: " + result.getScore());
                }
            } else {
                System.out.println("✗ 查询失败：" + (error != null ? error.getMessage() : "未知错误"));
            }
            latch.countDown();
        });

        latch.await(30, TimeUnit.SECONDS);
    }

    /**
     * 示例3：完整的 RAG 流程
     */
    @Test
    public void testRAGWorkflow() throws InterruptedException {
        System.out.println("\n=== 示例3：完整的 RAG 流程 ===");

        CountDownLatch uploadLatch = new CountDownLatch(1);
        CountDownLatch queryLatch = new CountDownLatch(1);

        // 步骤1：上传知识库
        System.out.println("\n步骤1：上传知识库...");
        String question = "如何使用 Qdrant？";
        double[] embedding = {0.5, 0.6, 0.7, 0.8};
        String answer = "Qdrant 是一个向量数据库，可以通过 REST API 进行向量的存储和检索。";

        AnswerAndEmbeddingUploader uploader = (AnswerAndEmbeddingUploader) provider;
        uploader.uploadAnswerAndEmbedding(question, embedding, answer, error -> {
            if (error == null) {
                System.out.println("✓ 知识库上传成功");
            } else {
                System.out.println("✗ 知识库上传失败：" + error.getMessage());
            }
            uploadLatch.countDown();
        });

        uploadLatch.await(30, TimeUnit.SECONDS);

        // 等待一下，确保数据已写入
        Thread.sleep(1000);

        // 步骤2：查询相似问题
        System.out.println("\n步骤2：查询相似问题...");
        double[] queryEmbedding = {0.52, 0.62, 0.72, 0.82};

        EmbeddingQuerier querier = (EmbeddingQuerier) provider;
        querier.queryEmbedding(queryEmbedding, (results, error) -> {
            if (error == null && results != null && !results.isEmpty()) {
                System.out.println("✓ 找到相似问题：");
                QueryResult topResult = results.get(0);
                System.out.println("  原问题: " + topResult.getText());
                System.out.println("  答案: " + topResult.getAnswer());
                System.out.println("  相似度: " + topResult.getScore());

                // 步骤3：使用检索到的答案（RAG）
                System.out.println("\n步骤3：RAG - 使用检索到的上下文生成回答");
                System.out.println("  上下文: " + topResult.getAnswer());
                System.out.println("  最终回答: " + topResult.getAnswer());
            } else {
                System.out.println("✗ 查询失败：" + (error != null ? error.getMessage() : "未找到结果"));
            }
            queryLatch.countDown();
        });

        queryLatch.await(30, TimeUnit.SECONDS);
    }

    /**
     * 示例4：从 JSON 配置创建
     */
    @Test
    public void testCreateFromJson() throws Exception {
        System.out.println("\n=== 示例4：从 JSON 配置创建 ===");

        String jsonConfig = """
        {
          "type": "qdrant",
          "serviceName": "localhost",
          "serviceHost": "127.0.0.1",
          "servicePort": 6333,
          "collectionID": "test_collection",
          "topK": 5,
          "timeout": 10000
        }
        """;

        System.out.println("JSON 配置:");
        System.out.println(jsonConfig);

        // 解析 JSON
        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree(jsonConfig);

        // 创建配置
        ProviderConfig config = new ProviderConfig();
        config.fromJson(json);
        config.validate();

        // 创建提供者
        Provider jsonProvider = VectorProviderFactory.createProvider(config);

        System.out.println("\n从 JSON 创建的 Provider:");
        System.out.println("  类型: " + jsonProvider.getProviderType());
        System.out.println("  创建成功！");
    }
}

