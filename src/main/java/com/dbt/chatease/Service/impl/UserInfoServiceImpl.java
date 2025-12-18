package com.dbt.chatease.Service.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.dbt.chatease.DTO.UserInfoDTO;
import com.dbt.chatease.DTO.UserInfoUpdateDTO;
import com.dbt.chatease.DTO.UserLoginDTO;
import com.dbt.chatease.DTO.UserRegisterDTO;
import com.dbt.chatease.Entity.ChatMessage;
import com.dbt.chatease.Entity.SysSetting;
import com.dbt.chatease.Entity.UserInfo;
import com.dbt.chatease.Entity.UserRobotRelation;
import com.dbt.chatease.Repository.ChatMessageRepository;
import com.dbt.chatease.Repository.SysSettingRepository;
import com.dbt.chatease.Repository.UserInfoRepository;
import com.dbt.chatease.Repository.UserRobotRelationRepository;
import com.dbt.chatease.Service.UserInfoService;
import com.dbt.chatease.Utils.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserInfoServiceImpl implements UserInfoService {
    private final JavaMailSender mailSender;
    private final RedisTemplate<String, String> redisTemplate;
    private final UserInfoRepository userInfoRepository;
    private final EmailValidator emailValidator;
    private final UserIdGenerator userIdGenerator;
    private final JwtUtil jwtUtil;
    private final SysSettingRepository sysSettingRepo;
    private final UserRobotRelationRepository userRobotRelationRepo;
    private final ChatMessageRepository chatMessageRepository;

    private String generateCode() {
        int code = 100000 + new Random().nextInt(900000);
        return String.valueOf(code);
    }

    @Override
    @Async
    public void sendVerificationCode(String email) {
        if (!emailValidator.isValid(email)) {
            log.warn("Verification code failed: Invalid email: {}", email);
            throw new IllegalArgumentException(Constants.INVALID_EMAIL);
        }

        String code = generateCode();
        log.info("Generated verification code: {} for email: {}", code, email);

        try {
            //Send email
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Verification Code From ChatEase");
            message.setText("Your verification code is: " + code + "\nIt will expire in 5 minutes");
            mailSender.send(message);

            //Store in Redis, expire in 5 minutes
            redisTemplate.opsForValue().set(Constants.VERIFICATION_CODE_PREFIX + email, code, 5, TimeUnit.MINUTES);
        } catch (MailException e) {
            log.error("Failed to send verification code to email: {}", email, e);
            throw new RuntimeException("Failed to send verification code.");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(UserRegisterDTO userRegisterDTO) {
        //Validate email format
        String email = userRegisterDTO.getEmail();
        if (!emailValidator.isValid(email)) {
            log.warn("Invalid email provided during registration: {}", email);
            throw new IllegalArgumentException(Constants.INVALID_EMAIL);
        }

        //Validate password length
        if (userRegisterDTO.getPassword().length() < 6) {
            throw new IllegalArgumentException(Constants.PASSWORD_TOO_SHORT);
        } else if (userRegisterDTO.getPassword().length() > 60) {
            throw new IllegalArgumentException(Constants.PASSWORD_TOO_LONG);
        }

        //Verify code and check if account already exists
        String CodeFromRedis = redisTemplate.opsForValue().get(Constants.VERIFICATION_CODE_PREFIX + email);
        if (CodeFromRedis == null || !CodeFromRedis.equals(userRegisterDTO.getCode())) {
            log.warn("Invalid verification code for email: {}", email);
            throw new IllegalArgumentException(Constants.INVALID_CODE);
        }
        if (userInfoRepository.existsByEmail(email)) {
            log.info("Account already exists for email: {}", email);
            throw new IllegalArgumentException(Constants.ACCOUNT_EXISTS);
        }

        //Create new user
        UserInfo userInfo = new UserInfo();
        BeanUtils.copyProperties(userRegisterDTO, userInfo);
        String uniqueId = userIdGenerator.generateUniqueId();
        Long lastOffTimeMillis = System.currentTimeMillis();
        //Set additional required fields
        userInfo.setUserId(uniqueId)
                .setAvatar(Constants.AVATAR_DEFAULT_URL)
                .setJoinType(Constants.USER_STATUS_ACTIVE)
                .setCreateTime(LocalDateTime.now())
                .setStatus(Constants.USER_JOIN_TYPE_DEFAULT)
                .setLastOffTime(lastOffTimeMillis).
                setPassword(PasswordUtil.hashPassword(userInfo.getPassword()));

        userInfoRepository.save(userInfo);
        log.info("User registered successfully with email: {}", email);


        //TODO：Create a ChatEase robot friend(Done)
        //Get robot settings
        String robotUid = sysSettingRepo.findById("ROBOT_UID")
                .map(SysSetting::getSettingValue).orElse("UID_ROBOT_001");

        String welcomeMsg = sysSettingRepo.findById("ROBOT_WELCOME")
                .map(SysSetting::getSettingValue).orElse("Welcome to ChatEase!");

        //Establish relationship (UserRobotRelation)
        UserRobotRelation relation = new UserRobotRelation()
                .setUserId(userInfo.getUserId())
                .setRobotId(robotUid)
                .setStatus(1) // 1: Normal
                .setCreateTime(LocalDateTime.now())
                .setLastReadTime(0L); // Initial state: unread

        userRobotRelationRepo.save(relation);

        //Send welcome private message
        ChatMessage msg = new ChatMessage();
        String[] ids = {userInfo.getUserId(), robotUid};
        Arrays.sort(ids);
        String sessionId = ids[0] + "_" + ids[1];
        msg.setSessionId(sessionId);
        msg.setSendUserId(robotUid);
        msg.setContactId(userInfo.getUserId());
        msg.setContactType(0);
        msg.setMessageType(0);
        msg.setContent(welcomeMsg);
        msg.setStatus(1);
        msg.setSendTime(System.currentTimeMillis());
        chatMessageRepository.save(msg);

    }

    @Override
    public Result login(UserLoginDTO userLoginDTO) {
        String email = userLoginDTO.getEmail();
        if (!emailValidator.isValid(email)) {
            log.warn("Invalid email for login: {}", email);
            throw new IllegalArgumentException(Constants.INVALID_EMAIL);
        }

        //Check if account exists
        UserInfo userInfo = userInfoRepository.findByEmail(email);
        if (userInfo == null) {
            log.warn("No account found for email: {}", email);
            return Result.fail(Constants.LOGIN_FAILED);
        }

        //Verify password
        if (!PasswordUtil.checkPassword(userLoginDTO.getPassword(), userInfo.getPassword())) {
            log.warn("Incorrect password for email: {}", email);
            return Result.fail(Constants.LOGIN_FAILED);
        }

        //check user status
        if (!userInfo.getStatus().equals(Constants.USER_STATUS_ACTIVE)) {
            return Result.fail(Constants.USER_BANNED);
        }

        //Update last login time
        userInfo.setLastLoginTime(LocalDateTime.now());
        userInfoRepository.save(userInfo);

        UserInfoDTO userInfoDTO = new UserInfoDTO();
        BeanUtils.copyProperties(userInfo, userInfoDTO);

        //Generate JWT token
        String token = jwtUtil.generateToken(userInfoDTO);

        //Build response data
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("token", token);
        responseData.put("userInfo", userInfoDTO);

        return Result.ok(responseData);

    }

    @Override
    public Result getCurrentUserInfo() {
        //Get current user ID
        String currentUserId = UserContext.getCurrentUserId();
        UserInfo userInfo = userInfoRepository.findById(currentUserId).
                orElseThrow(() -> new IllegalArgumentException(Constants.USER_NOT_FOUND));
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        BeanUtils.copyProperties(userInfo, userInfoDTO);
        return Result.ok(userInfoDTO);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result updateCurrentUserInfo(UserInfoUpdateDTO userInfoUpdateDTO) {
        //Get current user ID
        String currentUserId = UserContext.getCurrentUserId();
        //Validated by @Size in DTO
        int updated = userInfoRepository.updateUserInfo(
                currentUserId,
                userInfoUpdateDTO.getNickName(),
                userInfoUpdateDTO.getAvatar(),
                userInfoUpdateDTO.getJoinType(),
                userInfoUpdateDTO.getSex(),
                userInfoUpdateDTO.getPersonalSignature(),
                userInfoUpdateDTO.getAreaName(),
                userInfoUpdateDTO.getAreaCode()
        );
        if (updated > 0) {
            return Result.ok(Constants.USER_INFO_UPDATED);
        }
        return Result.fail(Constants.USER_INFO_UPDATE_FAILED);

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result updatePassword(String password) {
        //Get current user ID
        String currentUserId = UserContext.getCurrentUserId();
        //Validate password length
        if (password.length() < 6) {
            throw new IllegalArgumentException(Constants.PASSWORD_TOO_SHORT);
        } else if (password.length() > 60) {
            throw new IllegalArgumentException(Constants.PASSWORD_TOO_LONG);
        }
        //Don't forget to hash the new password
        PasswordUtil.hashPassword(password);
        int updated = userInfoRepository.updatePassword(currentUserId, password);
        if (updated > 0) {
            return Result.ok("Password updated successfully");
        }
        return Result.fail(Constants.UNKNOWN_ERROR);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result logOut() {
        //Get current user ID
        String currentUserId = UserContext.getCurrentUserId();
        log.info("Logging out userId: {}", currentUserId);
        userInfoRepository.updateLogoutTime(currentUserId, System.currentTimeMillis());
        //TODO: Close WebSocket connection

        return Result.ok(Constants.LOGOUT_SUCCESS);

    }


}