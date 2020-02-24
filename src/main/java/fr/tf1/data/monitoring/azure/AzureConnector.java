package fr.tf1.data.monitoring.azure;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobContainerItem;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.management.storage.StorageAccountKey;
import com.microsoft.azure.management.storage.implementation.StorageManager;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
class AzureConnector {

    private final StorageManager storageManager;

    AzureConnector(final StorageManager storageManager) {
        this.storageManager = storageManager;
    }

    List<StorageAccount> getStorageAccounts() {
        return storageManager.storageAccounts().list();
    }

    BlobServiceClient createBlobServiceClient(final StorageAccount storageAccount) {
        final StorageAccountKey storageAccountKey = storageAccount.getKeys().get(0);
        final StorageSharedKeyCredential storageSharedKeyCredential = new StorageSharedKeyCredential(storageAccount.name(), storageAccountKey.value());

        return new BlobServiceClientBuilder().credential(storageSharedKeyCredential)
                                             .endpoint(storageAccount.endPoints().primary().blob())
                                             .buildClient();
    }

    BlobContainerClient createBlobContainerClient(final StorageAccount storageAccount, final BlobContainerItem blobContainerItem) {
        final StorageAccountKey storageAccountKey = storageAccount.getKeys().get(0);
        final StorageSharedKeyCredential storageSharedKeyCredential = new StorageSharedKeyCredential(storageAccount.name(), storageAccountKey.value());

        return new BlobContainerClientBuilder().endpoint(storageAccount.endPoints().primary().blob())
                                               .credential(storageSharedKeyCredential)
                                               .containerName(blobContainerItem.getName())
                                               .buildClient();
    }

    BlobClient createBlobClient(final StorageAccount storageAccount, final BlobContainerItem blobContainerItem, final String blobName) {
        final StorageAccountKey storageAccountKey = storageAccount.getKeys().get(0);
        final StorageSharedKeyCredential storageSharedKeyCredential = new StorageSharedKeyCredential(storageAccount.name(), storageAccountKey.value());

        return new BlobClientBuilder().addPolicy(ContentTypeRemovalHttpPipelinePolicy.INSTANCE)
                                      .endpoint(storageAccount.endPoints().primary().blob())
                                      .credential(storageSharedKeyCredential)
                                      .containerName(blobContainerItem.getName())
                                      .blobName(blobName)
                                      .buildClient();
    }
}
