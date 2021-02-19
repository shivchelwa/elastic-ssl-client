package org.me.elastic;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpHost;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.action.support.ActiveShardCount;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;

public class ElasticSSLClient {

	public static void main(String[] args) {
		
		RestHighLevelClient elasticClient;
		try {
			elasticClient = connect(args[0], null);
			createIndexes(elasticClient);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static RestHighLevelClient connect(String pathToCertStore, String keyStorePass) throws Exception {
		Path trustStorePath = Paths.get(pathToCertStore);
		KeyStore truststore = KeyStore.getInstance("pkcs12");
		try (InputStream is = Files.newInputStream(trustStorePath)) {
			if (keyStorePass != null && !keyStorePass.isBlank()) {
				truststore.load(is, keyStorePass.toCharArray());
			} else {
				truststore.load(is, null);
			}
		}
		SSLContextBuilder sslBuilder = SSLContexts.custom().loadTrustMaterial(truststore, null);
		final SSLContext sslContext = sslBuilder.build();
		RestClientBuilder builder = RestClient.builder(new HttpHost("localhost", 9200, "https"))
				.setHttpClientConfigCallback(new HttpClientConfigCallback() {
					@Override
					public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
						return httpClientBuilder.setSSLContext(sslContext);
					}
				});
		return new RestHighLevelClient(builder);

	}

	private static void createIndexes(RestHighLevelClient elasticClient) throws Exception {
		boolean indexExist = false;
		String indexName = "test_index";

		GetIndexRequest getIndexReq = new GetIndexRequest(indexName);
		getIndexReq.setTimeout(TimeValue.timeValueMillis(10000));
		getIndexReq.setMasterTimeout(TimeValue.timeValueMillis(10000));
		indexExist = elasticClient.indices().exists(getIndexReq, RequestOptions.DEFAULT);


		if (!indexExist) {
			System.out.println("Creating index..");
			CreateIndexRequest createIndexReq = new CreateIndexRequest(indexName);

			createIndexReq
					.settings(Settings.builder().put("index.number_of_shards", 1).put("index.number_of_replicas", 1));

			createIndexReq.setTimeout(TimeValue.timeValueMillis(10000));
			createIndexReq.setMasterTimeout(TimeValue.timeValueMillis(10000));
			createIndexReq.waitForActiveShards(ActiveShardCount.from(10000));
			CreateIndexResponse createIndexResponse = elasticClient.indices().create(createIndexReq,
					RequestOptions.DEFAULT);
			if (!createIndexResponse.isAcknowledged())
				throw new Exception(String.format("Error creating index[%s] for entity[%s]",
						createIndexResponse.index(), indexName));

		}
	}
}
