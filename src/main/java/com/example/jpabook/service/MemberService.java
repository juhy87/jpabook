package com.example.jpabook.service;

import com.example.jpabook.domain.Member;
import com.example.jpabook.repository.MemberBasicJPARepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {
    private final MemberBasicJPARepository memberBasicJPARepository;

    @Transactional
    public void saveMember(Member member){
        memberBasicJPARepository.save(member);
    }

    public Member findOne(Long id){
        return memberBasicJPARepository.findOne(id);
    }
}
