package com.woodongleee.src.userMatch;

import com.woodongleee.config.BaseException;
import com.woodongleee.config.BaseResponse;
import com.woodongleee.utils.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("user-match")
public class UserMatchController {

    private final UserMatchProvider userMatchProvider;
    private final UserMatchService userMatchService;
    private final JwtService jwtService;

    @Autowired
    public UserMatchController(UserMatchProvider userMatchProvider, UserMatchService userMatchService, JwtService jwtService){
        this.userMatchProvider = userMatchProvider;
        this.userMatchService = userMatchService;
        this.jwtService = jwtService;
    }

    //postman 테스트용 jwt 발급 api
    @GetMapping("/jwt/{userIdx}")
    public BaseResponse<String> getJwt(@PathVariable("userIdx") int userIdx){
        String jwt = jwtService.createJwt(userIdx);
        return new BaseResponse<>(jwt);
    }


    /*


    // 용병 모집글 조회 API -> ?time 을 어떤 기준으로 필터링 할 지 애매해서 일단 킵..
    @GetMapping("")
    public BaseResponse<List<MatchPostInfoRes>> getMatchPosts(@RequestParam(required = true) String town,
                                                              @RequestParam(required = true) String date,
                                                              @RequestParam(required = true) String time){
        try{
            int userIdx = jwtService.getUserIdx();

        }
        catch(BaseException e){
            return new BaseResponse<>(e.getStatus());
        }
    }

     */

    // 용병 신청 API
    @PostMapping("/{matchPostIdx}/apply")
    public BaseResponse<String> applyUserMatch(@PathVariable("matchPostIdx") int matchPostIdx){
        try{
            int userIdx = jwtService.getUserIdx();
            userMatchService.applyUserMatch(userIdx, matchPostIdx);

            String result = "용병 신청을 성공하였습니다.";
            return new BaseResponse<>(result);
        }
        catch (BaseException e){
            return new BaseResponse<>(e.getStatus());
        }
    }

    // 용병 신청 취소 API
    @PatchMapping("/{matchPostIdx}/apply")
    public BaseResponse<String> cancelApplyUserMatch(@PathVariable("matchPostIdx") int matchPostIdx){
        try{
            int userIdx = jwtService.getUserIdx();
            userMatchService.cancelApplyUserMatch(userIdx, matchPostIdx);

            String result = "용병 신청을 취소하였습니다.";
            return new BaseResponse<>(result);
        }
        catch (BaseException e){
            return new BaseResponse<>(e.getStatus());
        }
    }

}
