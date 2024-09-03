package com.apps.pochak.member;

import com.apps.pochak.member.fixture.MemberFixture;
import com.apps.pochak.global.s3.S3Service;
import com.apps.pochak.member.service.scheduler.ProfileImageDeletionBatch;
import com.apps.pochak.member.service.scheduler.ProfileImageDeletionQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.mockito.Mockito.*;

public class ProfileImageCleanupBatchTest {

    @Mock
    private S3Service awsS3Service;

    @Mock
    private ProfileImageDeletionQueue profileImageDeletionQueue;

    @InjectMocks
    private ProfileImageDeletionBatch profileImageDeletionBatch;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("이전 프로필 사진을 삭제한다")
    void testCleanUpOldProfileImage() {
        String oldImageUrl = MemberFixture.MEMBER1.getProfileImage();
        when(profileImageDeletionQueue.getImagesToDelete()).thenReturn(List.of(oldImageUrl));

        profileImageDeletionBatch.cleanUpOldProfileImages();

        verify(awsS3Service, times(1)).deleteFileFromS3(oldImageUrl);
        verify(profileImageDeletionQueue, times(1)).clearProcessedImages(List.of(oldImageUrl));
    }

    @Test
    @DisplayName("여러번 바뀐 이전 프로필 사진을 삭제한다.")
    void testCleanUpMultipleOldProfileImages() {
        String oldImageUrl1 = "http://example.com/oldImage1.jpg";
        String oldImageUrl2 = "http://example.com/oldImage2.jpg";
        String oldImageUrl3 = "http://example.com/oldImage3.jpg";

        when(profileImageDeletionQueue.getImagesToDelete()).thenReturn(List.of(oldImageUrl1, oldImageUrl2, oldImageUrl3));

        profileImageDeletionBatch.cleanUpOldProfileImages();

        verify(awsS3Service, times(1)).deleteFileFromS3(oldImageUrl1);
        verify(awsS3Service, times(1)).deleteFileFromS3(oldImageUrl2);
        verify(awsS3Service, times(1)).deleteFileFromS3(oldImageUrl3);

        verify(profileImageDeletionQueue, times(1)).clearProcessedImages(List.of(oldImageUrl1, oldImageUrl2, oldImageUrl3));
    }
}
