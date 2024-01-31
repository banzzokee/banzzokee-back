package homes.banzzokee.global.config.elasticsearch;

import co.elastic.clients.transport.TransportUtils;
import javax.net.ssl.SSLContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.lang.NonNull;

/**
 * 스프링 부트 3.2.2와 호환되는 elasticsearch 및 kibana 버젼 : 8.11.3
 */
@Configuration
@EnableElasticsearchRepositories
public class ElasticSearchConfig extends ElasticsearchConfiguration {

  @Value("${elasticsearch.hostAndPort}")
  private String hostAndPort;

  @Value("${elasticsearch.fingerprint}")
  private String fingerprint;

  @Value("${elasticsearch.username}")
  private String username;

  @Value("${elasticsearch.password}")
  private String password;

  @NonNull
  @Override
  public ClientConfiguration clientConfiguration() {

    SSLContext sslContext = TransportUtils.sslContextFromCaFingerprint(fingerprint);

    return ClientConfiguration.builder()
        .connectedTo(hostAndPort)
        .usingSsl(sslContext)
        .withBasicAuth(username, password)
        .build();
  }
}
