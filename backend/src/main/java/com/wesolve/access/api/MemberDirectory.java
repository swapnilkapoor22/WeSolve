package com.wesolve.access.api;

import java.util.Optional;
import java.util.UUID;

public interface MemberDirectory {

  Optional<Member> findAssignable(UUID id);

  Member requireBySignIn(String signInId);
}
