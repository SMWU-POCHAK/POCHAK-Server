package com.apps.pochak.elastic.member;

import com.apps.pochak.member.domain.Member;
import com.apps.pochak.member.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberElasticScheduler {
    private final MemberRepository memberRepository;
    private final MemberElasticRepository memberElasticRepository;

//    @Scheduled(cron = "0 0 * * * *")
    public void saveMemberData() {
        final List<Member> memberList
                = memberRepository.findModifiedMemberWithinOneHour(LocalDateTime.now().minusHours(1));

        final List<MemberDocument> memberDocumentList = memberList.stream().map(
                MemberDocument::new
        ).collect(Collectors.toList());

        memberElasticRepository.saveAll(memberDocumentList);
    }
}
