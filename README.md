<img width="1920" alt="소개페이지" src="https://github.com/SMWU-POCHAK/.github/assets/96935231/abda1ede-f3ea-488f-8099-be58e9f0ead8">

## <img width="40" src="https://github.com/5jisoo/5jisoo/assets/96935231/ed632e34-eb9d-47ae-990d-79f58b1e5669"/> 당신의 순간, 포착!  [![app store badge](http://img.shields.io/badge/App%20Store-4285F4?style=flat&logo=app-store&link=https://apps.apple.com/kr/app/pochak/id6502332418&logoColor=white)](https://apps.apple.com/kr/app/pochak/id6502332418)

**포착**은 사용자가 자신의 사진을 직접 업로드하는 것이 아니라, 타인이 촬영한 사용자의 사진을 공유함으로써 보다 자연스러운 일상을 기록할 수 있게 하는 소셜 미디어 플랫폼입니다.

어플리케이션에 내장된 후면 카메라 기능을 활용해 실시간으로 포착한 장면을 피드에 공유하는 방식을 취함으로써, 겉으로 비춰지고 싶은 모습만을 과시하는 보여주기식 SNS 소비 문화를 지양하고 **건강한 소통**을
활성화하고자 합니다.

### 📸 친구들의 시선으로 기록하는 나의 일상

**포착**은 **다른 사람의 시선에서 바라본 ‘나’의 일상**을 기록하는 특별한 서비스입니다.

사용자는 자신의 카메라를 사용하지 않아도, 친구들이 촬영한 사용자의 사진을 공유하는 방식으로 보다 쉽고 편리하게 자신의 피드를 꾸밀 수 있습니다. 이를 통해 과시적으로 연출된 모습이 아닌, 있는 그대로의 자연스러운
모습을 토대로 소중한 순간들을 기억할 수 있습니다.

### 🔄 상호 작용 및 공유

**포착**은 **서로의 순간에 참여**하고 함께 상호 작용할 수 있습니다.

사용자들은 피드에 이미지를 업로드하는 것 외에도 댓글을 통해 포착된/포착한 순간에 대해 이야기를 나누고, 좋아요를 통해 마음에 드는 사진에 반응할 수 있습니다.

### 🌟 프로필의 다양성

어플리케이션 내에서 사용자의 프로필은 두 가지 섹션으로 구분됩니다.

**‘포착된’ 프로필**은 다른 사람들이 기록한 사용자의 사진들을 보여줌으로써, 여러 시각에서 바라본 ‘나’의 순간들이 모여 사용자의 피드를 일구어 냅니다.

내가 기록한 다른 사용자의 사진들을 보여주는 **‘포착한’ 프로필**에서는 기억하고 싶은 일상 속 나의 시선들을 한눈에 볼 수 있습니다.

## STACK

- DB `MySQL`
- Host
  - GCP `Computing Engine` `Cloud SQL` `Cloud DNS` 
  - AWS `S3`
  - `Ubuntu 20.04.6 LTS` `nginx/1.18.0 (Ubuntu)`
  - `Docker version 27.1.2`
- CI/CD `GitHub Actions` `Jenkins Version 2.473`
- Framework `Spring Boot 3.2.1 (Java 17)`
- Authentication `JWT`
- API Document `Spring REST Docs`

<!-- 아키텍처 사진 넣기 -->

<!-- 

## ERD

> ERD 사진 넣기

-->

## Team Rule

[![GitHub](https://img.shields.io/badge/github%20convention-%23121011.svg?style=for-the-badge&logo=github&logoColor=white)](https://smwu-pochak.github.io/posts/server-team-rule/)



## Contributors


<table>
  <tr>
    <td align="center" valign="top" width="33%"><a href="https://github.com/5jisoo"><img src="https://avatars.githubusercontent.com/u/96935231?v=4" ></a></td>
    <td align="center" valign="top" width="33%"><a href="https://github.com/dudrhy12"><img src="https://avatars.githubusercontent.com/u/90203803?v=4" ></a></td>
    <td align="center" valign="top" width="33%"><a href="https://github.com/yeahjinjeong"><img src="https://avatars.githubusercontent.com/u/88896562?v=4" ></a></td>
  </tr>
  <tr>
    <td align="center" valign="top"><a href = "https://github.com/5jisoo">오지수 Lucy Oh</a><br>Backend Developer</td>
    <td align="center" valign="top"><a href = "https://github.com/dudrhy12">조영서 Bella Cho</a><br>Backend Developer</td>
    <td align="center" valign="top"><a href = "https://github.com/yeahjinjeong">정예진 Yejin Jeong</a><br>Backend Developer</td>
  </tr>
  <tr>
    <td valign="top"> <!-- 지수 -->
        <ul>
            <li>CI/CD 및 인프라 개선</li>
            <li>AWS RDS → CLOUD SQL 마이그레이션</li>
            <li>Spring REST Docs 연결</li>
            <li>게시물 신고 및 유저 차단 기능 개발</li>
            <li>회원 검색 기능 개발</li>
            <li>인증/인가 로직 리팩토링</li>
            <li>테스트코드 기본 설정</li>
            <li>
                프로필 탭 기능 개발
                  <ul>
                      <li>기본 프로필 정보, 포착된/포착한 게시물 조회</li>
                      <li>팔로워, 팔로우 목록 조회</li>
                      <li>타 유저 팔로우 및 취소</li>
                      <li>팔로워 삭제</li>
                  </ul>
            </li>
            <li>
                홈 탭 (게시물) 기능 개발
                  <ul>
                      <li>새로운 게시글 업로드</li>
                      <li>좋아요 반영, 좋아요 누른 유저 리스트 조회</li>
                      <li>댓글, 대댓글 조회, 등록, 삭제</li>
                  </ul>
            </li>
            <li>
                알람 및 게시글 수락 기능 개발
                  <ul>
                      <li>댓글, 팔로우 알람 생성</li>
                      <li>게시글 업로드 수락 과정 설정</li>
                      <li>알람 조회</li>
                  </ul>
            </li>
        </ul>
    </td>
    <td valign="top"> <!-- 영서 -->
        <ul>
            <li>CI/CD 및 인프라 개선</li>
            <li>
                프로필 탭 기능 개발
                  <ul>
                      <li>프로필 정보 수정</li>
                  </ul>
            </li>
        </ul>
    </td> 
    <td valign="top"> <!-- 예진 -->
        <ul>
        </ul>
    </td>

  </tr>
</table>
