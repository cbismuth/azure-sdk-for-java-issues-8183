package fr.tf1.data.monitoring.azure;

import com.azure.core.http.rest.PagedIterable;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobContainerItem;
import com.azure.storage.blob.models.BlobItem;
import com.microsoft.azure.management.storage.StorageAccount;
import org.springframework.stereotype.Service;

@Service
class AzureBlobItemsFinderService {

    private final AzureAuditConnector azureAuditConnector;
    private final AuditFileDownloadService auditFileDecoderService;

    AzureBlobItemsFinderService(final AzureAuditConnector azureAuditConnector,
                                final AuditFileDownloadService auditFileDecoderService) {
        this.azureAuditConnector = azureAuditConnector;
        this.auditFileDecoderService = auditFileDecoderService;
    }

    void queryStorageAccounts() {
        azureAuditConnector.getStorageAccounts()
                           .forEach(this::queryStorageAccount);
    }

    private void queryStorageAccount(final StorageAccount storageAccount) {
        azureAuditConnector.createBlobServiceClient(storageAccount)
                           .listBlobContainers()
                           .stream()
                           .forEach(blobContainerItem -> queryBlobContainerItem(storageAccount, blobContainerItem));
    }

    private void queryBlobContainerItem(final StorageAccount storageAccount, final BlobContainerItem blobContainerItem) {
        final BlobContainerClient blobContainerClient = azureAuditConnector.createBlobContainerClient(storageAccount, blobContainerItem);

        final PagedIterable<BlobItem> blobItems = blobContainerClient.listBlobs();

        blobItems.forEach(blobItem -> auditFileDecoderService.downloadToFile(storageAccount, blobContainerItem, blobItem));
    }
}
