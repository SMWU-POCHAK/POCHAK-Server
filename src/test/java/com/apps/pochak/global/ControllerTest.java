package com.apps.pochak.global;


import com.apps.pochak.discord.service.DiscordService;
import com.apps.pochak.login.provider.JwtProvider;
import com.apps.pochak.login.util.LoginArgumentResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;

@ExtendWith(RestDocumentationExtension.class)
public abstract class ControllerTest {
    protected static final String ACCESS_TOKEN_HEADER = "Authorization";
    protected static final String ACCESS_TOKEN = "accessToken";
    protected static final String REFRESH_TOKEN_HEADER = "RefreshToken";
    protected static final String REFRESH_TOKEN = "refreshToken";

    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    protected JwtProvider jwtProvider;

    @MockBean
    protected LoginArgumentResolver loginArgumentResolver;

    @MockBean
    DiscordService discordService;

    @BeforeEach
    void setUp(
            WebApplicationContext webApplicationContext,
            RestDocumentationContextProvider restDocumentation
    ) {
        this.mockMvc =
                MockMvcBuilders
                        .webAppContextSetup(webApplicationContext)
                        .addFilter(new CharacterEncodingFilter("UTF-8", true))
                        .apply(documentationConfiguration(restDocumentation))
                        .build();
    }
}
