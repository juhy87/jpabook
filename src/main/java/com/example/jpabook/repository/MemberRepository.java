package com.example.jpabook.repository;

import com.example.jpabook.domain.Member;
import com.querydsl.core.annotations.QueryProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom, QuerydslPredicateExecutor<Member> {
//public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

    //select m from MEmber m where m.username = ?
    List<Member> findByUsername(String username);

}
