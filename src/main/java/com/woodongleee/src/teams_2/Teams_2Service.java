package com.woodongleee.src.teams_2;

import com.woodongleee.config.BaseException;
import com.woodongleee.config.BaseResponseStatus;
import com.woodongleee.src.teams_2.model.AddTeamScheduleReq;
import com.woodongleee.src.user.UserProvider;
import com.woodongleee.utils.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.woodongleee.config.BaseResponseStatus.*;

@Service
public class Teams_2Service {

    private final Teams_2Dao teams2Dao;
    private final Teams_2Provider teams2Provider;
    private final JwtService jwtService;

    private final UserProvider userProvider;

    @Autowired
    public Teams_2Service(Teams_2Dao teams2Dao, Teams_2Provider teams2Provider, JwtService jwtService, UserProvider userProvider){
        this.teams2Dao = teams2Dao;
        this.teams2Provider = teams2Provider;
        this.jwtService=jwtService;
        this.userProvider = userProvider;
    }


    public int changeTeamRecruit(int userIdx, int teamIdx) throws BaseException{
        try{
            if(userProvider.checkUserExist(userIdx) == 0){
                throw new BaseException(USER_DOES_NOT_EXIST);
            }
            if(userProvider.checkUserStatus(userIdx).equals("INACTIVE")){
                throw new BaseException(LEAVED_USER);
            }

            if(teams2Dao.isLeader(userIdx) != 1){
                throw new BaseException(UNAUTHORIZED_ACCESS); // 리더가 아닌 경우
            }

            if(teams2Dao.checkTeamIdxExist(teamIdx) != 1){
                throw new BaseException(TEAM_DOES_NOT_EXIST); // 팀이 존재하지 않음
            }

            if(teams2Dao.isOurTeam(userIdx, teamIdx) != 1){
                throw new BaseException(NOT_TEAMMATE); // teamIdx와 userIdx가 매치되지 않는 경우
            }
            return teams2Dao.changeTeamRecruit(teamIdx);
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.DATABASE_ERROR);
        }
    }

    public void addTeamSchedule(AddTeamScheduleReq addTeamScheduleReq, int teamIdx, int userIdx) throws BaseException{
        try{
            if(userProvider.checkUserExist(userIdx) == 0){
                throw new BaseException(USER_DOES_NOT_EXIST);
            }
            if(userProvider.checkUserStatus(userIdx).equals("INACTIVE")){
                throw new BaseException(LEAVED_USER);
            }

            if(teams2Dao.isLeader(userIdx) != 1){
                throw new BaseException(UNAUTHORIZED_ACCESS); // 리더가 아닌 경우
            }

            if(teams2Dao.checkTeamIdxExist(teamIdx) != 1){
                throw new BaseException(TEAM_DOES_NOT_EXIST); // 팀이 존재하지 않음
            }

            if(teams2Dao.isOurTeam(userIdx, teamIdx) != 1){
                throw new BaseException(NOT_TEAMMATE); // teamIdx와 userIdx가 매치되지 않는 경우
            }

            if(teams2Dao.checkTimeOfSchedule(addTeamScheduleReq.getStartTime(), addTeamScheduleReq.getEndTime()) == 1){
                throw new BaseException(SCHEDULE_ALREADY_EXIST); // 시간이 안 맞는 경우
            }

            teams2Dao.addTeamSchedule(addTeamScheduleReq, teamIdx);
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.DATABASE_ERROR);
        }
    }

    public void disbandTeam(int userIdx, int teamIdx) throws BaseException{
        try{
            if(userProvider.checkUserExist(userIdx) == 0){
                throw new BaseException(USER_DOES_NOT_EXIST);
            }
            if(userProvider.checkUserStatus(userIdx).equals("INACTIVE")){
                throw new BaseException(LEAVED_USER);
            }
            if(teams2Dao.isLeader(userIdx) != 1){
                throw new BaseException(UNAUTHORIZED_ACCESS); // 리더가 아닌 경우
            }

            if(teams2Dao.checkTeamIdxExist(teamIdx) != 1){
                throw new BaseException(TEAM_DOES_NOT_EXIST); // 팀이 존재하지 않음(해체된 경우도 포함)
            }

            if(teams2Dao.isOurTeam(userIdx, teamIdx) != 1){
                throw new BaseException(NOT_TEAMMATE); // teamIdx와 userIdx가 매치되지 않는 경우
            }

            teams2Dao.disbandTeam(teamIdx);



        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.DATABASE_ERROR);
        }
    }

    public void dropUser(int leaderIdx, int userIdx) throws BaseException{
        try{
            if(userProvider.checkUserExist(userIdx) == 0){
                throw new BaseException(USER_DOES_NOT_EXIST);
            }
            if(userProvider.checkUserStatus(userIdx).equals("INACTIVE")){
                throw new BaseException(LEAVED_USER);
            }
            if(userProvider.checkUserExist(leaderIdx) == 0){
                throw new BaseException(USER_DOES_NOT_EXIST);
            }
            if(userProvider.checkUserStatus(leaderIdx).equals("INACTIVE")){
                throw new BaseException(LEAVED_USER);
            }
            // 유저 탈퇴 여부 확인

            if(teams2Dao.isLeader(leaderIdx) != 1){
                throw new BaseException(UNAUTHORIZED_ACCESS); // 리더가 아닌 경우
            }

            int teamIdx = teams2Dao.getTeamIdx(leaderIdx); // 리더의 팀idx

            if(teams2Dao.checkTeamIdxExist(teamIdx) != 1){
                throw new BaseException(TEAM_DOES_NOT_EXIST); // 팀이 존재하지 않음(해체된 경우도 포함)
            }

            if(teams2Dao.isOurTeam(userIdx, teamIdx) != 1){
                throw new BaseException(NOT_TEAMMATE); // teamIdx와 userIdx가 매치되지 않는 경우
            }

            teams2Dao.dropUser(userIdx);
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.DATABASE_ERROR);
        }
    }

    public void acceptUser(int teamApplyIdx, int userIdx) throws BaseException{
        try{
            if(userProvider.checkUserExist(userIdx) == 0){
                throw new BaseException(USER_DOES_NOT_EXIST);
            }
            if(userProvider.checkUserStatus(userIdx).equals("INACTIVE")){
                throw new BaseException(LEAVED_USER);
            }
            if(teams2Dao.isLeader(userIdx) != 1){
                throw new BaseException(UNAUTHORIZED_ACCESS); // 리더가 아닌 경우
            }

            String status = teams2Dao.getTeamApplyStatus(teamApplyIdx);
            if(!status.equals("APPLIED")){
                throw new BaseException(ACCEPT_NOT_AVAILABLE); // 신청이 취소되었거나 이미 거절된 경우
            }

            teams2Dao.acceptUser(teamApplyIdx);
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.DATABASE_ERROR);
        }
    }

    public void rejectUser(int teamApplyIdx, int userIdx) throws BaseException{
        try{
            if(userProvider.checkUserExist(userIdx) == 0){
                throw new BaseException(USER_DOES_NOT_EXIST);
            }
            if(userProvider.checkUserStatus(userIdx).equals("INACTIVE")){
                throw new BaseException(LEAVED_USER);
            }
            if(teams2Dao.isLeader(userIdx) != 1){
                throw new BaseException(UNAUTHORIZED_ACCESS); // 리더가 아닌 경우
            }

            String status = teams2Dao.getTeamApplyStatus(teamApplyIdx);
            if(!status.equals("APPLIED")){
                throw new BaseException(ACCEPT_NOT_AVAILABLE); // 신청이 취소되었거나 이미 거절된 경우
            }

            teams2Dao.rejectUser(teamApplyIdx);
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.DATABASE_ERROR);
        }
    }
}
