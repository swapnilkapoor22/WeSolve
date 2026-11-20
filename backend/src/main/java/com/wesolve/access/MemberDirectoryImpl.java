package com.wesolve.access;

import com.wesolve.access.api.Member;
import com.wesolve.access.api.MemberDirectory;
import com.wesolve.access.persistence.SupportMemberRepository;
import com.wesolve.platform.api.ApiException;
import com.wesolve.platform.api.SignInHash;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class MemberDirectoryImpl implements MemberDirectory {

  private final SupportMemberRepository members;

  public MemberDirectoryImpl(SupportMemberRepository members) {
    this.members = members;
  }

  @Override
  public Optional<Member> findAssignable(UUID id) {
    return members
        .findByIdAndAssignableTrue(id)
        .map(member -> new Member(member.getId(), member.getDisplayName(), member.isAssignable()));
  }

  @Override
  public Member requireBySignIn(String signInId) {
    return members
        .findByHashKey(SignInHash.of(signInId))
        .map(member -> new Member(member.getId(), member.getDisplayName(), member.isAssignable()))
        .orElseThrow(() -> new ApiException(403, "NO_ACCESS", "No access"));
  }
}
