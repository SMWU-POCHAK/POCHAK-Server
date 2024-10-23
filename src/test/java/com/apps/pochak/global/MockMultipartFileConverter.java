package com.apps.pochak.global;

import org.springframework.mock.web.MockMultipartFile;

import java.io.FileInputStream;
import java.io.IOException;

public class MockMultipartFileConverter {
    public static MockMultipartFile getMockMultipartFileOfPost() throws IOException {
        final String fileName = "APPS_LOGO";
        final String fileType = "PNG";

        final FileInputStream fileInputStream
                = new FileInputStream("src/test/resources/static/" + fileName + "." + fileType);

        return new MockMultipartFile(
                "postImage",
                fileName + "." + fileType,
                "multipart/form-data",
                fileInputStream
        );
    }

    public static MockMultipartFile getMockMultipartFileOfMember() throws IOException {
        final String fileName = "APPS_LOGO";
        final String fileType = "PNG";

        final FileInputStream fileInputStream
                = new FileInputStream("src/test/resources/static/" + fileName + "." + fileType);

        return new MockMultipartFile(
                "profileImage",
                fileName + "." + fileType,
                "multipart/form-data",
                fileInputStream
        );
    }
}
