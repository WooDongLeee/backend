package com.woodongleee.src.userMatch;

import com.woodongleee.src.userMatch.Domain.CheckApplyingPossibilityRes;
import com.woodongleee.src.userMatch.Domain.CheckCancelApplyingPossibilityRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

@Repository
public class UserMatchDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource){ this.jdbcTemplate = new JdbcTemplate(dataSource); }


    public int checkMatchPostExist(int matchPostIdx){ // 용병 모집글이 있는지 확인
        String checkMatchPostExistQuery = "select exists(select matchPostIdx from MatchPost where matchPostIdx = ?);";

        return this.jdbcTemplate.queryForObject(checkMatchPostExistQuery, int.class, matchPostIdx);
    }


    public CheckApplyingPossibilityRes checkApplyingPossibility(int matchPostIdx){ // 용병 신청 가능 여부 확인
        String checkApplyingPossibilityByHeadCntQuery = "select headCnt, joinCnt, userMatchCnt, date, startTime from MatchPost as MP\n" +
                "    join TeamSchedule TS on MP.teamScheduleIdx = TS.teamScheduleIdx\n" +
                "where matchPostIdx=?;";

        return this.jdbcTemplate.queryForObject(checkApplyingPossibilityByHeadCntQuery,
                (rs, rowNum) -> new CheckApplyingPossibilityRes(
                        rs.getInt("headCnt"),
                        rs.getInt("joinCnt"),
                        rs.getInt("userMatchCnt"),
                        rs.getString("startTime"))
        , matchPostIdx);
    }

    public void applyUserMatch(int userIdx, int matchPostIdx) { // 용병 신청
        String applyUserMatchQuery = "insert into MatchApply (userIdx, matchPostIdx) values(?, ?);";
        Object[] applyUserMatchParams = new Object[]{userIdx, matchPostIdx};
        this.jdbcTemplate.update(applyUserMatchQuery, applyUserMatchParams);
    }

    public int checkMatchApplyExist(int userIdx, int matchPostIdx){ // 용병 신청을 했는 지 확인
        String checkMatchApplyExistQuery = "select exists(select matchApplyIdx from MatchApply where userIdx = ? and matchPostIdx = ?);";
        Object[] checkMatchApplyExistParams = new Object[]{userIdx, matchPostIdx};
        return this.jdbcTemplate.queryForObject(checkMatchApplyExistQuery, int.class, checkMatchApplyExistParams);
    }
    public CheckCancelApplyingPossibilityRes checkCancelApplyingPossibility(int userIdx, int matchPostIdx){ // 용병 신청 취소 가능 여부 확인
        String checkCancelApplyingPossibilityQuery = "select status, startTime from MatchApply as MA\n" +
                "join MatchPost MP on MA.matchPostIdx = MP.matchPostIdx\n" +
                "join TeamSchedule TS on MP.teamScheduleIdx = TS.teamScheduleIdx\n" +
                "where MA.userIdx = ? and MP.matchPostIdx = ?;";
        Object[] checkCancelApplyingPossibilityParams = new Object[] {userIdx, matchPostIdx};

        return this.jdbcTemplate.queryForObject(checkCancelApplyingPossibilityQuery,
                (rs, rowNum) -> new CheckCancelApplyingPossibilityRes(
                        rs.getString("status"),
                        rs.getString("startTime")
                ), checkCancelApplyingPossibilityParams);
    }
    public void cancelApplyUserMatch(int userIdx, int matchPostIdx) { // 용병 신청 취소
        String cancelApplyUserMatchQuery = "update MatchApply set status = 'CANCELED' where userIdx = ? and matchPostIdx = ?";
        Object[] cancelApplyUserMatchParams = new Object[]{userIdx, matchPostIdx};
        this.jdbcTemplate.update(cancelApplyUserMatchQuery, cancelApplyUserMatchParams);
    }
}
