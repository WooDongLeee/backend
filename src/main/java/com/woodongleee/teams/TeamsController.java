package com.woodongleee.teams;

import com.woodongleee.config.BaseException;
import com.woodongleee.config.BaseResponse;
import com.woodongleee.config.BaseResponseStatus;
import com.woodongleee.teams.model.GetTeamsScheduleRes;
import com.woodongleee.teams.model.GetTeamsinfoRes;
import com.woodongleee.utils.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teams")
public class TeamsController {

    @Autowired
    private final TeamsProvider teamsProvider;

    @Autowired
    private final TeamsService teamsService;

    @Autowired
    private final JwtService jwtService;

    public TeamsController(TeamsProvider teamsProvider, TeamsService teamsService, JwtService jwtService){
        this.teamsProvider=teamsProvider;
        this.teamsService=teamsService;
        this.jwtService=jwtService;
    }

    //동네로 팀 목록 조회
    @ResponseBody
    @GetMapping("")
    public BaseResponse<List<GetTeamsinfoRes>> getTeamsInfobyTown(@RequestParam String town){
        try{
            int userIdxByJwt= jwtService.getUserIdx();
            List<GetTeamsinfoRes> getTeamsinfoRes=teamsProvider.getTeamsByTown(userIdxByJwt, town);
            return new BaseResponse<>(getTeamsinfoRes);
        }catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    //팀 이름으로 조회
    @ResponseBody
    @GetMapping("/name")
    public BaseResponse<GetTeamsinfoRes> getTeamsInfobyName(@RequestParam String name){
        try{
            int userIdxByJwt= jwtService.getUserIdx();
            GetTeamsinfoRes getTeamsinfoRes=teamsProvider.getTeamsByName(userIdxByJwt, name);
            return new BaseResponse<>(getTeamsinfoRes);
        }catch(BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    //팀 정보 조회
    @ResponseBody
    @GetMapping("/{teamIdx}")
    public BaseResponse<GetTeamsinfoRes> getTeaminfo(@PathVariable("teamIdx")int teamIdx){
        try{
            int userIdxByJwt= jwtService.getUserIdx();
            GetTeamsinfoRes getTeamsinfoRes=teamsProvider.getTeaminfo(userIdxByJwt, teamIdx);
            return new BaseResponse<>(getTeamsinfoRes);
        }catch (BaseException exception){
            return new BaseResponse<>((exception.getStatus()));
        }
    }

    //팀 일정 전부 조회
 //   @ResponseBody
 //   @GetMapping("/{teamIdx}/schedule")
 //   public BaseResponse<List<GetTeamsScheduleRes>> getTeamSchedule(@RequestParam String startDate, @RequestParam String endDate){
 //
 //   }
}