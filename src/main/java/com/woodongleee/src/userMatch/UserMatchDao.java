package com.woodongleee.src.userMatch;

import com.woodongleee.src.userMatch.Domain.CheckApplyingPossibilityRes;
import com.woodongleee.src.userMatch.Domain.CheckCancelApplyingPossibilityRes;
import com.woodongleee.src.userMatch.Domain.CheckCreateUserMatchPostPossibilityRes;
import com.woodongleee.src.userMatch.Domain.GetUserMatchPostInfoRes;
import org.graalvm.compiler.nodes.memory.OnHeapMemoryAccess;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class UserMatchDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource){ this.jdbcTemplate = new JdbcTemplate(dataSource); }


    public int checkMatchPostExist(int matchPostIdx){ // 용병 모집글이 있는지 확인
        String checkMatchPostExistQuery = "select exists(select matchPostIdx from MatchPost where matchPostIdx = ?);";

        return this.jdbcTemplate.queryForObject(checkMatchPostExistQuery, int.class, matchPostIdx);
    }


    public CheckApplyingPossibilityRes checkApplyingPossibility(int userIdx, int matchPostIdx){ // 용병 신청 가능 여부 확인
        String checkApplyingPossibilityByHeadCntQuery = "select headCnt, joinCnt, userMatchCnt, startTime, exists(select matchApplyIdx from MatchApply where userIdx=? and matchPostIdx=?) as status,\n" +
                "       teamIdx\n" +
                "from MatchPost as MP\n" +
                "join TeamSchedule TS on MP.teamScheduleIdx = TS.teamScheduleIdx\n" +
                "join TeamInfo TI on TS.homeIdx = TI.teamIdx\n" +
                "where matchPostIdx=?;";
        Object[] params = new Object[] {userIdx, matchPostIdx, matchPostIdx};
        return this.jdbcTemplate.queryForObject(checkApplyingPossibilityByHeadCntQuery,
                (rs, rowNum) -> new CheckApplyingPossibilityRes(
                        rs.getInt("headCnt"),
                        rs.getInt("joinCnt"),
                        rs.getInt("userMatchCnt"),
                        rs.getString("startTime"),
                        rs.getInt("status"),
                        rs.getInt("teamIdx"))
        , params);
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

    public int getTeamIdx(int userIdx) {
        String Query = "select if(teamIdx is null, -1, teamIdx) as teamIdx from User where userIdx=?;";
        return this.jdbcTemplate.queryForObject(Query, int.class, userIdx);
    }

    public CheckCreateUserMatchPostPossibilityRes checkCreateMatchPostPossibility(int userIdx, int teamScheduleIdx) {
        String Query = "select startTime, exists(select matchPostIdx from MatchPost where userIdx=? and teamScheduleIdx=?) as status\n" +
                "from TeamSchedule TS\n" +
                "left join MatchPost MP on MP.teamScheduleIdx = TS.teamScheduleIdx\n" +
                "where TS.teamScheduleIdx=?;";
        Object[] Params = new Object[] {userIdx, teamScheduleIdx, teamScheduleIdx};
        return this.jdbcTemplate.queryForObject(Query, (rs, rowNum) -> new CheckCreateUserMatchPostPossibilityRes(
                rs.getString("startTime"),
                rs.getInt("status")
        ), Params);
    }

    public int isLeader(int userIdx){
        String Query = "select case isLeader\n" +
                "    when 'T' then 1\n" +
                "    else -1\n" +
                "end\n" +
                "from User\n" +
                "where userIdx=?;";

        return this.jdbcTemplate.queryForObject(Query, int.class, userIdx);
    }

    public int createUserMatchPost(int userIdx, int teamScheduleIdx, String contents) {
        String Query = "insert into MatchPost(userIdx, teamScheduleIdx, contents, type) values(?,?,?,'USER');";
        Object[] Params = new Object[] {userIdx, teamScheduleIdx, contents};
        this.jdbcTemplate.update(Query,Params);
        return this.jdbcTemplate.queryForObject("select last_insert_id()", int.class);
    }

    public List<GetUserMatchPostInfoRes> getUserMatchPosts(int userIdx, String town, String startTime, String endTime) {
        String Query = "select\n" +
                "    case\n" +
                "        when MA.status is null\n" +
                "            then '미신청'\n" +
                "        when MA.status = 'APPLIED'\n" +
                "            then '신청중'\n" +
                "        when MA.status = 'CANCELED'\n" +
                "            then '취소'\n" +
                "        when MA.status = 'DENIED'\n" +
                "            then '거절'\n" +
                "    end as status,\n" +
                "    MP.teamScheduleIdx, MP.matchPostIdx as matchPostIdx, name as teamName, address, (select name from TeamInfo where TS.awayIdx=TeamInfo.teamIdx) as opponentTeamName,\n" +
                "       (headCnt - joinCnt - userMatchCnt) as recruitCnt, contents, startTime, endTime, teamProfileImgUrl as profileImgUrl\n" +
                "from MatchPost MP\n" +
                "join TeamSchedule TS on MP.teamScheduleIdx = TS.teamScheduleIdx\n" +
                "join TeamInfo TI on TS.homeIdx = TI.teamIdx\n" +
                "left join MatchApply MA on MA.matchPostIdx = MP.matchPostIdx and MA.userIdx=?\n" +
                "where type='USER' and startTime >= ? and endTime <= ? and locate(?, address);";
        Object[] Params = new Object[] {userIdx, startTime, endTime, town};
        return this.jdbcTemplate.query(Query, (rs, rowNum) -> new GetUserMatchPostInfoRes(
                rs.getString("status"),
                rs.getInt("teamScheduleIdx"),
                rs.getInt("matchPostIdx"),
                rs.getString("teamName"),
                rs.getString("address"),
                rs.getString("opponentTeamName"),
                rs.getInt("recruitCnt"),
                rs.getString("contents"),
                rs.getString("startTime"),
                rs.getString("endTime"),
                rs.getString("profileImgUrl")
        ), Params);
    }

    
}
