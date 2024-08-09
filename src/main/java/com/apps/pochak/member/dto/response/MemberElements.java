package com.apps.pochak.member.dto.response;

import com.apps.pochak.global.PageInfo;
import com.apps.pochak.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberElements {
    private PageInfo pageInfo;
    private List<MemberElement> memberList;

    public MemberElements(final Page<MemberElement> memberElementPage) {
        this.pageInfo = new PageInfo(memberElementPage);
        this.memberList = memberElementPage.getContent();
    }

    public static MemberElements from(final Page<Member> memberPage) {
        PageInfo pageInfo = new PageInfo(memberPage);
        List<MemberElement> memberElementList = memberPage.getContent()
                .stream()
                .map(MemberElement::new)
                .collect(Collectors.toList());

        return new MemberElements(pageInfo, memberElementList);
    }
}
