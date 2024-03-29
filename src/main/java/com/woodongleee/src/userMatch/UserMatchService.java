package com.woodongleee.src.userMatch;

import com.woodongleee.config.BaseException;
import com.woodongleee.config.BaseResponse;
import com.woodongleee.config.BaseResponseStatus;
import com.woodongleee.src.user.UserProvider;
import com.woodongleee.src.userMatch.model.*;
import com.woodongleee.utils.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.woodongleee.config.BaseResponseStatus.*;

@Service
public class UserMatchService {
    private final UserMatchDao userMatchDao;
    private final JwtService jwtService;

    private final UserProvider userProvider;

    @Autowired
    public UserMatchService(UserMatchDao userMatchDao, JwtService jwtService, UserProvider userProvider) {
        this.userMatchDao = userMatchDao;
        this.jwtService = jwtService;
        this.userProvider = userProvider;
    }

    public void applyUserMatch(int userIdx, int matchPostIdx) throws BaseException {
        try {
            if(userProvider.checkUserExist(userIdx) == 0){
                throw new BaseException(USER_DOES_NOT_EXIST);
            }
            if(userProvider.checkUserStatus(userIdx).equals("INACTIVE")){
                throw new BaseException(LEAVED_USER);
            }
            if (userMatchDao.checkMatchPostExist(matchPostIdx) != 1) {
                throw new BaseException(MATCHING_DOES_NOT_EXIST); // 존재하지 않는 matchPostIdx
            }

            CheckApplyingPossibilityRes checkApplyingPossibilityRes = userMatchDao.checkApplyingPossibility(userIdx, matchPostIdx);
            int status = checkApplyingPossibilityRes.getStatus();
            int headCnt = checkApplyingPossibilityRes.getHeadCnt();
            int joinCnt = checkApplyingPossibilityRes.getJoinCnt();
            int userMatchCnt = checkApplyingPossibilityRes.getUserMatchCnt();
            int teamIdx = checkApplyingPossibilityRes.getTeamIdx();

            if (userMatchDao.getTeamIdx(userIdx) == teamIdx) {
                throw new BaseException(ACCEPT_NOT_AVAILABLE); // 해당 경기의 참가 팀 소속입니다.
            }

            if (status == 1) {
                throw new BaseException(MATCH_ALREADY_EXIST); // 이미 신청했습니다.
            }

            if ((joinCnt + userMatchCnt) >= headCnt) {
                throw new BaseException(MATCH_FULLY_ACCEPTED); // 용병 모집이 완료된 경기입니다.
            }


            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String startTime = checkApplyingPossibilityRes.getStartTime();
            String curTime = format.format(new Date());
            Date _startTime = format.parse(startTime);
            Date _curTime = format.parse(curTime);
            long diff = _startTime.getTime() - _curTime.getTime();
            diff = (((diff / 1000) / 60) / 60);

            if (diff <= 2) {
                throw new BaseException(MATCH_APPLY_PERIOD_ERROR); // 용병 모집 기한이 지난 경기입니다.
            }


            userMatchDao.applyUserMatch(userIdx, matchPostIdx);
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public void cancelApplyUserMatch(int userIdx, int matchPostIdx) throws BaseException {
        try {
            if(userProvider.checkUserExist(userIdx) == 0){
                throw new BaseException(USER_DOES_NOT_EXIST);
            }
            if(userProvider.checkUserStatus(userIdx).equals("INACTIVE")){
                throw new BaseException(LEAVED_USER);
            }

            if (userMatchDao.checkMatchPostExist(matchPostIdx) != 1) {
                throw new BaseException(MATCHING_DOES_NOT_EXIST); // 존재하지 않는 matchPostIdx
            }

            if (userMatchDao.checkMatchApplyExist(userIdx, matchPostIdx) != 1) {
                throw new BaseException(MATCH_APPLY_DOES_NOT_EXIST); // 존재하지 않는 매칭신청입니다.
            }

            CheckCancelApplyingPossibilityRes checkCancelApplyingPossibilityRes = userMatchDao.checkCancelApplyingPossibility(userIdx, matchPostIdx);

            if (checkCancelApplyingPossibilityRes.getStatus().equals("CANCELED") || checkCancelApplyingPossibilityRes.getStatus().equals("ACCEPTED")) {
                throw new BaseException(ACCEPT_NOT_AVAILABLE); // 이미 취소된 경우 or 이미 참여 결정된 경우
            }


            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String startTime = checkCancelApplyingPossibilityRes.getStartTime();
            String curTime = format.format(new Date());
            Date _startTime = format.parse(startTime);
            Date _curTime = format.parse(curTime);
            long diff = _startTime.getTime() - _curTime.getTime();
            diff = (((diff / 1000) / 60) / 60);

            if (diff <= 2) {
                throw new BaseException(MATCH_APPLY_PERIOD_ERROR); // 용병 모집 기한이 지난 경기입니다.
            }

            userMatchDao.cancelApplyUserMatch(userIdx, matchPostIdx);
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.DATABASE_ERROR);
        }
    }


    public BaseResponse<CreateUserMatchPostRes> createUserMatchPost(int userIdx, int teamScheduleIdx, String contents) throws BaseException {
        try {
            if(userProvider.checkUserExist(userIdx) == 0){
                throw new BaseException(USER_DOES_NOT_EXIST);
            }
            if(userProvider.checkUserStatus(userIdx).equals("INACTIVE")){
                throw new BaseException(LEAVED_USER);
            }

            if (userMatchDao.isLeader(userIdx) != 1) {
                throw new BaseException(UNAUTHORIZED_ACCESS); // 리더가 아닙니다.
            }

            CheckCreateUserMatchPostPossibilityRes createUserMatchPostPossibilityRes = userMatchDao.checkCreateMatchPostPossibility(userIdx, teamScheduleIdx);
            if (createUserMatchPostPossibilityRes.getStatus() == 1) {
                throw new BaseException(ACCEPT_NOT_AVAILABLE); // 이미 용병 모집글이 작성되었습니다.
            }

            if (userMatchDao.isOurMatch(userIdx, teamScheduleIdx) != 1) {
                throw new BaseException(NOT_TEAMMATE); // teamScheduleIdx가 다른 팀의 것입니다.
            }

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String startTime = createUserMatchPostPossibilityRes.getStartTime();
            String curTime = format.format(new Date());
            Date _startTime = format.parse(startTime);
            Date _curTime = format.parse(curTime);
            long diff = _startTime.getTime() - _curTime.getTime();
            diff = (((diff / 1000) / 60) / 60);

            if (diff <= 2) {
                throw new BaseException(BaseResponseStatus.MATCH_CREATE_PERIOD_ERROR); // 용병 모집글 작성 기한이 지났습니다.
            }

            int matchPostIdx = userMatchDao.createUserMatchPost(userIdx, teamScheduleIdx, contents);
            return new BaseResponse<>(new CreateUserMatchPostRes(matchPostIdx, "USER"));
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.DATABASE_ERROR);
        }
    }


