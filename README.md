<img alt="소개페이지" src="https://github.com/APPS-sookmyung/2023-POCHAK-server/assets/96935231/42e4e80e-ea86-4694-aef7-bc52a4ac4c25">

## POCHAK

당신의 순간, 포착

## About POCHAK

<details>
    <summary>✨소개 이미지</summary>
    <img alt="HOME" src="https://github.com/APPS-sookmyung/2023-POCHAK-server/assets/96935231/0483a2fe-f210-47c7-afad-bc0e8fb142c9">
    <img alt="SEARCH   ADD   ALARM" src="https://github.com/APPS-sookmyung/2023-POCHAK-server/assets/96935231/aa780252-51fc-4097-917e-a1b61b1bca67">
    <img alt="PROFILE" src="https://github.com/APPS-sookmyung/2023-POCHAK-server/assets/96935231/0b123371-7af1-4ff6-930f-4bc2f3778c30">
</details>

<br>

POCHAK은 사용자가 직접 사진을 올리는 것이 아니라, 친구들이 사용자를 중심으로 촬영한 사진을 통해 자연스럽게 일상을 기록하고 공유하는 특별한 소셜 미디어 플랫폼입니다. 다양한 시각에서의 일상을 통해 사용자들에게 새로운 경험을 제공합니다.

### 📸 친구들의 시선으로 기록되다

POCHAK은 다른 사용자들이 당신의 일상을 기록하도록 하는 독특한 서비스입니다. 당신은 자신의 카메라를 통해 아름다운 순간들을 기록하지 않아도, 친구들이나 팔로워들이 당신을 찍은 사진을 피드에 올려 공유할 수 있습니다.

### 🔄 상호 작용과 공유

POCHAK은 사용자들이 서로의 순간에 참여하고 상호 작용할 수 있도록 하는 기능을 강조합니다. 댓글, 좋아요, 그리고 공유를 통해 순간들을 더 특별하게 만들어보세요!

### 🌟 프로필의 다양성

당신의 POCHAK 프로필은 다른 사람들이 기록한 당신의 순간들을 보여줍니다. 여러 시각으로부터의 사진들이 모여 하나의 아름다운 이야기를 만들어냅니다.

## Stacks

<details>
    <summary>✨구조도</summary>
    <img width="80%" alt="구조도" src="https://github.com/SMWU-POCHAK/POCHAK-Server/assets/96935231/7e292540-5633-4199-a8ed-907c524a1e7d">
</details>

### 공통

> CI/CD

- GitHub Actions
- AWS CodeDeploy, EC2, S3

> 서비스

- AWS EC2, RDS, Lambda, Route53, API Gateway, Rekognition
- Nginx
- MySQL
- Elasticsearch

### 1차 기능 개발

> 서비스 특성상 데이터베이스를 DynamoDB에서 MySQL로 변경 후 다시 개발하였습니다. [1차 개발 버전](https://github.com/SMWU-POCHAK/POCHAK-Server/tree/dynamodb-version)

- Spring Boot 2.7.14 (Java 17)
- [Spring Data DynamoDB](https://github.com/boostchicken/spring-data-dynamodb)
- DynamoDB

### 2차 기능 개발

- Spring Boot 3.2.1 (Java 17)
- Spring Data JPA
- MySQL

## Contributors

> 최신순 `(2차 → 1차)` 으로 나열되어 있습니다.

<table>
  <tr>
    <td align="center" valign="top" width="25%"><a href="https://github.com/Haileyyyyyyyy"><img src="https://avatars.githubusercontent.com/u/90396746?v=4" ></a></td>
    <td align="center" valign="top" width="25%"><a href="https://github.com/5jisoo"><img src="https://avatars.githubusercontent.com/u/96935231?v=4" ></a></td>
    <td align="center" valign="top" width="25%"><a href="https://github.com/dudrhy12"><img src="https://avatars.githubusercontent.com/u/90203803?v=4" ></a></td>
    <td align="center" valign="top" width="25%"><a href="https://github.com/hagoeun0119"><img src="https://avatars.githubusercontent.com/u/93965468?v=4" ></a></td>
  </tr>
  <tr>
    <td align="center" valign="top"><a href = "https://github.com/Haileyyyyyyyy">김다연 Hailey Kim</a><br>PM, Backend Developer</td>
    <td align="center" valign="top"><a href = "https://github.com/5jisoo">오지수 Lucy Oh</a><br>Backend Developer</td>
    <td align="center" valign="top"><a href = "https://github.com/dudrhy12">조영서 Bella Cho</a><br>Backend Developer</td>
    <td align="center" valign="top"><a href = "https://github.com/hagoeun0119">하고은 Goeun Ha</a><br>Backend Developer</td>
  </tr>
  <tr>
    <td valign="top"> <!-- 다연 -->
      ● 탐색탭 추천 알고리즘 개발 </br>
      ● 실시간 아이디 검색 기능 개발 </br>
      ● CI/CD 구축 </br>
      <details>
      <summary>[1차] 홈탭 기능 개발</summary>
      <ul>
          <li>새로운 게시글 업로드</li>
          <li>좋아요 반영, 좋아요 누른 유저 리스트 조회</li>
          <li>댓글 조회, 등록, 삭제</li>
      </ul>
      </details>
      </ul>
    </td>
    <td valign="top"> <!-- 지수 -->
      ● 게시물 신고 및 유저 차단 기능 개발 </br>
      <details>
      <summary>프로필 탭 기능 개발</summary>
      <ul>
          <li>기본 프로필 정보, 포착된/포착한 게시물 조회</li>
          <li>팔로워, 팔로우 목록 조회</li>
          <li>타 유저 팔로우 및 취소</li>
          <li>팔로워 삭제</li>
      </ul>
      </details>
      <details>
      <summary>[2차] 홈탭(게시물)기능 개발</summary>
      <ul>
          <li>새로운 게시글 업로드</li>
          <li>좋아요 반영, 좋아요 누른 유저 리스트 조회</li>
          <li>댓글, 대댓글 조회, 등록, 삭제</li>
      </ul>
      </details>
      <details>
      <summary>[2차] 알람 및 게시글 수락 기능 개발</summary>
      <ul>
          <li>댓글, 팔로우 알람 생성</li>
          <li>게시글 업로드 수락 과정 설정</li>
      </ul>
      </details>
      ● Elasticsearch 데이터 동기화 - 배치 기능 개발 </br>
      ● 탐색탭 추천 API 연결 </br>
    </td>
    <td valign="top"> <!-- 영서 -->
      ● 프로필 수정 기능 작업 중 <br>
    </td>
    <td valign="top"> <!-- 고은 -->
      <details>
      <summary>구글 / 애플 소셜 로그인 기능 개발</summary>
      <ul>
          <li>Spring Security 인증, 인가 정책 설정</li>
          <li>회원가입, 로그인, 회원 탈퇴</li>
      </ul>
      </details>
      ● 이미지 업로드 기능 개발 <br>
      <details>
      <summary>[2차] 좋아요 기능 개발</summary>
      <ul>
          <li>게시글 좋아요 반영, 좋아요 누른 유저 리스트 조회</li>
          <li>회원가입, 로그인, 회원 탈퇴 기능</li>
      </ul>
      </details>
      <details>
      <summary>[1차] 알람 및 게시글 수락 기능 개발</summary>
      <ul>
          <li>댓글, 팔로우 알람 생성</li>
          <li>게시글 업로드 수락 과정 설정</li>
      </ul>
      </details>
    </td>
  </tr>
</table>
