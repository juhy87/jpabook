package com.example.jpabook.repository;

import com.example.jpabook.domain.Member;
import com.example.jpabook.domain.QMember;
import com.example.jpabook.domain.Team;
import com.example.jpabook.dto.MemberDto;
import com.example.jpabook.dto.QMemberDto;
import com.example.jpabook.dto.UserDto;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import java.util.List;

import static com.example.jpabook.domain.QMember.member;
import static com.example.jpabook.domain.QTeam.team;
import static com.querydsl.jpa.JPAExpressions.select;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class QuerydslTest {

    @Autowired
    private EntityManager em;

    private JPAQueryFactory queryFactory;

    @BeforeEach
    public void before() {

        Team team1 = new Team("A Team");
        Team team2 = new Team("B Team");

        em.persist(team1);
        em.persist(team2);

        Member member1 = new Member("member1", 10, team1);
        Member member2 = new Member("member2", 20, team1);
        Member member3 = new Member("member3", 30, team2);
        Member member4 = new Member("member4", 40, team2);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    //    @Test
    public void startJPQL() {

        String qlString = "select m from Member m where m.username = :username";
        Member findMember = em.createQuery(qlString, Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        Assertions.assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void startQuerydsl() {
        queryFactory = new JPAQueryFactory(em);
        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();
        Assertions.assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void searchParam() {
        queryFactory = new JPAQueryFactory(em);
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1").or(member.age.eq(10)))
                .fetchOne();
        Assertions.assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void searchParamComma() {
        queryFactory = new JPAQueryFactory(em);
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"),
                        member.age.eq(10))
                .fetchOne();
        Assertions.assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void resultFetch() {

        List<Member> fetchList = queryFactory
                .selectFrom(member)
                .fetch();

        Member member1 = queryFactory
                .selectFrom(QMember.member)
                .fetchOne();


        Member member2 = queryFactory
                .selectFrom(member)
                .fetchFirst();

        QueryResults<Member> memberQueryResults = queryFactory
                .selectFrom(member)
                .fetchResults();

        memberQueryResults.getTotal();
        List<Member> results = memberQueryResults.getResults();

        long total = queryFactory
                .selectFrom(member)
                .fetchCount();

    }

    /**
     * 1. ?????? ????????????(desc)
     * 2. ?????? ?????? ????????????(asc)
     * 3. 2?????? ?????? ????????? ????????? ???????????? ??????(nulls last)
     */
    @Test
    public void sort() {
        em.persist(new Member(null, 100));
        em.persist(new Member("member1", 100));
        em.persist(new Member("member1", 200));
        em.persist(new Member("member1", 300));
        em.persist(new Member("member2", 300));
        em.persist(new Member("member3", 300));
        em.persist(new Member("member1", 400));

        List<Member> fetchList = queryFactory
                .selectFrom(member)
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();
    }

    @Test
    public void paging() {

        List<Member> memberList = queryFactory
                .selectFrom(member)
                .offset(1)
                .limit(1)
                .fetch();

    }

    @Test
    public void group1() {

        List<Tuple> result = queryFactory
                .select(member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min())
                .from(member)
                .fetch();

        Tuple tuple = result.get(0);
        tuple.get(member.count());
        tuple.get(member.age.sum());
        tuple.get(member.age.avg());


    }

    /**
     * ???????????? ????????? ?????? ??????
     */
    @Test
    public void group2() {
        //given
        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);
        teamA.get(team.name);
        teamB.get(team.name);
        teamA.get(member.age.avg());
        teamB.get(member.age.avg());


    }

    @Test
    public void testJoin() {
        //given

        //result1, result2 ??? ??????.
        queryFactory = new JPAQueryFactory(em);
        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        List<Member> result2 = queryFactory
                .selectFrom(member)
                .join(member.team, team).on(team.name.eq("teamA"))
                .fetch();

    }

    /**
     * ???????????? ????????? JOIN ??????
     * ????????? ????????? ??? ????????? ?????? ?????? ??????
     */
    @Test
    public void testJoin2() {
        //given

        queryFactory = new JPAQueryFactory(em);
        List<Member> result = queryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        List<Tuple> fetch = queryFactory
                .select(member, team)
                .from(member, team)
                .leftJoin(team).on(member.username.eq(team.name))
                .fetch();
    }

    /**
     * ????????? ?????? ???????????????, ??? ????????? teamA ??? ?????? ??????
     * JPQL :select m, t from m left join m.team t on t.name = "teamA"
     */
    @Test
    public void join_on_filtering() {

        queryFactory = new JPAQueryFactory(em);
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team).on(team.name.eq("teamA"))
                .fetch();

    }

    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    public void fetchJoinNo() {
        em.flush();
        em.clear();
        queryFactory = new JPAQueryFactory(em);
        Member member1 = queryFactory
                .selectFrom(member)
                .where(QMember.member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(member1.getTeam());
        // ?????? loaded = false;

        Member member2 = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin() //!!!!!! ????????????!!!
                .where(QMember.member.username.eq("member1"))
                .fetchOne();


        boolean loaded2 = emf.getPersistenceUnitUtil().isLoaded(member2.getTeam());
        // ?????? loaded2 = true;

    }

    /**
     * ????????? ?????? ?????? ?????? ??????
     */
    @Test
    public void subQuery() {
        //given

        queryFactory = new JPAQueryFactory(em);
        QMember memberSub = new QMember("memberSub");

        List<Member> members = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        select(memberSub.age.max())
                                .from(memberSub)

                ))
                .fetch();

        Assertions.assertThat(members).extracting("age")
                .containsExactly(40);

    }

    /**
     * ????????? ?????? ????????? ?????? ??????
     */
    @Test
    public void subQuery2() {
        //given

        queryFactory = new JPAQueryFactory(em);
        QMember memberSub = new QMember("memberSub");

        List<Member> members = queryFactory
                .selectFrom(member)
                .where(member.age.goe(
                        select(memberSub.age.avg())
                                .from(memberSub)

                ))
                .fetch();

    }

    @Test
    public void select_subQuery() {
        queryFactory = new JPAQueryFactory(em);
        QMember memberSub = new QMember("memberSub");
        List<Tuple> result = queryFactory
                .select(member.username
                        , select(memberSub.age.avg())
                                .from(memberSub)
                )
                .from(member)
                .fetch();

    }

    @Test
    public void baseCase() {
        //given
        queryFactory = new JPAQueryFactory(em);
        List<String> result = queryFactory
                .select(member.age
                        .when(10).then("??????")
                        .when(20).then("?????????")
                        .otherwise("??????"))
                .from(member)
                .fetch();


        //when

        //then

    }

    /**
     * ????????? case?????? CaseBuilder ??????
     */
    @Test
    public void complexCase() {
        //given
        queryFactory = new JPAQueryFactory(em);

        List<String> result = queryFactory
                .select(new CaseBuilder()
                        .when(member.age.between(10, 20)).then("10~20")
                        .when(member.age.between(20, 30)).then("20~30")
                        .otherwise("??????")
                )
                .from(member)
                .fetch();

    }

    @Test
    public void constant() {
        queryFactory = new JPAQueryFactory(em);
        List<Tuple> result = queryFactory.select(member.username, Expressions.constant("A"))
                .from(member)
                .fetch();

    }

    /**
     * {username}_{age}
     */
    @Test
    public void concat() {
        //given
        queryFactory = new JPAQueryFactory(em);

        List<String> result = queryFactory.select(member.username.concat(member.age.stringValue()))
                .from(member)
                .fetch();
        for (String s : result
        ) {
            System.out.println(s);

        }


    }

    @Test
    public void simpleProjection() {
        queryFactory = new JPAQueryFactory(em);
        List<String> result = queryFactory.select(member.username)
                .from(member)
                .fetch();
    }


    @Test
    public void tupleProjection() {
        queryFactory = new JPAQueryFactory(em);
        Tuple result = queryFactory.
                select(member.username, member.age)
                .from(member)
                .fetchOne();

        String username = result.get(member.username);
        Integer age = result.get(member.age);

    }

    /**
     * DTO ???????????? (JPQL)
     * new operation ??????
     */
    @Test
    public void findByJPQL() {
        List<MemberDto> MemberDtoList =  em.createQuery("select new com.example.jpabook.dto.MemberDto(m.username, m.age) from Member m", MemberDto.class)
                                        .getResultList();

    }

    /** DTO ???????????? (Querydsl)
     * 1. Setter ??????
     * 2. field ?????? - ?????? ???????????? ????????????
     * 3. ????????? ??????
     * 4. @QueryProject
     */
    @Test
    public void findByQuerydsl() {
        queryFactory = new JPAQueryFactory(em);

        //1. Setter ??????
        List<MemberDto> memberDtoList1 = queryFactory
                .select(Projections.bean(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        //2. field ??????
        List<MemberDto> memberDtoList2 = queryFactory
                .select(Projections.fields(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        //3. ????????? ??????
        List<MemberDto> memberDtoList3 = queryFactory
                .select(Projections.constructor(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        //4. @QueryProjection ??????
        List<MemberDto> memberDtoList4 = queryFactory
                .select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch();


        //5. Alias
        List<UserDto> memberDtoList5 = queryFactory
                .select(Projections.constructor(UserDto.class,
                        member.username.as("name"),
                        member.age))
                .from(member)
                .fetch();

    }

    /**
     *  subQuery alias
     *
     * Expressions.as ????????? as??? ??????!!!
     * subquery??? ????????? ?????? Expressions.as ??????
     *
     */
    @Test
    public void subQueryAlias() {
        queryFactory = new JPAQueryFactory(em);
        QMember memberSub = new QMember("memberSub");
        List<UserDto> memberDtoList4 = queryFactory
                .select(Projections.constructor(UserDto.class,
                        member.username.as("name"),
                        Expressions.as(member.username, "name"),
                        Expressions.as(
                                JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub), "age")
                        ))
                .from(member)
                .fetch();
    }

    @Test
    public void dynamicQuery_BooleanBuilder(){

        String usernameParam = "member1";
        Integer age = 10;
        List<Member> result = searchMember1(usernameParam, age);

    }

    private List<Member> searchMember1(String usernameParam, Integer age) {

        BooleanBuilder builder = new BooleanBuilder();
        if(usernameParam != null){
            builder.and(member.username.eq(usernameParam));
        }
        if(age != null){
            builder.and(member.age.eq(age));

        }
        queryFactory = new JPAQueryFactory(em);
        List<Member> result = queryFactory.select(member)
                .from(member)
                .where(builder)
                .fetch();
        return result;
    }

    /**
     * BooleanBuilder ?????? Where??? ?????? Param?????? ??????
     * null??? ???????????? ????????????.
     * ????????? ????????? ????????? ??? ???????????? ??????.
     * BooleanExpression return type ????????? ????????? ????????? ?????? ??????.!!!
     *
     */
    @Test
    public void dynamicQuery_WhereParam(){
        String usernameParam = "member1";
        Integer age = 10;
        List<Member> result = searchMember2(usernameParam, age);
    }

    private List<Member> searchMember2(String usernameParam, Integer age) {

        queryFactory = new JPAQueryFactory(em);
        List<Member> result = queryFactory.select(member)
                .from(member)
                .where(usernameEq(usernameParam), ageEq(age))
//                .where(allEq(usernameParam, age))
                .fetch();
        return result;
    }


    private BooleanExpression usernameEq(String usernameParam) {
        return usernameParam != null ? member.username.eq(usernameParam) : null;
    }
    private BooleanExpression ageEq(Integer age) {
        return age != null ? member.age.eq(age) : null;
    }

    private BooleanExpression allEq(String usernameParam, Integer age) {
        return usernameEq(usernameParam).and(ageEq(age));

    }

}
    