    public BaseResponse<ModifyUserMatchPostRes> modifyUserMatchPost(int userIdx, int teamScheduleIdx, String contents) throws BaseException {
        try {
            if(userProvider.checkUserExist(userIdx) == 0){
                throw new BaseException(USER_DOES_NOT_EXIST);
            }
            if(userProvider.checkUserStatus(userIdx).equals("INACTIVE")){
                throw new BaseException(LEAVED_USER);
            }

            if (userMatchDao.isLeader(userIdx) != 1) {
                throw new BaseException(UNAUTHORIZED_ACCESS); // 리더가 아닙니다.
            }

            // 모집글 수정 가능 여부 확인은, 생성가능 여부 확인 방식과 다르지 않아 재사용
            CheckCreateUserMatchPostPossibilityRes createUserMatchPostPossibilityRes = userMatchDao.checkCreateMatchPostPossibility(userIdx, teamScheduleIdx);
            if (createUserMatchPostPossibilityRes.getStatus() != 1) {
                throw new BaseException(ACCEPT_NOT_AVAILABLE); // 용병 모집글이 작성되지 않았습니다.
            }

            if (userMatchDao.isOurMatch(userIdx, teamScheduleIdx) != 1) {
                throw new BaseException(NOT_TEAMMATE); // teamScheduleIdx가 다른 팀의 것입니다.
            }

            //우리팀 경기가 아닙니다 추가.. -> 잘못된 teamScheduleIdx
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String startTime = createUserMatchPostPossibilityRes.getStartTime();
            String curTime = format.format(new Date());
            Date _startTime = format.parse(startTime);
            Date _curTime = format.parse(curTime);
            long diff = _startTime.getTime() - _curTime.getTime();
            diff = (((diff / 1000) / 60) / 60);

            if (diff <= 2) {
                throw new BaseException(MATCH_CREATE_PERIOD_ERROR); // 용병 모집글 작성 기한이 지났습니다.
            }

            int matchPostIdx = userMatchDao.modifyUserMatchPost(userIdx, teamScheduleIdx, contents);
            return new BaseResponse<>(new ModifyUserMatchPostRes(matchPostIdx, "USER"));
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseException(DATABASE_ERROR);
        }
    }

