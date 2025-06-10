package com.example.intelliview.repository;

import com.example.intelliview.domain.Interview;
import com.example.intelliview.domain.Member;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewRepository extends JpaRepository<Interview, Long> {

    public List<Interview> findByMember(Member member);

}
