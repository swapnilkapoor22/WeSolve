package com.wesolve.access.api;

import java.util.UUID;

public record Member(UUID id, String displayName, boolean assignable) {}
