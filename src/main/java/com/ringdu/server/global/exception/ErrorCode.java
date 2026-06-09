package com.ringdu.server.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "잘못된 입력값입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
    DUPLICATED_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    DUPLICATED_PHONE(HttpStatus.CONFLICT, "이미 사용 중인 전화번호입니다."),
    PASSWORD_CONFIRM_MISMATCH(HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
    SIGNUP_ROLE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "해당 권한은 일반 회원가입으로 생성할 수 없습니다."),
    ACADEMY_APPROVAL_PENDING(HttpStatus.FORBIDDEN, "아직 관리자 승인 대기 중입니다. 승인 후 로그인할 수 있습니다."),
    ACADEMY_SIGNUP_APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "학원 가입 신청을 찾을 수 없습니다."),
    ACADEMY_SIGNUP_APPLICATION_ALREADY_REVIEWED(HttpStatus.CONFLICT, "이미 처리된 학원 가입 신청입니다."),
    ACADEMY_NOT_FOUND(HttpStatus.NOT_FOUND, "학원 정보를 찾을 수 없습니다."),
    ACADEMY_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 생성된 학원 정보가 있습니다."),
    ACADEMY_ACCESS_DENIED(HttpStatus.FORBIDDEN, "학원 계정만 접근할 수 있습니다."),
    TEACHER_INVITATION_NOT_FOUND(HttpStatus.NOT_FOUND, "선생님 초대장을 찾을 수 없습니다."),
    TEACHER_INVITATION_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 대기 중인 선생님 초대장이 있습니다."),
    TEACHER_INVITATION_ALREADY_PROCESSED(HttpStatus.CONFLICT, "이미 처리된 선생님 초대장입니다."),
    TEACHER_INVITATION_EMAIL_MISMATCH(HttpStatus.FORBIDDEN, "본인에게 온 선생님 초대장만 처리할 수 있습니다."),
    TEACHER_INVITATION_PHONE_MISMATCH(HttpStatus.FORBIDDEN, "본인 전화번호로 온 선생님 초대장만 처리할 수 있습니다."),
    TEACHER_ALREADY_CONNECTED(HttpStatus.CONFLICT, "이미 학원에 연결된 선생님입니다."),
    TEACHER_ROLE_REQUIRED(HttpStatus.BAD_REQUEST, "선생님 계정만 초대할 수 있습니다."),
    PARENT_STUDENT_INVITATION_NOT_FOUND(HttpStatus.NOT_FOUND, "부모-학생 연결 초대장을 찾을 수 없습니다."),
    PARENT_STUDENT_INVITATION_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 대기 중인 부모-학생 연결 초대장이 있습니다."),
    PARENT_STUDENT_INVITATION_ALREADY_PROCESSED(HttpStatus.CONFLICT, "이미 처리된 부모-학생 연결 초대장입니다."),
    PARENT_STUDENT_INVITATION_EMAIL_MISMATCH(HttpStatus.FORBIDDEN, "본인에게 온 부모-학생 연결 초대장만 처리할 수 있습니다."),
    PARENT_STUDENT_ALREADY_CONNECTED(HttpStatus.CONFLICT, "이미 연결된 부모-학생 관계입니다."),
    PARENT_ROLE_REQUIRED(HttpStatus.BAD_REQUEST, "부모 계정만 사용할 수 있습니다."),
    STUDENT_ROLE_REQUIRED(HttpStatus.BAD_REQUEST, "학생 계정만 사용할 수 있습니다."),
    SOCIAL_ACCOUNT_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 연결된 소셜 계정입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    STUDENT_NOT_FOUND(HttpStatus.NOT_FOUND, "학생 정보를 찾을 수 없습니다."),
    CLASSROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "강의실을 찾을 수 없습니다."),
    CLASSROOM_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용 중인 강의실 이름입니다."),
    ACADEMY_CLASS_NOT_FOUND(HttpStatus.NOT_FOUND, "수업을 찾을 수 없습니다."),
    ACADEMY_CLASS_TIME_INVALID(HttpStatus.BAD_REQUEST, "수업 시작 시간은 종료 시간보다 빨라야 합니다."),
    ACADEMY_CLASS_TIME_OVERLAP(HttpStatus.CONFLICT, "같은 요일과 강의실에 겹치는 수업이 있습니다."),
    ACADEMY_CLASS_TEACHER_NOT_CONNECTED(HttpStatus.BAD_REQUEST, "해당 학원에 연결된 선생님만 담당자로 지정할 수 있습니다."),
    ACADEMY_CLASS_STUDENT_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 수업에 추가된 학생입니다."),
    ATTENDANCE_SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "출석부를 찾을 수 없습니다."),
    ATTENDANCE_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "출석 기록을 찾을 수 없습니다."),
    ATTENDANCE_RECORD_STUDENT_NOT_IN_CLASS(HttpStatus.BAD_REQUEST, "수업에 등록된 학생만 출석 처리할 수 있습니다."),
    CONSULTATION_AVAILABILITY_NOT_FOUND(HttpStatus.NOT_FOUND, "상담 가능 시간을 찾을 수 없습니다."),
    CONSULTATION_AVAILABILITY_TIME_INVALID(HttpStatus.BAD_REQUEST, "상담 시작 시간은 종료 시간보다 빨라야 합니다."),
    CONSULTATION_AVAILABILITY_TIME_OVERLAP(HttpStatus.CONFLICT, "같은 요일에 겹치는 상담 가능 시간이 있습니다."),
    CONSULTATION_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "상담 요청을 찾을 수 없습니다."),
    CONSULTATION_MEMO_NOT_FOUND(HttpStatus.NOT_FOUND, "상담 메모를 찾을 수 없습니다."),
    CONSULTATION_REQUEST_STATUS_INVALID(HttpStatus.CONFLICT, "현재 상태에서는 상담 요청을 처리할 수 없습니다."),
    CONSULTATION_REQUEST_TIME_INVALID(HttpStatus.BAD_REQUEST, "상담 요청 시작 시간은 종료 시간보다 빨라야 합니다."),
    CONSULTATION_REQUEST_TIME_UNAVAILABLE(HttpStatus.BAD_REQUEST, "선생님이 등록한 상담 가능 시간 안에서 요청해야 합니다."),
    CONSULTATION_REQUEST_ALREADY_EXISTS(HttpStatus.CONFLICT, "선택한 선생님의 같은 시간대에 이미 상담 예약이 있습니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),
    INACTIVE_USER(HttpStatus.FORBIDDEN, "비활성화된 계정입니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "Refresh Token을 찾을 수 없습니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "만료된 Refresh Token입니다."),
    REFRESH_TOKEN_REVOKED(HttpStatus.UNAUTHORIZED, "이미 폐기된 Refresh Token입니다."),
    REFRESH_TOKEN_REUSE_DETECTED(HttpStatus.UNAUTHORIZED, "Refresh Token 재사용이 감지되었습니다."),
    LOCAL_LOGIN_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "소셜 가입 계정은 일반 로그인을 사용할 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
