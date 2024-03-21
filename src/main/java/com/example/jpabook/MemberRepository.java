package com.example.jpabook;

import com.example.jpabook.domain.Member;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class MemberRepository {

    @PersistenceContext
    private EntityManager em;

    public long save(Member member) {
        em.persist(member);
        return member.getId();
    }

    public Member find(long id) {
        return em.find(Member.class, id);
    }
}
