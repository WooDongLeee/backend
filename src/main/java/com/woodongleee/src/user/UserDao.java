package com.woodongleee.src.user;

import com.woodongleee.src.user.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class UserDao {

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource){
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    public int createUser(CreateUserReq newUser) {
        String createUserQuery = "insert into User (name, age, gender, email, id, password, town) VALUES (?,?,?,?,?,?,?)";
        Object[] createUserParams = new Object[]{newUser.getName(), newUser.getAge(), newUser.getGender(), newUser.getEmail(), newUser.getId(), newUser.getPassword(), newUser.getTown()};
        this.jdbcTemplate.update(createUserQuery, createUserParams);

        String lastInserIdQuery = "select last_insert_id()";
        return this.jdbcTemplate.queryForObject(lastInserIdQuery,int.class);
    }

    public int isIdDuplicated(String id) {
        String isIdDuplicatedQuery = "select exists(select id from User where id = ?)";
        return this.jdbcTemplate.queryForObject(isIdDuplicatedQuery,
                int.class,
                id);
    }

    public int isEmailDuplicated(String email) {
        String isEmailDuplicatedQuery = "select exists(select email from User where email = ?)";
        return this.jdbcTemplate.queryForObject(isEmailDuplicatedQuery,
                int.class,
                email);
    }

    public void createCode(String email, String code) {
        String createCodeQuery = "insert into EmailCode (email, code, expirationTime) VALUES (?,?,DATE_ADD(NOW(), INTERVAL 5 MINUTE))";
        Object[] createCodeParams = new Object[]{email, code};
        this.jdbcTemplate.update(createCodeQuery, createCodeParams);
    }

    public VerifyDomain verify(String email) {
        String verifyCodeQuery = "select email, code, expirationTime from EmailCode where email = ?";
        return this.jdbcTemplate.queryForObject(verifyCodeQuery, (rs, rowNum) -> new VerifyDomain(
                rs.getString("email"),
                rs.getString("code"),
                rs.getTimestamp("expirationTime")),
                email
        );
    }

    public int isEmailVerifyCodeRequestDuplicated(String email) {
        String isEmailVerifyCodeRequestDuplicatedQuery = "select exists(select email from EmailCode where email = ?)";
        String checkEmailCodeRequestExistParams = email;
        return this.jdbcTemplate.queryForObject(isEmailVerifyCodeRequestDuplicatedQuery,
                int.class,
                checkEmailCodeRequestExistParams);
    }

    public int deleteDuplicatedEmail(String email) {
        String deleteDuplicatedEmailQuery = "delete from EmailCode where email = ?";
        String deleteDuplicatedEmailParams = email;
        return this.jdbcTemplate.update(deleteDuplicatedEmailQuery, deleteDuplicatedEmailParams);
    }

    public UserLoginUserIdxAndPassword login(String id) {
        String userLoginQuery = "select userIdx, password from User where id = ?";
        String userLoginParams = id;
        return this.jdbcTemplate.queryForObject(userLoginQuery,(rs, rowNum) -> new UserLoginUserIdxAndPassword(
                        rs.getInt("userIdx"),
                        rs.getString("password")),
                userLoginParams);
    }

    public GetUserByJwtRes getUserByJwt(int userIdx) {
        String getUserByJwtQuery = "select U.name, age, gender, email, id, U.town, U.introduce, T.name as teamName, T.teamProfileImgUrl, U.status\n" +
                "from User as U\n" +
                "left join TeamInfo T on U.teamIdx = T.teamIdx\n" +
                "where U.userIdx = ?;";
        return this.jdbcTemplate.queryForObject(getUserByJwtQuery, (rs,rowNum) -> new GetUserByJwtRes(
                rs.getString("name"),
                rs.getInt("age"),
                rs.getString("gender"),
                rs.getString("email"),
                rs.getString("id"),
                rs.getString("town"),
                rs.getString("introduce"),
                rs.getString("teamName"),
                rs.getString("teamProfileImgUrl"),
                rs.getString("status")),
                userIdx
        );
    }

    public int updateUser(int userIdx, UpdateUserReq updateUserReq) {
        String updateUserQuery = "update User\n" +
                "set name = ?, age = ?, gender = ?, town = ?, introduce = ?, profileImgUrl = ?\n" +
                "where userIdx = ?";
        Object[] updateUserParams = new Object[]{
                updateUserReq.getName(),
                updateUserReq.getAge(),
                updateUserReq.getGender(),
                updateUserReq.getTown(),
                updateUserReq.getIntroduce(),
                updateUserReq.getProfileImgUrl(),
                userIdx
        };

        return this.jdbcTemplate.update(updateUserQuery, updateUserParams);

    }

    public int checkUserExist(int userIdx) {
        String checkUserQuery = "select exists(select email from User where userIdx = ?)";
        int checkUserParams = userIdx;
        return this.jdbcTemplate.queryForObject(checkUserQuery,
                int.class,
                checkUserParams);
    }

    public String checkUserStatus(int userIdx) {
        String checkUserStatusQuery = "select status from User where userIdx = ?";
        int checkUserParams = userIdx;
        return this.jdbcTemplate.queryForObject(checkUserStatusQuery,
                String.class,
                checkUserParams);
    }

    public int updatePassword(int userIdx, String password) {
        String updatePasswordQuery = "update User set password = ? where userIdx = ?";
        Object[] updatePasswordPrams = new Object[] {password, userIdx};
        return this.jdbcTemplate.update(updatePasswordQuery, updatePasswordPrams);
    }

    public String checkPassword(int userIdx) {
        String checkPasswordQuery = "select password from User where userIdx = ?";
        int checkPasswordParam = userIdx;
        return this.jdbcTemplate.queryForObject(checkPasswordQuery,
                String.class,
                checkPasswordParam);
    }

    public int updateId(int userIdx, String id) {
        String updateIdQuery = "update User set id  = ? where userIdx = ?";
        Object[] updateIdParam = new Object[]{id,userIdx};
        return this.jdbcTemplate.update(updateIdQuery, updateIdParam);
    }
    public int deleteUser(int userIdx) {
        String deleteUserQuery = "update User set status = ? where userIdx = ?";
        Object[] deleteUserParams = new Object[]{"INACTIVE", userIdx};
        return this.jdbcTemplate.update(deleteUserQuery, deleteUserParams);
    }

    public List<GetUserScheduleRes> getUserSchedule(int userIdx) {
        String getUserScheduleQuery = "select TIH.name as homeName, TIA.name as awayName, TS.address, TS.startTime, TS.endTime, TS.date, MP.type as type\n" +
                "from UserSchedule as US\n" +
                "join TeamSchedule as TS on TS.teamScheduleIdx = US.teamScheduleIdx\n" +
                "join TeamInfo as TIH on TIH.teamIdx = TS.homeIdx\n" +
                "left join TeamInfo as TIA on TIA.teamIdx = TS.awayIdx\n" +
                "left join MatchPost as MP on MP.teamScheduleIdx = US.teamScheduleIdx\n" +
                "left join MatchApply as MA on MA.userIdx = US.userIdx\n" +
                "where US.userIdx = ?";
        int getUserScheduleParam = userIdx;
        return this.jdbcTemplate.query(getUserScheduleQuery,
                (rs, rowNum) -> new GetUserScheduleRes(
                        rs.getString("homeName"),
                        rs.getString("awayName"),
                        rs.getString("address"),
                        rs.getString("startTime"),
                        rs.getString("endTime"),
                        rs.getString("date"),
                        rs.getString("type")),
                getUserScheduleParam);
    }

    public GetIdByEmailRes getIdByEmail(String email) {
        String getIdByEmailQuery = "select id from User where email = ?";
        String getIdByEmailParam = email;
        return this.jdbcTemplate.queryForObject(getIdByEmailQuery, (rs,rowNum) -> new GetIdByEmailRes(
                rs.getString("id")),
                getIdByEmailParam
        );
    }

    public List<GetUserApplyRes> getUserApply(int userIdx) {
        String getUserApplyQuery = "select TIH.name as homeName, TIA.name as awayName, TS.address, TS.startTime, TS.endTime, TS.date, MP.type, MA.status\n" +
                "from MatchApply as MA\n" +
                "join MatchPost as MP on MP.matchPostIdx = MA.matchPostIdx\n" +
                "join TeamSchedule as TS on TS.teamScheduleIdx = MP.teamScheduleIdx\n" +
                "join TeamInfo as TIH on TIH.teamIdx = TS.homeIdx\n" +
                "left join TeamInfo as TIA on TIA.teamIdx = TS.awayIdx\n" +
                "where MA.userIdx = ? and MP.type = ?";
        Object[] getUserApplyParams = new Object[]{userIdx,"USER"};
        return this.jdbcTemplate.query(getUserApplyQuery,
                (rs,rowNum) -> new GetUserApplyRes(
                    rs.getString("homeName"),
                    rs.getString("awayName"),
                    rs.getString("address"),
                    rs.getString("startTime"),
                    rs.getString("endTime"),
                    rs.getString("date"),
                    rs.getString("type"),
                    rs.getString("status")),
                getUserApplyParams);
    }
}

