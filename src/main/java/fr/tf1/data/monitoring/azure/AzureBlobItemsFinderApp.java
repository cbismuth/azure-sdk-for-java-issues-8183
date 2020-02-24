package fr.tf1.data.monitoring.azure;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

final class AzureBlobItemsFinderApp {

    public static void main(final String[] args) {
        try (final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(SpringConfig.class)) {
            final AzureBlobItemsFinderService service = context.getBean(AzureBlobItemsFinderService.class);

            service.queryStorageAccounts();
        }
    }
}
