package com.apps.pochak.global.image;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.apps.pochak.global.api_payload.exception.GeneralException;
import com.apps.pochak.global.api_payload.exception.handler.ImageException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import static com.apps.pochak.global.api_payload.code.status.ErrorStatus.*;

@Service
@Component
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String upload(MultipartFile multipartFile, DirName dirName) {
        if (multipartFile.isEmpty())
            throw new GeneralException(NULL_FILE);
        try {
            File uploadFile = convert(multipartFile)
                    .orElseThrow(() -> new ImageException(CONVERT_FILE_ERROR));
            return upload(uploadFile, dirName);
        } catch (IOException e) {
            throw new GeneralException(IO_EXCEPTION);
        }
    }

    private String upload(File uploadFile, DirName dirName) {
        String fileName = dirName.getDirName() + "/" + UUID.randomUUID() + uploadFile.getName();
        String uploadImageUrl = putS3(uploadFile, fileName);
        deleteFile(uploadFile);
        return uploadImageUrl;
    }

    private Optional<File> convert(MultipartFile file) throws IOException {
        File convertFile = new File(System.getProperty("user.dir") + "/" + file.getOriginalFilename());
        if (convertFile.createNewFile()) {
            try (FileOutputStream fos = new FileOutputStream(convertFile)) {
                fos.write(file.getBytes());
            }
            return Optional.of(convertFile);
        }
        return Optional.empty();
    }

    private String putS3(File uploadFile, String fileName) {
        try {
            amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, uploadFile).withCannedAcl(CannedAccessControlList.PublicRead));
            return amazonS3Client.getUrl(bucket, fileName).toString();
        } catch (AmazonS3Exception e) {
            throw new ImageException(S3_UPLOAD_ERROR);
        }
    }

    private void deleteFile(File targetFile) {
        try {
            targetFile.delete();
        } catch (AmazonServiceException e) {
            throw new ImageException(DELETE_FILE_ERROR);
        }
    }

    public void deleteFileFromS3(String fileUrl) {
        try {
            String splitStr = ".com/";
            String encodedFileName = fileUrl.substring(fileUrl.indexOf(splitStr) + splitStr.length());
            String fileName = URLDecoder.decode(encodedFileName, StandardCharsets.UTF_8.name());
            amazonS3Client.deleteObject(new DeleteObjectRequest(bucket, fileName));

        } catch (AmazonServiceException e) {
            throw new ImageException(DELETE_FILE_ERROR);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
