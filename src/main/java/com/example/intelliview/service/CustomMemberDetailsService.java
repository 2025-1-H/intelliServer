package com.example.intelliview.service;

import com.example.intelliview.domain.Member;
import com.example.intelliview.dto.user.CustomUserDetails;
import com.example.intelliview.repository.MemberRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomMemberDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    public CustomMemberDetailsService(MemberRepository memberRepository) {

        this.memberRepository = memberRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Member memberData   = memberRepository.findByEmail(email);

        if (memberData != null) {

            return new CustomUserDetails(memberData);
        }
        throw new UsernameNotFoundException("User not found with email: " + email
        );
    }
}


