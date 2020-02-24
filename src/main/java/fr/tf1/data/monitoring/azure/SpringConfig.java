package fr.tf1.data.monitoring.azure;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.AzureResponseBuilder;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.resources.Subscription;
import com.microsoft.azure.management.resources.Subscriptions;
import com.microsoft.azure.management.resources.fluentcore.utils.ProviderRegistrationInterceptor;
import com.microsoft.azure.management.resources.fluentcore.utils.ResourceManagerThrottlingInterceptor;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.azure.management.resources.implementation.ResourceManager.Authenticated;
import com.microsoft.azure.management.storage.implementation.StorageManager;
import com.microsoft.azure.serializer.AzureJacksonAdapter;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.retry.ExponentialBackoffRetryStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

@Configuration
@ComponentScan("fr.tf1.data.monitoring")
@PropertySource("classpath:application.properties")
public class SpringConfig {

    @Bean
    static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Value("${monitoring.azure.clientId}")
    private String clientId;
    @Value("${monitoring.azure.tenantId}")
    private String tenantId;
    @Value("${monitoring.azure.secret}")
    private String secret;

    @Bean
    Subscription defaultSubscription(final ApplicationTokenCredentials credentials) {
        final Authenticated authenticated = ResourceManager.authenticate(credentials);

        final Subscriptions subscriptions = authenticated.subscriptions();

        checkSubscriptions(subscriptions);

        return subscriptions.list().get(0);
    }

    private void checkSubscriptions(final Subscriptions subscriptions) {
        final int size = subscriptions.list().size();

        if (size != 1) {
            throw new IllegalStateException(String.format("Expected only one Azure subscription ([%d] found)", size));
        }
    }

    @Bean
    StorageManager storageManager(final RestClient restClient, final Subscription defaultSubscription) {
        return StorageManager.authenticate(restClient, defaultSubscription.subscriptionId());
    }

    @Bean
    RestClient restClient(final ApplicationTokenCredentials credentials) {
        return new RestClient.Builder().withBaseUrl(credentials.environment(), AzureEnvironment.Endpoint.RESOURCE_MANAGER)
                                       .withCredentials(credentials)
                                       .withSerializerAdapter(new AzureJacksonAdapter())
                                       .withResponseBuilderFactory(new AzureResponseBuilder.Factory())
                                       .withInterceptor(new ProviderRegistrationInterceptor(credentials))
                                       .withInterceptor(new ResourceManagerThrottlingInterceptor())
                                       .withRetryStrategy(new ExponentialBackoffRetryStrategy())
                                       .build();
    }

    @Bean
    ApplicationTokenCredentials applicationTokenCredentials() {
        return new ApplicationTokenCredentials(clientId, tenantId, secret, AzureEnvironment.AZURE);
    }

    @Bean
    ConversionService conversionService() {
        return new DefaultConversionService();
    }
}
