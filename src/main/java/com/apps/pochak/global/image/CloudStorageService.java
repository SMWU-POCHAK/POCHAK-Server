package com.apps.pochak.global.image;

import com.apps.pochak.global.api_payload.exception.GeneralException;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.apps.pochak.global.api_payload.code.status.ErrorStatus.IO_EXCEPTION;
import static com.apps.pochak.global.api_payload.code.status.ErrorStatus.NULL_FILE;

@Service
@RequiredArgsConstructor
public class CloudStorageService {

    private final Storage storage;

    @Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketName;

    public String upload(
            final MultipartFile multipartFile,
            final DirName dirName
    ) {
        if (multipartFile.isEmpty()) throw new GeneralException(NULL_FILE);

        String objectName = dirName.getDirName() + "/" + UUID.randomUUID() + "_" + multipartFile.getOriginalFilename();
        String contentType = multipartFile.getContentType();

        try {
            storage.create(
                    BlobInfo.newBuilder(bucketName, objectName)
                            .setContentType(contentType)
                            .build(),
                    multipartFile.getInputStream()
            );
        } catch (IOException e) {
            throw new GeneralException(IO_EXCEPTION);
        }

        return "https://storage.googleapis.com/" + bucketName + "/" + objectName;
    }

    public void delete(final String fileUrl) {
        String objectName = getObjectNameFromUrl(fileUrl);
        Blob blob = storage.get(bucketName, objectName);
        if (blob == null) return;

        BlobId idWithGeneration = blob.getBlobId();
        storage.delete(idWithGeneration);
    }

    public void delete(final List<String> fileUrlList) {
        List<BlobId> blobIdList = fileUrlList.stream().map(
                        url -> {
                            String objectName = getObjectNameFromUrl(url);
                            Blob blob = storage.get(bucketName, objectName);
                            if (blob == null) return null;
                            return blob.getBlobId();
                        }
                ).filter(Objects::nonNull)
                .toList();

        storage.delete(blobIdList);
    }

    private String getObjectNameFromUrl(final String fileUrl) {
        String splitStr = bucketName + "/";
        String encodedFileName = fileUrl.substring(fileUrl.indexOf(splitStr) + splitStr.length());
        return URLDecoder.decode(encodedFileName, StandardCharsets.UTF_8);
    }
}
