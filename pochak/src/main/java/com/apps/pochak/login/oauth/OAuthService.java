package com.apps.pochak.login.oauth;

import com.apps.pochak.common.BaseException;
import com.apps.pochak.common.BaseResponse;
import com.apps.pochak.login.dto.OAuthResponse;
import com.apps.pochak.login.dto.UserInfoRequest;
import com.apps.pochak.login.jwt.JwtService;
import com.apps.pochak.user.domain.SocialType;
import com.apps.pochak.user.domain.User;
import com.apps.pochak.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.apps.pochak.common.BaseResponseStatus.SUCCESS;

@Service
@RequiredArgsConstructor
public class OAuthService {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public OAuthResponse signup(UserInfoRequest userInfoRequest) {
        userRepository.findUserWithSocialId(userInfoRequest.getSocialId())
                .ifPresent(i -> {
                    throw new IllegalStateException("이미 존재하는 회원입니다.");
                });

        String refreshToken = jwtService.createRefreshToken();
        String accessToken = jwtService.createAccessToken(userInfoRequest.getHandle());

        User user = User.signupUser()
                .name(userInfoRequest.getName())
                .email(userInfoRequest.getEmail())
                .handle(userInfoRequest.getHandle())
                .message(userInfoRequest.getMessage())
                .socialId(userInfoRequest.getSocialId())
                .profileImage(userInfoRequest.getProfileImage())
                .socialType(SocialType.of(userInfoRequest.getSocialType()))
                .socialRefreshToken(userInfoRequest.getSocialRefreshToken())
                .build();

        user.updateRefreshToken(refreshToken);
        userRepository.saveUser(user);
        return OAuthResponse.builder()
                .isNewMember(false)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public BaseResponse logout(String handle) throws BaseException {
        User user = userRepository.findUserByUserHandle(handle);
        user.updateRefreshToken(null);
        userRepository.saveUser(user);
        return new BaseResponse(SUCCESS);
    }
}
