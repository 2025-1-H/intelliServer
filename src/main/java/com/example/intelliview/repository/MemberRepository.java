package com.example.intelliview.repository;

import com.example.intelliview.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Boolean existsByEmail(String email);

    Member findByUsername(String username);

    Member findByEmail(String email);
}