    public void deleteUserMatchPost(int userIdx, int teamScheduleIdx) throws BaseException{
        try{
            if(userProvider.checkUserExist(userIdx) == 0){
                throw new BaseException(USER_DOES_NOT_EXIST);
            }
            if(userProvider.checkUserStatus(userIdx).equals("INACTIVE")){
                throw new BaseException(LEAVED_USER);
            }

            if (userMatchDao.isLeader(userIdx) != 1) {
                throw new BaseException(UNAUTHORIZED_ACCESS); // 리더가 아닙니다.
            }

            // 모집글 삭제 가능 여부 확인은, 생성가능 여부 확인 방식과 다르지 않아 재사용
            CheckCreateUserMatchPostPossibilityRes createUserMatchPostPossibilityRes = userMatchDao.checkCreateMatchPostPossibility(userIdx, teamScheduleIdx);
            if (createUserMatchPostPossibilityRes.getStatus() != 1) {
                throw new BaseException(ACCEPT_NOT_AVAILABLE); // 용병 모집글이 작성되지 않았습니다.
            }

            if (userMatchDao.isOurMatch(userIdx, teamScheduleIdx) != 1) {
                throw new BaseException(NOT_TEAMMATE); // teamScheduleIdx가 다른 팀의 것입니다.
            }

            userMatchDao.deleteUserMatchPost(userIdx, teamScheduleIdx);

        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.DATABASE_ERROR);
        }

    }


    public void acceptUserMatchApply(int userIdx, int matchApplyIdx) throws BaseException{
        try{
            if(userProvider.checkUserExist(userIdx) == 0){
                throw new BaseException(USER_DOES_NOT_EXIST);
            }
            if(userProvider.checkUserStatus(userIdx).equals("INACTIVE")){
                throw new BaseException(LEAVED_USER);
            }

            if (userMatchDao.isLeader(userIdx) != 1) {
                throw new BaseException(UNAUTHORIZED_ACCESS); // 리더가 아닙니다.
            }

            if (userMatchDao.existsMatchApply(matchApplyIdx) != 1){
                throw new BaseException(MATCHING_DOES_NOT_EXIST); // matchApplyIdx가 잘못된 경우
            }

            CheckUserMatchApplyPossibilityRes checkUserMatchApplyPossibilityRes = userMatchDao.checkUserMatchApplyPossibility(userIdx, matchApplyIdx);

            if(!checkUserMatchApplyPossibilityRes.getApplyStatus().equals("APPLIED")){
                throw new BaseException(ACCEPT_NOT_AVAILABLE); // status가 applied 아닌 경우
            }

            if(checkUserMatchApplyPossibilityRes.getCount() <= 0){
                throw new BaseException(MATCH_FULLY_ACCEPTED); // 신청 인원 마감된 경우
            }

            if(checkUserMatchApplyPossibilityRes.getStatus() != 1){
                throw new BaseException(NOT_TEAMMATE); // 우리팀 경기에 대한 신청이 아닌 경우
            }

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String startTime = checkUserMatchApplyPossibilityRes.getStartTime();
            String curTime = format.format(new Date());
            Date _startTime = format.parse(startTime);
            Date _curTime = format.parse(curTime);
            long diff = _startTime.getTime() - _curTime.getTime();
            diff = (((diff / 1000) / 60) / 60);

            if (diff <= 2) {
                throw new BaseException(MATCH_APPLY_PERIOD_ERROR); // 신청 승인 기한이 지난 경우
            }

            userMatchDao.acceptUserMatchApply(matchApplyIdx); // 신청 승인
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.DATABASE_ERROR);
        }
    }

    public void rejectUserMatchApply(int userIdx, int matchApplyIdx) throws BaseException{
        try {
            if(userProvider.checkUserExist(userIdx) == 0){
                throw new BaseException(USER_DOES_NOT_EXIST);
            }
            if(userProvider.checkUserStatus(userIdx).equals("INACTIVE")){
                throw new BaseException(LEAVED_USER);
            }

            if (userMatchDao.isLeader(userIdx) != 1) {
                throw new BaseException(UNAUTHORIZED_ACCESS); // 리더가 아닙니다.
            }

            if (userMatchDao.existsMatchApply(matchApplyIdx) != 1){
                throw new BaseException(MATCHING_DOES_NOT_EXIST); // matchApplyIdx가 잘못된 경우
            }

            CheckUserMatchApplyPossibilityRes checkUserMatchApplyPossibilityRes = userMatchDao.checkUserMatchApplyPossibility(userIdx, matchApplyIdx);

            if(checkUserMatchApplyPossibilityRes.getApplyStatus().equals("CANCELED") || checkUserMatchApplyPossibilityRes.getApplyStatus().equals("DENIED")){
                throw new BaseException(ACCEPT_NOT_AVAILABLE); // 신청이 취소된 경우 or 이미 거절한 경우
            }

            if(checkUserMatchApplyPossibilityRes.getStatus() != 1){
                throw new BaseException(NOT_TEAMMATE); // 우리팀 경기에 대한 신청이 아닌 경우
            }

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String startTime = checkUserMatchApplyPossibilityRes.getStartTime();
            String curTime = format.format(new Date());
            Date _startTime = format.parse(startTime);
            Date _curTime = format.parse(curTime);
            long diff = _startTime.getTime() - _curTime.getTime();
            diff = (((diff / 1000) / 60) / 60);

            if (diff <= 2) {
                throw new BaseException(MATCH_APPLY_PERIOD_ERROR); // 신청 거절 기한이 지난 경우
            }

            userMatchDao.rejectUserMatchApply(matchApplyIdx);

        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.DATABASE_ERROR);
        }
    }
}
