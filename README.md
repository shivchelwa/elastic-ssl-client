# elastic-ssl-client
A high-level Java client to test HTTPS connectivity to Elastic.

# Setup HTTPS on ElasticSearch

Follow the steps mentioned here https://www.elastic.co/guide/en/elasticsearch/reference/7.11/configuring-tls.html#node-certificates to setup TLS and HTTPS on     Elastic cluster.

Note: No password used to generated certificates.

# Run Java Client
* mvn clean package
* java -jar target/elastic-ssl-client-jar-with-dependencies.jar org.me.elastic.ElasticSSLClient <path-to-http.p12>
