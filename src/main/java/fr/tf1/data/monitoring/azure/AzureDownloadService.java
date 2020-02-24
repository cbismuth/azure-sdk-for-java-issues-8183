package fr.tf1.data.monitoring.azure;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.models.BlobContainerItem;
import com.azure.storage.blob.models.BlobItem;
import com.microsoft.azure.management.storage.StorageAccount;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
class AzureDownloadService {

    private final AzureConnector azureConnector;

    AzureDownloadService(final AzureConnector azureConnector) {
        this.azureConnector = azureConnector;
    }

    void downloadToFile(final StorageAccount storageAccount,
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
