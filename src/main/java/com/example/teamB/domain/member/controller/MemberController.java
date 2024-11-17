package com.example.teamB.domain.member.controller;

import com.example.teamB.domain.member.dto.MemberRequestDTO;
import com.example.teamB.domain.member.dto.MemberResponseDTO;
import com.example.teamB.domain.member.entity.Member;
import com.example.teamB.domain.member.exception.MemberErrorCode;
import com.example.teamB.domain.member.exception.MemberException;
import com.example.teamB.domain.member.repository.MemberRepository;
import com.example.teamB.domain.member.service.command.MemberCommandService;
import com.example.teamB.global.apiPayload.CustomResponse;
import com.example.teamB.global.apiPayload.code.BaseSuccessCode;
import com.example.teamB.global.jwt.util.JwtProvider;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class MemberController {

    private final MemberCommandService memberCommandService;
    private final JwtProvider jwtProvider;

    /** 1단계: 회원가입 요청 DTO
     * 이메일, 비밀번호 입력
     * 이메일 중복 확인 및 인증 메일 전송*/
    @PostMapping("/signup/request-and-verify")
    public CustomResponse<Void> signupRequestAndVerifyEmail(@RequestBody @Valid MemberRequestDTO.SignupRequestAndVerifyEmailDTO dto) throws MessagingException {
        memberCommandService.signupRequestAndVerifyEmail(dto);
        return CustomResponse.onSuccess(null);
    }

    /** 2단계: 인증 코드 검증 */
    @PostMapping("/signup/verify-code")
    public CustomResponse<Void> verifyCode(@RequestBody @Valid MemberRequestDTO.VerificationCodeDTO dto) {
        memberCommandService.verifyCode(dto.getEmail(), dto.getVerificationCode());
        return CustomResponse.onSuccess(null);
    }

    /** 3단계: 추가 정보 입력 (이름, 닉네임, 젠더 입력) */
    @PostMapping("/signup/additional-info")
    public CustomResponse<Void> addAdditionalInfo(@RequestBody @Valid MemberRequestDTO.AdditionalInfoDTO dto) {
        memberCommandService.addAdditionalInfo(dto);
        return CustomResponse.onSuccess(null);
    }

    /** 4단계: 회원가입 완료 */
    @PostMapping("/signup/complete")
    public CustomResponse<MemberResponseDTO.MemberTokenDTO> completeSignup(@RequestBody @Valid MemberRequestDTO.SignupCompleteDTO dto) {
        return CustomResponse.onSuccess(memberCommandService.completeSignup(dto));
    }

    /** 회원 로그인 API */
    @PostMapping("/login")
    public CustomResponse<MemberResponseDTO.MemberTokenDTO> login(@RequestBody MemberRequestDTO.MemberLoginDTO dto) {
        return CustomResponse.onSuccess(memberCommandService.login(dto));
    }

    /** 회원 탈퇴 API */
    @PostMapping("/withdraw")
    public CustomResponse<Void> withdraw(@RequestHeader("Authorization") String authorizationHeader) {
        // "Bearer " 제거
        String accessToken = authorizationHeader.replace("Bearer ", "");
        memberCommandService.withdraw(accessToken);
        return CustomResponse.onSuccess(null);
    }


    /** 비밀번호 변경 요청 */
    @PostMapping("/password/change/request")
    public CustomResponse<Void> requestPasswordChange(@RequestBody @Valid MemberRequestDTO.PasswordChangeRequestDTO dto) throws MessagingException {
        memberCommandService.requestPasswordChange(dto);
        return CustomResponse.onSuccess(null);
    }

    /** 비밀번호 변경 이메일 인증 */
    @PostMapping("/password/change/verify")
    public CustomResponse<Void> verifyPasswordChangeCode(@RequestBody @Valid MemberRequestDTO.VerificationCodeDTO dto) {
        memberCommandService.verifyPasswordChangeCode(dto);
        return CustomResponse.onSuccess(null);
    }

    /** 새 비밀번호 입력 */
    @PostMapping("/password/change/complete")
    public CustomResponse<Void> completePasswordChange(@RequestBody @Valid MemberRequestDTO.PasswordChangeCompleteDTO dto) {
        memberCommandService.completePasswordChange(dto);
        return CustomResponse.onSuccess(null);
    }

    /** 닉네임 변경 */
    @PostMapping("/nickname")
    public CustomResponse<Void> changeNickname(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody @Valid MemberRequestDTO.ChangeNicknameDTO dto) {
        String accessToken = authorizationHeader.replace("Bearer ", "");
        memberCommandService.changeNickname(accessToken, dto.getNewNickname());
        return CustomResponse.onSuccess(null);
    }

    /** 알람 설정 변경 */
    @PostMapping("/alarm-settings")
    public CustomResponse<Void> changeAlarmSettings(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody @Valid MemberRequestDTO.ChangeAlarmSettingsDTO dto) {
        String accessToken = authorizationHeader.replace("Bearer ", "");
        memberCommandService.changeAlarmSettings(accessToken, dto.getAlarmStatus(), dto.getAlarmTime());
        return CustomResponse.onSuccess(null);
    }

    /** 자기 정보 조회 */
    @GetMapping("/profile")
    public CustomResponse<MemberResponseDTO.MemberInfoDTO> getMyInfo(@RequestHeader("Authorization") String authorizationHeader) {
        String accessToken = authorizationHeader.replace("Bearer ", "");
        MemberResponseDTO.MemberInfoDTO myInfo = memberCommandService.getProfile(accessToken);
        return CustomResponse.onSuccess(myInfo);
    }

}