# Elasticsearch 8.x + IK 中文分词插件
# 用于 blog_article 索引的中文全文检索
FROM docker.elastic.co/elasticsearch/elasticsearch:8.11.1

# 安装 IK 分词器（版本需与 ES 版本严格匹配）
RUN ./bin/elasticsearch-plugin install --batch \
    https://release.infinilabs.com/analysis-ik/stable/elasticsearch-analysis-ik-8.11.1.zip
