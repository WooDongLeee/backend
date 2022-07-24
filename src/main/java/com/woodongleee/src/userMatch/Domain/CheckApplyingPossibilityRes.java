package com.woodongleee.src.userMatch.Domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CheckApplyingPossibilityRes {
    private int headCnt;
    private int joinCnt;
    private int userMatchCnt;
    private String startTime;
}
