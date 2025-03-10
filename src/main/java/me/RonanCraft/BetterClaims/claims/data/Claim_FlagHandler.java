/*
 * Copyright (c) 2022 RonanCraft Network
 * All rights reserved.
 */

package me.RonanCraft.BetterClaims.claims.data;

import me.RonanCraft.BetterClaims.claims.ClaimData;
import me.RonanCraft.BetterClaims.claims.enums.CLAIM_FLAG;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class Claim_FlagHandler {
    public HashMap<CLAIM_FLAG, Object> flags = new HashMap<>();

    private final ClaimData claimData;

    public Claim_FlagHandler(ClaimData claimData) {
        this.claimData = claimData;
    }

    public void setFlag(CLAIM_FLAG flag, Object value, boolean update) {
        flags.put(flag, value);
        claimData.updated(update);
    }

    public Object getFlag(@Nullable CLAIM_FLAG flag) {
        return flags.getOrDefault(flag, flag == null ? false : flag.getDefault());
    }

    public HashMap<CLAIM_FLAG, Object> getFlags() {
        return flags;
    }
}
