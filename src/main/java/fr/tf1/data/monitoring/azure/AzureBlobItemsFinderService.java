package fr.tf1.data.monitoring.azure;

import com.azure.core.http.rest.PagedIterable;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobContainerItem;
import com.azure.storage.blob.models.BlobItem;
import com.microsoft.azure.management.storage.StorageAccount;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
class AzureBlobItemsFinderService {

    private final AzureConnector azureConnector;

    AzureBlobItemsFinderService(final AzureConnector azureConnector) {
        this.azureConnector = azureConnector;
    }

    void queryStorageAccounts() {
        azureConnector.getStorageAccounts()
                      .forEach(this::queryStorageAccount);
    }

    private void queryStorageAccount(final StorageAccount storageAccount) {
        azureConnector.createBlobServiceClient(storageAccount)
                      .listBlobContainers()
                      .stream()
                      .forEach(blobContainerItem -> queryBlobContainerItem(storageAccount, blobContainerItem));
    }

    private void queryBlobContainerItem(final StorageAccount storageAccount, final BlobContainerItem blobContainerItem) {
        final BlobContainerClient blobContainerClient = azureConnector.createBlobContainerClient(storageAccount, blobContainerItem);

        final PagedIterable<BlobItem> blobItems = blobContainerClient.listBlobs();

        blobItems.forEach(blobItem -> downloadToFile(storageAccount, blobContainerItem, blobItem));
    }

    private void downloadToFile(final StorageAccount storageAccount,
                                final BlobContainerItem blobContainerItem,
                                final BlobItem blobItem) {
        final BlobClient blobClient = azureConnector.createBlobClient(storageAccount, blobContainerItem, blobItem.getName());

        final String absolutePath = createTempFile(blobItem);

        blobClient.downloadToFile(absolutePath, true);
    }

    private String createTempFile(final BlobItem blobItem) {
        try {
            final Path tempFile = Files.createTempFile("" + System.currentTimeMillis(), FilenameUtils.getName(blobItem.getName()));

            tempFile.toFile().deleteOnExit();

            return tempFile.toString();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
